package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.model.AAOrganizationEntity;
import it.gov.pagopa.cgn.portal.model.AAOrganizationReferentEntity;
import it.gov.pagopa.cgn.portal.model.AAReferentEntity;
import it.gov.pagopa.cgn.portal.repository.AAOrganizationRepository;
import it.gov.pagopa.cgn.portal.repository.AAReferentRepository;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsAttributeAuthority;
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
class AttributeAuthorityServiceGetOrganizationTest extends IntegrationAbstractTest {

    @Autowired
    private AttributeAuthorityService attributeAuthorityService;

    @Autowired
    private AAOrganizationRepository aaOrganizationRepository;

    @Autowired
    private AAReferentRepository aaReferentRepository;

    @Test
    void getOrganization_OrganizationExists_ReturnsOkWithData() {
        AAReferentEntity referent1 = createAndSaveReferent("RSSMRA80A01H501U");
        AAReferentEntity referent2 = createAndSaveReferent("VRDGPP85M06H501X");

        AAOrganizationEntity organization = createAndSaveOrganization(
                "12345678901",
                "Test Organization",
                "test@pec.it",
                List.of(referent1, referent2)
        );

        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response = 
                attributeAuthorityService.getOrganization(organization.getFiscalCode());

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        
        OrganizationWithReferentsAttributeAuthority body = response.getBody();
        Assertions.assertEquals("12345678901", body.getKeyOrganizationFiscalCode());
        Assertions.assertEquals("12345678901", body.getOrganizationFiscalCode());
        Assertions.assertEquals("Test Organization", body.getOrganizationName());
        Assertions.assertEquals("test@pec.it", body.getPec());
        Assertions.assertNotNull(body.getInsertedAt());
        Assertions.assertNotNull(body.getReferents());
        Assertions.assertEquals(2, body.getReferents().size());
        Assertions.assertTrue(body.getReferents().contains("RSSMRA80A01H501U"));
        Assertions.assertTrue(body.getReferents().contains("VRDGPP85M06H501X"));
    }

    @Test
    void getOrganization_OrganizationNotFound_ReturnsNotFound() {
        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response = 
                attributeAuthorityService.getOrganization("NONEXISTENT123");

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    void getOrganization_OrganizationWithoutReferents_ReturnsOkWithEmptyReferentsList() {
        AAOrganizationEntity organization = createAndSaveOrganization(
                "98765432109",
                "Organization Without Referents",
                "norefer@pec.it",
                List.of()
        );

        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response = 
                attributeAuthorityService.getOrganization(organization.getFiscalCode());

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        
        OrganizationWithReferentsAttributeAuthority body = response.getBody();
        Assertions.assertEquals("98765432109", body.getKeyOrganizationFiscalCode());
        Assertions.assertNotNull(body.getReferents());
        Assertions.assertEquals(0, body.getReferents().size());
    }

    @Test
    void getOrganization_OrganizationWithSingleReferent_ReturnsOkWithOneReferent() {
        AAReferentEntity referent = createAndSaveReferent("BNCLRA75D12H501Z");

        AAOrganizationEntity organization = createAndSaveOrganization(
                "11223344556",
                "Single Referent Org",
                "single@pec.it",
                List.of(referent)
        );

        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response = 
                attributeAuthorityService.getOrganization(organization.getFiscalCode());

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        
        OrganizationWithReferentsAttributeAuthority body = response.getBody();
        Assertions.assertEquals("11223344556", body.getKeyOrganizationFiscalCode());
        Assertions.assertNotNull(body.getReferents());
        Assertions.assertEquals(1, body.getReferents().size());
        Assertions.assertEquals("BNCLRA75D12H501Z", body.getReferents().getFirst());
    }

    @Test
    void getOrganization_ResponseFieldsCorrectlyMapped() {
        AAReferentEntity referent = createAndSaveReferent("GLLMRC70A01F205X");

        AAOrganizationEntity organization = createAndSaveOrganization(
                "99887766554",
                "Mapping Test Org",
                "mapping@test.pec.it",
                List.of(referent)
        );

        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response = 
                attributeAuthorityService.getOrganization(organization.getFiscalCode());

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        
        OrganizationWithReferentsAttributeAuthority body = response.getBody();
        Assertions.assertEquals(organization.getFiscalCode(), body.getKeyOrganizationFiscalCode());
        Assertions.assertEquals(organization.getFiscalCode(), body.getOrganizationFiscalCode());
        Assertions.assertEquals(organization.getName(), body.getOrganizationName());
        Assertions.assertEquals(organization.getPec(), body.getPec());
        
        String insertedAtString = body.getInsertedAt();
        Assertions.assertNotNull(insertedAtString);
        Assertions.assertFalse(insertedAtString.isEmpty());
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
