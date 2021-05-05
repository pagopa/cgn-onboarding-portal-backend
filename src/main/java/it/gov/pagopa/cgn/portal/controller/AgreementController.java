package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.converter.AgreementConverter;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.facade.DiscountFacade;
import it.gov.pagopa.cgn.portal.facade.DocumentFacade;
import it.gov.pagopa.cgn.portal.facade.ProfileFacade;
import it.gov.pagopa.cgn.portal.security.JwtAuthenticationToken;
import it.gov.pagopa.cgn.portal.security.JwtOperatorUser;
import it.gov.pagopa.cgn.portal.service.AgreementService;
import it.gov.pagopa.cgnonboardingportal.api.AgreementsApi;
import it.gov.pagopa.cgnonboardingportal.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@PreAuthorize("hasRole('ROLE_MERCHANT')")
public class AgreementController implements AgreementsApi {

    private final AgreementService agreementService;

    private final ProfileFacade profileFacade;
    private final DiscountFacade discountFacade;
    private final DocumentFacade documentFacade;

    private final AgreementConverter agreementConverter;

    @Override
    public ResponseEntity<Agreement> createAgreement() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        JwtOperatorUser user = (JwtOperatorUser) authentication.getPrincipal();

        return ResponseEntity.ok(
                agreementConverter.toDto(agreementService.createAgreementIfNotExists(user.getMerchantTaxCode())));
    }

    @Override
    public ResponseEntity<Void> requestApproval(String agreementId) {
        agreementService.requestApproval(agreementId);
        return ResponseEntity.noContent().build();
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
        try {
            if (document.getOriginalFilename() == null || !document.getOriginalFilename().endsWith("pdf")) {
                throw new InvalidRequestException("Invalid file extension. Upload a PDF document.");
            }
            return documentFacade.uploadDocument(agreementId, documentType, document.getInputStream(), document.getSize());
        } catch (IOException exc) {
            throw new RuntimeException("Upload document failed", exc);
        }
    }

    @Override
    public ResponseEntity<Void> deleteDocument(String agreementId, String documentType) {
        documentFacade.deleteDocument(agreementId, documentType);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> publishDiscount(String agreementId, String discountId) {
        discountFacade.publishDiscount(agreementId, discountId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UploadedImage> uploadImage(String agreementId, MultipartFile image) {
        String imageUrl = agreementService.uploadImage(agreementId, image);
        UploadedImage uploadedImage = new UploadedImage();
        uploadedImage.setImageUrl(imageUrl);
        return ResponseEntity.ok(uploadedImage);
    }


    @Autowired
    public AgreementController(AgreementService agreementService,
                               DocumentFacade documentFacade,
                               ProfileFacade profileFacade,
                               AgreementConverter agreementConverter,
                               DiscountFacade discountFacade) {
        this.agreementService = agreementService;
        this.agreementConverter = agreementConverter;
        this.profileFacade = profileFacade;
        this.discountFacade = discountFacade;
        this.documentFacade = documentFacade;
    }
}

