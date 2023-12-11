package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.converter.backoffice.*;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.service.AgreementUserService;
import it.gov.pagopa.cgn.portal.service.AttributeAuthorityService;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
@ActiveProfiles("dev")
class BackofficeAttributeAuthorityFacadeTest extends IntegrationAbstractTest {

    private BackofficeAttributeAuthorityFacade backofficeAttributeAuthorityFacade;
    private AttributeAuthorityService attributeAuthorityService;
    private OrganizationWithReferentsConverter organizationWithReferentsConverter;
    private OrganizationWithReferentsAndStatusConverter organizationWithReferentsAndStatusConverter;
    private ReferentFiscalCodeConverter referentFiscalCodeConverter;
    private OrganizationsConverter organizationsConverter;
    private AgreementUserService agreementUserServiceSpy;
    private ProfileService profileServiceSpy;
    private AgreementEntity agreementEntity;
    private ProfileEntity profileEntity;

    @BeforeEach
    void init() {
        ProfileService profileService = new ProfileService(factory,
                                                           profileRepository,
                                                           agreementServiceLight,
                                                           documentService);
        profileServiceSpy = Mockito.spy(profileService);

        AgreementUserService agreementUserService = new AgreementUserService(agreementUserRepository);
        agreementUserServiceSpy = Mockito.spy(agreementUserService);

        attributeAuthorityService = Mockito.mock(AttributeAuthorityService.class);
        organizationWithReferentsConverter = new OrganizationWithReferentsConverter();
        organizationWithReferentsAndStatusConverter = new OrganizationWithReferentsAndStatusConverter();
        organizationsConverter = new OrganizationsConverter(organizationWithReferentsAndStatusConverter);
        referentFiscalCodeConverter = new ReferentFiscalCodeConverter();

        var organizationWithReferentsPostConverter = new OrganizationWithReferentsPostConverter();

        backofficeAttributeAuthorityFacade = new BackofficeAttributeAuthorityFacade(attributeAuthorityService,
                                                                                    agreementService,
                                                                                    agreementUserServiceSpy,
                                                                                    profileServiceSpy,
                                                                                    organizationsConverter,
                                                                                    organizationWithReferentsConverter,
                                                                                    organizationWithReferentsAndStatusConverter,
                                                                                    organizationWithReferentsPostConverter,
                                                                                    referentFiscalCodeConverter);

        String anOrganizationTaxCodeOrVat = "abcdeghilmnopqrs";
        agreementEntity = agreementService.createAgreementIfNotExists(anOrganizationTaxCodeOrVat, EntityType.PUBLICADMINISTRATION);
        profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
    }

    @Test
    void GetOrganizations_Ok() {
        OrganizationWithReferentsAndStatus organization0 = createOrganizationWithReferentsAndStatusMock("12345678",
                                                                                                        "12345678",
                                                                                                        "org0",
                                                                                                        "org0@pec.it");
        OrganizationWithReferentsAndStatus organization1 = createOrganizationWithReferentsAndStatusMock("12345679",
                                                                                                        "12345679",
                                                                                                        "org1",
                                                                                                        "org1@pec.it");

        List<OrganizationWithReferentsAndStatus> items = new LinkedList();
        items.add(organization0);
        items.add(organization1);

        Organizations organizations = new Organizations();
        organizations.setCount(2);
        organizations.setItems(items);

        Mockito.when(attributeAuthorityService.getOrganizations(Mockito.any(),
                                                                Mockito.any(),
                                                                Mockito.any(),
                                                                Mockito.any(),
                                                                Mockito.any()))
               .thenReturn(ResponseEntity.ok(organizationsConverter.toAttributeAuthorityModel(organizations)));
        ResponseEntity<Organizations> organizationsResponse = backofficeAttributeAuthorityFacade.getOrganizations(null,
                                                                                                                  0,
                                                                                                                  2,
                                                                                                                  null,
                                                                                                                  null);

        Assertions.assertEquals(HttpStatus.OK, organizationsResponse.getStatusCode());
        Assertions.assertNotNull(organizationsResponse.getBody());
        Assertions.assertEquals(2, organizationsResponse.getBody().getCount());
        Assertions.assertEquals(organization0, organizationsResponse.getBody().getItems().get(0));
        Assertions.assertEquals(organization1, organizationsResponse.getBody().getItems().get(1));
    }

