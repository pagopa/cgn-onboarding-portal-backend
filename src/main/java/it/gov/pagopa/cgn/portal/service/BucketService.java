package it.gov.pagopa.cgn.portal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.apache.commons.csv.CSVRecord;
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
    public DiscountEntity createPendingBucketLoad(DiscountEntity discount) {
        BucketCodeLoadEntity bucketCodeLoadEntity = new BucketCodeLoadEntity();
        bucketCodeLoadEntity.setDiscountId(discount.getId());
        bucketCodeLoadEntity.setStatus(BucketCodeLoadStatusEnum.PENDING);
        bucketCodeLoadEntity.setUid(discount.getLastBucketCodeLoadUid());
        bucketCodeLoadEntity.setFileName(discount.getLastBucketCodeLoadFileName());
        bucketCodeLoadRepository.save(bucketCodeLoadEntity);
        // attach BucketCodeLoad to Discount
        discount.setLastBucketCodeLoad(bucketCodeLoadEntity);
        discountRepository.save(discount);
        return discount;
    }

    public boolean isLastBucketLoadTerminated(Long bucketLoadId) {
        return List.of(BucketCodeLoadStatusEnum.FAILED, BucketCodeLoadStatusEnum.FINISHED)
                .contains(bucketCodeLoadRepository.findById(bucketLoadId).orElseThrow().getStatus());
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void setRunningBucketLoad(Long discountId) {
        DiscountEntity discountEntity = discountRepository.getOne(discountId);
        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository.getOne(discountEntity.getLastBucketCodeLoad().getId());
        try {
            Stream<CSVRecord> csvStream = azureStorage.readCsvDocument(bucketCodeLoadEntity.getUid());
            bucketCodeLoadEntity.setStatus(BucketCodeLoadStatusEnum.RUNNING);
            bucketCodeLoadEntity.setNumberOfCodes(csvStream.count());
        } catch (Exception ex) {
            bucketCodeLoadEntity.setStatus(BucketCodeLoadStatusEnum.FAILED);
        } finally {
            bucketCodeLoadRepository.save(bucketCodeLoadEntity);
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void performBucketLoad(Long discountId) {
        DiscountEntity discountEntity = discountRepository.getOne(discountId);
        BucketCodeLoadEntity bucketCodeLoadEntity = bucketCodeLoadRepository.getOne(discountEntity.getLastBucketCodeLoad().getId());
        if (bucketCodeLoadEntity.getStatus().equals(BucketCodeLoadStatusEnum.FAILED))
            return;

        try {
            Stream<CSVRecord> csvStream = azureStorage.readCsvDocument(bucketCodeLoadEntity.getUid());
            Spliterator<DiscountBucketCodeEntity> split = csvStream
                    .map(csvRecord -> new DiscountBucketCodeEntity(csvRecord.get(0), discountEntity,
                            bucketCodeLoadEntity.getId()))
                    .spliterator();
            int chunkSize = 25000;

            while (true) {
                List<DiscountBucketCodeEntity> bucketCodeListChunk = new ArrayList<>();
                int i = 0;
                while (i < chunkSize && split.tryAdvance(bucketCodeListChunk::add)) i++;
                if (bucketCodeListChunk.isEmpty())
                    break;
                discountBucketCodeRepository.bulkPersist(bucketCodeListChunk);
            }

            bucketCodeLoadEntity.setStatus(BucketCodeLoadStatusEnum.FINISHED);
        } catch (Exception e) {
            bucketCodeLoadEntity.setStatus(BucketCodeLoadStatusEnum.FAILED);
        } finally {
            bucketCodeLoadRepository.save(bucketCodeLoadEntity);
        }
    }

    public Long countLoadedCodes(DiscountEntity discountEntity) {
        return discountBucketCodeRepository.countByDiscountAndBucketCodeLoadId(discountEntity, discountEntity.getLastBucketCodeLoad().getId());
    }
}
