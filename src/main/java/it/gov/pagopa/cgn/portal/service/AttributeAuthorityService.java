package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.model.AAOrganizationEntity;
import it.gov.pagopa.cgn.portal.model.AAOrganizationReferentEntity;
import it.gov.pagopa.cgn.portal.model.AAReferentEntity;
import it.gov.pagopa.cgn.portal.repository.AAOrganizationRepository;
import it.gov.pagopa.cgn.portal.repository.AAReferentRepository;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AttributeAuthorityService {

    private static final String FISCAL_CODE_REGEX = "^[A-Z]{6}[0-9LMNPQRSTUV]{2}[ABCDEHLMPRST][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z]$";
    private static final Pattern FISCAL_CODE_PATTERN = Pattern.compile(FISCAL_CODE_REGEX);

    private final AAOrganizationRepository aaOrganizationRepository;

    private final AAReferentRepository aaReferentRepository;

    public AttributeAuthorityService(AAOrganizationRepository aaOrganizationRepository,
                                     AAReferentRepository aaReferentRepository) {
        this.aaOrganizationRepository = aaOrganizationRepository;
        this.aaReferentRepository = aaReferentRepository;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<OrganizationsAttributeAuthority> getOrganizations(String searchQuery,
                                                                            Integer page,
                                                                            Integer pageSize,
                                                                            String sortBy,
                                                                            String sortDirection) {
        try {
            int pageNum = page != null ? page : 0;
            int pageSizeNum = pageSize != null ? pageSize : 20;

            boolean isFiscalCodeSearch = isFiscalCode(searchQuery);

            Sort sort = buildSort(sortBy, sortDirection);
            Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);

            Page<AAOrganizationEntity> result;
            long count;

            if (isFiscalCodeSearch) {
                result = aaOrganizationRepository.findByReferentFiscalCode(searchQuery, pageable);
                count = aaOrganizationRepository.countByReferentFiscalCode(searchQuery);
            } else if (searchQuery != null && !searchQuery.isEmpty()) {
                result = aaOrganizationRepository.findByNameOrFiscalCodeContainingIgnoreCase(searchQuery, pageable);
                count = aaOrganizationRepository.countByNameOrFiscalCodeContainingIgnoreCase(searchQuery);
            } else {
                result = aaOrganizationRepository.findAllWithReferents(pageable);
                count = aaOrganizationRepository.countAllWithReferents();
            }

            List<OrganizationWithReferentsAttributeAuthority> items = result.getContent()
                                                                            .stream()
                                                                            .map(this::mapToOrganizationWithReferents)
                                                                            .toList();

            OrganizationsAttributeAuthority response = new OrganizationsAttributeAuthority();
            response.setCount(Math.toIntExact(count));
            response.setItems(items);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error fetching organizations", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<OrganizationWithReferentsAttributeAuthority> upsertOrganization(
            OrganizationWithReferentsPostAttributeAuthority organizationWithReferentsAttributeAuthority) {
        log.warn("Write operation blocked: upsertOrganization not allowed on external Attribute Authority API");
        throw new HttpClientErrorException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Write operations to Attribute Authority are not allowed."
        );
    }

    @Transactional(readOnly = true)
    public ResponseEntity<OrganizationWithReferentsAttributeAuthority> getOrganization(String keyOrganizationFiscalCode) {
        try {
            return aaOrganizationRepository.findById(keyOrganizationFiscalCode)
                    .map(this::mapToOrganizationWithReferents)
                    .map(org -> new ResponseEntity<>(org, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error("Error fetching organization with fiscal code: {}", keyOrganizationFiscalCode, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Void> deleteOrganization(String keyOrganizationFiscalCode) {
        log.warn("Write operation blocked: deleteOrganization not allowed on external Attribute Authority API");
        throw new HttpClientErrorException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Write operations to Attribute Authority are not allowed."
        );
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<String>> getReferents(String keyOrganizationFiscalCode) {
        try {
            return aaOrganizationRepository.findById(keyOrganizationFiscalCode)
                    .map(organization -> organization.getOrganizationReferents() == null
                            ? List.<String>of()
                            : organization.getOrganizationReferents().stream()
                            .map(AAOrganizationReferentEntity::getReferent)
                            .map(AAReferentEntity::getFiscalCode)
                            .toList())
                    .map(referents -> new ResponseEntity<>(referents, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error("Error fetching referents for organization: {}", keyOrganizationFiscalCode, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Void> insertReferent(String keyOrganizationFiscalCode,
                                               ReferentFiscalCodeAttributeAuthority referentFiscalCodeAttributeAuthority) {
        log.warn("Write operation blocked: insertReferent not allowed on external Attribute Authority API");
        throw new HttpClientErrorException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Write operations to Attribute Authority are not allowed."
        );
    }

    public ResponseEntity<Void> deleteReferent(String keyOrganizationFiscalCode, String referentFiscalCode) {
        log.warn("Write operation blocked: deleteReferent not allowed on external Attribute Authority API");
        throw new HttpClientErrorException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Write operations to Attribute Authority are not allowed."
        );
    }

    @Transactional(readOnly = true)
    public List<CompanyAttributeAuthority> getAgreementOrganizations(String referentFiscalCode)
            throws HttpClientErrorException {
        try {
            return aaReferentRepository.findById(referentFiscalCode)
                    .map(referent -> referent.getOrganizationReferents() == null
                            ? List.<CompanyAttributeAuthority>of()
                            : referent.getOrganizationReferents().stream()
                            .map(AAOrganizationReferentEntity::getOrganization)
                            .map(this::mapToCompany)
                            .toList())
                    .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
        } catch (HttpClientErrorException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching companies for referent: {}", referentFiscalCode, e);
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching companies");
        }
    }

    @Transactional(readOnly = true)
    public int countUserOrganizations(String referentFiscalCode)
            throws HttpClientErrorException {
        return getAgreementOrganizations(referentFiscalCode).size();
    }

    private boolean isFiscalCode(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return FISCAL_CODE_PATTERN.matcher(input).matches();
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortDirection == null) {
            return Sort.unsorted();
        }

        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        String columnName = mapSortByToColumnName(sortBy);
        return Sort.by(direction, columnName);
    }

    private String mapSortByToColumnName(String sortBy) {
        if (sortBy == null) {
            return "fiscalCode";
        }
        return switch (sortBy) {
            case "name" -> "name";
            case "pec" -> "pec";
            case "insertedAt" -> "insertedAt";
            default -> "fiscalCode";
        };
    }

    private OrganizationWithReferentsAttributeAuthority mapToOrganizationWithReferents(
            AAOrganizationEntity organization) {
        OrganizationWithReferentsAttributeAuthority result = new OrganizationWithReferentsAttributeAuthority();
        result.setKeyOrganizationFiscalCode(organization.getFiscalCode());
        result.setOrganizationFiscalCode(organization.getFiscalCode());
        result.setOrganizationName(organization.getName());
        result.setPec(organization.getPec());
        result.setInsertedAt(organization.getInsertedAt().toString());
        result.setReferents(organization.getOrganizationReferents().stream()
                .map(orgRef -> orgRef.getReferent().getFiscalCode())
                .toList());
        return result;
    }

    private CompanyAttributeAuthority mapToCompany(AAOrganizationEntity organization) {
        CompanyAttributeAuthority result = new CompanyAttributeAuthority();
        result.setFiscalCode(organization.getFiscalCode());
        result.setOrganizationName(organization.getName());
        result.setPec(organization.getPec());
        return result;
    }
}