    @Test
    void GetOrganization_ENABLED_Ok() {
        OrganizationWithReferentsAndStatus organization0 = createOrganizationWithReferentsAndStatusMock("12345678",
                                                                                                        "12345678",
                                                                                                        "org0",
                                                                                                        "org0@pec.it");

        Mockito.when(attributeAuthorityService.getOrganization(Mockito.any()))
               .thenReturn(ResponseEntity.ok(organizationWithReferentsAndStatusConverter.toAttributeAuthorityModel(
                       organization0)));
        ResponseEntity<OrganizationWithReferentsAndStatus> organizationResponse
                = backofficeAttributeAuthorityFacade.getOrganization("1234567890");

        Assertions.assertEquals(HttpStatus.OK, organizationResponse.getStatusCode());
        Assertions.assertNotNull(organizationResponse.getBody());
        Assertions.assertEquals(organization0, organizationResponse.getBody());
        Assertions.assertEquals(OrganizationStatus.ENABLED, organizationResponse.getBody().getStatus());
    }

    @Test
    void GetOrganization_DRAFT_Ok() {
        OrganizationWithReferentsAndStatus organization0
                = createOrganizationWithReferentsAndStatusMock(profileEntity.getTaxCodeOrVat(),
                                                               profileEntity.getTaxCodeOrVat(),
                                                               "org0",
                                                               "org0@pec.it");

        Mockito.when(attributeAuthorityService.getOrganization(Mockito.any()))
               .thenReturn(ResponseEntity.ok(organizationWithReferentsAndStatusConverter.toAttributeAuthorityModel(
                       organization0)));
        ResponseEntity<OrganizationWithReferentsAndStatus> organizationResponse
                = backofficeAttributeAuthorityFacade.getOrganization(profileEntity.getTaxCodeOrVat());

        Assertions.assertEquals(HttpStatus.OK, organizationResponse.getStatusCode());
        Assertions.assertNotNull(organizationResponse.getBody());
        Assertions.assertEquals(OrganizationStatus.DRAFT, organizationResponse.getBody().getStatus());
    }

    @Test
    void GetOrganization_PENDING_Ok() {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        saveSampleDocuments(agreementEntity);
        agreementService.requestApproval(agreementEntity.getId());

        OrganizationWithReferentsAndStatus organization0
                = createOrganizationWithReferentsAndStatusMock(profileEntity.getTaxCodeOrVat(),
                                                               profileEntity.getTaxCodeOrVat(),
                                                               "org0",
                                                               "org0@pec.it");

        Mockito.when(attributeAuthorityService.getOrganization(Mockito.any()))
               .thenReturn(ResponseEntity.ok(organizationWithReferentsAndStatusConverter.toAttributeAuthorityModel(
                       organization0)));
        ResponseEntity<OrganizationWithReferentsAndStatus> organizationResponse
                = backofficeAttributeAuthorityFacade.getOrganization(profileEntity.getTaxCodeOrVat());

        Assertions.assertEquals(HttpStatus.OK, organizationResponse.getStatusCode());
        Assertions.assertNotNull(organizationResponse.getBody());
        Assertions.assertEquals(OrganizationStatus.PENDING, organizationResponse.getBody().getStatus());
    }

    @Test
    void GetOrganization_ACTIVE_Ok() {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        saveSampleDocuments(agreementEntity);
        agreementService.requestApproval(agreementEntity.getId());

        setAdminAuth();
        backofficeAgreementService.assignAgreement(agreementEntity.getId());
        documentRepository.saveAll(saveBackofficeSampleDocuments(agreementEntity));
        backofficeAgreementService.approveAgreement(agreementEntity.getId());

        OrganizationWithReferentsAndStatus organization0
                = createOrganizationWithReferentsAndStatusMock(profileEntity.getTaxCodeOrVat(),
                                                               profileEntity.getTaxCodeOrVat(),
                                                               "org0",
                                                               "org0@pec.it");

        Mockito.when(attributeAuthorityService.getOrganization(Mockito.any()))
               .thenReturn(ResponseEntity.ok(organizationWithReferentsAndStatusConverter.toAttributeAuthorityModel(
                       organization0)));
        ResponseEntity<OrganizationWithReferentsAndStatus> organizationResponse
                = backofficeAttributeAuthorityFacade.getOrganization(profileEntity.getTaxCodeOrVat());

        Assertions.assertEquals(HttpStatus.OK, organizationResponse.getStatusCode());
        Assertions.assertNotNull(organizationResponse.getBody());
        Assertions.assertEquals(OrganizationStatus.ACTIVE, organizationResponse.getBody().getStatus());
    }

