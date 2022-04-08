package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.converter.AbstractAttributeAuthorityConverter;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Organization;
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
        return toAttributeAuthorityModel;
    }

    protected Function<OrganizationAttributeAuthority, Organization> fromAttributeAuthorityModel = attributeAuthorityModel -> {
        Organization backofficeModel = new Organization();
        backofficeModel.setOrganizationFiscalCode(attributeAuthorityModel.getOrganizationFiscalCode());
        backofficeModel.setOrganizationName(attributeAuthorityModel.getOrganizationName());
        backofficeModel.setPec(attributeAuthorityModel.getPec());
        return backofficeModel;
    };

    protected Function<Organization, OrganizationAttributeAuthority> toAttributeAuthorityModel = backofficeModel -> {
        OrganizationAttributeAuthority attributeAuthorityModel = new OrganizationAttributeAuthority();
        attributeAuthorityModel.setOrganizationFiscalCode(backofficeModel.getOrganizationFiscalCode());
        attributeAuthorityModel.setOrganizationName(backofficeModel.getOrganizationName());
        attributeAuthorityModel.setPec(backofficeModel.getPec());
        return attributeAuthorityModel;
    };


}
