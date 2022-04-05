package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.converter.backoffice.OrganizationWithReferentsConverter;
import it.gov.pagopa.cgn.portal.converter.backoffice.OrganizationWithReferentsPostConverter;
import it.gov.pagopa.cgn.portal.converter.backoffice.OrganizationsConverter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.service.AgreementUserService;
import it.gov.pagopa.cgn.portal.service.AttributeAuthorityService;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferents;
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

@SpringBootTest
@ActiveProfiles("dev")
class BackofficeAttributeAuthorityFacadeTest extends IntegrationAbstractTest {

    private BackofficeAttributeAuthorityFacade backofficeAttributeAuthorityFacade;
    private AttributeAuthorityService attributeAuthorityService;
    private OrganizationWithReferentsConverter organizationWithReferentsConverter;
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

        var organizationWithReferentsPostConverter = new OrganizationWithReferentsPostConverter();
        var organizationsConverter = new OrganizationsConverter(organizationWithReferentsConverter);

        backofficeAttributeAuthorityFacade = new BackofficeAttributeAuthorityFacade(attributeAuthorityService, agreementUserServiceSpy, profileServiceSpy, organizationsConverter, organizationWithReferentsConverter, organizationWithReferentsPostConverter);

        String anOrganizationTaxCodeOrVat = "abcdeghilmnopqrs";
        agreementEntity = agreementService.createAgreementIfNotExists(anOrganizationTaxCodeOrVat);
        profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());
        documentRepository.saveAll(TestUtils.createSampleDocumentList(agreementEntity));
    }

    @Test
    void UpsertOrganization_New_Ok() {
        String anOrganizationFiscalCode = "12345678";
        String anOrganizationName = "An organization";
        String anOrganizationPec = "an-organization@pec.it";
        LocalDate anInsertedAt = LocalDate.now();

        OrganizationWithReferents organizationWithReferents = new OrganizationWithReferents();
        organizationWithReferents.setKeyOrganizationFiscalCode(anOrganizationFiscalCode);
        organizationWithReferents.setOrganizationFiscalCode(anOrganizationFiscalCode);
        organizationWithReferents.setOrganizationName(anOrganizationName);
        organizationWithReferents.setPec(anOrganizationPec);
        organizationWithReferents.setInsertedAt(anInsertedAt);

        Mockito.when(attributeAuthorityService.upsertOrganization(Mockito.any())).thenReturn(organizationWithReferentsConverter.toAttributeAuthorityModel(organizationWithReferents));

        ResponseEntity<OrganizationWithReferents> response = backofficeAttributeAuthorityFacade.upsertOrganization(organizationWithReferents);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(anOrganizationFiscalCode, response.getBody().getOrganizationFiscalCode());
        Assertions.assertEquals(anOrganizationName, response.getBody().getOrganizationName());
        Assertions.assertEquals(anOrganizationPec, response.getBody().getPec());
        Assertions.assertEquals(anInsertedAt, response.getBody().getInsertedAt());

        Mockito.verify(agreementUserServiceSpy, Mockito.times(0)).updateMerchantTaxCode(Mockito.any(), Mockito.any());
        Mockito.verify(profileServiceSpy, Mockito.times(0)).updateProfile(Mockito.any(), Mockito.any());
    }

    @Test
    @Transactional(Transactional.TxType.REQUIRED)
    void UpsertOrganization_Upsert_Ok() {
        String anOrganizationFiscalCode = profileEntity.getTaxCodeOrVat();
        String anOrganizationName = "New name";
        String anOrganizationPec = "new-pec@pec.it";
        LocalDate anInsertedAt = LocalDate.now();

        OrganizationWithReferents organizationWithReferents = new OrganizationWithReferents();
        organizationWithReferents.setKeyOrganizationFiscalCode(anOrganizationFiscalCode);
        organizationWithReferents.setOrganizationFiscalCode(anOrganizationFiscalCode);
        organizationWithReferents.setOrganizationName(anOrganizationName);
        organizationWithReferents.setPec(anOrganizationPec);
        organizationWithReferents.setInsertedAt(anInsertedAt);

        Mockito.when(attributeAuthorityService.upsertOrganization(Mockito.any())).thenReturn(organizationWithReferentsConverter.toAttributeAuthorityModel(organizationWithReferents));

        ResponseEntity<OrganizationWithReferents> response = backofficeAttributeAuthorityFacade.upsertOrganization(organizationWithReferents);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(anOrganizationFiscalCode, response.getBody().getOrganizationFiscalCode());
        Assertions.assertEquals(anOrganizationName, response.getBody().getOrganizationName());
        Assertions.assertEquals(anOrganizationPec, response.getBody().getPec());
        Assertions.assertEquals(anInsertedAt, response.getBody().getInsertedAt());

        Mockito.verify(agreementUserServiceSpy, Mockito.times(0)).updateMerchantTaxCode(Mockito.any(), Mockito.any());
        Mockito.verify(profileServiceSpy, Mockito.times(1)).updateProfile(Mockito.any(), Mockito.any());
    }

}
