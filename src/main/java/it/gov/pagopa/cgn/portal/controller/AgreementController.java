package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.facade.AgreementFacade;
import it.gov.pagopa.cgn.portal.facade.DiscountFacade;
import it.gov.pagopa.cgn.portal.facade.DocumentFacade;
import it.gov.pagopa.cgn.portal.facade.ProfileFacade;
import it.gov.pagopa.cgn.portal.service.ApiTokenService;
import it.gov.pagopa.cgn.portal.service.HelpService;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import it.gov.pagopa.cgnonboardingportal.api.AgreementsApi;
import it.gov.pagopa.cgnonboardingportal.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@PreAuthorize("hasRole('ROLE_MERCHANT')")
public class AgreementController
        implements AgreementsApi {

    private final ProfileFacade profileFacade;
    private final DiscountFacade discountFacade;
    private final DocumentFacade documentFacade;
    private final AgreementFacade agreementFacade;
    private final ApiTokenService apiTokenService;
    private final HelpService helpService;

    @Autowired
    public AgreementController(AgreementFacade agreementFacade,
                               DocumentFacade documentFacade,
                               ProfileFacade profileFacade,
                               DiscountFacade discountFacade,
                               ApiTokenService apiTokenService,
                               HelpService helpService) {
        this.agreementFacade = agreementFacade;
        this.profileFacade = profileFacade;
        this.discountFacade = discountFacade;
        this.documentFacade = documentFacade;
        this.apiTokenService = apiTokenService;
        this.helpService = helpService;
    }

    @Override
    public ResponseEntity<Agreement> createAgreement() {
        return agreementFacade.createAgreement(CGNUtils.getJwtOperatorUserId());
    }

    @Override
    public ResponseEntity<Void> requestApproval(String agreementId) {
        return agreementFacade.requestApproval(agreementId);
    }

    @Override
    public ResponseEntity<Profile> createProfile(String agreementId, CreateProfile createRegistryDto) {
        return profileFacade.createProfile(agreementId, createRegistryDto);
    }

    @Override
    public ResponseEntity<Profile> getProfile(String agreementId) {
        return profileFacade.getProfile(agreementId);
    }

    @Override
    public ResponseEntity<Profile> updateProfile(String agreementId, UpdateProfile updateProfile) {
        return profileFacade.updateProfile(agreementId, updateProfile);
    }

    @Override
    public ResponseEntity<Discount> createDiscount(String agreementId, CreateDiscount createDiscountDto) {
        return discountFacade.createDiscount(agreementId, createDiscountDto);
    }

    @Override
    public ResponseEntity<Discounts> getDiscounts(String agreementId) {
        return discountFacade.getDiscounts(agreementId);
    }

    @Override
    public ResponseEntity<Discount> getDiscountById(String agreementId, String discountId) {
        return discountFacade.getDiscountById(agreementId, discountId);
    }

    @Override
    public ResponseEntity<Discount> updateDiscount(String agreementId, String discountId, UpdateDiscount discount) {
        return discountFacade.updateDiscount(agreementId, discountId, discount);
    }

    @Override
    public ResponseEntity<Void> deleteDiscount(String agreementId, String discountId) {
        discountFacade.deleteDiscount(agreementId, discountId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Documents> getDocuments(String agreementId) {
        return documentFacade.getDocuments(agreementId);
    }

    @Override
    public ResponseEntity<Resource> downloadDocumentTemplate(String agreementId, String documentType) {
        return documentFacade.getDocumentTemplate(agreementId, documentType);
    }

    @Override
    public ResponseEntity<Document> uploadDocument(String agreementId, String documentType, MultipartFile document) {
        CGNUtils.checkIfPdfFile(document.getOriginalFilename());
        return documentFacade.uploadDocument(agreementId, documentType, document);
    }

    @Override
    public ResponseEntity<BucketLoad> uploadBucket(String agreementId, MultipartFile document) {
        CGNUtils.checkIfCsvFile(document.getOriginalFilename());
        return documentFacade.uploadBucket(agreementId, document);
    }

    @Override
    public ResponseEntity<Void> deleteDocument(String agreementId, String documentType) {
        documentFacade.deleteDocument(agreementId, documentType);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> testDiscount(String agreementId, String discountId) {
        discountFacade.testDiscount(agreementId, discountId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> publishDiscount(String agreementId, String discountId) {
        discountFacade.publishDiscount(agreementId, discountId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> unpublishDiscount(String agreementId, String discountId) {
        discountFacade.unpublishDiscount(agreementId, discountId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UploadedImage> uploadImage(String agreementId, MultipartFile image) {
        return agreementFacade.uploadImage(agreementId, image);
    }

    @Override
    public ResponseEntity<ApiTokens> getTokens(String agreementId) {
        return ResponseEntity.ok(apiTokenService.getTokens(agreementId));
    }

    @Override
    public ResponseEntity<ApiTokens> regenerateToken(String agreementId, String tokenType) {
        return ResponseEntity.ok(apiTokenService.regenerateToken(agreementId, tokenType));
    }

    @Override
    public ResponseEntity<Void> sendHelpRequest(String agreementId, HelpRequest helpRequest) {
        helpService.sendHelpMessage(agreementId,
                                    helpRequest.getCategory(),
                                    Optional.ofNullable(helpRequest.getTopic()),
                                    helpRequest.getMessage());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<DiscountBucketCodeLoadingProgess> getDiscountBucketCodeLoadingProgess(String agreementId,
                                                                                                String discountId) {
        return ResponseEntity.ok(discountFacade.getDiscountBucketCodeLoadingProgess(agreementId, discountId));
    }
}
