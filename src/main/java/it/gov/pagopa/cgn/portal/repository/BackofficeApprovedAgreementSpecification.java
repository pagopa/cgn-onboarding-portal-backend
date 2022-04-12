package it.gov.pagopa.cgn.portal.repository;


import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.ApprovedAgreementEntity;
import org.hibernate.query.criteria.internal.OrderImpl;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;


public class BackofficeApprovedAgreementSpecification extends CommonBackofficeSpecification<ApprovedAgreementEntity> {

    public BackofficeApprovedAgreementSpecification(BackofficeFilter filter, String currentUser) {
        super(filter, currentUser);
    }

    @Override
    protected void addFiltersDatePredicate(Root<ApprovedAgreementEntity> root, CriteriaBuilder cb, List<Predicate> predicateList) {
        if (!Objects.isNull(filter.getDateFrom())) {
            predicateList.add(cb.greaterThanOrEqualTo(getLastUpdateDatePath(root), filter.getDateFrom()));
        }
        if (!Objects.isNull(filter.getDateTo())) {
            predicateList.add(cb.lessThanOrEqualTo(getLastUpdateDatePath(root), filter.getDateTo()));
        }
    }

    @Override
    protected void addStaticFiltersPredicate(Root<ApprovedAgreementEntity> root, CriteriaBuilder cb, List<Predicate> predicateList) {
        predicateList.add(cb.equal(root.get("state"), AgreementStateEnum.APPROVED));
    }

    @Override
    protected Order getOrder(Root<ApprovedAgreementEntity> root, CriteriaBuilder cb) {
        if (filter.getApprovedSortColumnEnum() != null) {
            return getOrderByFilter(root);
        }
        return new OrderImpl(getLastUpdateDatePath(root), false);
    }

    private Order getOrderByFilter(Root<ApprovedAgreementEntity> root) {
        switch (filter.getApprovedSortColumnEnum()) {
            case OPERATOR:
                return new OrderImpl(getProfileFullNamePath(root), isSortAscending());
            case AGREEMENT_DATE:
                return new OrderImpl(root.get("startDate"), isSortAscending());
            case LAST_MODIFY_DATE:
                return new OrderImpl(getLastUpdateDatePath(root), isSortAscending());
            case PUBLISHED_DISCOUNTS:
                return new OrderImpl(root.get("publishedDiscounts"), isSortAscending());
            default:
                throw new InvalidRequestException("Invalid sort column");
        }
    }

    private Path<LocalDate> getLastUpdateDatePath(Root<ApprovedAgreementEntity> root) {
        return root.get("informationLastUpdateDate");
    }

}
