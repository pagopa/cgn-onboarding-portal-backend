package it.gov.pagopa.cgn.portal.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AttributeAuthorityModelTest {

    // -----------------------------------------------------------------------
    // CompanyAttributeAuthority
    // -----------------------------------------------------------------------

    @Test
    void companyAttributeAuthority_defaultConstructor_setsEmptyFields() {
        CompanyAttributeAuthority company = new CompanyAttributeAuthority();
        assertEquals("", company.getFiscalCode());
        assertEquals("", company.getOrganizationName());
        assertEquals("", company.getPec());
    }

    @Test
    void companyAttributeAuthority_settersAndGetters() {
        CompanyAttributeAuthority company = new CompanyAttributeAuthority();
        company.setFiscalCode("CF123");
        company.setOrganizationName("Org Name");
        company.setPec("pec@example.com");

        assertEquals("CF123", company.getFiscalCode());
        assertEquals("Org Name", company.getOrganizationName());
        assertEquals("pec@example.com", company.getPec());
    }

    @Test
    void companyAttributeAuthority_fluentBuilders_returnSelf() {
        CompanyAttributeAuthority company = new CompanyAttributeAuthority()
                .fiscalCode("CF123")
                .organizationName("Org Name")
                .pec("pec@example.com");

        assertEquals("CF123", company.getFiscalCode());
        assertEquals("Org Name", company.getOrganizationName());
        assertEquals("pec@example.com", company.getPec());
    }

    @Test
    void companyAttributeAuthority_equals_hashCode() {
        CompanyAttributeAuthority a = new CompanyAttributeAuthority()
                .fiscalCode("CF123").organizationName("Org").pec("pec@test.it");
        CompanyAttributeAuthority b = new CompanyAttributeAuthority()
                .fiscalCode("CF123").organizationName("Org").pec("pec@test.it");
        CompanyAttributeAuthority c = new CompanyAttributeAuthority()
                .fiscalCode("OTHER").organizationName("Org").pec("pec@test.it");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(null, a);
        assertNotEquals(new Object(), a);
    }

    @Test
    void companyAttributeAuthority_toString_containsFields() {
        CompanyAttributeAuthority company = new CompanyAttributeAuthority()
                .fiscalCode("CF123").organizationName("Org").pec("pec@test.it");
        String s = company.toString();
        assertTrue(s.contains("CF123"));
        assertTrue(s.contains("Org"));
        assertTrue(s.contains("pec@test.it"));
    }

    // -----------------------------------------------------------------------
    // OrganizationWithReferentsAttributeAuthority
    // -----------------------------------------------------------------------

    @Test
    void orgWithReferents_defaultConstructor_setsEmptyFields() {
        OrganizationWithReferentsAttributeAuthority org = new OrganizationWithReferentsAttributeAuthority();
        assertEquals("", org.getKeyOrganizationFiscalCode());
        assertEquals("", org.getOrganizationFiscalCode());
        assertEquals("", org.getOrganizationName());
        assertEquals("", org.getPec());
        assertEquals("", org.getInsertedAt());
        assertNotNull(org.getReferents());
    }

    @Test
    void orgWithReferents_settersAndGetters() {
        OrganizationWithReferentsAttributeAuthority org = new OrganizationWithReferentsAttributeAuthority();
        org.setKeyOrganizationFiscalCode("KEY01");
        org.setOrganizationFiscalCode("CF456");
        org.setOrganizationName("My Org");
        org.setPec("org@pec.it");
        org.setInsertedAt("2024-01-01T00:00:00Z");
        List<String> refs = Arrays.asList("REF1", "REF2");
        org.setReferents(refs);

        assertEquals("KEY01", org.getKeyOrganizationFiscalCode());
        assertEquals("CF456", org.getOrganizationFiscalCode());
        assertEquals("My Org", org.getOrganizationName());
        assertEquals("org@pec.it", org.getPec());
        assertEquals("2024-01-01T00:00:00Z", org.getInsertedAt());
        assertEquals(refs, org.getReferents());
    }

    @Test
    void orgWithReferents_fluentBuilders_returnSelf() {
        OrganizationWithReferentsAttributeAuthority org = new OrganizationWithReferentsAttributeAuthority()
                .keyOrganizationFiscalCode("KEY01")
                .organizationFiscalCode("CF456")
                .organizationName("My Org")
                .pec("org@pec.it")
                .referents(List.of("REF1"))
                .insertedAt("2024-01-01T00:00:00Z");

        assertEquals("KEY01", org.getKeyOrganizationFiscalCode());
        assertEquals("CF456", org.getOrganizationFiscalCode());
        assertEquals("My Org", org.getOrganizationName());
        assertEquals("org@pec.it", org.getPec());
        assertEquals("2024-01-01T00:00:00Z", org.getInsertedAt());
    }

    @Test
    void orgWithReferents_addReferentsItem() {
        OrganizationWithReferentsAttributeAuthority org = new OrganizationWithReferentsAttributeAuthority();
        org.addReferentsItem("REF1");
        org.addReferentsItem("REF2");
        assertEquals(2, org.getReferents().size());
        assertTrue(org.getReferents().contains("REF1"));
        assertTrue(org.getReferents().contains("REF2"));
    }

    @Test
    void orgWithReferents_equals_hashCode() {
        OrganizationWithReferentsAttributeAuthority a = new OrganizationWithReferentsAttributeAuthority()
                .keyOrganizationFiscalCode("KEY01").organizationFiscalCode("CF456")
                .organizationName("My Org").pec("org@pec.it")
                .referents(Collections.singletonList("REF1")).insertedAt("2024-01-01T00:00:00Z");
        OrganizationWithReferentsAttributeAuthority b = new OrganizationWithReferentsAttributeAuthority()
                .keyOrganizationFiscalCode("KEY01").organizationFiscalCode("CF456")
                .organizationName("My Org").pec("org@pec.it")
                .referents(Collections.singletonList("REF1")).insertedAt("2024-01-01T00:00:00Z");
        OrganizationWithReferentsAttributeAuthority c = new OrganizationWithReferentsAttributeAuthority()
                .keyOrganizationFiscalCode("OTHER").organizationFiscalCode("CF456")
                .organizationName("My Org").pec("org@pec.it")
                .referents(Collections.singletonList("REF1")).insertedAt("2024-01-01T00:00:00Z");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(null, a);
        assertNotEquals(new Object(), a);
    }

    @Test
    void orgWithReferents_toString_containsFields() {
        OrganizationWithReferentsAttributeAuthority org = new OrganizationWithReferentsAttributeAuthority()
                .keyOrganizationFiscalCode("KEY01").organizationFiscalCode("CF456")
                .organizationName("My Org").pec("org@pec.it").insertedAt("2024-01-01T00:00:00Z");
        String s = org.toString();
        assertTrue(s.contains("KEY01"));
        assertTrue(s.contains("CF456"));
        assertTrue(s.contains("My Org"));
        assertTrue(s.contains("org@pec.it"));
        assertTrue(s.contains("2024-01-01T00:00:00Z"));
    }

    // -----------------------------------------------------------------------
    // OrganizationWithReferentsPostAttributeAuthority
    // -----------------------------------------------------------------------

    @Test
    void orgWithReferentsPost_defaultConstructor_setsEmptyFields() {
        OrganizationWithReferentsPostAttributeAuthority org = new OrganizationWithReferentsPostAttributeAuthority();
        assertEquals("", org.getKeyOrganizationFiscalCode());
        assertEquals("", org.getOrganizationFiscalCode());
        assertEquals("", org.getOrganizationName());
        assertEquals("", org.getPec());
        assertNotNull(org.getReferents());
    }

    @Test
    void orgWithReferentsPost_settersAndGetters() {
        OrganizationWithReferentsPostAttributeAuthority org = new OrganizationWithReferentsPostAttributeAuthority();
        org.setKeyOrganizationFiscalCode("KEY02");
        org.setOrganizationFiscalCode("CF789");
        org.setOrganizationName("Post Org");
        org.setPec("post@pec.it");
        List<String> refs = Arrays.asList("R1", "R2");
        org.setReferents(refs);

        assertEquals("KEY02", org.getKeyOrganizationFiscalCode());
        assertEquals("CF789", org.getOrganizationFiscalCode());
        assertEquals("Post Org", org.getOrganizationName());
        assertEquals("post@pec.it", org.getPec());
        assertEquals(refs, org.getReferents());
    }

    @Test
    void orgWithReferentsPost_fluentBuilders_returnSelf() {
        OrganizationWithReferentsPostAttributeAuthority org = new OrganizationWithReferentsPostAttributeAuthority()
                .keyOrganizationFiscalCode("KEY02")
                .organizationFiscalCode("CF789")
                .organizationName("Post Org")
                .pec("post@pec.it")
                .referents(Arrays.asList("R1", "R2"));

        assertEquals("KEY02", org.getKeyOrganizationFiscalCode());
        assertEquals("CF789", org.getOrganizationFiscalCode());
        assertEquals("Post Org", org.getOrganizationName());
        assertEquals("post@pec.it", org.getPec());
        assertEquals(2, org.getReferents().size());
    }

    @Test
    void orgWithReferentsPost_addReferentsItem() {
        OrganizationWithReferentsPostAttributeAuthority org = new OrganizationWithReferentsPostAttributeAuthority();
        org.addReferentsItem("R1");
        org.addReferentsItem("R2");
        assertEquals(2, org.getReferents().size());
        assertTrue(org.getReferents().contains("R1"));
        assertTrue(org.getReferents().contains("R2"));
    }

    @Test
    void orgWithReferentsPost_equals_hashCode() {
        OrganizationWithReferentsPostAttributeAuthority a = new OrganizationWithReferentsPostAttributeAuthority()
                .keyOrganizationFiscalCode("KEY02").organizationFiscalCode("CF789")
                .organizationName("Post Org").pec("post@pec.it")
                .referents(Collections.singletonList("R1"));
        OrganizationWithReferentsPostAttributeAuthority b = new OrganizationWithReferentsPostAttributeAuthority()
                .keyOrganizationFiscalCode("KEY02").organizationFiscalCode("CF789")
                .organizationName("Post Org").pec("post@pec.it")
                .referents(Collections.singletonList("R1"));
        OrganizationWithReferentsPostAttributeAuthority c = new OrganizationWithReferentsPostAttributeAuthority()
                .keyOrganizationFiscalCode("DIFF").organizationFiscalCode("CF789")
                .organizationName("Post Org").pec("post@pec.it")
                .referents(Collections.singletonList("R1"));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(null, a);
        assertNotEquals(new Object(), a);
    }

    @Test
    void orgWithReferentsPost_toString_containsFields() {
        OrganizationWithReferentsPostAttributeAuthority org = new OrganizationWithReferentsPostAttributeAuthority()
                .keyOrganizationFiscalCode("KEY02").organizationFiscalCode("CF789")
                .organizationName("Post Org").pec("post@pec.it");
        String s = org.toString();
        assertTrue(s.contains("KEY02"));
        assertTrue(s.contains("CF789"));
        assertTrue(s.contains("Post Org"));
        assertTrue(s.contains("post@pec.it"));
    }
}
