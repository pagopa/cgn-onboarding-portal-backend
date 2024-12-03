package it.gov.pagopa.cgn.portal.converter;

import it.gov.pagopa.cgn.portal.converter.backoffice.OrganizationWithReferentsPostConverter;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsPostAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferents;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
public class OrganizationWithReferentsPostConverterTest {

    private final OrganizationWithReferentsPostConverter organizationWithReferentsPostConverter = new OrganizationWithReferentsPostConverter();

    @Test
    public void Convert_ToAttributeAuthorityModel_Ok() {
        OrganizationWithReferents organizationWithReferents = new OrganizationWithReferents();
        organizationWithReferents.setOrganizationName("org 1");
        organizationWithReferents.setKeyOrganizationFiscalCode("12345678");
        organizationWithReferents.setOrganizationFiscalCode("12345678");
        organizationWithReferents.setPec("12345678@pec.it");
        organizationWithReferents.setInsertedAt(LocalDate.now());
        organizationWithReferents.setReferents(Stream.of("AAAAAA00A00A000A", "BBBBBB00B00B000B")
                                                     .collect(Collectors.toList()));
        organizationWithReferents.setEntityType(EntityType.PRIVATE);
        OrganizationWithReferentsPostAttributeAuthority organizationWithReferentsAttributeAuthority = organizationWithReferentsPostConverter.toAttributeAuthorityModel(
                organizationWithReferents);
        commonAssertions(organizationWithReferents, organizationWithReferentsAttributeAuthority);
    }

    @Test
    public void Convert_FromAttributeAuthorityModel_Ko() {
        OrganizationWithReferentsPostAttributeAuthority organizationWithReferentsPostAttributeAuthority = new OrganizationWithReferentsPostAttributeAuthority();
        organizationWithReferentsPostAttributeAuthority.setOrganizationName("org 1");
        organizationWithReferentsPostAttributeAuthority.setKeyOrganizationFiscalCode("12345678");
        organizationWithReferentsPostAttributeAuthority.setOrganizationFiscalCode("12345678");
        organizationWithReferentsPostAttributeAuthority.setPec("12345678@pec.it");
        organizationWithReferentsPostAttributeAuthority.setReferents(Stream.of("AAAAAA00A00A000A", "BBBBBB00B00B000B")
                                                                           .collect(Collectors.toList()));
        Assertions.assertThrows(NotImplementedException.class,
                                () -> organizationWithReferentsPostConverter.fromAttributeAuthorityModel(
                                        organizationWithReferentsPostAttributeAuthority));
    }

    private void commonAssertions(OrganizationWithReferents organizationWithReferents,
                                  OrganizationWithReferentsPostAttributeAuthority organizationWithReferentsAttributeAuthority) {
        Assert.assertEquals(organizationWithReferents.getOrganizationName(),
                            organizationWithReferentsAttributeAuthority.getOrganizationName());
        Assert.assertEquals(organizationWithReferents.getOrganizationFiscalCode(),
                            organizationWithReferentsAttributeAuthority.getOrganizationFiscalCode());
        Assert.assertEquals(organizationWithReferents.getKeyOrganizationFiscalCode(),
                            organizationWithReferentsAttributeAuthority.getKeyOrganizationFiscalCode());
        Assert.assertEquals(organizationWithReferents.getPec(), organizationWithReferentsAttributeAuthority.getPec());
        Assert.assertEquals(organizationWithReferents.getReferents(),
                            organizationWithReferentsAttributeAuthority.getReferents());
        Assert.assertNotNull(organizationWithReferents.getEntityType());
    }

}
