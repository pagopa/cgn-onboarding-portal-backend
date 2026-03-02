package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.enums.ParamGroupEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.service.AgreementService;
import it.gov.pagopa.cgn.portal.service.DiscountService;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class EycaDataExportViewTest extends IntegrationAbstractTest {

    @Autowired
    private EycaDataExportRepository eycaDataExportRepository;

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ParamRepository paramRepository;

    @Test
    void eycaDataExportView_shouldUseImagePrefixFromParamTable() {
        AgreementEntity agreement = agreementService.createAgreementIfNotExists(
                TestUtils.FAKE_ID,
                EntityType.PRIVATE,
                TestUtils.FAKE_ORGANIZATION_NAME
        );
        agreement.setImageUrl("test-images/sample-image.jpg");
        agreementRepository.save(agreement);
        
        ProfileEntity profile = TestUtils.createSampleProfileEntity(agreement, SalesChannelEnum.OFFLINE, null);
        profileService.createProfile(profile, agreement.getId());

        DiscountEntity discount = TestUtils.createSampleDiscountEntity(agreement);
        discount.setState(DiscountStateEnum.PUBLISHED);
        discount.setVisibleOnEyca(true);
        discount.setStartDate(LocalDate.now().minusDays(1));
        discount.setEndDate(LocalDate.now().plusDays(30));
        discount = discountService.createDiscount(agreement.getId(), discount).getDiscountEntity();

        entityManager.flush();

        List<EycaDataExportViewEntity> results = eycaDataExportRepository.findAll();

        assertFalse(results.isEmpty(), "View should return at least one record");

        DiscountEntity finalDiscount = discount;
        EycaDataExportViewEntity entity = results.stream()
                                                 .filter(e -> e.getDiscountId().equals(finalDiscount.getId()))
                                                 .findFirst()
                                                 .orElse(null);
        
        assertNotNull(entity, "Should find entity for the created discount");
        
        ParamEntity paramEntity = paramRepository.findByParamGroupAndParamKey(ParamGroupEnum.SEND_DISCOUNTS_EYCA_JOB, "prefix_image_source__web")
                .orElseThrow(() -> new AssertionError("Param prefix_image_source__web should exist"));
        
        String expectedImage = paramEntity.getParamValue() + "test-images/sample-image.jpg";
        assertEquals(expectedImage, entity.getImage(), "Image URL should be correctly composed of prefix + image_url");
    }
}
