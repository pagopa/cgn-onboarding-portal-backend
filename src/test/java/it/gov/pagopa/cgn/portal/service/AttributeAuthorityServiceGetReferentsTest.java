package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.model.AAOrganizationEntity;
import it.gov.pagopa.cgn.portal.model.AAOrganizationReferentEntity;
import it.gov.pagopa.cgn.portal.model.AAReferentEntity;
import it.gov.pagopa.cgn.portal.repository.AAOrganizationRepository;
import it.gov.pagopa.cgn.portal.repository.AAReferentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ActiveProfiles("dev")
class AttributeAuthorityServiceGetReferentsTest extends IntegrationAbstractTest {

    @Autowired
    private AttributeAuthorityService attributeAuthorityService;

    @Autowired
    private AAOrganizationRepository aaOrganizationRepository;

    @Autowired
    private AAReferentRepository aaReferentRepository;

    @Test
    void getReferents_OrganizationExists_ReturnsReferentList() {
        AAReferentEntity referent1 = createAndSaveReferent("RSSMRA80A01H501U");
        AAReferentEntity referent2 = createAndSaveReferent("VRDGPP85M06H501X");

        AAOrganizationEntity organization = createAndSaveOrganization(
                "12345678901",
                "Test Organization",
                "test@pec.it",
                List.of(referent1, referent2)
        );

        ResponseEntity<List<String>> response = attributeAuthorityService.getReferents(organization.getFiscalCode());

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(2, response.getBody().size());
        Assertions.assertTrue(response.getBody().contains("RSSMRA80A01H501U"));
        Assertions.assertTrue(response.getBody().contains("VRDGPP85M06H501X"));
    }

    @Test
    void getReferents_OrganizationNotFound_ReturnsNotFound() {
        ResponseEntity<List<String>> response = attributeAuthorityService.getReferents("NONEXISTENT123");

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    void getReferents_OrganizationWithoutReferents_ReturnsEmptyList() {
        AAOrganizationEntity organization = createAndSaveOrganization(
                "98765432109",
                "Organization Without Referents",
                "norefer@pec.it",
                List.of()
        );

        ResponseEntity<List<String>> response = attributeAuthorityService.getReferents(organization.getFiscalCode());

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(0, response.getBody().size());
    }

    private AAReferentEntity createAndSaveReferent(String fiscalCode) {
        AAReferentEntity referent = new AAReferentEntity();
        referent.setFiscalCode(fiscalCode);
        referent.setOrganizationReferents(new ArrayList<>());
        return aaReferentRepository.save(referent);
    }

    private AAOrganizationEntity createAndSaveOrganization(
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

        return savedOrganization;
    }
}
