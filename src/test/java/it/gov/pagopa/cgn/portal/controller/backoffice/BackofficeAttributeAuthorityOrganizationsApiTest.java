package it.gov.pagopa.cgn.portal.controller.backoffice;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.controller.BackofficeAttributeAuthorityOrganizationsController;
import it.gov.pagopa.cgn.portal.converter.backoffice.*;
import it.gov.pagopa.cgn.portal.facade.BackofficeAttributeAuthorityFacade;
import it.gov.pagopa.cgn.portal.service.AttributeAuthorityService;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.api.AttributeAuthorityApi;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferents;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        Mockito.doReturn(ResponseEntity.ok().build()).when(attributeAuthorityServiceMock).getOrganizations(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());
        mockMvc.perform(get("/organizations")).andDo(log()).andExpect(status().isOk());
    }

    @Test
    void UpsertOrganization_Ok() throws Exception {
        OrganizationWithReferents organization = new OrganizationWithReferents();
        organization.setKeyOrganizationFiscalCode("12345678");
        organization.setOrganizationFiscalCode("12345678");
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
    void UpsertOrganization_Ko() throws Exception {
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
}
