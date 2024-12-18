package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppServerControllerTest
        extends IntegrationAbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void Get_GetWithoutAuth_ok()
            throws Exception {
        this.mockMvc.perform(get("/").contentType(MediaType.APPLICATION_JSON))
                    .andDo(log())
                    .andExpect(status().isNoContent());
    }
}
