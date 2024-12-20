package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.ApprovedAgreementEntity;
import it.gov.pagopa.cgn.portal.repository.ApprovedAgreementRepository;
import it.gov.pagopa.cgn.portal.repository.BackofficeApprovedAgreementSpecification;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprovedAgreementService {

    private final ApprovedAgreementRepository approvedAgreementRepository;

    @Autowired
    public ApprovedAgreementService(ApprovedAgreementRepository approvedAgreementRepository) {
        this.approvedAgreementRepository = approvedAgreementRepository;
    }

    @Transactional(readOnly = true)
    public Page<ApprovedAgreementEntity> getApprovedAgreements(BackofficeFilter filter) {

        BackofficeApprovedAgreementSpecification spec = new BackofficeApprovedAgreementSpecification(filter,
                                                                                                     CGNUtils.getJwtAdminUserName());
        return approvedAgreementRepository.findAll(spec, spec.getPage());
    }

}