    @Test
    void GetOrganization_REJECTED_to_DRAFT_Ok() {
        DiscountEntity discountEntity = TestUtils.createSampleDiscountEntity(agreementEntity);
        discountService.createDiscount(agreementEntity.getId(), discountEntity);
        saveSampleDocuments(agreementEntity);
        agreementService.requestApproval(agreementEntity.getId());

        setAdminAuth();
        backofficeAgreementService.assignAgreement(agreementEntity.getId());
        backofficeAgreementService.rejectAgreement(agreementEntity.getId(), "test");

        OrganizationWithReferentsAndStatus organization0
                = createOrganizationWithReferentsAndStatusMock(profileEntity.getTaxCodeOrVat(),
                                                               profileEntity.getTaxCodeOrVat(),
                                                               "org0",
                                                               "org0@pec.it");

        Mockito.when(attributeAuthorityService.getOrganization(Mockito.any()))
               .thenReturn(ResponseEntity.ok(organizationWithReferentsAndStatusConverter.toAttributeAuthorityModel(
                       organization0)));
        ResponseEntity<OrganizationWithReferentsAndStatus> organizationResponse
                = backofficeAttributeAuthorityFacade.getOrganization(profileEntity.getTaxCodeOrVat());

        Assertions.assertEquals(HttpStatus.OK, organizationResponse.getStatusCode());
        Assertions.assertNotNull(organizationResponse.getBody());
        Assertions.assertEquals(OrganizationStatus.DRAFT, organizationResponse.getBody().getStatus());
    }

    @Test
    void GetOrganization_Ko() {
        Mockito.when(attributeAuthorityService.getOrganization(Mockito.any()))
               .thenReturn(ResponseEntity.notFound().build());
        ResponseEntity<OrganizationWithReferentsAndStatus> organizationResponse
                = backofficeAttributeAuthorityFacade.getOrganization("1234567890");

        Assertions.assertEquals(HttpStatus.NOT_FOUND, organizationResponse.getStatusCode());
        Assertions.assertNull(organizationResponse.getBody());
    }

    @Test
    void GetOrganization_No_Content() {
        Mockito.when(attributeAuthorityService.getOrganization(Mockito.any())).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<OrganizationWithReferentsAndStatus> organizationResponse
                = backofficeAttributeAuthorityFacade.getOrganization("1234567890");

        Assertions.assertEquals(HttpStatus.OK, organizationResponse.getStatusCode());
        Assertions.assertNull(organizationResponse.getBody());
    }

    @Test
    void UpsertOrganization_New_Ok() {
        String anOrganizationFiscalCode = "12345678";
        String anOrganizationName = "An organization";
        String anOrganizationPec = "an-organization@pec.it";

        UpsertResult upsertResult = upsertOrganizationWithReferents(anOrganizationFiscalCode,
                                                                    anOrganizationFiscalCode,
                                                                    anOrganizationName,
                                                                    anOrganizationPec,
                                                                    false);

        Assertions.assertEquals(HttpStatus.OK, upsertResult.response.getStatusCode());
        Assertions.assertNotNull(upsertResult.response.getBody());
        Assertions.assertEquals(anOrganizationFiscalCode, upsertResult.response.getBody().getOrganizationFiscalCode());
        Assertions.assertEquals(anOrganizationName, upsertResult.response.getBody().getOrganizationName());
        Assertions.assertEquals(anOrganizationPec, upsertResult.response.getBody().getPec());
        Assertions.assertEquals(upsertResult.organizationWithReferents.getInsertedAt(),
                                upsertResult.response.getBody().getInsertedAt());

        Mockito.verify(agreementUserServiceSpy, Mockito.times(0)).updateMerchantTaxCode(Mockito.any(), Mockito.any());
        Mockito.verify(profileServiceSpy, Mockito.times(0)).updateProfile(Mockito.any(), Mockito.any());
    }

