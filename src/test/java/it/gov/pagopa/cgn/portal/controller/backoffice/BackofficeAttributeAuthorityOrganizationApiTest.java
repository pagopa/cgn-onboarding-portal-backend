package it.gov.pagopa.cgn.portal.controller.backoffice;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.controller.BackofficeAttributeAuthorityOrganizationController;
import it.gov.pagopa.cgn.portal.facade.BackofficeAttributeAuthorityFacade;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ReferentFiscalCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BackofficeAttributeAuthorityOrganizationApiTest extends IntegrationAbstractTest {

    private BackofficeAttributeAuthorityFacade backofficeAttributeAuthorityFacade;

    private BackofficeAttributeAuthorityOrganizationController backofficeAttributeAuthorityOrganizationController;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        backofficeAttributeAuthorityFacade = Mockito.mock(BackofficeAttributeAuthorityFacade.class);
        backofficeAttributeAuthorityOrganizationController =
                new BackofficeAttributeAuthorityOrganizationController(backofficeAttributeAuthorityFacade);
        mockMvc = MockMvcBuilders.standaloneSetup(backofficeAttributeAuthorityOrganizationController).build();
        setAdminAuth();
    }

    @Test
    void GetOrganization_Ok() throws Exception {
        mockMvc.perform(get("/organization/1234567890")).andDo(log()).andExpect(status().isOk());
    }

    @Test
    void DeleteOrganization_Ok() throws Exception {
        mockMvc.perform(delete("/organization/1234567890")).andDo(log()).andExpect(status().isOk());
    }

    @Test
    void GetReferents_Ok() throws Exception {
        mockMvc.perform(get("/organization/1234567890/referents")).andDo(log()).andExpect(status().isOk());
    }

    @Test
    void InsertReferent_Ok() throws Exception {
        ReferentFiscalCode referentFiscalCode = new ReferentFiscalCode();
        referentFiscalCode.setReferentFiscalCode("AAAAAA00A00A000A");
        mockMvc.perform(post("/organization/1234567890/referents").contentType(MediaType.APPLICATION_JSON)
                                                                  .content(TestUtils.getJson(referentFiscalCode)))
               .andDo(log())
               .andExpect(status().isOk());
    }

    @Test
    void DeleteReferent_Ok() throws Exception {
        mockMvc.perform(delete("/organization/1234567890/referents/AAAAAA00A00A000A"))
               .andDo(log())
               .andExpect(status().isOk());
    }
}
