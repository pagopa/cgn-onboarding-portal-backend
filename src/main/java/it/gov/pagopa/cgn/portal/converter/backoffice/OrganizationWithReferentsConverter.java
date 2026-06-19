package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.converter.AbstractAttributeAuthorityConverter;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferents;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferentsAndStatus;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class OrganizationWithReferentsConverter
        extends
        AbstractAttributeAuthorityConverter<OrganizationWithReferentsAndStatus, OrganizationWithReferents> {


    protected Function<OrganizationWithReferentsAndStatus, OrganizationWithReferents> fromAttributeAuthorityModel = attributeAuthorityModel -> {
        OrganizationWithReferents backofficeModel = new OrganizationWithReferents();
        backofficeModel.setKeyOrganizationFiscalCode(attributeAuthorityModel.getKeyOrganizationFiscalCode());
        backofficeModel.setOrganizationFiscalCode(attributeAuthorityModel.getOrganizationFiscalCode());
        backofficeModel.setOrganizationName(attributeAuthorityModel.getOrganizationName());
        backofficeModel.setPec(attributeAuthorityModel.getPec());
        backofficeModel.setInsertedAt(attributeAuthorityModel.getInsertedAt());
        backofficeModel.setReferents(attributeAuthorityModel.getReferents());
        return backofficeModel;
    };
    protected Function<OrganizationWithReferents, OrganizationWithReferentsAndStatus> toAttributeAuthorityModel = backofficeModel -> {
        OrganizationWithReferentsAndStatus attributeAuthorityModel = new OrganizationWithReferentsAndStatus();
        attributeAuthorityModel.setKeyOrganizationFiscalCode(backofficeModel.getKeyOrganizationFiscalCode());
        attributeAuthorityModel.setOrganizationFiscalCode(backofficeModel.getOrganizationFiscalCode());
        attributeAuthorityModel.setOrganizationName(backofficeModel.getOrganizationName());
        attributeAuthorityModel.setPec(backofficeModel.getPec());
        attributeAuthorityModel.setInsertedAt(backofficeModel.getInsertedAt());
        attributeAuthorityModel.setReferents(backofficeModel.getReferents());
        return attributeAuthorityModel;
    };

    @Override
    protected Function<OrganizationWithReferentsAndStatus, OrganizationWithReferents> fromAttributeAuthorityModelFunction() {
        return fromAttributeAuthorityModel;
    }

    @Override
    protected Function<OrganizationWithReferents, OrganizationWithReferentsAndStatus> toAttributeAuthorityModelFunction() {
        return toAttributeAuthorityModel;
    }
}
