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
        String keyFiscalCode = "RSSMRA80A01H501U";
        AAOrganizationEntity existing = new AAOrganizationEntity();
        existing.setFiscalCode(keyFiscalCode);
        existing.setName("Old Name");
        existing.setPec("old@pec.it");
        existing.setInsertedAt(OffsetDateTime.now());
        aaOrganizationRepository.saveAndFlush(existing);

        OrganizationWithReferentsPostAttributeAuthority request = new OrganizationWithReferentsPostAttributeAuthority();
        request.setKeyOrganizationFiscalCode(keyFiscalCode);
        request.setOrganizationFiscalCode("12345678901234");
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

        Optional<AAOrganizationEntity> updated = aaOrganizationRepository.findById(keyFiscalCode);
        Assertions.assertTrue(updated.isPresent());
        Assertions.assertEquals("Updated Name", updated.get().getName());
    }

    @Test
    void UpsertOrganization_UpdateReferents_Ok() {
        String keyFiscalCode = "RSSMRA80A01H501U";
        AAReferentEntity referent1 = new AAReferentEntity();
        referent1.setFiscalCode("AAAAAA00A00A000A");
        aaReferentRepository.saveAndFlush(referent1);

        AAReferentEntity referent2 = new AAReferentEntity();
        referent2.setFiscalCode("BBBBBB00B00B000B");
        aaReferentRepository.saveAndFlush(referent2);

        AAOrganizationEntity existing = new AAOrganizationEntity();
        existing.setFiscalCode(keyFiscalCode);
        existing.setName("Test Org");
        existing.setPec("test@pec.it");
        existing.setInsertedAt(OffsetDateTime.now());
        aaOrganizationRepository.saveAndFlush(existing);

        OrganizationWithReferentsPostAttributeAuthority request = new OrganizationWithReferentsPostAttributeAuthority();
        request.setKeyOrganizationFiscalCode(keyFiscalCode);
        request.setOrganizationFiscalCode("12345678901234");
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
    }

    @Test
    void UpsertOrganization_EmptyReferents_Ok() {
        String keyFiscalCode = "RSSMRA80A01H501U";
        OrganizationWithReferentsPostAttributeAuthority request = new OrganizationWithReferentsPostAttributeAuthority();
        request.setKeyOrganizationFiscalCode(keyFiscalCode);
        request.setOrganizationFiscalCode("12345678901234");
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
}
