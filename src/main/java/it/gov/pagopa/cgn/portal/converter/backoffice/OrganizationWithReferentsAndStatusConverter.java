package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.converter.AbstractAttributeAuthorityConverter;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationStatus;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferentsAndStatus;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.function.Function;

@Component
public class OrganizationWithReferentsAndStatusConverter
        extends AbstractAttributeAuthorityConverter<OrganizationWithReferentsAttributeAuthority, OrganizationWithReferentsAndStatus> {

    @Override
    protected Function<OrganizationWithReferentsAttributeAuthority, OrganizationWithReferentsAndStatus> fromAttributeAuthorityModelFunction() {
        return fromAttributeAuthorityModel;
    }

    @Override
    protected Function<OrganizationWithReferentsAndStatus, OrganizationWithReferentsAttributeAuthority> toAttributeAuthorityModelFunction() {
        return toAttributeAuthorityModel;
    }

    protected Function<OrganizationWithReferentsAttributeAuthority, OrganizationWithReferentsAndStatus>
            fromAttributeAuthorityModel = attributeAuthorityModel -> {
        OrganizationWithReferentsAndStatus backofficeModel = new OrganizationWithReferentsAndStatus();
        backofficeModel.setKeyOrganizationFiscalCode(attributeAuthorityModel.getKeyOrganizationFiscalCode());
        backofficeModel.setOrganizationFiscalCode(attributeAuthorityModel.getOrganizationFiscalCode());
        backofficeModel.setOrganizationName(attributeAuthorityModel.getOrganizationName());
        backofficeModel.setPec(attributeAuthorityModel.getPec());
        backofficeModel.setInsertedAt(attributeAuthorityModel.getInsertedAt().toLocalDateTime().toLocalDate());
        backofficeModel.setReferents(attributeAuthorityModel.getReferents());
        backofficeModel.setStatus(OrganizationStatus.ENABLED);
        return backofficeModel;
    };

    protected Function<OrganizationWithReferentsAndStatus, OrganizationWithReferentsAttributeAuthority>
            toAttributeAuthorityModel = backofficeModel -> {
        OrganizationWithReferentsAttributeAuthority attributeAuthorityModel =
                new OrganizationWithReferentsAttributeAuthority();
        attributeAuthorityModel.setKeyOrganizationFiscalCode(backofficeModel.getKeyOrganizationFiscalCode());
        attributeAuthorityModel.setOrganizationFiscalCode(backofficeModel.getOrganizationFiscalCode());
        attributeAuthorityModel.setOrganizationName(backofficeModel.getOrganizationName());
        attributeAuthorityModel.setPec(backofficeModel.getPec());
        attributeAuthorityModel.setInsertedAt(Timestamp.valueOf(backofficeModel.getInsertedAt().atStartOfDay()));
        attributeAuthorityModel.setReferents(backofficeModel.getReferents());
        return attributeAuthorityModel;
    };


}
