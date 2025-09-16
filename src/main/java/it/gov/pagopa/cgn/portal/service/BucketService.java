package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.enums.BucketCodeExpiringThresholdEnum;
import it.gov.pagopa.cgn.portal.enums.BucketCodeLoadStatusEnum;
import it.gov.pagopa.cgn.portal.exception.InternalErrorException;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.repository.BucketCodeLoadRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountBucketCodeRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountBucketCodeSummaryRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.util.*;
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
    private static final String TEXT_EMAIL_SENT = "an email notification has been sent.";

    @Autowired
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
    public void updateDiscountBucketCodeSummary(DiscountBucketCodeSummaryEntity discountBucketCodeSummaryEntity) {
        log.info("Updating discount bucket summary for id {}:", discountBucketCodeSummaryEntity.getId());
        var discountBucketCodeSummary = discountBucketCodeSummaryRepository.getReferenceById(
                discountBucketCodeSummaryEntity.getId());
        DiscountEntity discount = discountBucketCodeSummary.getDiscount();
        var remainingCodes = discountBucketCodeRepository.countNotUsedByDiscountId(discount.getId());
        discountBucketCodeSummary.setAvailableCodes(remainingCodes);
        discountBucketCodeSummary.setUpdateTime(OffsetDateTime.now());
        if (remainingCodes <= 0) {
            discountBucketCodeSummary.setExpiredAt(OffsetDateTime.now());
        } else {
            discountBucketCodeSummary.setExpiredAt(null);
        }
        discountBucketCodeSummaryRepository.save(discountBucketCodeSummary);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void notifyWeeklyMerchantDiscountBucketCodesSummary(ProfileEntity profileEntity,
                                                               List<Map<String, Long>> listOfDiscountsToAvailableCodes) {
        emailNotificationFacade.notifyWeeklyMerchantDiscountBucketCodesSummary(profileEntity,
                                                                               listOfDiscountsToAvailableCodes);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void checkDiscountBucketCodeSummaryAndSendNotification(DiscountBucketCodeSummaryEntity discountBucketCodeSummaryEntity) {
        var discountBucketCodeSummary = discountBucketCodeSummaryRepository.getReferenceById(
                discountBucketCodeSummaryEntity.getId());
        DiscountEntity discount = discountBucketCodeSummary.getDiscount();

        var totalCodes = discountBucketCodeSummary.getTotalCodes();
        var remainingCodes = discountBucketCodeSummary.getAvailableCodes();

        if (totalCodes <= 0) {
            throw new InternalErrorException("totalCodes <= 0 summary id: " + discountBucketCodeSummary.getId());
        }

        double remainingPercent = Math.floor(Float.valueOf(remainingCodes) / Float.valueOf(totalCodes) * 100);
        DecimalFormat df = new DecimalFormat("0.###");
        String actualValuesStr = String.format("DiscountId: %s, totalCodes: %s remainingCodes: %s, remainingPercent: %s: {}",discount.getId(),totalCodes,remainingCodes,df.format(remainingPercent));

        if (remainingCodes <= 0) {
            emailNotificationFacade.notifyMerchantDiscountBucketCodesExpired(discount);
            log.info(actualValuesStr,"All bucket codes have expired; an email notification has been sent.");
            return;
        }

        if (remainingPercent <= BucketCodeExpiringThresholdEnum.PERCENT_10.getValue()) {
            emailNotificationFacade.notifyMerchantDiscountBucketCodesExpiring(discount,
                                                                              BucketCodeExpiringThresholdEnum.PERCENT_10,
                                                                              remainingCodes);
            log.info(actualValuesStr, TEXT_EMAIL_SENT);
            return;
        }

        if (remainingPercent <= BucketCodeExpiringThresholdEnum.PERCENT_25.getValue()) {
            emailNotificationFacade.notifyMerchantDiscountBucketCodesExpiring(discount,
                                                                              BucketCodeExpiringThresholdEnum.PERCENT_25,
                                                                              remainingCodes);
            log.info(actualValuesStr,TEXT_EMAIL_SENT);
            return;
        }

        if (remainingPercent <= BucketCodeExpiringThresholdEnum.PERCENT_50.getValue()) {
            emailNotificationFacade.notifyMerchantDiscountBucketCodesExpiring(discount,
                                                                              BucketCodeExpiringThresholdEnum.PERCENT_50,
                                                                              remainingCodes);
            log.info(actualValuesStr,TEXT_EMAIL_SENT);
        }

        log.info(actualValuesStr,"there are enough bucket codes. No notification email sent.");
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
            discountBucketCodeSummaryEntity.setTotalCodes(availableCodes);
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
