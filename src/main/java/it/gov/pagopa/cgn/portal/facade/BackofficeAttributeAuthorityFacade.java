package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.converter.backoffice.*;
import it.gov.pagopa.cgn.portal.model.AgreementUserEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.service.AgreementUserService;
import it.gov.pagopa.cgn.portal.service.AttributeAuthorityService;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferents;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.OrganizationWithReferentsAndStatus;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Organizations;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ReferentFiscalCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.function.BiConsumer;

@Component
public class BackofficeAttributeAuthorityFacade {

    private final AttributeAuthorityService attributeAuthorityService;

    private AgreementUserService agreementUserService;

    private ProfileService profileService;

    private final OrganizationsConverter organizationsConverter;

    private final OrganizationWithReferentsConverter organizationWithReferentsConverter;

    private final OrganizationWithReferentsAndStatusConverter organizationWithReferentsAndStatusConverter;

    private final OrganizationWithReferentsPostConverter organizationWithReferentsPostConverter;

    private final ReferentFiscalCodeConverter referentFiscalCodeConverter;

    public ResponseEntity<Organizations> getOrganizations(String searchQuery,
                                                          Integer page,
                                                          Integer pageSize,
                                                          String sortBy,
                                                          String sortDirection) {
        return organizationsConverter.fromAttributeAuthorityResponse(attributeAuthorityService.getOrganizations(
                searchQuery,
                page,
                pageSize,
                sortBy,
                sortDirection));
    }

    public ResponseEntity<OrganizationWithReferentsAndStatus> getOrganization(String keyOrganizationFiscalCode) {
        return organizationWithReferentsAndStatusConverter.fromAttributeAuthorityResponse(attributeAuthorityService.getOrganization(
                keyOrganizationFiscalCode));
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponseEntity<OrganizationWithReferents> upsertOrganization(OrganizationWithReferents organizationWithReferents) {
        // find agreement for this organization and apply an update consumer
        agreementUserService.findCurrentAgreementUser(organizationWithReferents.getKeyOrganizationFiscalCode())
                            .ifPresent(agreementUserEntity -> updateAgreementUserAndProfileConsumer.accept(
                                    agreementUserEntity,
                                    organizationWithReferents));

        // we upsert into attribute authority only after the db has been updated successfully
        // if attribute authority fails then the db transaction would be rolled back
        ResponseEntity<OrganizationWithReferentsAttributeAuthority> updatedOrganizationWithReferentsAttributeAuthority =
                attributeAuthorityService.upsertOrganization(organizationWithReferentsPostConverter.toAttributeAuthorityModel(
                        organizationWithReferents));

        return organizationWithReferentsConverter.fromAttributeAuthorityResponse(
                updatedOrganizationWithReferentsAttributeAuthority);
    }

    public ResponseEntity<Void> deleteOrganization(String keyOrganizationFiscalCode) {
        return attributeAuthorityService.deleteOrganization(keyOrganizationFiscalCode);
    }

    public ResponseEntity<List<String>> getReferents(String keyOrganizationFiscalCode) {
        return attributeAuthorityService.getReferents(keyOrganizationFiscalCode);
    }

    public ResponseEntity<Void> insertReferent(String keyOrganizationFiscalCode,
                                               ReferentFiscalCode referentFiscalCode) {
        return attributeAuthorityService.insertReferent(keyOrganizationFiscalCode,
                                                        referentFiscalCodeConverter.toAttributeAuthorityModel(
                                                                referentFiscalCode));
    }

    public ResponseEntity<Void> deleteReferent(String keyOrganizationFiscalCode, String referentFiscalCode) {
        return attributeAuthorityService.deleteReferent(keyOrganizationFiscalCode, referentFiscalCode);
    }

    @Autowired
    public BackofficeAttributeAuthorityFacade(AttributeAuthorityService attributeAuthorityService,
                                              AgreementUserService agreementUserService,
                                              ProfileService profileService,
                                              OrganizationsConverter organizationsConverter,
                                              OrganizationWithReferentsConverter organizationWithReferentsConverter,
                                              OrganizationWithReferentsAndStatusConverter organizationWithReferentsAndStatusConverter,
                                              OrganizationWithReferentsPostConverter organizationWithReferentsPostConverter,
                                              ReferentFiscalCodeConverter referentFiscalCodeConverter) {
        this.attributeAuthorityService = attributeAuthorityService;
        this.agreementUserService = agreementUserService;
        this.profileService = profileService;
        this.organizationsConverter = organizationsConverter;
        this.organizationWithReferentsConverter = organizationWithReferentsConverter;
        this.organizationWithReferentsAndStatusConverter = organizationWithReferentsAndStatusConverter;
        this.organizationWithReferentsPostConverter = organizationWithReferentsPostConverter;
        this.referentFiscalCodeConverter = referentFiscalCodeConverter;
    }

    private final BiConsumer<AgreementUserEntity, OrganizationWithReferents> updateAgreementUserAndProfileConsumer =
            (agreementUserEntity, organizationWithReferents) -> {
                // update AgreementUser if merchant tax code has changed
                if (!organizationWithReferents.getKeyOrganizationFiscalCode()
                                              .equals(organizationWithReferents.getOrganizationFiscalCode())) {
                    agreementUserService.updateMerchantTaxCode(agreementUserEntity.getAgreementId(),
                                                               organizationWithReferents.getOrganizationFiscalCode());
                }

                // get and update profile
                ProfileEntity profile = profileService.getProfileFromAgreementId(agreementUserEntity.getAgreementId());
                profile.setFullName(organizationWithReferents.getOrganizationName());
                profile.setTaxCodeOrVat(organizationWithReferents.getOrganizationFiscalCode());
                profileService.updateProfile(agreementUserEntity.getAgreementId(), profile);
            };
}
