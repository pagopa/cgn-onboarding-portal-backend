package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.model.AAOrganizationEntity;
import it.gov.pagopa.cgn.portal.model.AAOrganizationReferentEntity;
import it.gov.pagopa.cgn.portal.model.AAReferentEntity;
import it.gov.pagopa.cgn.portal.repository.AAOrganizationReferentRepository;
import it.gov.pagopa.cgn.portal.repository.AAOrganizationRepository;
import it.gov.pagopa.cgn.portal.repository.AAReferentRepository;
import org.junit.jupiter.api.AfterEach;
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
class AttributeAuthorityServiceDeleteReferentTest
        extends IntegrationAbstractTest {

    @Autowired
    private AttributeAuthorityService attributeAuthorityService;

    @Autowired
    private AAOrganizationRepository aaOrganizationRepository;

    @Autowired
    private AAReferentRepository aaReferentRepository;

    @Autowired
    private AAOrganizationReferentRepository aaOrganizationReferentRepository;

    @AfterEach
    void cleanAAData() {
        aaOrganizationReferentRepository.deleteAll();
        aaOrganizationReferentRepository.flush();
        aaOrganizationRepository.deleteAll();
        aaOrganizationRepository.flush();
        aaReferentRepository.deleteAll();
        aaReferentRepository.flush();
    }

    @Test
    void DeleteReferent_Ok() {
        // Given: Organization with referent
        String keyFiscalCode = "RSSMRA80A01H501U";
        String referentCode = "AAAAAA00A00A000A";

        AAReferentEntity referent = new AAReferentEntity();
        referent.setFiscalCode(referentCode);
        aaReferentRepository.saveAndFlush(referent);

        AAOrganizationEntity organization = new AAOrganizationEntity();
        organization.setFiscalCode(keyFiscalCode);
        organization.setName("Test Org");
        organization.setPec("test@pec.it");
        organization.setInsertedAt(OffsetDateTime.now());

        List<AAOrganizationReferentEntity> referents = new ArrayList<>();
        AAOrganizationReferentEntity join = new AAOrganizationReferentEntity();
        join.setOrganization(organization);
        join.setReferent(referent);
        referents.add(join);

        organization.setOrganizationReferents(referents);
        aaOrganizationRepository.saveAndFlush(organization);

        Assertions.assertEquals(1, aaOrganizationReferentRepository.count());

        // When: Delete referent
        ResponseEntity<Void> response = attributeAuthorityService.deleteReferent(keyFiscalCode, referentCode);

        // Then: Verify response
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify join deleted (orphanRemoval)
        Assertions.assertEquals(0, aaOrganizationReferentRepository.count());

        // Verify referent entity still exists
        Assertions.assertEquals(1, aaReferentRepository.count());
    }

    @Test
    void DeleteReferent_OrganizationNotFound() {
        ResponseEntity<Void> response = attributeAuthorityService.deleteReferent("RSSMRA80A01H501U", "AAAAAA00A00A000A");

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void DeleteReferent_ReferentNotInOrganization_Ok() {
        // Given: Organization without that referent
        String keyFiscalCode = "RSSMRA80A01H501U";
        AAOrganizationEntity organization = new AAOrganizationEntity();
        organization.setFiscalCode(keyFiscalCode);
        organization.setName("Test Org");
        organization.setPec("test@pec.it");
        organization.setInsertedAt(OffsetDateTime.now());
        organization.setOrganizationReferents(new ArrayList<>());
        aaOrganizationRepository.saveAndFlush(organization);

        ResponseEntity<Void> response = attributeAuthorityService.deleteReferent(keyFiscalCode, "AAAAAA00A00A000A");

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void DeleteReferent_MultipleReferents_Ok() {
        // Given: Organization with multiple referents
        String keyFiscalCode = "RSSMRA80A01H501U";

        AAReferentEntity referent1 = new AAReferentEntity();
        referent1.setFiscalCode("AAAAAA00A00A000A");
        aaReferentRepository.saveAndFlush(referent1);

        AAReferentEntity referent2 = new AAReferentEntity();
        referent2.setFiscalCode("BBBBBB00B00B000B");
        aaReferentRepository.saveAndFlush(referent2);

        AAOrganizationEntity organization = new AAOrganizationEntity();
        organization.setFiscalCode(keyFiscalCode);
        organization.setName("Test Org");
        organization.setPec("test@pec.it");
        organization.setInsertedAt(OffsetDateTime.now());

        List<AAOrganizationReferentEntity> referents = new ArrayList<>();
        AAOrganizationReferentEntity join1 = new AAOrganizationReferentEntity();
        join1.setOrganization(organization);
        join1.setReferent(referent1);
        referents.add(join1);

        AAOrganizationReferentEntity join2 = new AAOrganizationReferentEntity();
        join2.setOrganization(organization);
        join2.setReferent(referent2);
        referents.add(join2);

        organization.setOrganizationReferents(referents);
        aaOrganizationRepository.saveAndFlush(organization);

        Assertions.assertEquals(2, aaOrganizationReferentRepository.count());

        // When: Delete one referent
        ResponseEntity<Void> response = attributeAuthorityService.deleteReferent(keyFiscalCode, "AAAAAA00A00A000A");

        // Then: Verify only one join deleted
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(1, aaOrganizationReferentRepository.count());

        // Verify both referents still exist
        Assertions.assertEquals(2, aaReferentRepository.count());
    }
}
