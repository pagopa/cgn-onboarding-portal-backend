package it.gov.pagopa.cgn.portal.controller;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.service.AgreementService;
import it.gov.pagopa.cgn.portal.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class DocumentApiTest extends IntegrationAbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ConfigProperties configProperties;

    private BlobContainerClient documentContainerClient;

    @BeforeEach
    void before() {

        documentContainerClient = new BlobContainerClientBuilder()
                .connectionString(getAzureConnectionString())
                .containerName(configProperties.getDocumentsContainerName())
                .buildClient();

        if (!documentContainerClient.exists()) {
            documentContainerClient.create();
        }
    }


    @Test
    void GetDocuments_GetDocuments_Ok() throws Exception {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        byte[] content = "pdf-document".getBytes(StandardCharsets.UTF_8);
        documentService.storeDocument(agreementEntity.getId(),
                DocumentTypeEnum.AGREEMENT, new ByteArrayInputStream(content), content.length);

        this.mockMvc.perform(
                get(TestUtils.getDocumentPath(agreementEntity.getId())))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items[0].documentType").value("agreement"))
                .andExpect(jsonPath("$.items[0].documentUrl").isNotEmpty());
    }
}