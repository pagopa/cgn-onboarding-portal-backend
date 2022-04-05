package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.converter.AbstractAttributeAuthorityConverter;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Organization;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class OrganizationConverter extends AbstractAttributeAuthorityConverter<OrganizationAttributeAuthority, Organization> {

    @Override
    protected Function<OrganizationAttributeAuthority, Organization> fromAttributeAuthorityModelFunction() {
        return fromAttributeAuthorityModel;
    }

    @Override
    protected Function<Organization, OrganizationAttributeAuthority> toAttributeAuthorityModelFunction() {
        throw new NotImplementedException();
    }

    protected Function<OrganizationAttributeAuthority, Organization> fromAttributeAuthorityModel = attributeAuthorityModel -> {
        Organization backofficeModel = new Organization();
        backofficeModel.setOrganizationFiscalCode(attributeAuthorityModel.getOrganizationFiscalCode());
        backofficeModel.setOrganizationName(attributeAuthorityModel.getOrganizationName());
        backofficeModel.setPec(attributeAuthorityModel.getPec());
        return backofficeModel;
    };


}
