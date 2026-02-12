package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.model.AAOrganizationEntity;
import it.gov.pagopa.cgn.portal.model.AAOrganizationReferentEntity;
import it.gov.pagopa.cgn.portal.model.AAReferentEntity;
import it.gov.pagopa.cgn.portal.repository.AAOrganizationRepository;
import it.gov.pagopa.cgn.portal.repository.AAReferentRepository;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationsAttributeAuthority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class AttributeAuthorityServiceGetOrganizationsTest extends IntegrationAbstractTest {

    @Autowired
    private AttributeAuthorityService attributeAuthorityService;

    @Autowired
    private AAOrganizationRepository aaOrganizationRepository;

    @Autowired
    private AAReferentRepository aaReferentRepository;

    @BeforeEach
    void setUp() {
        aaOrganizationRepository.deleteAll();
        aaReferentRepository.deleteAll();
    }

    @Test
    void testGetOrganizationsNoParams_ReturnsAllOrganizationsWithDefaultPageSize() {
        AAReferentEntity ref1 = createAndSaveReferent("RSSMRA80A01H501U");
        AAReferentEntity ref2 = createAndSaveReferent("VRNGNN85M25L736K");

        createAndSaveOrganization("12345678", "Org One", "org1@pec.it",
                List.of(ref1, ref2));
        createAndSaveOrganization("87654321", "Org Two", "org2@pec.it",
                List.of(ref1));

        ResponseEntity<OrganizationsAttributeAuthority> response = 
                attributeAuthorityService.getOrganizations(null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getCount());
        assertNotNull(response.getBody().getItems());
        assertEquals(2, response.getBody().getCount());
        assertEquals(2, response.getBody().getItems().size());
    }

    @Test
    void testGetOrganizationsByNameSearch_ReturnsPaginatedResults() {
        AAReferentEntity ref = createAndSaveReferent("RSSMRA80A01H501U");
        
        createAndSaveOrganization("11111111", "Alpha Company", "alpha@pec.it", List.of(ref));
        createAndSaveOrganization("22222222", "Beta Organization", "beta@pec.it", List.of(ref));
        createAndSaveOrganization("33333333", "Alpha Beta Corp", "alphabeta@pec.it", List.of(ref));

        ResponseEntity<OrganizationsAttributeAuthority> response = 
                attributeAuthorityService.getOrganizations("Alpha", 0, 10, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getCount());
        assertNotNull(response.getBody().getItems());
        assertEquals(2, response.getBody().getCount());
        assertEquals(2, response.getBody().getItems().size());
    }

    @Test
    void testGetOrganizationsByFiscalCodeSearch_ReturnsOrganizationsWithReferent() {
        AAReferentEntity ref1 = createAndSaveReferent("RSSMRA80A01H501U");
        AAReferentEntity ref2 = createAndSaveReferent("VRNGNN85M25L736K");

        createAndSaveOrganization("11111111", "Org 1", "org1@pec.it", List.of(ref1));
        createAndSaveOrganization("22222222", "Org 2", "org2@pec.it", List.of(ref2));

        ResponseEntity<OrganizationsAttributeAuthority> response = 
                attributeAuthorityService.getOrganizations("RSSMRA80A01H501U", null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getCount());
        assertNotNull(response.getBody().getItems());
        assertEquals(1, response.getBody().getCount());
        assertEquals(1, response.getBody().getItems().size());
        assertNotNull(response.getBody().getItems().getFirst());
        assertEquals("11111111", response.getBody().getItems().getFirst().getOrganizationFiscalCode());
    }

    @Test
    void testGetOrganizationsWithPagination_ReturnsCorrectPage() {
        AAReferentEntity ref = createAndSaveReferent("RSSMRA80A01H501U");

        for (int i = 0; i < 25; i++) {
            createAndSaveOrganization(String.format("%08d", i), "Org " + i, "org" + i + "@pec.it",
                    List.of(ref));
        }

        ResponseEntity<OrganizationsAttributeAuthority> response1 = 
                attributeAuthorityService.getOrganizations(null, 0, 10, null, null);
        ResponseEntity<OrganizationsAttributeAuthority> response2 = 
                attributeAuthorityService.getOrganizations(null, 1, 10, null, null);

        assertNotNull(response1.getBody());
        assertNotNull(response1.getBody().getCount());
        assertNotNull(response1.getBody().getItems());
        assertNotNull(response2.getBody());
        assertNotNull(response2.getBody().getItems());
        assertEquals(25, response1.getBody().getCount());
        assertEquals(10, response1.getBody().getItems().size());
        assertEquals(10, response2.getBody().getItems().size());
    }

    @Test
    void testGetOrganizationsWithSort_ReturnsCorrectlySortedResults() {
        AAReferentEntity ref = createAndSaveReferent("RSSMRA80A01H501U");

        createAndSaveOrganization("33333333", "Charlie Inc", "charlie@pec.it", List.of(ref));
        createAndSaveOrganization("11111111", "Alpha Corp", "alpha@pec.it", List.of(ref));
        createAndSaveOrganization("22222222", "Beta Ltd", "beta@pec.it", List.of(ref));

        ResponseEntity<OrganizationsAttributeAuthority> responseAsc = 
                attributeAuthorityService.getOrganizations(null, null, null, "name", "ASC");
        ResponseEntity<OrganizationsAttributeAuthority> responseDesc = 
                attributeAuthorityService.getOrganizations(null, null, null, "name", "DESC");

        assertNotNull(responseAsc.getBody());
        assertNotNull(responseAsc.getBody().getItems());
        assertNotNull(responseAsc.getBody().getItems().getFirst());
        assertNotNull(responseDesc.getBody());
        assertNotNull(responseDesc.getBody().getItems());
        assertNotNull(responseDesc.getBody().getItems().getFirst());
        assertEquals("Alpha Corp", responseAsc.getBody().getItems().getFirst().getOrganizationName());
        assertEquals("Charlie Inc", responseDesc.getBody().getItems().getFirst().getOrganizationName());
    }

    @Test
    void testGetOrganizationsEmptyResult_ReturnsEmptyListWithZeroCount() {
        ResponseEntity<OrganizationsAttributeAuthority> response = 
                attributeAuthorityService.getOrganizations("nonexistent", null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getCount());
        assertNotNull(response.getBody().getItems());
        assertEquals(0, response.getBody().getCount());
        assertEquals(0, response.getBody().getItems().size());
    }

    @Test
    void testGetOrganizationsResponseStructure_MapsAllFieldsCorrectly() {
        AAReferentEntity ref1 = createAndSaveReferent("RSSMRA80A01H501U");
        AAReferentEntity ref2 = createAndSaveReferent("VRNGNN85M25L736K");

        createAndSaveOrganization("12345678", "Test Org", "test@pec.it", List.of(ref1, ref2));

        ResponseEntity<OrganizationsAttributeAuthority> response = 
                attributeAuthorityService.getOrganizations(null, null, null, null, null);

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getItems());
        OrganizationWithReferentsAttributeAuthority item = response.getBody().getItems().getFirst();
        assertNotNull(item);
        assertNotNull(item.getKeyOrganizationFiscalCode());
        assertNotNull(item.getOrganizationFiscalCode());
        assertNotNull(item.getOrganizationName());
        assertNotNull(item.getPec());
        assertNotNull(item.getInsertedAt());
        assertNotNull(item.getReferents());
        assertEquals("12345678", item.getKeyOrganizationFiscalCode());
        assertEquals("12345678", item.getOrganizationFiscalCode());
        assertEquals("Test Org", item.getOrganizationName());
        assertEquals("test@pec.it", item.getPec());
        assertEquals(2, item.getReferents().size());
        assertTrue(item.getReferents().contains("RSSMRA80A01H501U"));
        assertTrue(item.getReferents().contains("VRNGNN85M25L736K"));
    }

    @Test
    void testGetOrganizationsOnlyWithReferents_ExcludesOrganizationsWithoutReferents() {
        AAReferentEntity ref = createAndSaveReferent("RSSMRA80A01H501U");
        
        createAndSaveOrganization("11111111", "Org With Ref", "withref@pec.it", List.of(ref));
        
        AAOrganizationEntity orgWithoutRef = new AAOrganizationEntity();
        orgWithoutRef.setFiscalCode("99999999");
        orgWithoutRef.setName("Org Without Ref");
        orgWithoutRef.setPec("noref@pec.it");
        orgWithoutRef.setInsertedAt(OffsetDateTime.now());
        orgWithoutRef.setOrganizationReferents(List.of());
        aaOrganizationRepository.save(orgWithoutRef);

        ResponseEntity<OrganizationsAttributeAuthority> response = 
                attributeAuthorityService.getOrganizations(null, null, null, null, null);

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getCount());
        assertNotNull(response.getBody().getItems());
        assertEquals(1, response.getBody().getCount());
        assertEquals(1, response.getBody().getItems().size());
    }

    private AAReferentEntity createAndSaveReferent(String fiscalCode) {
        AAReferentEntity referent = new AAReferentEntity();
        referent.setFiscalCode(fiscalCode);
        referent.setOrganizationReferents(List.of());
        return aaReferentRepository.save(referent);
    }

    private AAOrganizationEntity createAndSaveOrganization(String fiscalCode, String name, String pec, 
                                                           List<AAReferentEntity> referents) {
        AAOrganizationEntity org = new AAOrganizationEntity();
        org.setFiscalCode(fiscalCode);
        org.setName(name);
        org.setPec(pec);
        org.setInsertedAt(OffsetDateTime.now());
        AAOrganizationEntity savedOrg = aaOrganizationRepository.save(org);
        
        if (referents != null && !referents.isEmpty()) {
            List<AAOrganizationReferentEntity> orgReferents = referents.stream()
                    .map(ref -> {
                        AAOrganizationReferentEntity orgRef = new AAOrganizationReferentEntity();
                        orgRef.setOrganization(savedOrg);
                        orgRef.setReferent(ref);
                        return orgRef;
                    })
                    .toList();
            savedOrg.setOrganizationReferents(orgReferents);
        } else {
            savedOrg.setOrganizationReferents(List.of());
        }
        return aaOrganizationRepository.save(savedOrg);
    }
}
