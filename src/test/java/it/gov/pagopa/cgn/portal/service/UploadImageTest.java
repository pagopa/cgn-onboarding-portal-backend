package it.gov.pagopa.cgn.portal.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@SpringBootTest
@ActiveProfiles({"dev"})
class UploadImageTest extends IntegrationAbstractTest {

    @Autowired
    private ConfigProperties configProperties;

    private MultipartFile multipartFile;
    private AgreementEntity agreementEntity;


    @BeforeEach
    void init() throws IOException {
        byte[] image = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-image.png"));
        agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);
        multipartFile = new MockMultipartFile("fileItem", "test-image.png", "image/png", image);

        BlobContainerClient documentContainerClient = new BlobContainerClientBuilder()
                .connectionString(getAzureConnectionString())
                .containerName(configProperties.getImagesContainerName())
                .buildClient();
        if (!documentContainerClient.exists()) {
            documentContainerClient.create();
        }
    }

    @Test
    void UploadImage_UploadImage_Ok() {
        String imageUrl = agreementService.uploadImage(agreementEntity.getId(), multipartFile);
        Assertions.assertNotNull(imageUrl);
        agreementEntity = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(imageUrl, agreementEntity.getImageUrl());
    }

    @Test
    void UploadImage_UploadImageMultipleTimes_Ok() {
        String imageUrl = agreementService.uploadImage(agreementEntity.getId(), multipartFile);
        Assertions.assertNotNull(imageUrl);
        agreementEntity = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(imageUrl, agreementEntity.getImageUrl());
        Assertions.assertDoesNotThrow(()-> agreementService.uploadImage(agreementEntity.getId(), multipartFile));
    }

    @Test
    void UploadImage_UploadImageWithWrongAgreementId_ThrowException() {
        Assertions.assertThrows(InvalidRequestException.class,
                () ->agreementService.uploadImage("invalidAgreementId", multipartFile));
        agreementEntity = agreementService.findById(agreementEntity.getId());
        Assertions.assertNull(agreementEntity.getImageUrl());
    }

    @Test
    void UploadImage_UploadImageOfRejectedAgreement_StateAgreementUpdateToDraft() {
        AgreementTestObject testObject = createPendingAgreement();
        AgreementEntity agreementEntity = testObject.getAgreementEntity();
        agreementEntity = backofficeAgreementService.rejectAgreement(agreementEntity.getId(), "reason");

        String imageUrl = agreementService.uploadImage(agreementEntity.getId(), multipartFile);
        Assertions.assertNotNull(imageUrl);
        agreementEntity = agreementService.findById(agreementEntity.getId());
        Assertions.assertEquals(imageUrl, agreementEntity.getImageUrl());
        Assertions.assertEquals(AgreementStateEnum.DRAFT, agreementEntity.getState());
        Assertions.assertNull(agreementEntity.getStartDate());
        Assertions.assertNull(agreementEntity.getEndDate());
        Assertions.assertNull(agreementEntity.getRejectReasonMessage());
        Assertions.assertNull(agreementEntity.getRequestApprovalTime());
        Assertions.assertNull(agreementEntity.getBackofficeAssignee());
        List<DocumentEntity> documents = documentRepository.findByAgreementId(agreementEntity.getId());
        Assertions.assertTrue(CollectionUtils.isEmpty(documents));

    }
}
