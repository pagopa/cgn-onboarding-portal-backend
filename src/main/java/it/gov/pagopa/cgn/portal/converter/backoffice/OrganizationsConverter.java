package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.converter.AbstractAttributeAuthorityConverter;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferents;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Organizations;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class OrganizationsConverter extends AbstractAttributeAuthorityConverter<OrganizationsAttributeAuthority, Organizations> {

    private OrganizationWithReferentsConverter organizationWithReferentsConverter;

    @Autowired
    public OrganizationsConverter(OrganizationWithReferentsConverter organizationWithReferentsConverter) {
        this.organizationWithReferentsConverter = organizationWithReferentsConverter;
    }

    @Override
    protected Function<OrganizationsAttributeAuthority, Organizations> fromAttributeAuthorityModelFunction() {
        return fromAttributeAuthorityModel;
    }

    @Override
    protected Function<Organizations, OrganizationsAttributeAuthority> toAttributeAuthorityModelFunction() {
        throw new NotImplementedException();
    }

    protected Function<OrganizationsAttributeAuthority, Organizations> fromAttributeAuthorityModel = attributeAuthorityModel -> {
        Organizations backofficeModel = new Organizations();
        backofficeModel.setItems((List<OrganizationWithReferents>) organizationWithReferentsConverter.fromAttributeAuthorityModelCollection(attributeAuthorityModel.getItems()));
        backofficeModel.setCount(attributeAuthorityModel.getCount());
        return backofficeModel;
    };


}
