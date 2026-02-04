package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.model.AAOrganizationEntity;
import it.gov.pagopa.cgn.portal.model.AAOrganizationReferentEntity;
import it.gov.pagopa.cgn.portal.model.AAReferentEntity;
import it.gov.pagopa.cgn.portal.repository.AAOrganizationRepository;
import it.gov.pagopa.cgn.portal.repository.AAReferentRepository;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.CompanyAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsPostAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.ReferentFiscalCodeAttributeAuthority;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@SpringBootTest
@ActiveProfiles("dev")
class AttributeAuthorityServiceTest
        extends IntegrationAbstractTest {

    private AAOrganizationRepository aaOrganizationRepository;
    private AAReferentRepository aaReferentRepository;

    private AttributeAuthorityService attributeAuthorityService;

    @Mock
    private Page<AAOrganizationEntity> mockPage;

    @BeforeEach
    void init() {
        aaOrganizationRepository = Mockito.mock(AAOrganizationRepository.class);
        aaReferentRepository = Mockito.mock(AAReferentRepository.class);
        attributeAuthorityService = new AttributeAuthorityService(aaOrganizationRepository,
                                                                  aaReferentRepository);
    }

    @Test
    void GetOrganizations_Ok() {
        Mockito.when(aaOrganizationRepository.findAllWithReferents(Mockito.any()))
               .thenReturn(mockPage);
        Mockito.when(aaOrganizationRepository.countAllWithReferents())
               .thenReturn(0L);
        
        ResponseEntity<OrganizationsAttributeAuthority> response = attributeAuthorityService.getOrganizations(null,
                                                                                                              null,
                                                                                                              null,
                                                                                                              null,
                                                                                                              null);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Mockito.verify(aaOrganizationRepository, Mockito.times(1)).findAllWithReferents(Mockito.any());
        Mockito.verify(aaOrganizationRepository, Mockito.times(1)).countAllWithReferents();
    }

    @Test
    void GetOrganization_Ok() {
        Mockito.when(aaOrganizationRepository.findById(Mockito.anyString()))
               .thenReturn(java.util.Optional.empty());
        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response = attributeAuthorityService.getOrganization(
                "1234567890");
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Mockito.verify(aaOrganizationRepository, Mockito.times(1)).findById("1234567890");
    }

    @Test
    void GetReferents_Ok() {
        AAReferentEntity referent = new AAReferentEntity();
        referent.setFiscalCode("AAAAAA00A00A000A");

        AAOrganizationEntity organization = new AAOrganizationEntity();
        organization.setFiscalCode("1234567890");

        AAOrganizationReferentEntity joinEntity = new AAOrganizationReferentEntity();
        joinEntity.setOrganization(organization);
        joinEntity.setReferent(referent);

        organization.setOrganizationReferents(List.of(joinEntity));

        Mockito.when(aaOrganizationRepository.findById("1234567890"))
               .thenReturn(java.util.Optional.of(organization));

        ResponseEntity<List<String>> response = attributeAuthorityService.getReferents("1234567890");
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(1, response.getBody().size());
        Assertions.assertEquals("AAAAAA00A00A000A", response.getBody().getFirst());
        Mockito.verify(aaOrganizationRepository, Mockito.times(1)).findById("1234567890");
    }

    @Test
    void shouldReturnListOfCompanies_givenFiscalCode() {
        String fiscalCode = "RSSMRA80A01H501U";
        AAOrganizationEntity organization1 = new AAOrganizationEntity();
        organization1.setFiscalCode("1");
        organization1.setName("Org 1");
        organization1.setPec("org1@pec.it");

        AAOrganizationEntity organization2 = new AAOrganizationEntity();
        organization2.setFiscalCode("2");
        organization2.setName("Org 2");
        organization2.setPec("org2@pec.it");

        AAReferentEntity referent = new AAReferentEntity();
        referent.setFiscalCode(fiscalCode);

        AAOrganizationReferentEntity join1 = new AAOrganizationReferentEntity();
        join1.setOrganization(organization1);
        join1.setReferent(referent);

        AAOrganizationReferentEntity join2 = new AAOrganizationReferentEntity();
        join2.setOrganization(organization2);
        join2.setReferent(referent);

        referent.setOrganizationReferents(List.of(join1, join2));

        Mockito.when(aaReferentRepository.findById(fiscalCode))
               .thenReturn(java.util.Optional.of(referent));

        List<CompanyAttributeAuthority> result = attributeAuthorityService.getAgreementOrganizations(fiscalCode);

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("1", result.getFirst().getFiscalCode());
        Assertions.assertEquals("2", result.get(1).getFiscalCode());
        Mockito.verify(aaReferentRepository, Mockito.times(1)).findById(fiscalCode);
    }

    @Test
    void shouldReturnCorrectOrganizationCount_givenFiscalCode() {
        String fiscalCode = "RSSMRA80A01H501U";
        AAOrganizationEntity organization1 = new AAOrganizationEntity();
        organization1.setFiscalCode("1");
        organization1.setName("Org 1");
        organization1.setPec("org1@pec.it");

        AAOrganizationEntity organization2 = new AAOrganizationEntity();
        organization2.setFiscalCode("2");
        organization2.setName("Org 2");
        organization2.setPec("org2@pec.it");

        AAOrganizationEntity organization3 = new AAOrganizationEntity();
        organization3.setFiscalCode("3");
        organization3.setName("Org 3");
        organization3.setPec("org3@pec.it");

        AAReferentEntity referent = new AAReferentEntity();
        referent.setFiscalCode(fiscalCode);

        AAOrganizationReferentEntity join1 = new AAOrganizationReferentEntity();
        join1.setOrganization(organization1);
        join1.setReferent(referent);

        AAOrganizationReferentEntity join2 = new AAOrganizationReferentEntity();
        join2.setOrganization(organization2);
        join2.setReferent(referent);

        AAOrganizationReferentEntity join3 = new AAOrganizationReferentEntity();
        join3.setOrganization(organization3);
        join3.setReferent(referent);

        referent.setOrganizationReferents(List.of(join1, join2, join3));

        Mockito.when(aaReferentRepository.findById(fiscalCode))
               .thenReturn(java.util.Optional.of(referent));

        int count = attributeAuthorityService.countUserOrganizations(fiscalCode);

        Assertions.assertEquals(3, count);
        Mockito.verify(aaReferentRepository, Mockito.times(1)).findById(fiscalCode);
    }

    @Test
    void UpsertOrganization_ServiceUnavailable() {
        HttpClientErrorException exception = Assertions.assertThrows(
                HttpClientErrorException.class,
                () -> attributeAuthorityService.upsertOrganization(new OrganizationWithReferentsPostAttributeAuthority())
        );
        
        Assertions.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void DeleteOrganization_ServiceUnavailable() {
        HttpClientErrorException exception = Assertions.assertThrows(
                HttpClientErrorException.class,
                () -> attributeAuthorityService.deleteOrganization("1234567890")
        );
        
        Assertions.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void InsertReferent_ServiceUnavailable() {
        ReferentFiscalCodeAttributeAuthority referent = new ReferentFiscalCodeAttributeAuthority();
        referent.setReferentFiscalCode("AAAAAA00A00A000A");
        
        HttpClientErrorException exception = Assertions.assertThrows(
                HttpClientErrorException.class,
                () -> attributeAuthorityService.insertReferent("1234567890", referent)
        );
        
        Assertions.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void DeleteReferent_ServiceUnavailable() {
        HttpClientErrorException exception = Assertions.assertThrows(
                HttpClientErrorException.class,
                () -> attributeAuthorityService.deleteReferent("1234567890", "AAAAAA00A00A000A")
        );
        
        Assertions.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

}