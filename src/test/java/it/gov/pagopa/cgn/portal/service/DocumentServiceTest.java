package it.gov.pagopa.cgn.portal.service;


import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;


@SpringBootTest
@ActiveProfiles("dev")
class DocumentServiceTest extends IntegrationAbstractTest {

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private DiscountService discountService;

    private BlobContainerClient documentContainerClient;

    private AgreementEntity agreementEntity;

    @BeforeEach
    void init() {

        documentContainerClient = new BlobContainerClientBuilder()
                .connectionString(getAzureConnectionString())
                .containerName(configProperties.getDocumentsContainerName())
                .buildClient();

        if (!documentContainerClient.exists()) {
            documentContainerClient.create();
        }

        agreementEntity = agreementService.createAgreementIfNotExists();
        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
    }


    @Test
    void Upload_UploadDocumentWithValidData_Ok() throws IOException {
        byte[] content = "pdf-document".getBytes(StandardCharsets.UTF_8);

        DocumentEntity documentEntity = documentService.storeDocument(agreementEntity.getId(), DocumentTypeEnum.AGREEMENT, new ByteArrayInputStream(content), content.length);

        Assertions.assertEquals(agreementEntity.getId(), documentEntity.getAgreement().getId());
        Assertions.assertEquals(DocumentTypeEnum.AGREEMENT, documentEntity.getDocumentType());
        Assertions.assertTrue(documentEntity.getDocumentUrl().length() > 0);

        BlobClient client = documentContainerClient.getBlobClient(agreementEntity.getId() + "/" + DocumentTypeEnum.AGREEMENT.getCode().toLowerCase() + ".pdf");

        Assertions.assertArrayEquals(content, IOUtils.toByteArray(client.openInputStream()));
    }

    @Test
    void Delete_DeleteDocument_Ok() throws IOException {
        byte[] content = "pdf-document".getBytes(StandardCharsets.UTF_8);

        documentService.storeDocument(agreementEntity.getId(), DocumentTypeEnum.AGREEMENT, new ByteArrayInputStream(content), content.length);
        documentService.deleteDocument(agreementEntity.getId(), DocumentTypeEnum.AGREEMENT);
        List<DocumentEntity> documents = documentService.getDocuments(agreementEntity.getId());
        Assertions.assertTrue(CollectionUtils.isEmpty(documents));

    }


    @Test
    void Get_GenerateAgreementDocument_Ok() throws Exception {
        PDDocument document = PDDocument.load(documentService.renderDocument(agreementEntity.getId(), DocumentTypeEnum.AGREEMENT).toByteArray());

        PDFTextStripper stripper = new PDFTextStripper();
        String actual = stripper.getText(document);

        Assertions.assertTrue(actual.contains("CONVENZIONE"));
        Assertions.assertTrue(actual.contains("PER L’ADESIONE AL PROGETTO CARTA GIOVANI NAZIONALE"));

        Assertions.assertTrue(actual.contains("FULL_NAME"));
        Assertions.assertTrue(actual.contains("address@pagopa.it"));
    }


    @Test
    void Get_GenerateManifestationOfInterestDocument_Ok() throws IOException {

        PDDocument document = PDDocument.load(documentService.renderDocument(agreementEntity.getId(), DocumentTypeEnum.MANIFESTATION_OF_INTEREST).toByteArray());

        PDFTextStripper stripper = new PDFTextStripper();
        String actual = stripper.getText(document);

        Assertions.assertTrue(actual.contains("Allegato 1"));
        Assertions.assertTrue(actual.contains("MANIFESTAZIONE DI INTERESSE"));
        Assertions.assertTrue(actual.contains("PER L’ADESIONE AL PROGETTO CARTA GIOVANI NAZIONALE"));
        Assertions.assertTrue(actual.contains("FULL_NAME"));
        Assertions.assertTrue(actual.contains("address@pagopa.it"));
        Assertions.assertTrue(actual.contains("A Description"));
        Assertions.assertTrue(actual.contains("15%"));
        Assertions.assertTrue(actual.contains(ProductCategoryEnum.TRAVELS.name()));
        Assertions.assertTrue(actual.contains("SPORT"));
        Assertions.assertTrue(actual.contains("https://www.pagopa.gov.it/"));
        Assertions.assertTrue(actual.contains("CEO"));
        Assertions.assertTrue(actual.contains("Tel +390123456789"));
        Assertions.assertTrue(actual.contains("referent.registry@pagopa.it"));

    }
}
