package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.converter.backoffice.OrganizationWithReferentsConverter;
import it.gov.pagopa.cgn.portal.converter.backoffice.OrganizationWithReferentsPostConverter;
import it.gov.pagopa.cgn.portal.converter.backoffice.OrganizationsConverter;
import it.gov.pagopa.cgn.portal.converter.backoffice.ReferentFiscalCodeConverter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.service.AgreementUserService;
import it.gov.pagopa.cgn.portal.service.AttributeAuthorityService;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferents;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Organizations;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ReferentFiscalCode;
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
    private ReferentFiscalCodeConverter referentFiscalCodeConverter;
    private OrganizationsConverter organizationsConverter;
    private AgreementUserService agreementUserServiceSpy;
    private ProfileService profileServiceSpy;
    private AgreementEntity agreementEntity;
    private ProfileEntity profileEntity;

    @BeforeEach
    void init() {
        ProfileService profileService = new ProfileService(profileRepository, agreementServiceLight, documentService);
        profileServiceSpy = Mockito.spy(profileService);

        AgreementUserService agreementUserService = new AgreementUserService(agreementUserRepository);
        agreementUserServiceSpy = Mockito.spy(agreementUserService);

        attributeAuthorityService = Mockito.mock(AttributeAuthorityService.class);
        organizationWithReferentsConverter = new OrganizationWithReferentsConverter();
        organizationsConverter = new OrganizationsConverter(organizationWithReferentsConverter);
        referentFiscalCodeConverter = new ReferentFiscalCodeConverter();

        var organizationWithReferentsPostConverter = new OrganizationWithReferentsPostConverter();
        var organizationsConverter = new OrganizationsConverter(organizationWithReferentsConverter);

        backofficeAttributeAuthorityFacade = new BackofficeAttributeAuthorityFacade(attributeAuthorityService,
                                                                                    agreementUserServiceSpy,
                                                                                    profileServiceSpy,
                                                                                    organizationsConverter,
                                                                                    organizationWithReferentsConverter,
                                                                                    organizationWithReferentsPostConverter,
                                                                                    referentFiscalCodeConverter);

        String anOrganizationTaxCodeOrVat = "abcdeghilmnopqrs";
        agreementEntity = agreementService.createAgreementIfNotExists(anOrganizationTaxCodeOrVat);
        profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
    }

    @Test
    void GetOrganizations_Ok() {
        UpsertResult organizationResponse0 =
                upsertOrganizationWithReferents("1234567890", "1234567890", "Org 0", "org0@pec.it", false);
        UpsertResult organizationResponse1 =
                upsertOrganizationWithReferents("1234567891", "1234567891", "Org 1", "org1@pec.it", false);
        Organizations organizations = new Organizations();
        organizations.setCount(2);
        List<OrganizationWithReferents> items = new LinkedList();
        items.add(organizationResponse0.organizationWithReferents);
        items.add(organizationResponse1.organizationWithReferents);
        organizations.setItems(items);

        Mockito.when(attributeAuthorityService.getOrganizations(Mockito.any(),
                                                                Mockito.any(),
                                                                Mockito.any(),
                                                                Mockito.any(),
                                                                Mockito.any()))
               .thenReturn(ResponseEntity.ok(organizationsConverter.toAttributeAuthorityModel(organizations)));
        ResponseEntity<Organizations> organizationsResponse =
                backofficeAttributeAuthorityFacade.getOrganizations(null, 0, 2, null, null);

        Assertions.assertEquals(HttpStatus.OK, organizationsResponse.getStatusCode());
        Assertions.assertNotNull(organizationsResponse.getBody());
        Assertions.assertEquals(2, organizationsResponse.getBody().getCount());
        Assertions.assertEquals(organizationResponse0.organizationWithReferents,
                                organizationsResponse.getBody().getItems().get(0));
        Assertions.assertEquals(organizationResponse1.organizationWithReferents,
                                organizationsResponse.getBody().getItems().get(1));
    }

    @Test
    void GetOrganization_Ok() {
        UpsertResult organizationResponse0 =
                upsertOrganizationWithReferents("1234567890", "1234567890", "Org 0", "org0@pec.it", false);

        Mockito.when(attributeAuthorityService.getOrganization(Mockito.any()))
               .thenReturn(ResponseEntity.ok(organizationWithReferentsConverter.toAttributeAuthorityModel(
                       organizationResponse0.organizationWithReferents)));
        ResponseEntity<OrganizationWithReferents> organizationResponse =
                backofficeAttributeAuthorityFacade.getOrganization("1234567890");

        Assertions.assertEquals(HttpStatus.OK, organizationResponse.getStatusCode());
        Assertions.assertNotNull(organizationResponse.getBody());
        Assertions.assertEquals(organizationResponse0.organizationWithReferents, organizationResponse.getBody());
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
        ResponseEntity<Void> response =
                backofficeAttributeAuthorityFacade.insertReferent("1234567890", referentFiscalCode);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void DeleteReferent_Ok() {
        Mockito.when(attributeAuthorityService.deleteReferent(Mockito.any(), Mockito.any()))
               .thenReturn(ResponseEntity.noContent().build());
        ResponseEntity<Void> response =
                backofficeAttributeAuthorityFacade.deleteReferent("1234567890", "AAAAAA00A00A000A");
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

    private UpsertResult upsertOrganizationWithReferents(String aKeyOrganizationFiscalCode,
                                                         String anOrganizationFiscalCode,
                                                         String anOrganizationName,
                                                         String anOrganizationPec,
                                                         boolean testServiceError) {
        LocalDate anInsertedAt = LocalDate.now();
        OrganizationWithReferents organizationWithReferents = new OrganizationWithReferents();
        organizationWithReferents.setKeyOrganizationFiscalCode(aKeyOrganizationFiscalCode);
        organizationWithReferents.setOrganizationFiscalCode(anOrganizationFiscalCode);
        organizationWithReferents.setOrganizationName(anOrganizationName);
        organizationWithReferents.setPec(anOrganizationPec);
        organizationWithReferents.setInsertedAt(anInsertedAt);

        if (testServiceError) {
            Mockito.when(attributeAuthorityService.upsertOrganization(Mockito.any())).thenThrow(RuntimeException.class);
        } else {
            Mockito.when(attributeAuthorityService.upsertOrganization(Mockito.any()))
                   .thenReturn(ResponseEntity.ok(organizationWithReferentsConverter.toAttributeAuthorityModel(
                           organizationWithReferents)));
        }

        ResponseEntity<OrganizationWithReferents> response =
                backofficeAttributeAuthorityFacade.upsertOrganization(organizationWithReferents);
        return new UpsertResult(organizationWithReferents, response);
    }
}
