package it.gov.pagopa.cgn.portal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.transaction.Transactional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import it.gov.pagopa.cgn.portal.enums.BucketCodeLoadStatusEnum;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.BucketCodeLoadEntity;
import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.repository.BucketCodeLoadRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountBucketCodeRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;

@Service
public class BucketService {
    private final DiscountBucketCodeRepository discountBucketCodeRepository;
    private final BucketCodeLoadRepository bucketCodeLoadRepository;
    private final DiscountRepository discountRepository;
    private final AzureStorage azureStorage;

    public BucketService(DiscountBucketCodeRepository discountBucketCodeRepository,
            BucketCodeLoadRepository bucketCodeLoadRepository, DiscountRepository discountRepository,
            AzureStorage azureStorage) {
        this.discountBucketCodeRepository = discountBucketCodeRepository;
        this.bucketCodeLoadRepository = bucketCodeLoadRepository;
        this.discountRepository = discountRepository;
        this.azureStorage = azureStorage;
    }

    public boolean checkBucketLoadUID(String uid) {
        return azureStorage.existsDocument(uid + ".csv");
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void createPendingBucketLoad(DiscountEntity discountEntity) {
        BucketCodeLoadEntity bucketCodeLoadEntity = new BucketCodeLoadEntity();
        bucketCodeLoadEntity.setDiscountId(discountEntity.getId());
        bucketCodeLoadEntity.setStatus(BucketCodeLoadStatusEnum.PENDING);
        bucketCodeLoadEntity.setUid(discountEntity.getLastBucketCodeFileUid());
        // TODO: retrieve real number of codes
        bucketCodeLoadEntity.setNumberOfCodes(100L);
        bucketCodeLoadRepository.save(bucketCodeLoadEntity);
    }

    @Async("threadPoolTaskExecutor")
    @Transactional(Transactional.TxType.REQUIRED)
    public void storeCodesBucket(Long discountId) {
        DiscountEntity discountEntity = discountRepository.getOne(discountId);
        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository.findByDiscountIdAndUid(discountId,
                discountEntity.getLastBucketCodeFileUid());

        bucketCodeLoadEntity.setStatus(BucketCodeLoadStatusEnum.RUNNING);
        List<DiscountBucketCodeEntity> bucketCodeList = new ArrayList<>();
        try {
            StreamSupport.stream(azureStorage.readCsvDocument(bucketCodeLoadEntity.getUid()).spliterator(), true)
                    .collect(Collectors.toList()).stream()
                    .map(record -> new DiscountBucketCodeEntity(record.get(0), discountEntity))
                    .forEach(bucketCodeList::add);
            discountBucketCodeRepository.bulkPersist(bucketCodeList);
            bucketCodeLoadEntity.setStatus(BucketCodeLoadStatusEnum.FINISHED);
        } catch (Exception e) {
            bucketCodeLoadEntity.setStatus(BucketCodeLoadStatusEnum.FAILED);
        }
    }

}