    @Test
    @Transactional(Transactional.TxType.REQUIRED)
    void UpsertOrganization_Update_Ok() {
        String anOrganizationFiscalCode = profileEntity.getTaxCodeOrVat();
        String anOrganizationName = "New name";
        String anOrganizationPec = "new-pec@pec.it";

        UpsertResult upsertResult = upsertOrganizationWithReferents(anOrganizationFiscalCode,
                                                                    anOrganizationFiscalCode,
                                                                    anOrganizationName,
                                                                    anOrganizationPec,
                                                                    false);

        Assertions.assertEquals(HttpStatus.OK, upsertResult.response.getStatusCode());
        Assertions.assertNotNull(upsertResult.response.getBody());
        Assertions.assertEquals(anOrganizationFiscalCode, upsertResult.response.getBody().getOrganizationFiscalCode());
        Assertions.assertEquals(anOrganizationName, upsertResult.response.getBody().getOrganizationName());
        Assertions.assertEquals(anOrganizationPec, upsertResult.response.getBody().getPec());
        Assertions.assertEquals(upsertResult.organizationWithReferents.getInsertedAt(),
                                upsertResult.response.getBody().getInsertedAt());

        Mockito.verify(agreementUserServiceSpy, Mockito.times(0)).updateMerchantTaxCode(Mockito.any(), Mockito.any());
        Mockito.verify(profileServiceSpy, Mockito.times(1)).updateProfile(Mockito.any(), Mockito.any());
    }

    @Test
    @Transactional(Transactional.TxType.REQUIRED)
    void UpsertOrganization_UpdateMerchantTaxCode_Ok() {
        String anOrganizationFiscalCode = profileEntity.getTaxCodeOrVat();
        String aNewOrganizationFiscalCode = "12345678";
        String anOrganizationName = "New name";
        String anOrganizationPec = "new-pec@pec.it";

        UpsertResult upsertResult = upsertOrganizationWithReferents(anOrganizationFiscalCode,
                                                                    aNewOrganizationFiscalCode,
                                                                    anOrganizationName,
                                                                    anOrganizationPec,
                                                                    false);

        Assertions.assertEquals(HttpStatus.OK, upsertResult.response.getStatusCode());
        Assertions.assertNotNull(upsertResult.response.getBody());
        Assertions.assertEquals(aNewOrganizationFiscalCode,
                                upsertResult.response.getBody().getOrganizationFiscalCode());
        Assertions.assertEquals(anOrganizationName, upsertResult.response.getBody().getOrganizationName());
        Assertions.assertEquals(anOrganizationPec, upsertResult.response.getBody().getPec());
        Assertions.assertEquals(upsertResult.organizationWithReferents.getInsertedAt(),
                                upsertResult.response.getBody().getInsertedAt());

        Mockito.verify(agreementUserServiceSpy, Mockito.times(1)).updateMerchantTaxCode(Mockito.any(), Mockito.any());
        Mockito.verify(profileServiceSpy, Mockito.times(1)).updateProfile(Mockito.any(), Mockito.any());
    }

