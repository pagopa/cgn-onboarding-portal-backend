package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.converter.AbstractAttributeAuthorityConverter;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsPostAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferents;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class OrganizationWithReferentsPostConverter
        extends
        AbstractAttributeAuthorityConverter<OrganizationWithReferentsPostAttributeAuthority, OrganizationWithReferents> {

    protected Function<OrganizationWithReferentsPostAttributeAuthority, OrganizationWithReferents> fromAttributeAuthorityModel = attributeAuthorityModel -> {
        throw new NotImplementedException();
    };
    protected Function<OrganizationWithReferents, OrganizationWithReferentsPostAttributeAuthority> toAttributeAuthorityModel = backofficeModel -> {
        OrganizationWithReferentsPostAttributeAuthority attributeAuthorityModel = new OrganizationWithReferentsPostAttributeAuthority();
        attributeAuthorityModel.setKeyOrganizationFiscalCode(backofficeModel.getKeyOrganizationFiscalCode());
        attributeAuthorityModel.setOrganizationFiscalCode(backofficeModel.getOrganizationFiscalCode());
        attributeAuthorityModel.setOrganizationName(backofficeModel.getOrganizationName());
        attributeAuthorityModel.setPec(backofficeModel.getPec());
        attributeAuthorityModel.setReferents(backofficeModel.getReferents());
        return attributeAuthorityModel;
    };

    @Override
    protected Function<OrganizationWithReferentsPostAttributeAuthority, OrganizationWithReferents> fromAttributeAuthorityModelFunction() {
        return fromAttributeAuthorityModel;
    }

    @Override
    protected Function<OrganizationWithReferents, OrganizationWithReferentsPostAttributeAuthority> toAttributeAuthorityModelFunction() {
        return toAttributeAuthorityModel;
    }


}
