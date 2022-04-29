package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.converter.backoffice.*;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.AgreementUserEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.service.AgreementService;
import it.gov.pagopa.cgn.portal.service.AgreementUserService;
import it.gov.pagopa.cgn.portal.service.AttributeAuthorityService;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Component
public class BackofficeAttributeAuthorityFacade {

    private final AttributeAuthorityService attributeAuthorityService;

    private AgreementService agreementService;

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
        ResponseEntity<Organizations> response = organizationsConverter.fromAttributeAuthorityResponse(
                attributeAuthorityService.getOrganizations(searchQuery, page, pageSize, sortBy, sortDirection));

        getOrganizationsAgreementAndMapStatus.accept(response);

        return response;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponseEntity<OrganizationWithReferentsAndStatus> getOrganization(String keyOrganizationFiscalCode) {
        ResponseEntity<OrganizationWithReferentsAndStatus> response
                = organizationWithReferentsAndStatusConverter.fromAttributeAuthorityResponse(attributeAuthorityService.getOrganization(
                keyOrganizationFiscalCode));
        getOrganizationAgreementAndMapStatus.accept(response);
        return response;
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
        ResponseEntity<OrganizationWithReferentsAttributeAuthority> updatedOrganizationWithReferentsAttributeAuthority
                = attributeAuthorityService.upsertOrganization(organizationWithReferentsPostConverter.toAttributeAuthorityModel(
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
                                              AgreementService agreementService,
                                              AgreementUserService agreementUserService,
                                              ProfileService profileService,
                                              OrganizationsConverter organizationsConverter,
                                              OrganizationWithReferentsConverter organizationWithReferentsConverter,
                                              OrganizationWithReferentsAndStatusConverter organizationWithReferentsAndStatusConverter,
                                              OrganizationWithReferentsPostConverter organizationWithReferentsPostConverter,
                                              ReferentFiscalCodeConverter referentFiscalCodeConverter) {
        this.attributeAuthorityService = attributeAuthorityService;
        this.agreementService = agreementService;
        this.agreementUserService = agreementUserService;
        this.profileService = profileService;
        this.organizationsConverter = organizationsConverter;
        this.organizationWithReferentsConverter = organizationWithReferentsConverter;
        this.organizationWithReferentsAndStatusConverter = organizationWithReferentsAndStatusConverter;
        this.organizationWithReferentsPostConverter = organizationWithReferentsPostConverter;
        this.referentFiscalCodeConverter = referentFiscalCodeConverter;
    }

    private final BiConsumer<AgreementUserEntity, OrganizationWithReferents> updateAgreementUserAndProfileConsumer
            = (agreementUserEntity, organizationWithReferents) -> {
        // update AgreementUser if merchant tax code has changed
        if (!organizationWithReferents.getKeyOrganizationFiscalCode()
                                      .equals(organizationWithReferents.getOrganizationFiscalCode())) {
            agreementUserService.updateMerchantTaxCode(agreementUserEntity.getAgreementId(),
                                                       organizationWithReferents.getOrganizationFiscalCode());
        }

        // get and update profile if present
        Optional<ProfileEntity> maybeProfile
                = profileService.getOptProfileFromAgreementId(agreementUserEntity.getAgreementId());
        if (maybeProfile.isPresent()) {
            ProfileEntity p = maybeProfile.get();
            p.setFullName(organizationWithReferents.getOrganizationName());
            p.setTaxCodeOrVat(organizationWithReferents.getOrganizationFiscalCode());
            profileService.updateProfile(agreementUserEntity.getAgreementId(), p);
        }
    };

    private final BiConsumer<AgreementEntity, OrganizationWithReferentsAndStatus> mapStatus
            = (agreement, organization) -> {
        switch (agreement.getState()) {
            case DRAFT:
            case REJECTED:
                organization.setStatus(OrganizationStatus.DRAFT);
                break;
            case PENDING:
                organization.setStatus(OrganizationStatus.PENDING);
                break;
            case APPROVED:
                organization.setStatus(OrganizationStatus.ACTIVE);
                break;
            default:
                break;
        }
    };

    private final Consumer<OrganizationWithReferentsAndStatus> mapOrganizationStatus
            = organization -> agreementUserService.findCurrentAgreementUser(organization.getKeyOrganizationFiscalCode())
                                                  .flatMap(agreementUserEntity -> agreementService.getById(
                                                          agreementUserEntity.getAgreementId()))
                                                  .ifPresent(a -> mapStatus.accept(a, organization));

    private final Consumer<ResponseEntity<OrganizationWithReferentsAndStatus>> getOrganizationAgreementAndMapStatus
            = response -> {
        if (HttpStatus.OK.equals(response.getStatusCode()) && response.getBody() != null) {
            mapOrganizationStatus.accept(response.getBody());
        }
    };

    private final Consumer<Collection<OrganizationWithReferentsAndStatus>> mapOrganizationsStatus
            = organizations -> organizations.forEach(mapOrganizationStatus);

    private final Consumer<ResponseEntity<Organizations>> getOrganizationsAgreementAndMapStatus = response -> {
        if (HttpStatus.OK.equals(response.getStatusCode()) && response.getBody() != null) {
            mapOrganizationsStatus.accept(response.getBody().getItems());
        }
    };

}
