package it.gov.pagopa.cgn.portal.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.repository.AddressRepository;
import it.gov.pagopa.cgn.portal.support.TestReferentRepository;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;


@SpringBootTest
@ActiveProfiles("dev")
class DocumentServiceTest extends IntegrationAbstractTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private TestReferentRepository testReferentRepository;

    @Autowired
    private AzureStorage azureStorage;

    private BlobContainerClient documentContainerClient;

    private AgreementEntity agreementEntity;
    private AgreementEntity agreementEntityPA;

    private MockMultipartFile multipartFile;

    @BeforeEach
    void init() throws IOException {

        documentContainerClient = new BlobContainerClientBuilder().connectionString(getAzureConnectionString())
                                                                  .containerName(configProperties.getDocumentsContainerName())
                                                                  .buildClient();

        if (!documentContainerClient.exists()) {
            documentContainerClient.create();
        }
        byte[] csv = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-codes.csv"));
        multipartFile = new MockMultipartFile("bucketload", "test-codes.csv", "text/csv", csv);

        agreementEntity = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID, EntityType.PRIVATE);
        agreementEntityPA = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID_2, EntityType.PUBLICADMINISTRATION);

        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());

        ProfileEntity profileEntityPA = TestUtils.createSampleProfileEntity(agreementEntityPA);
        profileService.createProfile(profileEntityPA, agreementEntityPA.getId());

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);

        DiscountEntity discountEntityPA = TestUtils.createSampleDiscountEntity(agreementEntityPA);
        discountService.createDiscount(agreementEntityPA.getId(), discountEntityPA);

        ReflectionTestUtils.setField(configProperties, "bucketMinCsvRows", 0);
    }

    void setProfileSalesChannel(SalesChannelEnum salesChannel) {
        ProfileEntity profileEntity = profileService.getProfile(agreementEntity.getId()).orElseThrow();
        profileEntity.setSalesChannel(salesChannel);
        // to avoid LazyInitializationException
        profileEntity.setReferent(testReferentRepository.findByProfileId(profileEntity.getId()));
        profileEntity.setAddressList(addressRepository.findByProfileId(profileEntity.getId()));
        profileEntity.setSecondaryReferentList(secondaryReferentRepository.findByProfileId(profileEntity.getId()));
        profileService.updateProfile(agreementEntity.getId(), profileEntity);
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
    }

    void setProfileSalesChannelPA(SalesChannelEnum salesChannel) {
        ProfileEntity profileEntityPA = profileService.getProfile(agreementEntityPA.getId()).orElseThrow();
        profileEntityPA.setSalesChannel(salesChannel);
        // to avoid LazyInitializationException
        profileEntityPA.setReferent(testReferentRepository.findByProfileId(profileEntityPA.getId()));
        profileEntityPA.setAddressList(addressRepository.findByProfileId(profileEntityPA.getId()));
        profileEntityPA.setSecondaryReferentList(secondaryReferentRepository.findByProfileId(profileEntityPA.getId()));
        profileService.updateProfile(agreementEntityPA.getId(), profileEntityPA);
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntityPA));
    }

    private void setProfileDiscountType(DiscountCodeTypeEnum discountType) {
        ProfileEntity profileEntity = profileService.getProfile(agreementEntity.getId()).orElseThrow();
        profileEntity.setDiscountCodeType(discountType);
        profileEntity.setReferent(testReferentRepository.findByProfileId(profileEntity.getId()));
        profileEntity.setAddressList(addressRepository.findByProfileId(profileEntity.getId()));
        profileEntity.setSecondaryReferentList(secondaryReferentRepository.findByProfileId(profileEntity.getId()));
        profileService.updateProfile(agreementEntity.getId(), profileEntity);
    }

    private void setProfileDiscountTypePA(DiscountCodeTypeEnum discountType) {
        ProfileEntity profileEntityPA = profileService.getProfile(agreementEntityPA.getId()).orElseThrow();
        profileEntityPA.setDiscountCodeType(discountType);
        profileEntityPA.setReferent(testReferentRepository.findByProfileId(profileEntityPA.getId()));
        profileEntityPA.setAddressList(addressRepository.findByProfileId(profileEntityPA.getId()));
        profileEntityPA.setSecondaryReferentList(secondaryReferentRepository.findByProfileId(profileEntityPA.getId()));
        profileService.updateProfile(agreementEntityPA.getId(), profileEntityPA);
    }

    @Test
    void Upload_UploadDocumentWithValidData_Ok() throws IOException {
        byte[] content = "pdf-document".getBytes(StandardCharsets.UTF_8);

        DocumentEntity documentEntity = documentService.storeDocument(agreementEntity.getId(),
                                                                      DocumentTypeEnum.AGREEMENT,
                                                                      new ByteArrayInputStream(content),
                                                                      content.length);

        Assertions.assertEquals(agreementEntity.getId(), documentEntity.getAgreement().getId());
        Assertions.assertEquals(DocumentTypeEnum.AGREEMENT, documentEntity.getDocumentType());
        Assertions.assertTrue(documentEntity.getDocumentUrl().length() > 0);

        BlobClient client = documentContainerClient.getBlobClient(agreementEntity.getId() +
                                                                  "/" +
                                                                  DocumentTypeEnum.AGREEMENT.getCode().toLowerCase() +
                                                                  ".pdf");

        Assertions.assertArrayEquals(content, IOUtils.toByteArray(client.openInputStream()));
    }

    @Test
    void Upload_UploadBucketWithValidData_Ok() throws IOException {
        setProfileDiscountType(DiscountCodeTypeEnum.BUCKET);

        byte[] content = multipartFile.getInputStream().readAllBytes();
        String bucketUID = documentService.storeBucket(agreementEntity.getId(),
                                                       multipartFile.getInputStream(),
                                                       multipartFile.getSize());

        Assertions.assertNotNull(bucketUID);

        BlobClient client = documentContainerClient.getBlobClient(bucketUID + ".csv");

        Assertions.assertArrayEquals(content, IOUtils.toByteArray(client.openInputStream()));
    }

    void Upload_UploadBucketWithValidData_OkPA() throws IOException {
        setProfileDiscountTypePA(DiscountCodeTypeEnum.BUCKET);

        byte[] content = multipartFile.getInputStream().readAllBytes();
        String bucketUID = documentService.storeBucket(agreementEntity.getId(),
                multipartFile.getInputStream(),
                multipartFile.getSize());

        Assertions.assertNotNull(bucketUID);

        BlobClient client = documentContainerClient.getBlobClient(bucketUID + ".csv");

        Assertions.assertArrayEquals(content, IOUtils.toByteArray(client.openInputStream()));
    }

    @Test
    void Upload_UploadBucketWithInvalidData_Ko() throws IOException {
        setProfileDiscountType(DiscountCodeTypeEnum.BUCKET);

        byte[] content = "".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(content);
        String agreementId = agreementEntity.getId();
        Assertions.assertThrows(InvalidRequestException.class,
                                () -> documentService.storeBucket(agreementId, in, content.length));

    }

    void Upload_UploadBucketWithInvalidData_KoPA() throws IOException {
        setProfileDiscountTypePA(DiscountCodeTypeEnum.BUCKET);

        byte[] content = "".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(content);
        String agreementId = agreementEntity.getId();
        Assertions.assertThrows(InvalidRequestException.class,
                () -> documentService.storeBucket(agreementId, in, content.length));

    }

    @Test
    void Upload_UploadBucketWithInvalidCodesData_Ko() throws IOException {
        setProfileDiscountType(DiscountCodeTypeEnum.BUCKET);

        byte[] content = "A".repeat(50).getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(content);
        String agreementId = agreementEntity.getId();
        Assertions.assertThrows(InvalidRequestException.class,
                                () -> documentService.storeBucket(agreementId, in, content.length));

    }

    void Upload_UploadBucketWithInvalidCodesData_KoPA() throws IOException {
        setProfileDiscountTypePA(DiscountCodeTypeEnum.BUCKET);

        byte[] content = "A".repeat(50).getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(content);
        String agreementId = agreementEntity.getId();
        Assertions.assertThrows(InvalidRequestException.class,
                () -> documentService.storeBucket(agreementId, in, content.length));

    }

    @Test
    void Delete_DeleteDocument_Ok() {
        byte[] content = "pdf-document".getBytes(StandardCharsets.UTF_8);

        documentService.storeDocument(agreementEntity.getId(),
                                      DocumentTypeEnum.AGREEMENT,
                                      new ByteArrayInputStream(content),
                                      content.length);
        long deleteDocument = documentService.deleteDocument(agreementEntity.getId(), DocumentTypeEnum.AGREEMENT);
        Assertions.assertEquals(1, deleteDocument);
        List<DocumentEntity> documents = documentService.getPrioritizedDocuments(agreementEntity.getId());
        Assertions.assertTrue(CollectionUtils.isEmpty(documents));

    }

    @Test
    void Delete_DeleteDocumentNotFound_Return0DocumentDeleted() {
        Assertions.assertEquals(0, documentService.deleteDocument(agreementEntity.getId(), DocumentTypeEnum.AGREEMENT));

    }

    @Test
    void Get_GenerateAgreementDocument_Ok() throws Exception {
        PDDocument document = PDDocument.load(documentService.renderDocument(agreementEntity.getId(),
                                                                             DocumentTypeEnum.AGREEMENT).toByteArray());

        PDFTextStripper stripper = new PDFTextStripper();
        String actual = stripper.getText(document);
        Assertions.assertTrue(actual.contains("di seguito “l’Operatore”"));
        Assertions.assertTrue(actual.contains("CONVENZIONE"));
        Assertions.assertTrue(actual.contains("PER L’ADESIONE AL PROGETTO CARTA GIOVANI NAZIONALE"));

        Assertions.assertTrue(actual.contains("FULL_NAME"));
        Assertions.assertTrue(actual.contains("address@pagopa.it"));
    }

    @Test
    void Get_GenerateAgreementDocument_OkPA() throws Exception {
        PDDocument document = PDDocument.load(documentService.renderDocument(agreementEntityPA.getId(),
                DocumentTypeEnum.AGREEMENT).toByteArray());

        PDFTextStripper stripper = new PDFTextStripper();
        String actual = stripper.getText(document);
        Assertions.assertTrue(actual.contains("di seguito “l’Operatore Pubblico”"));
        Assertions.assertTrue(actual.contains("CONVENZIONE"));
        Assertions.assertTrue(actual.contains("PER L’ADESIONE AL PROGETTO CARTA GIOVANI NAZIONALE"));

        Assertions.assertTrue(actual.contains("FULL_NAME"));
        Assertions.assertTrue(actual.contains("address@pagopa.it"));
    }

    @Test
    void Get_GenerateAdhesionRequestDocument_Ok() throws IOException {

        PDDocument document = PDDocument.load(documentService.renderDocument(agreementEntity.getId(),
                                                                             DocumentTypeEnum.ADHESION_REQUEST)
                                                             .toByteArray());

        PDFTextStripper stripper = new PDFTextStripper();
        String actual = stripper.getText(document);
        GenerateAdhesionRequestAssertions(actual);

    }

    @Test
    void Get_GenerateAdhesionRequestDocument_koPA() throws IOException {
        DocumentService dsMock = mock(DocumentService.class);

        when(dsMock.renderDocument(anyString(), eq(DocumentTypeEnum.ADHESION_REQUEST)))
                .thenThrow(new CGNException("The adhesion document is not required for PA"));

        CGNException thrown = Assertions.assertThrows(CGNException.class, () -> {
            documentService.renderDocument(agreementEntityPA.getId(),DocumentTypeEnum.ADHESION_REQUEST);
        });
    }

    @Test
    void Get_GenerateAdhesionRequestWithEmptyDiscountValuesDocument_Offline_Ok() throws IOException {
        setProfileSalesChannel(SalesChannelEnum.OFFLINE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setDiscountValue(null);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        PDDocument document = PDDocument.load(documentService.renderDocument(agreementEntity.getId(),
                                                                             DocumentTypeEnum.ADHESION_REQUEST)
                                                             .toByteArray());

        PDFTextStripper stripper = new PDFTextStripper();
        String actual = stripper.getText(document);
        GenerateAdhesionRequestAssertions(actual);

    }

    @Test
    void Get_GenerateAdhesionRequestWithEmptyDiscountValuesDocument_StaticCode_Ok() throws IOException {
        setProfileDiscountType(DiscountCodeTypeEnum.STATIC);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setDiscountValue(null);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        PDDocument document = PDDocument.load(documentService.renderDocument(agreementEntity.getId(),
                                                                             DocumentTypeEnum.ADHESION_REQUEST)
                                                             .toByteArray());

        PDFTextStripper stripper = new PDFTextStripper();
        String actual = stripper.getText(document);
        GenerateAdhesionRequestAssertions(actual);

    }

    @Test
    void Get_GenerateAdhesionRequestWithEmptyDiscountValuesDocument_API_Ok() throws IOException {
        setProfileDiscountType(DiscountCodeTypeEnum.API);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountEntity.setDiscountValue(null);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        PDDocument document = PDDocument.load(documentService.renderDocument(agreementEntity.getId(),
                                                                             DocumentTypeEnum.ADHESION_REQUEST)
                                                             .toByteArray());

        PDFTextStripper stripper = new PDFTextStripper();
        String actual = stripper.getText(document);
        GenerateAdhesionRequestAssertions(actual);

    }

    @Test
    void Get_GenerateAdhesionRequestWithEmptyDiscountValuesDocument_LandingPage_Ok() throws IOException {
        setProfileDiscountType(DiscountCodeTypeEnum.LANDINGPAGE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity,
                                                                                            "anurl.com",
                                                                                            "areferrer");
        discountEntity.setDiscountValue(null);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        PDDocument document = PDDocument.load(documentService.renderDocument(agreementEntity.getId(),
                                                                             DocumentTypeEnum.ADHESION_REQUEST)
                                                             .toByteArray());

        PDFTextStripper stripper = new PDFTextStripper();
        String actual = stripper.getText(document);
        GenerateAdhesionRequestAssertions(actual);

    }

    @Test
    void Get_GenerateAdhesionRequestWithEmptyDiscountValuesDocument_Bucket_Ok() throws IOException {
        setProfileDiscountType(DiscountCodeTypeEnum.BUCKET);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        azureStorage.uploadCsv(multipartFile.getInputStream(),
                               discountEntity.getLastBucketCodeLoadUid(),
                               multipartFile.getSize());

        discountEntity.setDiscountValue(null);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        PDDocument document = PDDocument.load(documentService.renderDocument(agreementEntity.getId(),
                                                                             DocumentTypeEnum.ADHESION_REQUEST)
                                                             .toByteArray());

        PDFTextStripper stripper = new PDFTextStripper();
        String actual = stripper.getText(document);
        GenerateAdhesionRequestAssertions(actual);

    }

    private void GenerateAdhesionRequestAssertions(String actual) {
        Assertions.assertTrue(actual.contains("Allegato 1"));
        Assertions.assertTrue(actual.contains("DOMANDA DI"));
        Assertions.assertTrue(actual.contains("ADESIONE AL PROGETTO CARTA GIOVANI NAZIONALE"));
        Assertions.assertTrue(actual.contains("FULL_NAME"));
        Assertions.assertTrue(actual.contains("address@pagopa.it"));
        Assertions.assertTrue(actual.contains(""));
        Assertions.assertTrue(actual.contains(ProductCategoryEnum.SPORTS.getDescription().toLowerCase()));
        Assertions.assertTrue(actual.contains("https://www.pagopa.gov.it/"));
        Assertions.assertTrue(actual.contains("CEO"));
        Assertions.assertTrue(actual.contains("Tel: +390123456789"));
        Assertions.assertTrue(actual.contains("referent.registry@pagopa.it"));
    }
}
