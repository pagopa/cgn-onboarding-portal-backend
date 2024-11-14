package it.gov.pagopa.cgn.portal.facade;


import it.gov.pagopa.cgn.portal.converter.AgreementConverter;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.service.AgreementService;
import it.gov.pagopa.cgnonboardingportal.model.Agreement;
import it.gov.pagopa.cgnonboardingportal.model.CompletedStep;
import it.gov.pagopa.cgnonboardingportal.model.UploadedImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class AgreementFacade {

    private final AgreementService agreementService;
    private final AgreementConverter agreementConverter;

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponseEntity<Agreement> createAgreement(String merchantTaxCode) {
        AgreementEntity agreementEntity = agreementService.getAgreementByMerchantTaxCode(merchantTaxCode);
        Agreement dto = agreementConverter.toDto(agreementEntity);
        dto.setCompletedSteps(getCompletedSteps(agreementEntity));
        return ResponseEntity.ok(dto);
    }

    public ResponseEntity<Void> requestApproval(String agreementId) {
        agreementService.requestApproval(agreementId);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<UploadedImage> uploadImage(String agreementId, MultipartFile image) {
        String imageUrl = agreementService.uploadImage(agreementId, image);
        UploadedImage uploadedImage = new UploadedImage();
        uploadedImage.setImageUrl(imageUrl);
        return ResponseEntity.ok(uploadedImage);
    }

    @Autowired
    public AgreementFacade(AgreementService agreementService, AgreementConverter agreementConverter) {
        this.agreementService = agreementService;
        this.agreementConverter = agreementConverter;
    }

    private List<CompletedStep> getCompletedSteps(AgreementEntity agreementEntity) {
        if (AgreementStateEnum.APPROVED.equals(agreementEntity.getState())
                || AgreementStateEnum.PENDING.equals(agreementEntity.getState())) {
            return Arrays.asList(CompletedStep.values());
        }
        List<CompletedStep> steps = new ArrayList<>();
        if (agreementEntity.getProfile() != null) {
            steps.add(CompletedStep.PROFILE);
        }
        if (!CollectionUtils.isEmpty(agreementEntity.getDiscountList())) {
            steps.add(CompletedStep.DISCOUNT);
        }
        if (!CollectionUtils.isEmpty(agreementEntity.getDocumentList())
                && agreementEntity.getDocumentList().size() >= DocumentTypeEnum.getNumberOfDocumentProfile()) {
            steps.add(CompletedStep.DOCUMENT);
        }
        return steps;
    }
}
