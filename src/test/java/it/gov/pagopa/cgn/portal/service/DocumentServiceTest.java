package it.gov.pagopa.cgn.portal.service;


import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


@SpringBootTest
@ActiveProfiles("dev")
class DocumentServiceTest extends IntegrationAbstractTest {

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ConfigProperties configProperties;

    private BlobContainerClient documentContainerClient;

    private AgreementEntity agreementEntity;

    @BeforeEach
    void init() {

        documentContainerClient = new BlobContainerClientBuilder()
                .connectionString(getAzureConnectionString())
                .containerName(configProperties.getDocumentsContainerName())
                .buildClient();
        documentContainerClient.create();

        agreementEntity = agreementService.createAgreementIfNotExists();
    }


    @Test
    void Upload_UploadDocumentWithValidData_Ok() throws IOException {
        byte[] content = "pdf-document".getBytes(StandardCharsets.UTF_8);

        DocumentEntity documentEntity = documentService.storeDocument(agreementEntity.getId(), DocumentTypeEnum.AGREEMENT, new ByteArrayInputStream(content), content.length);

        Assertions.assertEquals(agreementEntity.getId(), documentEntity.getAgreementId());
        Assertions.assertEquals(DocumentTypeEnum.AGREEMENT, documentEntity.getDocumentType());
        Assertions.assertTrue(documentEntity.getDocumentUrl().length() > 0);

        BlobClient client = documentContainerClient.getBlobClient(agreementEntity.getId() + "/" + DocumentTypeEnum.AGREEMENT.getCode().toLowerCase() + ".pdf");

        Assertions.assertArrayEquals(content, IOUtils.toByteArray(client.openInputStream()));
    }


}
