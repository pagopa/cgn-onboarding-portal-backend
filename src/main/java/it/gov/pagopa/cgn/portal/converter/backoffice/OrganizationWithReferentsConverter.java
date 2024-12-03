package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.converter.AbstractAttributeAuthorityConverter;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferents;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.function.Function;

@Component
public class OrganizationWithReferentsConverter
        extends
        AbstractAttributeAuthorityConverter<OrganizationWithReferentsAttributeAuthority, OrganizationWithReferents> {

    protected Function<OrganizationWithReferentsAttributeAuthority, OrganizationWithReferents> fromAttributeAuthorityModel = attributeAuthorityModel -> {
        OrganizationWithReferents backofficeModel = new OrganizationWithReferents();
        backofficeModel.setKeyOrganizationFiscalCode(attributeAuthorityModel.getKeyOrganizationFiscalCode());
        backofficeModel.setOrganizationFiscalCode(attributeAuthorityModel.getOrganizationFiscalCode());
        backofficeModel.setOrganizationName(attributeAuthorityModel.getOrganizationName());
        backofficeModel.setPec(attributeAuthorityModel.getPec());
        backofficeModel.setInsertedAt(attributeAuthorityModel.getInsertedAt().toLocalDateTime().toLocalDate());
        backofficeModel.setReferents(attributeAuthorityModel.getReferents());
        return backofficeModel;
    };
    protected Function<OrganizationWithReferents, OrganizationWithReferentsAttributeAuthority> toAttributeAuthorityModel = backofficeModel -> {
        OrganizationWithReferentsAttributeAuthority attributeAuthorityModel = new OrganizationWithReferentsAttributeAuthority();
        attributeAuthorityModel.setKeyOrganizationFiscalCode(backofficeModel.getKeyOrganizationFiscalCode());
        attributeAuthorityModel.setOrganizationFiscalCode(backofficeModel.getOrganizationFiscalCode());
        attributeAuthorityModel.setOrganizationName(backofficeModel.getOrganizationName());
        attributeAuthorityModel.setPec(backofficeModel.getPec());
        attributeAuthorityModel.setInsertedAt(Timestamp.valueOf(backofficeModel.getInsertedAt().atStartOfDay()));
        attributeAuthorityModel.setReferents(backofficeModel.getReferents());
        return attributeAuthorityModel;
    };

    @Override
    protected Function<OrganizationWithReferentsAttributeAuthority, OrganizationWithReferents> fromAttributeAuthorityModelFunction() {
        return fromAttributeAuthorityModel;
    }

    @Override
    protected Function<OrganizationWithReferents, OrganizationWithReferentsAttributeAuthority> toAttributeAuthorityModelFunction() {
        return toAttributeAuthorityModel;
    }


}
