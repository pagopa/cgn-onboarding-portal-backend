package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.model.AAOrganizationEntity;
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

        Assertions.assertEquals(2, aaReferentRepository.count());
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
}
