package it.gov.pagopa.cgn.portal.controller.backoffice;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.controller.BackofficeAttributeAuthorityOrganizationsController;
import it.gov.pagopa.cgn.portal.converter.backoffice.BackofficeAgreementConverter;
import it.gov.pagopa.cgn.portal.converter.backoffice.OrganizationWithReferentsConverter;
import it.gov.pagopa.cgn.portal.facade.BackofficeAttributeAuthorityFacade;
import it.gov.pagopa.cgn.portal.service.AttributeAuthorityService;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BackofficeAttributeAuthorityOrganizationsApiTest
        extends IntegrationAbstractTest {

    @Autowired
    protected OrganizationWithReferentsConverter organizationWithReferentsConverter;

    @Autowired
    protected BackofficeAgreementConverter backofficeAgreementConverter;
    @Autowired
    protected MockMvc mockMvc;
    @Mock
    private AttributeAuthorityService attributeAuthorityServiceMock;

    @PostConstruct
    void setup() {
        BackofficeAttributeAuthorityFacade facade = new BackofficeAttributeAuthorityFacade(attributeAuthorityServiceMock,
                                                                                           agreementService,
                                                                                           agreementUserService,
                                                                                           profileService,
                                                                                           organizationWithReferentsConverter,
                                                                                           backofficeAgreementConverter);
        BackofficeAttributeAuthorityOrganizationsController controller = new BackofficeAttributeAuthorityOrganizationsController(
                facade);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @BeforeEach
    void beforeEach() {
        setAdminAuth();
    }

    @Test
    void GetOrganizations_Ok()
            throws Exception {

        OrganizationWithReferentsAndStatus organization0 = createOrganizationWithReferentsAndStatusMock("12345678",
                                                                                                        "12345678",
                                                                                                        "org0",
                                                                                                        "org0@pec.it",
                                                                                                        OrganizationStatus.DRAFT,
                                                                                                        EntityType.PRIVATE);

        OrganizationWithReferentsAndStatus organization1 = createOrganizationWithReferentsAndStatusMock("12345679",
                                                                                                        "12345679",
                                                                                                        "org1",
                                                                                                        "org1@pec.it",
                                                                                                        OrganizationStatus.DRAFT,
                                                                                                        EntityType.PUBLIC_ADMINISTRATION);

        agreementService.createAgreementIfNotExists(organization0.getKeyOrganizationFiscalCode(),
                                                    organization0.getEntityType(),
                                                    TestUtils.FAKE_ORGANIZATION_NAME);

        agreementService.createAgreementIfNotExists(organization1.getKeyOrganizationFiscalCode(),
                                                    organization1.getEntityType(),
                                                    TestUtils.FAKE_ORGANIZATION_NAME);

        Organizations organizationsAA = new Organizations();

        organizationsAA.setItems(List.of(organization0,organization1));

        Mockito.doReturn(ResponseEntity.ok(organizationsAA))
               .when(attributeAuthorityServiceMock)
               .getOrganizations(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        mockMvc.perform(get("/organizations").contentType(MediaType.APPLICATION_JSON))
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andDo(log())
               .andExpect(jsonPath("$.items[0].entityType").value(EntityType.PRIVATE.getValue()))
               .andExpect(jsonPath("$.items[1].entityType").value(EntityType.PUBLIC_ADMINISTRATION.getValue()));
    }

    @Test
    void UpsertOrganization_Ok()
            throws Exception {
        OrganizationWithReferents organizationMockResult = getOrganizationWithReferents("00000000000");
        OrganizationWithReferentsAndStatus attributeAuthorityResponse =
            createOrganizationWithReferentsAndStatusFrom(organizationMockResult);


        Mockito.doReturn(ResponseEntity.ok().body(attributeAuthorityResponse))
               .when(attributeAuthorityServiceMock)
               .upsertOrganization(Mockito.any());

        mockMvc.perform(post("/organizations").contentType(MediaType.APPLICATION_JSON)
                                              .content(TestUtils.getJson(organizationMockResult)))
               .andDo(log())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.organizationName").value(organizationMockResult.getOrganizationName()))
               .andExpect(jsonPath("$.entityType").value(EntityType.PRIVATE.getValue()));

    }

    @Test
    void UpsertOrganization_BadRequest()
            throws Exception {
        OrganizationWithReferents organization = getOrganizationWithReferents("12345678");

        Mockito.doReturn(ResponseEntity.badRequest().build())
               .when(attributeAuthorityServiceMock)
               .upsertOrganization(Mockito.any());

        mockMvc.perform(post("/organizations").contentType(MediaType.APPLICATION_JSON)
                                              .content(TestUtils.getJson(organization)))
               .andDo(log())
               .andExpect(status().isBadRequest());
    }

    @Test
    void UpsertOrganization_referentNotFound_ok()
            throws Exception {
        OrganizationWithReferents organizationMockResult = getOrganizationWithReferents("00000000000");
        OrganizationWithReferentsAndStatus attributeAuthorityResponse =
            createOrganizationWithReferentsAndStatusFrom(organizationMockResult);

        Mockito.doReturn(ResponseEntity.ok().body(attributeAuthorityResponse))
               .when(attributeAuthorityServiceMock)
               .upsertOrganization(Mockito.any());

        Mockito.when(attributeAuthorityServiceMock.countUserOrganizations(Mockito.any()))
               .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        mockMvc.perform(post("/organizations").contentType(MediaType.APPLICATION_JSON)
                                              .content(TestUtils.getJson(organizationMockResult)))
               .andDo(log())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.organizationName").value(organizationMockResult.getOrganizationName()))
               .andExpect(jsonPath("$.entityType").value(EntityType.PRIVATE.getValue()));
    }

    private static OrganizationWithReferents getOrganizationWithReferents(String number) {
        OrganizationWithReferents organization = new OrganizationWithReferents();
        organization.setKeyOrganizationFiscalCode(number);
        organization.setOrganizationFiscalCode(number);
        organization.setOrganizationName("org 1");
        organization.setPec("org1@pec.it");
        organization.setEntityType(EntityType.PRIVATE);
        return organization;
    }

    private static OrganizationWithReferentsAndStatus createOrganizationWithReferentsAndStatusFrom(
            OrganizationWithReferents organizationWithReferents) {
        OrganizationWithReferentsAndStatus organizationWithReferentsAndStatus = new OrganizationWithReferentsAndStatus();
        organizationWithReferentsAndStatus.setKeyOrganizationFiscalCode(organizationWithReferents.getKeyOrganizationFiscalCode());
        organizationWithReferentsAndStatus.setOrganizationFiscalCode(organizationWithReferents.getOrganizationFiscalCode());
        organizationWithReferentsAndStatus.setOrganizationName(organizationWithReferents.getOrganizationName());
        organizationWithReferentsAndStatus.setPec(organizationWithReferents.getPec());
        organizationWithReferentsAndStatus.setInsertedAt(LocalDate.now());
        organizationWithReferentsAndStatus.setReferents(organizationWithReferents.getReferents());
        organizationWithReferentsAndStatus.setEntityType(organizationWithReferents.getEntityType());
        organizationWithReferentsAndStatus.setStatus(OrganizationStatus.DRAFT);
        return organizationWithReferentsAndStatus;
    }


    private OrganizationWithReferentsAndStatus createOrganizationWithReferentsAndStatusMock(String aKeyOrganizationFiscalCode,
                                                                                            String anOrganizationFiscalCode,
                                                                                            String anOrganizationName,
                                                                                            String anOrganizationPec,
                                                                                            OrganizationStatus status,
                                                                                            EntityType entityType) {
        OrganizationWithReferentsAndStatus organizationWithReferents = new OrganizationWithReferentsAndStatus();
        organizationWithReferents.setKeyOrganizationFiscalCode(aKeyOrganizationFiscalCode);
        organizationWithReferents.setOrganizationFiscalCode(anOrganizationFiscalCode);
        organizationWithReferents.setOrganizationName(anOrganizationName);
        organizationWithReferents.setPec(anOrganizationPec);
        organizationWithReferents.setInsertedAt(LocalDate.now());
        organizationWithReferents.setStatus(status);
        organizationWithReferents.setEntityType(entityType);
        return organizationWithReferents;
    }
}

