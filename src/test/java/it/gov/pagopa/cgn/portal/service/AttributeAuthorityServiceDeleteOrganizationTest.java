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
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("dev")
class AttributeAuthorityServiceDeleteOrganizationTest
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
    void DeleteOrganization_Ok() {
        String keyFiscalCode = "RSSMRA80A01H501U";
        AAOrganizationEntity existing = new AAOrganizationEntity();
        existing.setFiscalCode(keyFiscalCode);
        existing.setName("Test Org");
        existing.setPec("test@pec.it");
        existing.setInsertedAt(OffsetDateTime.now());
        aaOrganizationRepository.saveAndFlush(existing);

        ResponseEntity<Void> response = attributeAuthorityService.deleteOrganization(keyFiscalCode);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        Optional<AAOrganizationEntity> deleted = aaOrganizationRepository.findById(keyFiscalCode);
        Assertions.assertTrue(deleted.isEmpty());
    }

    @Test
    void DeleteOrganization_VatFiscalCode_Ok() {
        String keyFiscalCode = "54683216974";
        AAOrganizationEntity existing = new AAOrganizationEntity();
        existing.setFiscalCode(keyFiscalCode);
        existing.setName("Test Org VAT");
        existing.setPec("vat@pec.it");
        existing.setInsertedAt(OffsetDateTime.now());
        aaOrganizationRepository.saveAndFlush(existing);

        ResponseEntity<Void> response = attributeAuthorityService.deleteOrganization(keyFiscalCode);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        Optional<AAOrganizationEntity> deleted = aaOrganizationRepository.findById(keyFiscalCode);
        Assertions.assertTrue(deleted.isEmpty());
    }

    @Test
    void DeleteOrganization_NotFound() {
        String keyFiscalCode = "RSSMRA80A01H501U";

        ResponseEntity<Void> response = attributeAuthorityService.deleteOrganization(keyFiscalCode);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void DeleteOrganization_CascadeDeletesReferents_Ok() {
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

        ResponseEntity<Void> response = attributeAuthorityService.deleteOrganization(keyFiscalCode);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        Optional<AAOrganizationEntity> deleted = aaOrganizationRepository.findById(keyFiscalCode);
        Assertions.assertTrue(deleted.isEmpty());

        Assertions.assertEquals(0, aaOrganizationReferentRepository.count());

        Assertions.assertEquals(2, aaReferentRepository.count());
    }

}
