package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.converter.AbstractAttributeAuthorityConverter;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.ReferentFiscalCodeAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ReferentFiscalCode;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ReferentFiscalCodeConverter
        extends AbstractAttributeAuthorityConverter<ReferentFiscalCodeAttributeAuthority, ReferentFiscalCode> {

    protected Function<ReferentFiscalCodeAttributeAuthority, ReferentFiscalCode> fromAttributeAuthorityModel = attributeAuthorityModel -> {
        throw new NotImplementedException();
    };
    protected Function<ReferentFiscalCode, ReferentFiscalCodeAttributeAuthority> toAttributeAuthorityModel = backofficeModel -> {
        ReferentFiscalCodeAttributeAuthority referentFiscalCodeAttributeAuthority = new ReferentFiscalCodeAttributeAuthority();
        referentFiscalCodeAttributeAuthority.setReferentFiscalCode(backofficeModel.getReferentFiscalCode());
        return referentFiscalCodeAttributeAuthority;
    };

    @Override
    protected Function<ReferentFiscalCodeAttributeAuthority, ReferentFiscalCode> fromAttributeAuthorityModelFunction() {
        return fromAttributeAuthorityModel;
    }

    @Override
    protected Function<ReferentFiscalCode, ReferentFiscalCodeAttributeAuthority> toAttributeAuthorityModelFunction() {
        return toAttributeAuthorityModel;
    }


}
