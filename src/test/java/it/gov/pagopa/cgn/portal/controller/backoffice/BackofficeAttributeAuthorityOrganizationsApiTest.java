package it.gov.pagopa.cgn.portal.controller.backoffice;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.controller.BackofficeAttributeAuthorityOrganizationsController;
import it.gov.pagopa.cgn.portal.facade.BackofficeAttributeAuthorityFacade;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferents;
import it.gov.pagopa.cgnonboardingportal.model.AgreementState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BackofficeAttributeAuthorityOrganizationsApiTest extends IntegrationAbstractTest {

    private BackofficeAttributeAuthorityFacade backofficeAttributeAuthorityFacade;

    private BackofficeAttributeAuthorityOrganizationsController backofficeAttributeAuthorityOrganizationsController;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        backofficeAttributeAuthorityFacade = Mockito.mock(BackofficeAttributeAuthorityFacade.class);
        backofficeAttributeAuthorityOrganizationsController =
                new BackofficeAttributeAuthorityOrganizationsController(backofficeAttributeAuthorityFacade);
        mockMvc = MockMvcBuilders.standaloneSetup(backofficeAttributeAuthorityOrganizationsController).build();
        setAdminAuth();
    }

    @Test
    void GetOrganizations_Ok() throws Exception {
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

        mockMvc.perform(post("/organizations").contentType(MediaType.APPLICATION_JSON)
                                              .content(TestUtils.getJson(organization)))
               .andDo(log())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationName").value(organization.getOrganizationName()))
                .andExpect(jsonPath("$.entityType").value(EntityType.PRIVATE));

    }
}
