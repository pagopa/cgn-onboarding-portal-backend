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
class UploadImageTest
        extends IntegrationAbstractTest {

    @Autowired
    private ConfigProperties configProperties;

    private MultipartFile multipartFile;
    private AgreementEntity agreementEntity;


    @BeforeEach
    void init()
            throws IOException {
        byte[] image = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-image.png"));
        agreementEntity = this.agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                           EntityType.PRIVATE,
                                                                           TestUtils.FAKE_ORGANIZATION_NAME);
        multipartFile = new MockMultipartFile("fileItem", "test-image.png", "image/png", image);

        BlobContainerClient documentContainerClient = new BlobContainerClientBuilder().connectionString(
                getAzureConnectionString()).containerName(configProperties.getImagesContainerName()).buildClient();
        if (!documentContainerClient.exists()) {
            documentContainerClient.create();
        }
    }

    @Test
    void UploadImage_UploadImage_Ok() {
        String imageUrl = agreementService.uploadImage(agreementEntity.getId(), multipartFile);
        Assertions.assertNotNull(imageUrl);
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(imageUrl, agreementEntity.getImageUrl());
    }

    @Test
    void UploadImage_UploadImageMultipleTimes_Ok() {
        String imageUrl = agreementService.uploadImage(agreementEntity.getId(), multipartFile);
        Assertions.assertNotNull(imageUrl);
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertEquals(imageUrl, agreementEntity.getImageUrl());
        Assertions.assertDoesNotThrow(() -> agreementService.uploadImage(agreementEntity.getId(), multipartFile));
    }

    @Test
    void UploadImage_UploadImageWithWrongAgreementId_ThrowException() {
        Assertions.assertThrows(InvalidRequestException.class,
                                () -> agreementService.uploadImage("invalidAgreementId", multipartFile));
        agreementEntity = agreementService.findAgreementById(agreementEntity.getId());
        Assertions.assertNull(agreementEntity.getImageUrl());
    }

    @Test
    void UploadImage_UploadImageOfRejectedAgreement_StateAgreementUpdateToDraft() {
        AgreementTestObject testObject = createPendingAgreement();
        AgreementEntity agreement = testObject.getAgreementEntity();
        agreement = backofficeAgreementService.rejectAgreement(agreement.getId(), "reason");

        String imageUrl = agreementService.uploadImage(agreement.getId(), multipartFile);
        Assertions.assertNotNull(imageUrl);
        agreement = agreementService.findAgreementById(agreement.getId());
        Assertions.assertEquals(imageUrl, agreement.getImageUrl());
        Assertions.assertEquals(AgreementStateEnum.DRAFT, agreement.getState());
        Assertions.assertNull(agreement.getStartDate());
        Assertions.assertNull(agreement.getRejectReasonMessage());
        Assertions.assertNull(agreement.getRequestApprovalTime());
        Assertions.assertNull(agreement.getBackofficeAssignee());
        List<DocumentEntity> documents = documentRepository.findByAgreementId(agreement.getId());
        Assertions.assertTrue(CollectionUtils.isEmpty(documents));

    }
}
