package it.gov.pagopa.cgn.portal.controller.discount;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.security.JwtAuthenticationToken;
import it.gov.pagopa.cgn.portal.security.JwtOperatorUser;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import it.gov.pagopa.cgnonboardingportal.model.CreateDiscount;
import it.gov.pagopa.cgnonboardingportal.model.DiscountState;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.service.AgreementService;
import it.gov.pagopa.cgn.portal.service.DiscountService;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import it.gov.pagopa.cgnonboardingportal.model.CreateDiscount;
import it.gov.pagopa.cgnonboardingportal.model.DiscountState;
import it.gov.pagopa.cgnonboardingportal.model.ProductCategory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class DiscountApiTest extends IntegrationAbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private ProfileService profileService;

    private String discountPath;
    private AgreementEntity agreement;

    @BeforeEach
    void init() {
        agreement = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreement);
        profileService.createProfile(profileEntity, agreement.getId());
        discountPath = TestUtils.getDiscountPath(agreement.getId());
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(new JwtOperatorUser(TestUtils.FAKE_ID, TestUtils.FAKE_ID, "merchant_name"))
        );
    }

    @Test
    void Create_CreateDiscount_Ok() throws Exception {
        CreateDiscount discount = createSampleCreateDiscount();
        this.mockMvc.perform(
                post(discountPath).contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(discount)))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.agreementId").value(agreement.getId()))
                .andExpect(jsonPath("$.state").value(DiscountState.DRAFT.getValue()))   //default state
                .andExpect(jsonPath("$.name").value(discount.getName()))
                .andExpect(jsonPath("$.description").value(discount.getDescription()))
                .andExpect(jsonPath("$.startDate").value(discount.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(discount.getEndDate().toString()))
                .andExpect(jsonPath("$.discount").value(discount.getDiscount()))
                .andExpect(jsonPath("$.productCategories").isArray())
                .andExpect(jsonPath("$.productCategories").isNotEmpty())
                .andExpect(jsonPath("$.staticCode").value(discount.getStaticCode()))
                .andExpect(jsonPath("$.condition").value(discount.getCondition()))
                .andExpect(jsonPath("$.creationDate").value(LocalDate.now().toString()));
    }

    @Test
    void Create_CreateDiscountWithoutStartDate_Ok() throws Exception {
        CreateDiscount discount = createSampleCreateDiscount();
        discount.setStartDate(null);
        this.mockMvc.perform(
                post(discountPath).contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(discount)))
                .andDo(log())
                .andExpect(status().isBadRequest());
    }

    @Test
    void Create_CreateDiscountWithoutStaticCode_Ok() throws Exception {
        CreateDiscount discount = createSampleCreateDiscount();
        discount.setStaticCode(null);
        this.mockMvc.perform(
                post(discountPath).contentType(MediaType.APPLICATION_JSON).content(TestUtils.getJson(discount)))
                .andDo(log())
                .andExpect(status().isBadRequest());
    }

    @Test
    void Get_GetDiscount_Found() throws Exception {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);
        discountService.createDiscount(agreement.getId(), discountEntity);

        this.mockMvc.perform(
                get(discountPath).contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").isNotEmpty())
                .andExpect(jsonPath("$.items[0].productCategories", hasSize(2)));
    }

    @Test
    void Get_GetDiscount_NotFound() throws Exception {
        this.mockMvc.perform(
                get(discountPath).contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void Delete_DeleteDiscount_Ok() throws Exception {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreement);
        discountService.createDiscount(agreement.getId(), discountEntity);
        this.mockMvc.perform(
                delete(discountPath + "/" + discountEntity.getId()).contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isNoContent());
        List<DiscountEntity> discounts = discountService.getDiscounts(agreement.getId());
        Assertions.assertNotNull(discounts);
        Assertions.assertEquals(0, discounts.size());
    }

    @Test
    void Delete_DeleteDiscount_NotFound() throws Exception {
        this.mockMvc.perform(
                delete(discountPath + "/" + 1).contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isNotFound());
        List<DiscountEntity> discounts = discountService.getDiscounts(agreement.getId());
        Assertions.assertNotNull(discounts);
        Assertions.assertEquals(0, discounts.size());
    }

    private CreateDiscount createSampleCreateDiscount() {
        CreateDiscount createDiscount = new CreateDiscount();
        createDiscount.setName("create_discount_name");
        createDiscount.setDescription("create_discount_description");
        createDiscount.setDiscount(15);
        createDiscount.setCondition("create_discount_condition");
        createDiscount.setStartDate(LocalDate.now());
        createDiscount.setEndDate(LocalDate.now().plusMonths(6));
        createDiscount.setStaticCode("create_discount_static_code");
        createDiscount.setProductCategories(Arrays.asList(ProductCategory.TRAVELS, ProductCategory.SPORTS));
        return createDiscount;
    }
}
