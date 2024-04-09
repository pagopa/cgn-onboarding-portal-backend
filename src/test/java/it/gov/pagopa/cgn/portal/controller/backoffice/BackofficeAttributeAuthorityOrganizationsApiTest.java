package it.gov.pagopa.cgn.portal.controller.backoffice;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.controller.BackofficeAttributeAuthorityOrganizationsController;
import it.gov.pagopa.cgn.portal.converter.backoffice.*;
import it.gov.pagopa.cgn.portal.facade.BackofficeAttributeAuthorityFacade;
import it.gov.pagopa.cgn.portal.service.AttributeAuthorityService;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BackofficeAttributeAuthorityOrganizationsApiTest extends IntegrationAbstractTest {

    @Autowired
    protected OrganizationsConverter organizationsConverter;

    @Autowired
    protected OrganizationWithReferentsConverter organizationWithReferentsConverter;

    @Autowired
    protected OrganizationWithReferentsAndStatusConverter organizationWithReferentsAndStatusConverter;

    @Autowired
    protected OrganizationWithReferentsPostConverter organizationWithReferentsPostConverter;

    @Autowired
    protected ReferentFiscalCodeConverter referentFiscalCodeConverter;

    @Autowired
    protected BackofficeAgreementConverter backofficeAgreementConverter;

    @Mock
    private AttributeAuthorityService attributeAuthorityServiceMock;

    @Autowired
    protected MockMvc mockMvc;

    @PostConstruct
    void setup(){
        BackofficeAttributeAuthorityFacade facade =
                new BackofficeAttributeAuthorityFacade(
                        attributeAuthorityServiceMock,
                        agreementService,
                        agreementUserService,
                        profileService,
                        organizationsConverter,
                        organizationWithReferentsConverter,
                        organizationWithReferentsAndStatusConverter,
                        organizationWithReferentsPostConverter,
                        referentFiscalCodeConverter,
                        backofficeAgreementConverter);
        BackofficeAttributeAuthorityOrganizationsController controller = new BackofficeAttributeAuthorityOrganizationsController(facade);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @BeforeEach
    void beforeEach() {
        setAdminAuth();
    }

    @Test
    void GetOrganizations_Ok() throws Exception {

        OrganizationWithReferentsAndStatus organization0 = createOrganizationWithReferentsAndStatusMock("12345678",
                "12345678",
                "org0",
                "org0@pec.it",OrganizationStatus.DRAFT,EntityType.PRIVATE);

        OrganizationWithReferentsAndStatus organization1 = createOrganizationWithReferentsAndStatusMock("12345679",
                "12345679",
                "org1",
                "org1@pec.it",OrganizationStatus.DRAFT,EntityType.PUBLICADMINISTRATION);

        agreementService.createAgreementIfNotExists(organization0.getKeyOrganizationFiscalCode(),organization0.getEntityType());

        agreementService.createAgreementIfNotExists(organization1.getKeyOrganizationFiscalCode(),organization1.getEntityType());

        OrganizationsAttributeAuthority organizationsAA = new OrganizationsAttributeAuthority();

        organizationsAA.setItems(List.of(
                organizationWithReferentsAndStatusConverter.toAttributeAuthorityModel(organization0),
                organizationWithReferentsAndStatusConverter.toAttributeAuthorityModel(organization1)));

        Mockito.doReturn(ResponseEntity.ok(organizationsAA)).when(attributeAuthorityServiceMock)
                .getOrganizations(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());

//        TestUtils.printMvcResponse(mockMvc.perform(get("/organizations")));

            mockMvc.perform(get("/organizations").contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(jsonPath("$.items[0].entityType").value(EntityType.PRIVATE.getValue()))
                .andExpect(jsonPath("$.items[1].entityType").value(EntityType.PUBLICADMINISTRATION.getValue()));
    }

    @Test
    void UpsertOrganization_Ok() throws Exception {
        OrganizationWithReferents organization = new OrganizationWithReferents();
        organization.setKeyOrganizationFiscalCode("00000000000");
        organization.setOrganizationFiscalCode("00000000000");
        organization.setOrganizationName("org 1");
        organization.setPec("org1@pec.it");
        organization.setEntityType(EntityType.PRIVATE);

        OrganizationWithReferentsAttributeAuthority mockResult = new OrganizationWithReferentsAttributeAuthority();
        mockResult.setKeyOrganizationFiscalCode(organization.getKeyOrganizationFiscalCode());
        mockResult.setOrganizationFiscalCode(organization.getOrganizationFiscalCode());
        mockResult.setPec(organization.getPec());
        mockResult.setOrganizationName(organization.getOrganizationName());
        mockResult.setReferents(organization.getReferents());
        mockResult.setInsertedAt(Timestamp.valueOf(LocalDateTime.now()));

        Mockito.doReturn(ResponseEntity.ok().body(mockResult)).when(attributeAuthorityServiceMock).upsertOrganization(Mockito.any());

        mockMvc.perform(post("/organizations").contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.getJson(organization)))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationName").value(organization.getOrganizationName()))
                .andExpect(jsonPath("$.entityType").value(EntityType.PRIVATE.getValue()));

    }

    @Test
    void UpsertOrganization_BadRequest() throws Exception {
        OrganizationWithReferents organization = new OrganizationWithReferents();
        organization.setKeyOrganizationFiscalCode("12345678");
        organization.setOrganizationFiscalCode("12345678");
        organization.setOrganizationName("org 1");
        organization.setPec("org1@pec.it");
        organization.setEntityType(EntityType.PRIVATE);

        Mockito.doReturn(ResponseEntity.badRequest().build()).when(attributeAuthorityServiceMock).upsertOrganization(Mockito.any());

        mockMvc.perform(post("/organizations").contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.getJson(organization)))
                .andDo(log())
                .andExpect(status().isBadRequest());
    }

    private OrganizationWithReferentsAndStatus createOrganizationWithReferentsAndStatusMock(String aKeyOrganizationFiscalCode,
                                                                                            String anOrganizationFiscalCode,
                                                                                            String anOrganizationName,
                                                                                            String anOrganizationPec, OrganizationStatus status, EntityType entityType) {
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