    @Test
    void DeleteOrganization_Ok() {
        Mockito.when(attributeAuthorityService.deleteOrganization(Mockito.any()))
               .thenReturn(ResponseEntity.noContent().build());
        ResponseEntity<Void> response = backofficeAttributeAuthorityFacade.deleteOrganization("1234567890");
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void DeleteOrganization_Ko() {
        Mockito.when(attributeAuthorityService.deleteOrganization(Mockito.any()))
               .thenReturn(ResponseEntity.notFound().build());
        ResponseEntity<Void> response = backofficeAttributeAuthorityFacade.deleteOrganization("1234567890");
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void GetReferents_Ok() {
        Mockito.when(attributeAuthorityService.getReferents(Mockito.any()))
               .thenReturn(ResponseEntity.ok(Stream.of("AAAAAA00A00A000A").collect(Collectors.toList())));
        ResponseEntity<List<String>> response = backofficeAttributeAuthorityFacade.getReferents("1234567890");
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("AAAAAA00A00A000A", response.getBody().get(0));
    }

    @Test
    void InsertReferent_Ok() {
        Mockito.when(attributeAuthorityService.insertReferent(Mockito.any(), Mockito.any()))
               .thenReturn(ResponseEntity.noContent().build());
        ReferentFiscalCode referentFiscalCode = new ReferentFiscalCode();
        referentFiscalCode.setReferentFiscalCode("AAAAAA00A00A000A");
        ResponseEntity<Void> response = backofficeAttributeAuthorityFacade.insertReferent("1234567890",
                                                                                          referentFiscalCode);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void DeleteReferent_Ok() {
        Mockito.when(attributeAuthorityService.deleteReferent(Mockito.any(), Mockito.any()))
               .thenReturn(ResponseEntity.noContent().build());
        ResponseEntity<Void> response = backofficeAttributeAuthorityFacade.deleteReferent("1234567890",
                                                                                          "AAAAAA00A00A000A");
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    private class UpsertResult {
        OrganizationWithReferents organizationWithReferents;
        ResponseEntity<OrganizationWithReferents> response;

        public UpsertResult(OrganizationWithReferents organizationWithReferents,
                            ResponseEntity<OrganizationWithReferents> response) {
            this.organizationWithReferents = organizationWithReferents;
            this.response = response;
        }
    }

    private OrganizationWithReferents createOrganizationWithReferentsMock(String aKeyOrganizationFiscalCode,
                                                                          String anOrganizationFiscalCode,
                                                                          String anOrganizationName,
                                                                          String anOrganizationPec) {
        OrganizationWithReferents organizationWithReferents = new OrganizationWithReferents();
        organizationWithReferents.setKeyOrganizationFiscalCode(aKeyOrganizationFiscalCode);
        organizationWithReferents.setOrganizationFiscalCode(anOrganizationFiscalCode);
        organizationWithReferents.setOrganizationName(anOrganizationName);
        organizationWithReferents.setPec(anOrganizationPec);
        organizationWithReferents.setInsertedAt(LocalDate.now());
        return organizationWithReferents;
    }

    private OrganizationWithReferentsAndStatus createOrganizationWithReferentsAndStatusMock(String aKeyOrganizationFiscalCode,
                                                                                            String anOrganizationFiscalCode,
                                                                                            String anOrganizationName,
                                                                                            String anOrganizationPec) {
        OrganizationWithReferentsAndStatus organizationWithReferents = new OrganizationWithReferentsAndStatus();
        organizationWithReferents.setKeyOrganizationFiscalCode(aKeyOrganizationFiscalCode);
        organizationWithReferents.setOrganizationFiscalCode(anOrganizationFiscalCode);
        organizationWithReferents.setOrganizationName(anOrganizationName);
        organizationWithReferents.setPec(anOrganizationPec);
        organizationWithReferents.setInsertedAt(LocalDate.now());
        organizationWithReferents.setStatus(OrganizationStatus.ENABLED);
        return organizationWithReferents;
    }

    private UpsertResult upsertOrganizationWithReferents(String aKeyOrganizationFiscalCode,
                                                         String anOrganizationFiscalCode,
                                                         String anOrganizationName,
                                                         String anOrganizationPec,
                                                         boolean testServiceError) {
        OrganizationWithReferents organizationWithReferents = createOrganizationWithReferentsMock(
                aKeyOrganizationFiscalCode,
                anOrganizationFiscalCode,
                anOrganizationName,
                anOrganizationPec);

        if (testServiceError) {
            Mockito.when(attributeAuthorityService.upsertOrganization(Mockito.any())).thenThrow(RuntimeException.class);
        } else {
            Mockito.when(attributeAuthorityService.upsertOrganization(Mockito.any()))
                   .thenReturn(ResponseEntity.ok(organizationWithReferentsConverter.toAttributeAuthorityModel(
                           organizationWithReferents)));
        }

        ResponseEntity<OrganizationWithReferents> response = backofficeAttributeAuthorityFacade.upsertOrganization(
                organizationWithReferents);
        return new UpsertResult(organizationWithReferents, response);
    }
}
