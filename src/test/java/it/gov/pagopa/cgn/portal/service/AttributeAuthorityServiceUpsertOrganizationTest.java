package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.model.AAOrganizationEntity;
import it.gov.pagopa.cgn.portal.model.AAOrganizationReferentEntity;
import it.gov.pagopa.cgn.portal.model.AAReferentEntity;
import it.gov.pagopa.cgn.portal.repository.AAOrganizationReferentRepository;
import it.gov.pagopa.cgn.portal.repository.AAOrganizationRepository;
import it.gov.pagopa.cgn.portal.repository.AAReferentRepository;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsPostAttributeAuthority;
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
class AttributeAuthorityServiceUpsertOrganizationTest
        extends IntegrationAbstractTest {

    @Autowired
    private AttributeAuthorityService attributeAuthorityService;

    @Autowired
    private AAOrganizationRepository aaOrganizationRepository;

    @Autowired
    private AAOrganizationReferentRepository aaOrganizationReferentRepository;

    @Autowired
    private AAReferentRepository aaReferentRepository;

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
    void UpsertOrganization_Create_Ok() {
        String keyFiscalCode = "12345678901234";
        OrganizationWithReferentsPostAttributeAuthority request = new OrganizationWithReferentsPostAttributeAuthority();
        request.setKeyOrganizationFiscalCode(keyFiscalCode);
        request.setOrganizationFiscalCode(keyFiscalCode);
        request.setOrganizationName("Test Organization");
        request.setPec("test@pec.it");
        request.setReferents(List.of("AAAAAA00A00A000A", "BBBBBB00B00B000B"));

        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response =
                attributeAuthorityService.upsertOrganization(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        OrganizationWithReferentsAttributeAuthority body = response.getBody();
        Assertions.assertEquals(keyFiscalCode, body.getKeyOrganizationFiscalCode());
        Assertions.assertEquals("Test Organization", body.getOrganizationName());
        Assertions.assertEquals("test@pec.it", body.getPec());
        Assertions.assertEquals(2, body.getReferents().size());
        Assertions.assertTrue(body.getReferents().contains("AAAAAA00A00A000A"));
        Assertions.assertTrue(body.getReferents().contains("BBBBBB00B00B000B"));

        Optional<AAOrganizationEntity> savedOrg = aaOrganizationRepository.findById(keyFiscalCode);
        Assertions.assertTrue(savedOrg.isPresent());
        Assertions.assertEquals("Test Organization", savedOrg.get().getName());
    }

    @Test
    void UpsertOrganization_VatKeyFiscalCode_Ok() {
        String keyFiscalCode = "54683216974";
        OrganizationWithReferentsPostAttributeAuthority request = new OrganizationWithReferentsPostAttributeAuthority();
        request.setKeyOrganizationFiscalCode(keyFiscalCode);
        request.setOrganizationFiscalCode("54683216974");
        request.setOrganizationName("Test Organization VAT");
        request.setPec("vat@pec.it");
        request.setReferents(List.of("AAAAAA00A00A000A"));

        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response =
                attributeAuthorityService.upsertOrganization(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(keyFiscalCode, response.getBody().getKeyOrganizationFiscalCode());

        Optional<AAOrganizationEntity> savedOrg = aaOrganizationRepository.findById(keyFiscalCode);
        Assertions.assertTrue(savedOrg.isPresent());
        Assertions.assertEquals("Test Organization VAT", savedOrg.get().getName());
    }

    @Test
    void UpsertOrganization_Update_Ok() {
        String oldKeyFiscalCode = "56345678901231";
        String newKeyFiscalCode = "12345678901234";
        AAOrganizationEntity existing = new AAOrganizationEntity();
        existing.setFiscalCode(oldKeyFiscalCode);
        existing.setName("Old Name");
        existing.setPec("old@pec.it");
        existing.setInsertedAt(OffsetDateTime.now());
        aaOrganizationRepository.saveAndFlush(existing);

        OrganizationWithReferentsPostAttributeAuthority request = new OrganizationWithReferentsPostAttributeAuthority();
        request.setKeyOrganizationFiscalCode(oldKeyFiscalCode);
        request.setOrganizationFiscalCode(newKeyFiscalCode);
        request.setOrganizationName("Updated Name");
        request.setPec("updated@pec.it");
        request.setReferents(List.of("AAAAAA00A00A000A"));

        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response =
                attributeAuthorityService.upsertOrganization(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        OrganizationWithReferentsAttributeAuthority body = response.getBody();
        Assertions.assertEquals("Updated Name", body.getOrganizationName());
        Assertions.assertEquals("updated@pec.it", body.getPec());
        Assertions.assertEquals(1, body.getReferents().size());

        Optional<AAOrganizationEntity> updated = aaOrganizationRepository.findById(newKeyFiscalCode);
        Assertions.assertTrue(updated.isPresent());
        Assertions.assertEquals("Updated Name", updated.get().getName());
    }

    @Test
    void UpsertOrganization_UpdateReferents_Ok() {
        String keyFiscalCode = "RSSMRA80A01H501U";
        
        AAReferentEntity referentA = new AAReferentEntity();
        referentA.setFiscalCode("AAAAAA00A00A000A");
        aaReferentRepository.saveAndFlush(referentA);

        AAReferentEntity referentB = new AAReferentEntity();
        referentB.setFiscalCode("BBBBBB00B00B000B");
        aaReferentRepository.saveAndFlush(referentB);

        AAReferentEntity referentC = new AAReferentEntity();
        referentC.setFiscalCode("CCCCCC00C00C000C");
        aaReferentRepository.saveAndFlush(referentC);

        AAOrganizationEntity existing = new AAOrganizationEntity();
        existing.setFiscalCode(keyFiscalCode);
        existing.setName("Test Org");
        existing.setPec("test@pec.it");
        existing.setInsertedAt(OffsetDateTime.now());
        
        List<AAOrganizationReferentEntity> initialReferents = new ArrayList<>();
        AAOrganizationReferentEntity linkA = new AAOrganizationReferentEntity();
        linkA.setOrganization(existing);
        linkA.setReferent(referentA);
        initialReferents.add(linkA);
        
        AAOrganizationReferentEntity linkB = new AAOrganizationReferentEntity();
        linkB.setOrganization(existing);
        linkB.setReferent(referentB);
        initialReferents.add(linkB);
        
        existing.setOrganizationReferents(initialReferents);
        aaOrganizationRepository.saveAndFlush(existing);

        List<AAOrganizationReferentEntity> linksBefore = (List<AAOrganizationReferentEntity>) aaOrganizationReferentRepository.findAll();
        long countBefore = linksBefore.stream()
                .filter(link -> link.getOrganization() != null
                        && keyFiscalCode.equals(link.getOrganization().getFiscalCode()))
                .count();
        Assertions.assertEquals(2, countBefore, "Should have 2 referents before update");

        OrganizationWithReferentsPostAttributeAuthority request = new OrganizationWithReferentsPostAttributeAuthority();
        request.setKeyOrganizationFiscalCode(keyFiscalCode);
        request.setOrganizationFiscalCode(keyFiscalCode);
        request.setOrganizationName("Test Org");
        request.setPec("test@pec.it");
        request.setReferents(List.of("CCCCCC00C00C000C"));

        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response =
                attributeAuthorityService.upsertOrganization(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        OrganizationWithReferentsAttributeAuthority body = response.getBody();
        Assertions.assertEquals(1, body.getReferents().size());
        Assertions.assertEquals("CCCCCC00C00C000C", body.getReferents().getFirst());

        Optional<AAOrganizationEntity> updated = aaOrganizationRepository.findById(keyFiscalCode);
        Assertions.assertTrue(updated.isPresent());
        
        List<AAOrganizationReferentEntity> linksAfter = (List<AAOrganizationReferentEntity>) aaOrganizationReferentRepository.findAll();
        long countAfter = linksAfter.stream()
                .filter(link -> link.getOrganization() != null
                        && keyFiscalCode.equals(link.getOrganization().getFiscalCode()))
                .count();
        Assertions.assertEquals(1, countAfter, "Should have 1 referent after update");
        boolean hasC = linksAfter.stream()
                .anyMatch(link -> link.getOrganization() != null
                        && keyFiscalCode.equals(link.getOrganization().getFiscalCode())
                        && link.getReferent() != null
                        && "CCCCCC00C00C000C".equals(link.getReferent().getFiscalCode()));
        Assertions.assertTrue(hasC, "Referent should be linked");
        
        boolean hasA = linksAfter.stream()
                .anyMatch(link -> link.getOrganization() != null
                        && keyFiscalCode.equals(link.getOrganization().getFiscalCode())
                        && link.getReferent() != null
                        && "AAAAAA00A00A000A".equals(link.getReferent().getFiscalCode()));
        Assertions.assertFalse(hasA, "Referent A should no longer be linked");
        
        boolean hasB = linksAfter.stream()
                .anyMatch(link -> link.getOrganization() != null
                        && keyFiscalCode.equals(link.getOrganization().getFiscalCode())
                        && link.getReferent() != null
                        && "BBBBBB00B00B000B".equals(link.getReferent().getFiscalCode()));
        Assertions.assertFalse(hasB, "Referent B should no longer be linked");
    }

    @Test
    void UpsertOrganization_EmptyReferents_Ok() {
        String keyFiscalCode = "RSSMRA80A01H501U";
        OrganizationWithReferentsPostAttributeAuthority request = new OrganizationWithReferentsPostAttributeAuthority();
        request.setKeyOrganizationFiscalCode(keyFiscalCode);
        request.setOrganizationFiscalCode(keyFiscalCode);
        request.setOrganizationName("Test Organization");
        request.setPec("test@pec.it");
        request.setReferents(List.of());

        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response =
                attributeAuthorityService.upsertOrganization(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        OrganizationWithReferentsAttributeAuthority body = response.getBody();
        Assertions.assertEquals(0, body.getReferents().size());

        Optional<AAOrganizationEntity> savedOrg = aaOrganizationRepository.findById(keyFiscalCode);
        Assertions.assertTrue(savedOrg.isPresent());
    }

    @Test
    void UpsertOrganization_CreateWithExistingReferents_Ok() {
        AAReferentEntity referent1 = new AAReferentEntity();
        referent1.setFiscalCode("AAAAAA00A00A000A");
        aaReferentRepository.saveAndFlush(referent1);

        AAReferentEntity referent2 = new AAReferentEntity();
        referent2.setFiscalCode("BBBBBB00B00B000B");
        aaReferentRepository.saveAndFlush(referent2);

        String keyFiscalCode = "RSSMRA80A01H501U";
        OrganizationWithReferentsPostAttributeAuthority request = new OrganizationWithReferentsPostAttributeAuthority();
        request.setKeyOrganizationFiscalCode(keyFiscalCode);
        request.setOrganizationFiscalCode("12345678901234");
        request.setOrganizationName("Test Organization");
        request.setPec("test@pec.it");
        request.setReferents(List.of("AAAAAA00A00A000A", "BBBBBB00B00B000B"));

        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response =
                attributeAuthorityService.upsertOrganization(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(2, response.getBody().getReferents().size());

        // Verify both referents exist
        Assertions.assertTrue(aaReferentRepository.findById("AAAAAA00A00A000A").isPresent(), "Referent A must exist");
        Assertions.assertTrue(aaReferentRepository.findById("BBBBBB00B00B000B").isPresent(), "Referent B must exist");
    }

    @Test
    void UpsertOrganization_CreateNewReferent_Ok() {
        String newReferentFiscalCode = "NEWREF0A00A000AB";

        String keyFiscalCode = "RSSMRA80A01H501U";
        OrganizationWithReferentsPostAttributeAuthority request = new OrganizationWithReferentsPostAttributeAuthority();
        request.setKeyOrganizationFiscalCode(keyFiscalCode);
        request.setOrganizationFiscalCode("12345678901234");
        request.setOrganizationName("Test Organization");
        request.setPec("test@pec.it");
        request.setReferents(List.of(newReferentFiscalCode));

        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response =
                attributeAuthorityService.upsertOrganization(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(1, response.getBody().getReferents().size());

        Optional<AAReferentEntity> createdReferent = aaReferentRepository.findById(newReferentFiscalCode);
        Assertions.assertTrue(createdReferent.isPresent());
    }

    @Test
    void UpsertOrganization_AddReferentToExisting_PreservesExistingLinks() {
        String keyFiscalCode = "RSSMRA80A01H501U";
        
        AAReferentEntity referentA = new AAReferentEntity();
        referentA.setFiscalCode("AAAAAA00A00A000A");
        aaReferentRepository.saveAndFlush(referentA);

        AAOrganizationEntity organization = new AAOrganizationEntity();
        organization.setFiscalCode(keyFiscalCode);
        organization.setName("Test Org");
        organization.setPec("test@pec.it");
        organization.setInsertedAt(OffsetDateTime.now());
        aaOrganizationRepository.saveAndFlush(organization);

        OrganizationWithReferentsPostAttributeAuthority request1 = new OrganizationWithReferentsPostAttributeAuthority();
        request1.setKeyOrganizationFiscalCode(keyFiscalCode);
        request1.setOrganizationFiscalCode(keyFiscalCode);
        request1.setOrganizationName("Test Org");
        request1.setPec("test@pec.it");
        request1.setReferents(List.of("AAAAAA00A00A000A"));

        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response1 =
                attributeAuthorityService.upsertOrganization(request1);
        Assertions.assertEquals(HttpStatus.OK, response1.getStatusCode());

        List<AAOrganizationReferentEntity> linksAfterFirst = (List<AAOrganizationReferentEntity>) aaOrganizationReferentRepository.findAll();
        OffsetDateTime linkACreatedAt = linksAfterFirst.stream()
                .filter(link -> link.getReferent() != null 
                        && "AAAAAA00A00A000A".equals(link.getReferent().getFiscalCode()))
                .findFirst()
                .orElseThrow()
                .getCreatedAt();

        AAReferentEntity referentB = new AAReferentEntity();
        referentB.setFiscalCode("BBBBBB00B00B000B");
        aaReferentRepository.saveAndFlush(referentB);

        OrganizationWithReferentsPostAttributeAuthority request2 = new OrganizationWithReferentsPostAttributeAuthority();
        request2.setKeyOrganizationFiscalCode(keyFiscalCode);
        request2.setOrganizationFiscalCode(keyFiscalCode);
        request2.setOrganizationName("Test Org");
        request2.setPec("test@pec.it");
        request2.setReferents(List.of("AAAAAA00A00A000A", "BBBBBB00B00B000B"));

        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response2 =
                attributeAuthorityService.upsertOrganization(request2);

        Assertions.assertEquals(HttpStatus.OK, response2.getStatusCode());
        Assertions.assertNotNull(response2.getBody());
        Assertions.assertEquals(2, response2.getBody().getReferents().size());
        Assertions.assertTrue(response2.getBody().getReferents().contains("AAAAAA00A00A000A"));
        Assertions.assertTrue(response2.getBody().getReferents().contains("BBBBBB00B00B000B"));

        List<AAOrganizationReferentEntity> linksAfterSecond = (List<AAOrganizationReferentEntity>) aaOrganizationReferentRepository.findAll();
        OffsetDateTime linkACreatedAtAfter = linksAfterSecond.stream()
                .filter(link -> link.getReferent() != null 
                        && "AAAAAA00A00A000A".equals(link.getReferent().getFiscalCode()))
                .findFirst()
                .orElseThrow()
                .getCreatedAt();

        Assertions.assertEquals(linkACreatedAt, linkACreatedAtAfter, 
                "Link A's createdAt should be preserved, not recreated");
        
        Assertions.assertTrue(linksAfterSecond.stream()
                .anyMatch(link -> link.getReferent() != null 
                        && "BBBBBB00B00B000B".equals(link.getReferent().getFiscalCode())));
    }

    @Test
    void UpsertOrganization_ChangeFiscalCode_ShouldUpdateFiscalCode() {
        // Create organization with initial fiscal code
        String oldFiscalCode = "54798975426";
        AAOrganizationEntity existing = new AAOrganizationEntity();
        existing.setFiscalCode(oldFiscalCode);
        existing.setName("Original Name");
        existing.setPec("old@pec.it");
        existing.setInsertedAt(OffsetDateTime.now());
        existing.setOrganizationReferents(new ArrayList<>());
        aaOrganizationRepository.saveAndFlush(existing);
        
        // Verify organization exists with old fiscal code
        Assertions.assertTrue(aaOrganizationRepository.findById(oldFiscalCode).isPresent());
        
        // Update organization with new fiscal code
        String newFiscalCode = "54798975429";
        OrganizationWithReferentsPostAttributeAuthority request = new OrganizationWithReferentsPostAttributeAuthority();
        request.setKeyOrganizationFiscalCode(oldFiscalCode);  // Search with old PK
        request.setOrganizationFiscalCode(newFiscalCode);      // Update to new PK
        request.setOrganizationName("Test AA 2");
        request.setPec("pec@pec.it");
        request.setReferents(List.of("ISPXNB32R82Y766D"));

        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response =
                attributeAuthorityService.upsertOrganization(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        
        // Verify response contains new organizationFiscalCode
        Assertions.assertEquals(newFiscalCode, response.getBody().getOrganizationFiscalCode(),
                "Response should return new organizationFiscalCode");
        Assertions.assertEquals("Test AA 2", response.getBody().getOrganizationName());
        Assertions.assertEquals("pec@pec.it", response.getBody().getPec());
        
        // Verify old fiscal code no longer exists
        Assertions.assertTrue(aaOrganizationRepository.findById(oldFiscalCode).isEmpty(),
                "Organization with old fiscal code should no longer exist");
        
        // Verify organization exists with new fiscal code
        Optional<AAOrganizationEntity> updatedOrg = aaOrganizationRepository.findById(newFiscalCode);
        Assertions.assertTrue(updatedOrg.isPresent(), 
                "Organization should exist with new fiscal code");
        Assertions.assertEquals("Test AA 2", updatedOrg.get().getName());
        Assertions.assertEquals("pec@pec.it", updatedOrg.get().getPec());
    }

    @Test
    void UpsertOrganization_UpdateWithoutChangingPK_Ok() {
        String fiscalCode = "12345678901234";
        AAOrganizationEntity existing = new AAOrganizationEntity();
        existing.setFiscalCode(fiscalCode);
        existing.setName("Original Name");
        existing.setPec("original@pec.it");
        existing.setInsertedAt(OffsetDateTime.now());
        existing.setOrganizationReferents(new ArrayList<>());
        aaOrganizationRepository.saveAndFlush(existing);
        
        OrganizationWithReferentsPostAttributeAuthority request = new OrganizationWithReferentsPostAttributeAuthority();
        request.setKeyOrganizationFiscalCode(fiscalCode);
        request.setOrganizationFiscalCode(fiscalCode);
        request.setOrganizationName("Updated Name");
        request.setPec("updated@pec.it");
        request.setReferents(List.of("AAAAAA00A00A000A"));

        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response =
                attributeAuthorityService.upsertOrganization(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        
        Assertions.assertEquals(fiscalCode, response.getBody().getOrganizationFiscalCode(),
                "Fiscal code should remain unchanged");
        Assertions.assertEquals("Updated Name", response.getBody().getOrganizationName());
        Assertions.assertEquals("updated@pec.it", response.getBody().getPec());
        Assertions.assertEquals(1, response.getBody().getReferents().size());
        
        Optional<AAOrganizationEntity> updated = aaOrganizationRepository.findById(fiscalCode);
        Assertions.assertTrue(updated.isPresent(), 
                "Organization should still exist with same fiscal code");
        Assertions.assertEquals("Updated Name", updated.get().getName(),
                "Name should be updated");
        Assertions.assertEquals("updated@pec.it", updated.get().getPec(),
                "PEC should be updated");
        
        List<AAOrganizationReferentEntity> links = (List<AAOrganizationReferentEntity>) aaOrganizationReferentRepository.findAll();
        boolean hasReferent = links.stream()
                .anyMatch(link -> link.getOrganization() != null
                        && fiscalCode.equals(link.getOrganization().getFiscalCode())
                        && link.getReferent() != null
                        && "AAAAAA00A00A000A".equals(link.getReferent().getFiscalCode()));
        Assertions.assertTrue(hasReferent, "Referent should be linked to organization");
    }
}
