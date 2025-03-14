package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.enums.BucketCodeExpiringThresholdEnum;
import it.gov.pagopa.cgn.portal.enums.BucketCodeLoadStatusEnum;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.BucketCodeLoadEntity;
import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeEntity;
import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeSummaryEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.repository.BucketCodeLoadRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountBucketCodeRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountBucketCodeSummaryRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;

@Slf4j
@Service
public class BucketService {
    private final DiscountBucketCodeRepository discountBucketCodeRepository;
    private final DiscountBucketCodeSummaryRepository discountBucketCodeSummaryRepository;
    private final BucketCodeLoadRepository bucketCodeLoadRepository;
    private final DiscountRepository discountRepository;
    private final AzureStorage azureStorage;
    private final EmailNotificationFacade emailNotificationFacade;

    public BucketService(DiscountBucketCodeRepository discountBucketCodeRepository,
                         DiscountBucketCodeSummaryRepository discountBucketCodeSummaryRepository,
                         BucketCodeLoadRepository bucketCodeLoadRepository,
                         DiscountRepository discountRepository,
                         EmailNotificationFacade emailNotificationFacade,
                         AzureStorage azureStorage) {
        this.discountBucketCodeRepository = discountBucketCodeRepository;
        this.discountBucketCodeSummaryRepository = discountBucketCodeSummaryRepository;
        this.bucketCodeLoadRepository = bucketCodeLoadRepository;
        this.discountRepository = discountRepository;
        this.emailNotificationFacade = emailNotificationFacade;
        this.azureStorage = azureStorage;
    }

    public boolean checkBucketLoadUID(String uid) {
        return azureStorage.existsDocument(uid + ".csv");
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void prepareDiscountBucketCodeSummary(DiscountEntity discount) {
        DiscountBucketCodeSummaryEntity bucketCodeSummaryEntity = discountBucketCodeSummaryRepository.findById(discount.getId())
                                                                                                     .orElse(new DiscountBucketCodeSummaryEntity(
                                                                                                             discount));
        discountBucketCodeSummaryRepository.save(bucketCodeSummaryEntity);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public boolean checkDiscountBucketCodeSummaryExpirationAndSendNotification(DiscountBucketCodeSummaryEntity discountBucketCodeSummaryEntity) {
        var discountBucketCodeSummary = discountBucketCodeSummaryRepository.getReferenceById(
                discountBucketCodeSummaryEntity.getId());
        DiscountEntity discount = discountBucketCodeSummary.getDiscount();
        var remainingCodes = discountBucketCodeRepository.countNotUsedByDiscountId(discount.getId());
        var remainingPercent = Math.floor(
                remainingCodes / Float.valueOf(discountBucketCodeSummary.getAvailableCodes()) * 100);
        var notificationRequired = Arrays.stream(BucketCodeExpiringThresholdEnum.values())
                                         .sorted()
                                         .filter(t -> remainingPercent <= t.getValue())
                                         .findFirst()
                                         .map(t -> {
                                             if (t!=BucketCodeExpiringThresholdEnum.PERCENT_0) {
                                                 emailNotificationFacade.notifyMerchantDiscountBucketCodesExpiring(
                                                         discount,
                                                         t,
                                                         remainingCodes);
                                             }
                                             return true;
                                         });
        // update bucket summary
        if (remainingCodes <= 0) {
            // we send here the 0% email notification to be sure that we send it when there are no more codes
            // because calculating the percent with Math.floor could round to 0% a small amount of codes
            emailNotificationFacade.notifyMerchantDiscountBucketCodesExpired(discount);
            discountBucketCodeSummary.setExpiredAt(OffsetDateTime.now());
        }
        discountBucketCodeSummary.setAvailableCodes(remainingCodes);
        discountBucketCodeSummaryRepository.save(discountBucketCodeSummary);
        return notificationRequired.orElse(false);
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

    public boolean isLastBucketLoadStillLoading(Long bucketLoadId) {
        return !List.of(BucketCodeLoadStatusEnum.FAILED, BucketCodeLoadStatusEnum.FINISHED)
                    .contains(bucketCodeLoadRepository.findById(bucketLoadId).orElseThrow().getStatus());
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void setRunningBucketLoad(Long discountId) {
        DiscountEntity discountEntity = discountRepository.getReferenceById(discountId);
        BucketCodeLoadEntity bucketCodeLoadEntity = discountEntity.getLastBucketCodeLoad();
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
        DiscountEntity discountEntity = discountRepository.getReferenceById(discountId);
        DiscountBucketCodeSummaryEntity discountBucketCodeSummaryEntity = discountBucketCodeSummaryRepository.findByDiscount(
                discountEntity);
        BucketCodeLoadEntity bucketCodeLoadEntity = discountEntity.getLastBucketCodeLoad();
        if (bucketCodeLoadEntity.getStatus().equals(BucketCodeLoadStatusEnum.FAILED)) return;

        try {
            Stream<CSVRecord> csvStream = azureStorage.readCsvDocument(bucketCodeLoadEntity.getUid());
            Spliterator<DiscountBucketCodeEntity> split = csvStream.map(csvRecord -> new DiscountBucketCodeEntity(
                    csvRecord.get(0),
                    discountEntity,
                    bucketCodeLoadEntity.getId())).spliterator();

            int chunkSize = 5000;
            while (true) {
                List<DiscountBucketCodeEntity> bucketCodeListChunk = new ArrayList<>();
                int i = 0;
                while (i < chunkSize && split.tryAdvance(bucketCodeListChunk::add)) i++;
                if (bucketCodeListChunk.isEmpty()) break;
                discountBucketCodeRepository.bulkPersist(bucketCodeListChunk);
            }

            bucketCodeLoadEntity.setStatus(BucketCodeLoadStatusEnum.FINISHED);

            // update discountBucketCodeSummaryEntity
            var availableCodes = discountBucketCodeRepository.countNotUsedByDiscountId(discountId);
            discountBucketCodeSummaryEntity.setAvailableCodes(availableCodes);
            discountBucketCodeSummaryEntity.setExpiredAt(null);
            discountBucketCodeSummaryRepository.save(discountBucketCodeSummaryEntity);
        } catch (Exception e) {
            Arrays.stream(e.getStackTrace()).forEach(traceElement -> log.error(traceElement.toString()));
            bucketCodeLoadEntity.setStatus(BucketCodeLoadStatusEnum.FAILED);
        } finally {
            bucketCodeLoadRepository.save(bucketCodeLoadEntity);
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void deleteBucketCodes(Long discountId) {
        if (discountId!=null) {
            discountBucketCodeRepository.deleteByDiscountId(discountId);
            bucketCodeLoadRepository.deleteByDiscountId(discountId);
            discountBucketCodeSummaryRepository.deleteByDiscountId(discountId);
        }
    }

    public Long countLoadedCodes(DiscountEntity discountEntity) {
        return discountBucketCodeRepository.countByDiscountAndBucketCodeLoadId(discountEntity,
                                                                               discountEntity.getLastBucketCodeLoad()
                                                                                             .getId());
    }
}
