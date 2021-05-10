package it.gov.pagopa.cgn.portal.repository;


import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import org.hibernate.query.criteria.internal.OrderImpl;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;


public class BackofficeApprovedAgreementSpecification extends CommonAgreementSpecification {

    public BackofficeApprovedAgreementSpecification(BackofficeFilter filter, String currentUser) {
        super(filter, currentUser);
    }

    @Override
    protected void addFiltersDatePredicate(Root<AgreementEntity> root, CriteriaBuilder cb, List<Predicate> predicateList) {
        if (!Objects.isNull(filter.getDateFrom())) {
            predicateList.add(cb.greaterThanOrEqualTo(getLastUpdateDatePath(root), filter.getDateFrom()));
        }
        if (!Objects.isNull(filter.getDateTo())) {
            predicateList.add(cb.lessThanOrEqualTo(getLastUpdateDatePath(root), filter.getDateTo()));
        }
    }

    @Override
    protected void addStaticFiltersPredicate(Root<AgreementEntity> root, CriteriaBuilder cb, List<Predicate> predicateList) {
        predicateList.add(cb.equal(root.get("state"), AgreementStateEnum.APPROVED));
    }

    @Override
    protected Order getOrder(Root<AgreementEntity> root, CriteriaBuilder cb) {
        return new OrderImpl(getLastUpdateDatePath(root), false);
    }

    private Path<LocalDate> getLastUpdateDatePath(Root<AgreementEntity> root) {
        return root.get("informationLastUpdateDate");
    }

}
