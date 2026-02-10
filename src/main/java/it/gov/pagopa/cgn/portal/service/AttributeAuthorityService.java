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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

            boolean isFiscalCodeSearch = isReferentFiscalCode(searchQuery);

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

    @Transactional(readOnly = false)
    public ResponseEntity<Void> deleteOrganization(String keyOrganizationFiscalCode) {
        try {
            Optional<AAOrganizationEntity> organization = aaOrganizationRepository.findById(keyOrganizationFiscalCode);
            
            if (organization.isEmpty()) {
                log.warn("Organization not found: {}", keyOrganizationFiscalCode);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            aaOrganizationRepository.delete(organization.get());
            aaOrganizationRepository.flush();
            
            log.info("Deleted organization with fiscal code: {}", keyOrganizationFiscalCode);
            
            return new ResponseEntity<>(HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error deleting organization", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = false)
    public ResponseEntity<OrganizationWithReferentsAttributeAuthority> upsertOrganization(
            OrganizationWithReferentsPostAttributeAuthority organizationWithReferentsAttributeAuthority) {
        try {
            String keyOrgFiscalCode = organizationWithReferentsAttributeAuthority.getKeyOrganizationFiscalCode();
            
            AAOrganizationEntity organization = aaOrganizationRepository.findById(keyOrgFiscalCode)
                    .map(existing -> {
                        existing.setName(organizationWithReferentsAttributeAuthority.getOrganizationName());
                        existing.setPec(organizationWithReferentsAttributeAuthority.getPec());
                        return existing;
                    })
                    .orElseGet(() -> {
                        AAOrganizationEntity newOrg = new AAOrganizationEntity();
                        newOrg.setFiscalCode(keyOrgFiscalCode);
                        newOrg.setName(organizationWithReferentsAttributeAuthority.getOrganizationName());
                        newOrg.setPec(organizationWithReferentsAttributeAuthority.getPec());
                        newOrg.setInsertedAt(OffsetDateTime.now());
                        return newOrg;
                    });
            
            List<String> referentCodes = organizationWithReferentsAttributeAuthority.getReferents()
                    .stream()
                    .distinct()
                    .toList();
            
            List<AAReferentEntity> referents = new ArrayList<>();
            for (String code : referentCodes) {
                AAReferentEntity referent = aaReferentRepository.findById(code).orElse(null);
                if (referent == null) {
                    AAReferentEntity newReferent = new AAReferentEntity();
                    newReferent.setFiscalCode(code);
                    referent = aaReferentRepository.save(newReferent);
                }
                referents.add(referent);
            }
            
            List<AAOrganizationReferentEntity> newReferents = referents.stream()
                    .map(referent -> {
                        AAOrganizationReferentEntity join = new AAOrganizationReferentEntity();
                        join.setOrganization(organization);
                        join.setReferent(referent);
                        return join;
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
            
            List<AAOrganizationReferentEntity> currentReferents = organization.getOrganizationReferents();
            if (currentReferents == null) {
                currentReferents = new ArrayList<>();
                organization.setOrganizationReferents(currentReferents);
            } else {
                currentReferents.clear();
            }
            
            currentReferents.addAll(newReferents);
            
            AAOrganizationEntity savedOrganization = aaOrganizationRepository.save(organization);
            
            log.info("Upserted organization with fiscal code: {}", keyOrgFiscalCode);
            
            return new ResponseEntity<>(mapToOrganizationWithReferents(savedOrganization), HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error upserting organization", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

    @Transactional(readOnly = false)
    public ResponseEntity<Void> insertReferent(String keyOrganizationFiscalCode,
                                               ReferentFiscalCodeAttributeAuthority referentFiscalCodeAttributeAuthority) {
        try {
            Optional<AAOrganizationEntity> organizationOpt = aaOrganizationRepository.findById(keyOrganizationFiscalCode);
            if (organizationOpt.isEmpty()) {
                log.warn("Organization not found: {}", keyOrganizationFiscalCode);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            String referentFiscalCode = referentFiscalCodeAttributeAuthority.getReferentFiscalCode();

            AAReferentEntity referent = aaReferentRepository.findById(referentFiscalCode)
                    .orElseGet(() -> {
                        AAReferentEntity newReferent = new AAReferentEntity();
                        newReferent.setFiscalCode(referentFiscalCode);
                        return aaReferentRepository.save(newReferent);
                    });

            AAOrganizationEntity organization = organizationOpt.get();
            List<AAOrganizationReferentEntity> currentReferents = organization.getOrganizationReferents();
            if (currentReferents == null) {
                currentReferents = new ArrayList<>();
                organization.setOrganizationReferents(currentReferents);
            }

            boolean alreadyLinked = currentReferents.stream()
                    .anyMatch(link -> link.getReferent() != null
                            && referentFiscalCode.equals(link.getReferent().getFiscalCode()));

            if (!alreadyLinked) {
                AAOrganizationReferentEntity join = new AAOrganizationReferentEntity();
                join.setOrganization(organization);
                join.setReferent(referent);
                currentReferents.add(join);
            }

            aaOrganizationRepository.save(organization);

            log.info("Inserted referent {} for organization {}", referentFiscalCode, keyOrganizationFiscalCode);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error inserting referent for organization: {}", keyOrganizationFiscalCode, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = false)
    public ResponseEntity<Void> deleteReferent(String keyOrganizationFiscalCode, String referentFiscalCode) {
        try {
            Optional<AAOrganizationEntity> organizationOpt = aaOrganizationRepository.findById(keyOrganizationFiscalCode);
            if (organizationOpt.isEmpty()) {
                log.warn("Organization not found: {}", keyOrganizationFiscalCode);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            AAOrganizationEntity organization = organizationOpt.get();
            List<AAOrganizationReferentEntity> currentReferents = organization.getOrganizationReferents();

            if (currentReferents != null) {
                boolean removed = currentReferents.removeIf(link ->
                        link.getReferent() != null
                                && referentFiscalCode.equals(link.getReferent().getFiscalCode())
                );

                if (removed) {
                    aaOrganizationRepository.save(organization);
                    log.info("Deleted referent {} from organization {}", referentFiscalCode, keyOrganizationFiscalCode);
                }
            }

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error deleting referent from organization: {}", keyOrganizationFiscalCode, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

    private boolean isReferentFiscalCode(String input) {
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
        Set<String> validColumns = Set.of("fiscalCode", "name", "pec", "insertedAt");
        return validColumns.contains(sortBy) ? sortBy : "fiscalCode";
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
