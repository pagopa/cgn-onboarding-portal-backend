package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.converter.backoffice.*;
import it.gov.pagopa.cgn.portal.enums.EntityTypeEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.AgreementUserEntity;
import it.gov.pagopa.cgn.portal.service.*;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.*;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.*;
import it.gov.pagopa.cgnonboardingportal.model.ErrorCodeEnum;
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

    private final BackofficeAgreementConverter agreementConverter;

    public ResponseEntity<Organizations> getOrganizations(String searchQuery, Integer page, Integer pageSize, String sortBy, String sortDirection) {

        ResponseEntity<Organizations> response = organizationsConverter.fromAttributeAuthorityResponse(attributeAuthorityService.getOrganizations(searchQuery, page, pageSize, sortBy, sortDirection));

        getOrganizationsAgreementAndMapStatus.accept(response);

        return response;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponseEntity<OrganizationWithReferentsAndStatus> getOrganization(String keyOrganizationFiscalCode) {

        ResponseEntity<OrganizationWithReferentsAndStatus> response = organizationWithReferentsAndStatusConverter.fromAttributeAuthorityResponse(attributeAuthorityService.getOrganization(keyOrganizationFiscalCode));

        getOrganizationAgreementAndMapStatus.accept(response);
        return response;
    }

    private final Consumer<OrganizationWithReferents> createAgreementIfNotExistsConsumer = owr -> agreementService.createAgreementIfNotExists(owr.getOrganizationFiscalCode(), owr.getEntityType(), owr.getOrganizationName());

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponseEntity<OrganizationWithReferents> upsertOrganization(OrganizationWithReferents organizationWithReferents) {
        // check if we have more than 10 companies for each referent
        organizationWithReferents.getReferents().forEach(referentFiscalCode -> {
            if (attributeAuthorityService.countUserOrganizations(referentFiscalCode) > 10)
                throw new InvalidRequestException(ErrorCodeEnum.CANNOT_BIND_MORE_THAN_TEN_ORGANIZATIONS.getValue());
        });

        //workaround fino al rilascio flusso pagopa
        organizationWithReferents.setEntityType(organizationWithReferents.getEntityType() == null ? EntityType.PRIVATE : organizationWithReferents.getEntityType());

        // find agreement for this organization and apply an update consumer
        Optional<AgreementUserEntity> maybeAgreementUserEntity = agreementUserService.findCurrentAgreementUser(organizationWithReferents.getKeyOrganizationFiscalCode());
        if (maybeAgreementUserEntity.isPresent()) {
            updateAgreementUserProfileAndAgreement.accept(maybeAgreementUserEntity.get(), organizationWithReferents);
        } else {
            createAgreementIfNotExistsConsumer.accept(organizationWithReferents);
        }

        // we upsert into attribute authority only after the db has been updated successfully
        // if attribute authority fails then the db transaction would be rolled back
        ResponseEntity<OrganizationWithReferentsAttributeAuthority> updatedOrganizationWithReferentsAttributeAuthority = attributeAuthorityService.upsertOrganization(organizationWithReferentsPostConverter.toAttributeAuthorityModel(organizationWithReferents));

        ResponseEntity<OrganizationWithReferents> response = organizationWithReferentsConverter.fromAttributeAuthorityResponse(updatedOrganizationWithReferentsAttributeAuthority);

        OrganizationWithReferents respBody = response.getBody();
        if (respBody != null && response.getStatusCode() == HttpStatus.OK) {

            OrganizationWithReferents newRespBody = new OrganizationWithReferents();

            newRespBody.setEntityType(organizationWithReferents.getEntityType());
            newRespBody.setKeyOrganizationFiscalCode(respBody.getKeyOrganizationFiscalCode());
            newRespBody.setOrganizationFiscalCode(respBody.getOrganizationFiscalCode());
            newRespBody.setOrganizationName(respBody.getOrganizationName());
            newRespBody.setPec(respBody.getPec());
            newRespBody.setInsertedAt(respBody.getInsertedAt());
            newRespBody.setReferents(respBody.getReferents());

            return ResponseEntity.ok().body(newRespBody);
        }

        return response;

    }

    public ResponseEntity<Void> deleteOrganization(String keyOrganizationFiscalCode) {

        return attributeAuthorityService.deleteOrganization(keyOrganizationFiscalCode);
    }

    public ResponseEntity<List<String>> getReferents(String keyOrganizationFiscalCode) {

        return attributeAuthorityService.getReferents(keyOrganizationFiscalCode);
    }

    public ResponseEntity<Void> insertReferent(String keyOrganizationFiscalCode, ReferentFiscalCode referentFiscalCode) {

        return attributeAuthorityService.insertReferent(keyOrganizationFiscalCode, referentFiscalCodeConverter.toAttributeAuthorityModel(referentFiscalCode));
    }

    public ResponseEntity<Void> deleteReferent(String keyOrganizationFiscalCode, String referentFiscalCode) {

        return attributeAuthorityService.deleteReferent(keyOrganizationFiscalCode, referentFiscalCode);
    }

    private EntityTypeEnum getEntityTypeEnumFromEntityType(EntityType entityType) {

        return agreementConverter.toEntityEntityTypeEnum(entityType);
    }

    private final BiConsumer<AgreementUserEntity, OrganizationWithReferents> updateAgreementUserProfileAndAgreement = (agreementUserEntity, organizationWithReferents) -> {
        // update AgreementUser if merchant tax code has changed
        if (!organizationWithReferents.getKeyOrganizationFiscalCode().equals(organizationWithReferents.getOrganizationFiscalCode())) {
            agreementUserService.updateMerchantTaxCode(agreementUserEntity.getAgreementId(), organizationWithReferents.getOrganizationFiscalCode());
        }

        // get and update profile if present
        profileService.getProfile(agreementUserEntity.getAgreementId()).ifPresent(p -> {
            p.setFullName(organizationWithReferents.getOrganizationName());
            p.setTaxCodeOrVat(organizationWithReferents.getOrganizationFiscalCode());
            p.getAgreement().setEntityType(getEntityTypeEnumFromEntityType(organizationWithReferents.getEntityType()));
            p.getAgreement().setOrganizationName(organizationWithReferents.getOrganizationName());
            profileService.updateProfile(agreementUserEntity.getAgreementId(), p);
        });
    };

    private final BiConsumer<AgreementEntity, OrganizationWithReferentsAndStatus> mapStatus = (agreement, organization) -> {
        switch (agreement.getState()) {
            case DRAFT, REJECTED:
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

        organization.setEntityType(BackofficeAgreementConverter.getEntityTypeFromEntityTypeEnum(agreement.getEntityType()));
    };

    private final Consumer<OrganizationWithReferentsAndStatus> mapOrganizationStatus = organization -> agreementUserService.findCurrentAgreementUser(organization.getKeyOrganizationFiscalCode()).flatMap(agreementUserEntity -> agreementService.getAgreementById(agreementUserEntity.getAgreementId())).ifPresent(a -> mapStatus.accept(a, organization));

    private final Consumer<ResponseEntity<OrganizationWithReferentsAndStatus>> getOrganizationAgreementAndMapStatus = response -> {
        if (HttpStatus.OK.equals(response.getStatusCode()) && response.getBody() != null) {
            mapOrganizationStatus.accept(response.getBody());
        }
    };

    private final Consumer<Collection<OrganizationWithReferentsAndStatus>> mapOrganizationsStatus = organizations -> organizations.forEach(mapOrganizationStatus);

    private final Consumer<ResponseEntity<Organizations>> getOrganizationsAgreementAndMapStatus = response -> {
        if (HttpStatus.OK.equals(response.getStatusCode()) && response.getBody() != null) {
            mapOrganizationsStatus.accept(response.getBody().getItems());
        }
    };


    @Autowired
    public BackofficeAttributeAuthorityFacade(AttributeAuthorityService attributeAuthorityService, AgreementService agreementService, AgreementUserService agreementUserService, ProfileService profileService, OrganizationsConverter organizationsConverter, OrganizationWithReferentsConverter organizationWithReferentsConverter, OrganizationWithReferentsAndStatusConverter organizationWithReferentsAndStatusConverter, OrganizationWithReferentsPostConverter organizationWithReferentsPostConverter, ReferentFiscalCodeConverter referentFiscalCodeConverter, BackofficeAgreementConverter agreementConverter) {

        this.attributeAuthorityService = attributeAuthorityService;
        this.agreementService = agreementService;
        this.agreementUserService = agreementUserService;
        this.profileService = profileService;
        this.organizationsConverter = organizationsConverter;
        this.organizationWithReferentsConverter = organizationWithReferentsConverter;
        this.organizationWithReferentsAndStatusConverter = organizationWithReferentsAndStatusConverter;
        this.organizationWithReferentsPostConverter = organizationWithReferentsPostConverter;
        this.referentFiscalCodeConverter = referentFiscalCodeConverter;
        this.agreementConverter = agreementConverter;
    }


}
