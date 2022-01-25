package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.converter.profile.CreateProfileConverter;
import it.gov.pagopa.cgn.portal.converter.profile.ProfileConverter;
import it.gov.pagopa.cgn.portal.converter.profile.UpdateProfileConverter;
import it.gov.pagopa.cgn.portal.converter.referent.CreateReferentConverter;
import it.gov.pagopa.cgn.portal.converter.referent.ReferentConverter;
import it.gov.pagopa.cgn.portal.converter.referent.UpdateReferentConverter;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgnonboardingportal.model.DiscountCodeType;
import it.gov.pagopa.cgnonboardingportal.model.UpdateProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class ProfileFacadeTest extends IntegrationAbstractTest {

    private ProfileFacade profileFacade;

    private AgreementEntity agreementEntity;
    private ProfileEntity profileEntity;

    private static final String STATIC_CODE = "static_code";
    private static final String URL = "www.landingpage.com";
    private static final String REFERRER = "referrer";

    @BeforeEach
    void init() {
        var createReferentConverter = new CreateReferentConverter();
        var updateReferentConverter = new UpdateReferentConverter();
        var referentConverter = new ReferentConverter();
        var createProfileConverter = new CreateProfileConverter(createReferentConverter);
        var updateProfileConverter = new UpdateProfileConverter(updateReferentConverter);
        var profileConverter = new ProfileConverter(referentConverter);
        profileFacade = new ProfileFacade(profileService, createProfileConverter, updateProfileConverter, profileConverter, discountService);

        agreementEntity = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
    }

    @Test
    void UpdateProfile_ChangeDiscountCodeType_UnpublishDiscounts() {
        var agreementId = agreementEntity.getId();

        // set profile for landing page
        setProfileDiscountType(agreementEntity, DiscountCodeTypeEnum.LANDINGPAGE);

        // create a discount and request agreement approval
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity, URL, REFERRER);
        discountEntity = discountService.createDiscount(agreementId, discountEntity).getDiscountEntity();
        agreementService.requestApproval(agreementId);
        var discountId = discountEntity.getId();

        // admin approve agreement
        adminApproveAgreement();

        // operator publish the discount
        discountService.publishDiscount(agreementId, discountId);
        discountService.getDiscounts(agreementId).forEach(d -> {
            Assertions.assertEquals(DiscountStateEnum.PUBLISHED, d.getState());
            Assertions.assertNotNull(d.getLandingPageUrl());
            Assertions.assertNotNull(d.getLandingPageReferrer());
        });

        // operator change his profile to stati code
        UpdateProfile updateProfile = TestUtils.updatableOnlineProfileFromProfileEntity(profileEntity, DiscountCodeType.STATIC);
        profileFacade.updateProfile(agreementId, updateProfile);

        // discount should be landing page related and suspended
        discountService.getDiscounts(agreementId).forEach(d -> {
            Assertions.assertEquals(DiscountStateEnum.SUSPENDED, d.getState());
            Assertions.assertNotNull(d.getLandingPageUrl());
            Assertions.assertNotNull(d.getLandingPageReferrer());
        });

        // any try to publish the discount without its update should cause an exception
        Assertions.assertThrows(InvalidRequestException.class, () -> {
            discountService.publishDiscount(agreementId, discountId);
        });

        // update the discount to be a static code base discount
        discountEntity.setStaticCode(STATIC_CODE);
        discountService.updateDiscount(agreementId, discountId, discountEntity);

        // now we can publish the discount
        discountService.publishDiscount(agreementId, discountId);

        // discount should be static code related and published again
        discountService.getDiscounts(agreementId).forEach(d -> {
            Assertions.assertEquals(DiscountStateEnum.PUBLISHED, d.getState());
            Assertions.assertNull(d.getLandingPageUrl());
            Assertions.assertNull(d.getLandingPageReferrer());
            Assertions.assertNotNull(d.getStaticCode());
        });

    }

    void adminApproveAgreement() {
        TestUtils.setAdminAuth();
        backofficeAgreementService.assignAgreement(agreementEntity.getId());
        saveBackofficeSampleDocuments(agreementEntity);
        backofficeAgreementService.approveAgreement(agreementEntity.getId());
    }

}
