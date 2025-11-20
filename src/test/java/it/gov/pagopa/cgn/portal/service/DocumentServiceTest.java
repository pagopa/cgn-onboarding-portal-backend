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
import it.gov.pagopa.cgn.portal.exception.InternalErrorException;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.repository.AddressRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import it.gov.pagopa.cgn.portal.repository.DocumentRepository;
import it.gov.pagopa.cgn.portal.repository.ProfileRepository;
import it.gov.pagopa.cgn.portal.support.TestReferentRepository;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import it.gov.pagopa.cgnonboardingportal.model.ErrorCodeEnum;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest
@ActiveProfiles("dev")
class DocumentServiceTest
        extends IntegrationAbstractTest {

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
    void init()
            throws IOException {

        documentContainerClient = new BlobContainerClientBuilder().connectionString(getAzureConnectionString())
                                                                  .containerName(configProperties.getDocumentsContainerName())
                                                                  .buildClient();

        if (!documentContainerClient.exists()) {
            documentContainerClient.create();
        }
        byte[] csv = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test-codes.csv"));
        multipartFile = new MockMultipartFile("bucketload", "test-codes.csv", "text/csv", csv);

        agreementEntity = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                      EntityType.PRIVATE,
                                                                      TestUtils.FAKE_ORGANIZATION_NAME);
        agreementEntityPA = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID_2,
                                                                        EntityType.PUBLIC_ADMINISTRATION,
                                                                        TestUtils.FAKE_ORGANIZATION_NAME);

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
    void Upload_UploadDocumentWithValidData_Ok()
            throws IOException {
        byte[] content = "pdf-document".getBytes(StandardCharsets.UTF_8);

        DocumentEntity documentEntity = documentService.storeDocument(agreementEntity.getId(),
                                                                      DocumentTypeEnum.AGREEMENT,
                                                                      new ByteArrayInputStream(content),
                                                                      content.length);

        Assertions.assertEquals(agreementEntity.getId(), documentEntity.getAgreement().getId());
        Assertions.assertEquals(DocumentTypeEnum.AGREEMENT, documentEntity.getDocumentType());
        Assertions.assertFalse(documentEntity.getDocumentUrl().isEmpty());

        BlobClient client = documentContainerClient.getBlobClient(
                agreementEntity.getId() + "/" + DocumentTypeEnum.AGREEMENT.getCode().toLowerCase() + ".pdf");

        Assertions.assertArrayEquals(content, IOUtils.toByteArray(client.openInputStream()));
    }

    @Test
    void Upload_UploadBucketWithValidData_Ok()
            throws IOException {
        setProfileDiscountType(DiscountCodeTypeEnum.BUCKET);

        byte[] content = multipartFile.getBytes();
        String bucketUID = documentService.storeBucket(agreementEntity.getId(),
                                                       multipartFile.getInputStream(),
                                                       multipartFile.getSize());

        Assertions.assertNotNull(bucketUID);

        BlobClient client = documentContainerClient.getBlobClient(bucketUID + ".csv");

        Assertions.assertArrayEquals(content, IOUtils.toByteArray(client.openInputStream()));
    }

    @Test
    void Upload_UploadBucketWithValidData_OkPA()
            throws IOException {
        setProfileDiscountTypePA(DiscountCodeTypeEnum.BUCKET);

        byte[] content = multipartFile.getBytes();
        String bucketUID = documentService.storeBucket(agreementEntityPA.getId(),
                                                       multipartFile.getInputStream(),
                                                       multipartFile.getSize());

        Assertions.assertNotNull(bucketUID);

        BlobClient client = documentContainerClient.getBlobClient(bucketUID + ".csv");

        Assertions.assertArrayEquals(content, IOUtils.toByteArray(client.openInputStream()));
    }

    @Test
    void Upload_UploadBucketWithInvalidData_Ko() {
        setProfileDiscountType(DiscountCodeTypeEnum.BUCKET);
        byte[] content = "".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(content);
        String agreementId = agreementEntity.getId();
        Exception exception = Assertions.assertThrows(InternalErrorException.class,
                                                      () -> documentService.storeBucket(agreementId,
                                                                                        in,
                                                                                        content.length));

        Assertions.assertEquals(ErrorCodeEnum.CSV_DATA_NOT_VALID.getValue(), exception.getMessage());
    }

    @Test
    void Upload_UploadBucketWithInvalidData_KoPA() {
        setProfileDiscountTypePA(DiscountCodeTypeEnum.BUCKET);

        byte[] content = "".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(content);
        String agreementId = agreementEntityPA.getId();
        Exception exception = Assertions.assertThrows(InternalErrorException.class,
                                                      () -> documentService.storeBucket(agreementId,
                                                                                        in,
                                                                                        content.length));

        Assertions.assertEquals(ErrorCodeEnum.CSV_DATA_NOT_VALID.getValue(), exception.getMessage());
    }

    @Test
    void Upload_UploadBucketWithMaxLengthNotValid_Ko() {
        setProfileDiscountType(DiscountCodeTypeEnum.BUCKET);

        byte[] content = "ABCD123ABB12456CCC12w".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(content);
        String agreementId = agreementEntity.getId();
        Exception exception = Assertions.assertThrows(InvalidRequestException.class,
                                                      () -> documentService.storeBucket(agreementId,
                                                                                        in,
                                                                                        content.length));

        Assertions.assertEquals(ErrorCodeEnum.MAX_ALLOWED_BUCKET_CODE_LENGTH_NOT_RESPECTED.getValue(),
                                exception.getMessage());
    }

    @Test
    void Upload_UploadBucketWithMaxLengthNotValid_KoPA() {
        setProfileDiscountTypePA(DiscountCodeTypeEnum.BUCKET);

        byte[] content = "ABCD123ABB12456CCC12w".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(content);
        String agreementId = agreementEntityPA.getId();
        Exception exception = Assertions.assertThrows(InvalidRequestException.class,
                                                      () -> documentService.storeBucket(agreementId,
                                                                                        in,
                                                                                        content.length));

        Assertions.assertEquals(ErrorCodeEnum.MAX_ALLOWED_BUCKET_CODE_LENGTH_NOT_RESPECTED.getValue(),
                                exception.getMessage());
    }

    @Test
    void Upload_UploadBucketWithCodeTypeNoBucket_Ko() {
        setProfileDiscountType(DiscountCodeTypeEnum.LANDINGPAGE);

        byte[] content = "".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(content);
        String agreementId = agreementEntity.getId();
        Exception exception = Assertions.assertThrows(InvalidRequestException.class,
                                                      () -> documentService.storeBucket(agreementId,
                                                                                        in,
                                                                                        content.length));

        Assertions.assertEquals(ErrorCodeEnum.CANNOT_LOAD_BUCKET_CODE_FOR_DISCOUNT_NO_BUCKET.getValue(),
                                exception.getMessage());
    }

    @Test
    void Upload_UploadBucketWithCodeTypeNoBucket_KoPA() {
        setProfileDiscountTypePA(DiscountCodeTypeEnum.LANDINGPAGE);

        byte[] content = "".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(content);
        String agreementId = agreementEntityPA.getId();
        Exception exception = Assertions.assertThrows(InvalidRequestException.class,
                                                      () -> documentService.storeBucket(agreementId,
                                                                                        in,
                                                                                        content.length));

        Assertions.assertEquals(ErrorCodeEnum.CANNOT_LOAD_BUCKET_CODE_FOR_DISCOUNT_NO_BUCKET.getValue(),
                                exception.getMessage());
    }

    @Test
    void Upload_UploadBucketWithNotMinimumBucketCodes_Ko() {
        setProfileDiscountType(DiscountCodeTypeEnum.BUCKET);
        ReflectionTestUtils.setField(configProperties, "bucketMinCsvRows", 10000);
        //bucket csv without header
        byte[] content = "item1\n".repeat(9999).getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(content);
        String agreementId = agreementEntity.getId();
        Exception exception = Assertions.assertThrows(InvalidRequestException.class,
                                                      () -> documentService.storeBucket(agreementId,
                                                                                        in,
                                                                                        content.length));

        Assertions.assertEquals(ErrorCodeEnum.CANNOT_LOAD_BUCKET_FOR_NOT_RESPECTED_MINIMUM_BOUND.getValue(),
                                exception.getMessage());
    }

    @Test
    void Upload_UploadBucketWithNotAlphanumericBucketCodes_Ko() {
        setProfileDiscountType(DiscountCodeTypeEnum.BUCKET);
        ReflectionTestUtils.setField(configProperties, "bucketMinCsvRows", 10000);
        //bucket csv without header
        byte[] contentOnlyChars = "AaB\n".repeat(10000).getBytes(StandardCharsets.UTF_8);
        byte[] contentOnlyNum = "123\n".repeat(10000).getBytes(StandardCharsets.UTF_8);
        byte[] contentNotAllowedSpChar = "1AaB€\n".repeat(10000).getBytes(StandardCharsets.UTF_8);

        String agreementId = agreementEntity.getId();
        ByteArrayInputStream bisOc = new ByteArrayInputStream(contentOnlyChars);
        ByteArrayInputStream bisOn = new ByteArrayInputStream(contentOnlyNum);
        ByteArrayInputStream bisNaSc = new ByteArrayInputStream(contentNotAllowedSpChar);

        Exception exception = Assertions.assertThrows(InvalidRequestException.class,
                                                      () -> documentService.storeBucket(agreementId,
                                                                                        bisOc,
                                                                                        contentOnlyChars.length));

        Assertions.assertEquals(ErrorCodeEnum.BUCKET_CODES_MUST_BE_ALPHANUM_WITH_AT_LEAST_ONE_DIGIT_AND_ONE_CHAR.getValue(),
                                exception.getMessage());

        exception = Assertions.assertThrows(InvalidRequestException.class,
                                            () -> documentService.storeBucket(agreementId,
                                                                              bisOn,
                                                                              contentOnlyNum.length));

        Assertions.assertEquals(ErrorCodeEnum.BUCKET_CODES_MUST_BE_ALPHANUM_WITH_AT_LEAST_ONE_DIGIT_AND_ONE_CHAR.getValue(),
                                exception.getMessage());

        exception = Assertions.assertThrows(InvalidRequestException.class,
                                            () -> documentService.storeBucket(agreementId,
                                                                              bisNaSc,
                                                                              contentNotAllowedSpChar.length));

        Assertions.assertEquals(ErrorCodeEnum.NOT_ALLOWED_SPECIAL_CHARS.getValue(),
                                exception.getMessage());

    }

    @Test
    void Upload_UploadBucketWithAlphanumericBucketCodes_Ok() {
        setProfileDiscountType(DiscountCodeTypeEnum.BUCKET);
        ReflectionTestUtils.setField(configProperties, "bucketMinCsvRows", 10000);
        //bucket csv without header
        byte[] contentCharsAndNum = "A10b\n".repeat(10000).getBytes(StandardCharsets.UTF_8);

        Assertions.assertDoesNotThrow(() -> documentService.storeBucket(agreementEntity.getId(),
                                                                        new ByteArrayInputStream(contentCharsAndNum),
                                                                        contentCharsAndNum.length));
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
    void Get_GenerateAgreementDocument_Ok()
            throws Exception {
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
    void Get_GenerateAgreementDocument_OkPA()
            throws Exception {
        PDDocument document = PDDocument.load(documentService.renderDocument(agreementEntityPA.getId(),
                                                                             DocumentTypeEnum.AGREEMENT).toByteArray());

        PDFTextStripper stripper = new PDFTextStripper();
        String actual = stripper.getText(document);
        Assertions.assertTrue(actual.contains("di seguito “l’Operatore Pubblico”"));
        Assertions.assertTrue(actual.contains("CONVENZIONE"));
        Assertions.assertTrue(actual.contains("PER L’ADESIONE AL PROGETTO CARTA GIOVANI NAZIONALE"));

        Assertions.assertTrue(actual.contains("FULL_NAME"));
        Assertions.assertFalse(actual.contains("${current_date}"));
    }

    @Test
    void Get_GenerateAdhesionRequestDocument_Ok()
            throws IOException {

        PDDocument document = PDDocument.load(documentService.renderDocument(agreementEntity.getId(),
                                                                             DocumentTypeEnum.ADHESION_REQUEST)
                                                             .toByteArray());

        PDFTextStripper stripper = new PDFTextStripper();
        String actual = stripper.getText(document);
        GenerateAdhesionRequestAssertions(actual);

    }

    @Test
    void Get_GenerateAdhesionRequestDocument_koPA() {
        DocumentService dsMock = mock(DocumentService.class);

        when(dsMock.renderDocument(anyString(),
                                   eq(DocumentTypeEnum.ADHESION_REQUEST))).thenThrow(new InvalidRequestException(
                ErrorCodeEnum.ADHESION_DOCUMENT_NOT_REQUIRED_FOR_PA.getValue()));

        String agreementId = agreementEntityPA.getId();
        InvalidRequestException exception = Assertions.assertThrows(InvalidRequestException.class, () -> {
            documentService.renderDocument(agreementId, DocumentTypeEnum.ADHESION_REQUEST);
        });

        Assertions.assertEquals(exception.getMessage(), ErrorCodeEnum.ADHESION_DOCUMENT_NOT_REQUIRED_FOR_PA.getValue());
    }

    @Test
    void Get_GenerateAdhesionRequestWithEmptyDiscountValuesDocument_Offline_Ok()
            throws IOException {
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
    void Get_GenerateAdhesionRequestWithEmptyDiscountValuesDocument_StaticCode_Ok()
            throws IOException {
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
    void Get_GenerateAdhesionRequestWithEmptyDiscountValuesDocument_API_Ok()
            throws IOException {
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
    void Get_GenerateAdhesionRequestWithEmptyDiscountValuesDocument_LandingPage_Ok()
            throws IOException {
        setProfileDiscountType(DiscountCodeTypeEnum.LANDINGPAGE);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithLandingPage(agreementEntity,
                                                                                            "https://anurl.com",
                                                                                            "https://eycaanurl.com",
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
    void Get_GenerateAdhesionRequestWithEmptyDiscountValuesDocument_Bucket_Ok()
            throws IOException {
        setProfileDiscountType(DiscountCodeTypeEnum.BUCKET);

        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntityWithBucketCodes(agreementEntity);
        azureStorage.uploadCsv(multipartFile.getBytes(),
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

    @Test
    void testStoreBucketValidationsWithInvisibleCharacters()
            throws Exception {
        setProfileDiscountTypePA(DiscountCodeTypeEnum.BUCKET);

        ProfileRepository profileRepository = Mockito.mock(ProfileRepository.class);
        DocumentRepository documentRepository = Mockito.mock(DocumentRepository.class);
        DiscountRepository discountRepository = Mockito.mock(DiscountRepository.class);
        AgreementServiceLight agreementServiceLight = Mockito.mock(AgreementServiceLight.class);
        TemplateEngine templateEngine = Mockito.mock(TemplateEngine.class);

        AzureStorage azureStorage = Mockito.mock(AzureStorage.class);
        ConfigProperties configProperties = Mockito.mock(ConfigProperties.class);

        ProfileEntity mockedProfile = Mockito.mock(ProfileEntity.class);
        Mockito.when(mockedProfile.getDiscountCodeType()).thenReturn(DiscountCodeTypeEnum.BUCKET);
        Mockito.when(profileRepository.findByAgreementId(Mockito.anyString()))
               .thenReturn(Optional.of(mockedProfile));

        Mockito.when(configProperties.getBucketMinCsvRows()).thenReturn(1);
        Mockito.doNothing().when(azureStorage).uploadCsv(Mockito.any(), Mockito.anyString(), Mockito.anyLong());

        DocumentService documentService = new DocumentService(documentRepository,
                                                              profileRepository,
                                                              discountRepository,
                                                              agreementServiceLight,
                                                              azureStorage,
                                                              templateEngine,
                                                              configProperties);

        InputStream resourceStream = this.getClass().getClassLoader()
                                         .getResourceAsStream("test-bucket-codes.csv");
        Assertions.assertNotNull(resourceStream, "Test CSV file should exist in resources");
        List<String> rows = new java.io.BufferedReader(new java.io.InputStreamReader(resourceStream, StandardCharsets.UTF_8))
                .lines().toList();

        // Collect produced messages
        Set<String> producedMessages = new HashSet<>();

        int rowIndex = 0;
        for (String originalCode : rows) {
            rowIndex++;
            try (InputStream singleIs = new ByteArrayInputStream(originalCode.getBytes(StandardCharsets.UTF_8))) {
                documentService.storeBucket(agreementEntityPA.getId(), singleIs, originalCode.length());
            } catch (InvalidRequestException ex) {
                producedMessages.add(ex.getMessage());
                System.out.println("Riga " + rowIndex + ": codice='" + originalCode + "' messaggio='" + ex.getMessage() + "'");
            }
        }

        // Define the expected set of messages
        Set<String> expectedMessages = new HashSet<>();
        expectedMessages.add(ErrorCodeEnum.CSV_DATA_NOT_VALID.getValue());
        expectedMessages.add(ErrorCodeEnum.BUCKET_CODES_MUST_BE_ALPHANUM_WITH_AT_LEAST_ONE_DIGIT_AND_ONE_CHAR.getValue());
        expectedMessages.add(ErrorCodeEnum.NOT_ALLOWED_SPECIAL_CHARS.getValue());
        expectedMessages.add(ErrorCodeEnum.ONE_OR_MORE_CODES_ARE_NOT_VALID.getValue());

        // Assert that all expected messages are produced and no additional ones exist
        Assertions.assertTrue(producedMessages.containsAll(expectedMessages),
                              "Not all expected validation messages were produced");
        Assertions.assertEquals(expectedMessages.size(), producedMessages.size(),
                                "Unexpected validation messages were produced");
    }

    private void GenerateAdhesionRequestAssertions(String actual) {
        Assertions.assertTrue(actual.contains("Allegato 1"));
        Assertions.assertTrue(actual.contains("DOMANDA DI"));
        Assertions.assertTrue(actual.contains("ADESIONE AL PROGETTO CARTA GIOVANI NAZIONALE"));
        Assertions.assertTrue(actual.contains("FULL_NAME"));
        Assertions.assertTrue(actual.contains("address@pagopa.it"));
        Assertions.assertTrue(actual.contains(ProductCategoryEnum.SPORTS.getDescription().toLowerCase()));
        Assertions.assertTrue(actual.contains("https://www.pagopa.gov.it/"));
        Assertions.assertTrue(actual.contains("CEO"));
        Assertions.assertTrue(actual.contains("Tel: +390123456789"));
        Assertions.assertTrue(actual.contains("referent.registry@pagopa.it"));
    }
}
