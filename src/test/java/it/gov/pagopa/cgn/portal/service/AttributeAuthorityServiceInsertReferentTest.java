package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.model.AAOrganizationEntity;
import it.gov.pagopa.cgn.portal.model.AAOrganizationReferentEntity;
import it.gov.pagopa.cgn.portal.model.AAReferentEntity;
import it.gov.pagopa.cgn.portal.repository.AAOrganizationReferentRepository;
import it.gov.pagopa.cgn.portal.repository.AAOrganizationRepository;
import it.gov.pagopa.cgn.portal.repository.AAReferentRepository;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.ReferentFiscalCodeAttributeAuthority;
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
class AttributeAuthorityServiceInsertReferentTest
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
    void InsertReferent_Ok() {
        String keyFiscalCode = "RSSMRA80A01H501U";
        AAOrganizationEntity organization = new AAOrganizationEntity();
        organization.setFiscalCode(keyFiscalCode);
        organization.setName("Test Org");
        organization.setPec("test@pec.it");
        organization.setInsertedAt(OffsetDateTime.now());
        organization.setOrganizationReferents(new ArrayList<>());
        aaOrganizationRepository.saveAndFlush(organization);

        ReferentFiscalCodeAttributeAuthority request = new ReferentFiscalCodeAttributeAuthority();
        request.setReferentFiscalCode("AAAAAA00A00A000A");

        ResponseEntity<Void> response = attributeAuthorityService.insertReferent(keyFiscalCode, request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(1, aaOrganizationReferentRepository.count());
        Assertions.assertEquals(1, aaReferentRepository.count());
    }

    @Test
    void InsertReferent_VatOrganizationFiscalCode_Ok() {
        String keyFiscalCode = "54683216974";
        AAOrganizationEntity organization = new AAOrganizationEntity();
        organization.setFiscalCode(keyFiscalCode);
        organization.setName("Test Org VAT");
        organization.setPec("vat@pec.it");
        organization.setInsertedAt(OffsetDateTime.now());
        organization.setOrganizationReferents(new ArrayList<>());
        aaOrganizationRepository.saveAndFlush(organization);

        ReferentFiscalCodeAttributeAuthority request = new ReferentFiscalCodeAttributeAuthority();
        request.setReferentFiscalCode("AAAAAA00A00A000A");

        ResponseEntity<Void> response = attributeAuthorityService.insertReferent(keyFiscalCode, request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify link exists between organization and referent
        List<AAOrganizationReferentEntity> links = (List<AAOrganizationReferentEntity>) aaOrganizationReferentRepository.findAll();
        boolean linkExists = links.stream()
            .anyMatch(link -> link.getOrganization() != null && link.getReferent() != null
                           && keyFiscalCode.equals(link.getOrganization().getFiscalCode())
                           && "AAAAAA00A00A000A".equals(link.getReferent().getFiscalCode()));
        Assertions.assertTrue(linkExists, "Link between organization and referent must exist");
        
        // Verify referent exists
        Optional<AAReferentEntity> referent = aaReferentRepository.findById("AAAAAA00A00A000A");
        Assertions.assertTrue(referent.isPresent(), "Referent must exist");
    }

    @Test
    void InsertReferent_OrganizationNotFound() {
        ReferentFiscalCodeAttributeAuthority request = new ReferentFiscalCodeAttributeAuthority();
        request.setReferentFiscalCode("AAAAAA00A00A000A");

        ResponseEntity<Void> response = attributeAuthorityService.insertReferent("RSSMRA80A01H501U", request);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void InsertReferent_Duplicate_Ok() {
        String keyFiscalCode = "RSSMRA80A01H501U";
        AAOrganizationEntity organization = new AAOrganizationEntity();
        organization.setFiscalCode(keyFiscalCode);
        organization.setName("Test Org");
        organization.setPec("test@pec.it");
        organization.setInsertedAt(OffsetDateTime.now());
        organization.setOrganizationReferents(new ArrayList<>());
        aaOrganizationRepository.saveAndFlush(organization);

        ReferentFiscalCodeAttributeAuthority request = new ReferentFiscalCodeAttributeAuthority();
        request.setReferentFiscalCode("AAAAAA00A00A000A");

        ResponseEntity<Void> response1 = attributeAuthorityService.insertReferent(keyFiscalCode, request);
        ResponseEntity<Void> response2 = attributeAuthorityService.insertReferent(keyFiscalCode, request);

        Assertions.assertEquals(HttpStatus.OK, response1.getStatusCode());
        Assertions.assertEquals(HttpStatus.OK, response2.getStatusCode());
        
        // Verify only one link exists (duplicate insert should not create second link)
        List<AAOrganizationReferentEntity> links = (List<AAOrganizationReferentEntity>) aaOrganizationReferentRepository.findAll();
        long linkCount = links.stream()
            .filter(link -> link.getOrganization() != null && link.getReferent() != null
                         && keyFiscalCode.equals(link.getOrganization().getFiscalCode())
                         && "AAAAAA00A00A000A".equals(link.getReferent().getFiscalCode()))
            .count();
        Assertions.assertEquals(1, linkCount, "Should have exactly one link after duplicate insert");

        Optional<AAReferentEntity> referent = aaReferentRepository.findById("AAAAAA00A00A000A");
        Assertions.assertTrue(referent.isPresent());
    }
}
