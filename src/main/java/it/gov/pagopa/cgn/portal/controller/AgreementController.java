package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.converter.AgreementConverter;
import it.gov.pagopa.cgn.portal.facade.DiscountFacade;
import it.gov.pagopa.cgn.portal.facade.ProfileFacade;
import it.gov.pagopa.cgn.portal.service.AgreementService;
import it.gov.pagopa.cgnonboardingportal.api.AgreementsApi;
import it.gov.pagopa.cgnonboardingportal.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgreementController implements AgreementsApi {

    private final AgreementService agreementService;

    private final ProfileFacade profileFacade;
    private final DiscountFacade discountFacade;

    private final AgreementConverter agreementConverter;

    @Override
    public ResponseEntity<Agreement> createAgreement() {
        return ResponseEntity.ok(
                agreementConverter.toDto(agreementService.createAgreementIfNotExists()));
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

    @Autowired
    public AgreementController(AgreementService agreementService,
                               ProfileFacade profileFacade,
                               AgreementConverter agreementConverter,
                               DiscountFacade discountFacade) {
        this.agreementService = agreementService;
        this.agreementConverter = agreementConverter;
        this.profileFacade = profileFacade;
        this.discountFacade = discountFacade;
    }
}

