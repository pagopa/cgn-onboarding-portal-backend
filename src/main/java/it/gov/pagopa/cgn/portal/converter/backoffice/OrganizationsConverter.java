package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.converter.AbstractAttributeAuthorityConverter;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferentsAndStatus;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Organizations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class OrganizationsConverter
        extends AbstractAttributeAuthorityConverter<OrganizationsAttributeAuthority, Organizations> {

    private OrganizationWithReferentsAndStatusConverter organizationWithReferentsAndStatusConverter;

    @Autowired
    public OrganizationsConverter(OrganizationWithReferentsAndStatusConverter organizationWithReferentsAndStatusConverter) {
        this.organizationWithReferentsAndStatusConverter = organizationWithReferentsAndStatusConverter;
    }

    @Override
    protected Function<OrganizationsAttributeAuthority, Organizations> fromAttributeAuthorityModelFunction() {
        return fromAttributeAuthorityModel;
    }

    @Override
    protected Function<Organizations, OrganizationsAttributeAuthority> toAttributeAuthorityModelFunction() {
        return toAttributeAuthorityModel;
    }

    protected Function<OrganizationsAttributeAuthority, Organizations> fromAttributeAuthorityModel =
            attributeAuthorityModel -> {
                Organizations backofficeModel = new Organizations();
                backofficeModel.setItems((List<OrganizationWithReferentsAndStatus>) organizationWithReferentsAndStatusConverter.fromAttributeAuthorityModelCollection(
                        attributeAuthorityModel.getItems()));
                backofficeModel.setCount(attributeAuthorityModel.getCount());
                return backofficeModel;
            };

    protected Function<Organizations, OrganizationsAttributeAuthority> toAttributeAuthorityModel = backofficeModel -> {
        OrganizationsAttributeAuthority attributeAuthorityModel = new OrganizationsAttributeAuthority();
        attributeAuthorityModel.setCount(backofficeModel.getCount());
        attributeAuthorityModel.setItems((List<OrganizationWithReferentsAttributeAuthority>) organizationWithReferentsAndStatusConverter.toAttributeAuthorityModelCollection(
                backofficeModel.getItems()));
        return attributeAuthorityModel;
    };


}
