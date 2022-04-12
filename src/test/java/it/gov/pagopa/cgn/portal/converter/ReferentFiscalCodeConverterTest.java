package it.gov.pagopa.cgn.portal.converter;

import it.gov.pagopa.cgn.portal.converter.backoffice.ReferentFiscalCodeConverter;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.ReferentFiscalCodeAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ReferentFiscalCode;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ReferentFiscalCodeConverterTest {

    private final ReferentFiscalCodeConverter referentFiscalCodeConverter = new ReferentFiscalCodeConverter();

    @Test
    public void Convert_ToAttributeAuthorityModel_Ok() {
        ReferentFiscalCode referentFiscalCode = new ReferentFiscalCode();
        referentFiscalCode.setReferentFiscalCode("AAAAAA00A00A000A");
        ReferentFiscalCodeAttributeAuthority referentFiscalCodeAttributeAuthority =
                referentFiscalCodeConverter.toAttributeAuthorityModel(referentFiscalCode);
        commonAssertions(referentFiscalCode, referentFiscalCodeAttributeAuthority);
    }

    @Test
    public void Convert_FromAttributeAuthorityModel_Ko() {
        ReferentFiscalCodeAttributeAuthority referentFiscalCodeAttributeAuthority =
                new ReferentFiscalCodeAttributeAuthority();
        referentFiscalCodeAttributeAuthority.setReferentFiscalCode("AAAAAA00A00A000A");
        Assertions.assertThrows(NotImplementedException.class,
                                () -> referentFiscalCodeConverter.fromAttributeAuthorityModel(
                                        referentFiscalCodeAttributeAuthority));
    }

    private void commonAssertions(ReferentFiscalCode referentFiscalCode,
                                  ReferentFiscalCodeAttributeAuthority referentFiscalCodeAttributeAuthority) {
        Assert.assertEquals(referentFiscalCode.getReferentFiscalCode(),
                            referentFiscalCodeAttributeAuthority.getReferentFiscalCode());
    }

}
