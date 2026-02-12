package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.model.AAOrganizationEntity;
import it.gov.pagopa.cgn.portal.model.AAOrganizationReferentEntity;
import it.gov.pagopa.cgn.portal.model.AAReferentEntity;
import it.gov.pagopa.cgn.portal.repository.AAOrganizationRepository;
import it.gov.pagopa.cgn.portal.repository.AAReferentRepository;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.CompanyAttributeAuthority;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ActiveProfiles("dev")
class AttributeAuthorityServiceGetUserCompaniesTest extends IntegrationAbstractTest { 

    @Autowired
    private AttributeAuthorityService attributeAuthorityService;

    @Autowired
    private AAOrganizationRepository aaOrganizationRepository;

    @Autowired
    private AAReferentRepository aaReferentRepository;

    @Test
    void getAgreementOrganizations_ReferentExists_ReturnsCompanies() {
        AAReferentEntity referent = createAndSaveReferent("RSSMRA80A01H501U");

        createAndSaveOrganization("12345678901", "Org 1", "org1@pec.it", List.of(referent));
        createAndSaveOrganization("98765432109", "Org 2", "org2@pec.it", List.of(referent));

        List<CompanyAttributeAuthority> companies = attributeAuthorityService.getAgreementOrganizations(
                referent.getFiscalCode());

        Assertions.assertEquals(2, companies.size());
        Assertions.assertTrue(companies.stream().anyMatch(c -> "12345678901".equals(c.getFiscalCode())));
        Assertions.assertTrue(companies.stream().anyMatch(c -> "98765432109".equals(c.getFiscalCode())));
    }

    @Test
    void getAgreementOrganizations_ReferentNotFound_ThrowsNotFound() {
        HttpClientErrorException exception = Assertions.assertThrows(HttpClientErrorException.class,
                () -> attributeAuthorityService.getAgreementOrganizations("NONEXISTENT123"));

        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void countUserOrganizations_ReturnsCorrectCount() {
        AAReferentEntity referent = createAndSaveReferent("VRDGPP85M06H501X");

        createAndSaveOrganization("11111111111", "Org 1", "org1@pec.it", List.of(referent));
        createAndSaveOrganization("22222222222", "Org 2", "org2@pec.it", List.of(referent));
        createAndSaveOrganization("33333333333", "Org 3", "org3@pec.it", List.of(referent));

        int count = attributeAuthorityService.countUserOrganizations(referent.getFiscalCode());

        Assertions.assertEquals(3, count);
    }

    private AAReferentEntity createAndSaveReferent(String fiscalCode) {
        AAReferentEntity referent = new AAReferentEntity();
        referent.setFiscalCode(fiscalCode);
        referent.setOrganizationReferents(new ArrayList<>());
        return aaReferentRepository.save(referent);
    }

    private void createAndSaveOrganization(
            String fiscalCode,
            String name,
            String pec,
            List<AAReferentEntity> referents) {

        AAOrganizationEntity organization = new AAOrganizationEntity();
        organization.setFiscalCode(fiscalCode);
        organization.setName(name);
        organization.setPec(pec);
        organization.setInsertedAt(OffsetDateTime.now());
        organization.setOrganizationReferents(new ArrayList<>());

        AAOrganizationEntity savedOrganization = aaOrganizationRepository.save(organization);

        if (referents != null && !referents.isEmpty()) {
            for (AAReferentEntity referent : referents) {
                AAOrganizationReferentEntity joinEntity = new AAOrganizationReferentEntity();
                joinEntity.setOrganization(savedOrganization);
                joinEntity.setReferent(referent);
                savedOrganization.getOrganizationReferents().add(joinEntity);
                referent.getOrganizationReferents().add(joinEntity);
            }
            aaOrganizationRepository.save(savedOrganization);
        }
    }
}
