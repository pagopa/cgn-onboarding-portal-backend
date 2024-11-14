package it.gov.pagopa.cgn.portal.repository;


import it.gov.pagopa.cgn.portal.converter.backoffice.BackofficeAgreementConverter;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.AssigneeEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.criteria.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;


public class BackofficeAgreementToValidateSpecification extends CommonBackofficeSpecification<AgreementEntity> {

    public BackofficeAgreementToValidateSpecification(BackofficeFilter filter, String currentUser) {

        super(filter, currentUser);
    }

    @Override
    protected void addFiltersDatePredicate(Root<AgreementEntity> root, CriteriaBuilder cb, List<Predicate> predicateList) {

        addStatusFilter(root, cb, predicateList);
        if (!Objects.isNull(filter.getDateFrom())) {
            predicateList.add(cb.greaterThanOrEqualTo(getRequestApprovalTimePath(root), getOffsetDateTimeFromLocalDate(filter.getDateFrom())));
        }
        if (!Objects.isNull(filter.getDateTo())) {
            predicateList.add(cb.lessThanOrEqualTo(getRequestApprovalTimePath(root), getOffsetDateTimeFromLocalDate(filter.getDateTo())));
        }
    }

    @Override
    protected void addStaticFiltersPredicate(Root<AgreementEntity> root, CriteriaBuilder cb, List<Predicate> predicateList) {

        predicateList.add(cb.equal(root.get("state"), AgreementStateEnum.PENDING));
    }

    @Override
    protected Order getOrder(Root<AgreementEntity> root, CriteriaBuilder cb) {

        Path<OffsetDateTime> dateExpression = getRequestApprovalTimePath(root);

        if (filter.getRequestSortColumnEnum() != null) {
            return getOrderByFilter(root, cb);
        }
        // order by requestApprovalTime desc
        if (StringUtils.isBlank(currentUser)) {
            return order(dateExpression, cb, direction.isAscending());
        }

        Expression<Object> orderExpression = cb.selectCase()
                // first the agreements assigned to current user
                .when(cb.equal(getBackofficeAssigneePath(root), currentUser), LocalDateTime.now().minusYears(10))
                // last the agreements assigned to others user
                .when(cb.isNotNull(getBackofficeAssigneePath(root)), LocalDateTime.now().plusYears(10)).otherwise(dateExpression);

        return order(orderExpression, cb, direction.isAscending());
    }

    private Order getOrderByFilter(Root<AgreementEntity> root, CriteriaBuilder cb) {

        switch (filter.getRequestSortColumnEnum()) {
            case ASSIGNEE:
                return order(getBackofficeAssigneePath(root), cb);
            case STATE:
                        /* if order direction is ASC --> first rows with assignee null and then other,
                            otherwise first rows with assignee not null and then others
                         */
                return order(cb.selectCase().when(cb.isNull(getBackofficeAssigneePath(root)), 1).otherwise(2), cb);
            case OPERATOR:
                return order(getProfileFullNamePath(root), cb);
            case REQUEST_DATE:
                return order(getRequestApprovalTimePath(root), cb);
        }
        throw new InvalidRequestException("Invalid sort column");
    }


    private Path<OffsetDateTime> getRequestApprovalTimePath(Root<AgreementEntity> root) {

        return root.get("requestApprovalTime");
    }

    private void addStatusFilter(Root<AgreementEntity> root, CriteriaBuilder cb, List<Predicate> predicateList) {

        if (StringUtils.isNotEmpty(filter.getAgreementState())) {
            //if assigned, database status is Pending but assignee should be used (if present or else not null)
            if (BackofficeAgreementConverter.isAgreementStateIsAssigned(filter.getAgreementState())) {
                if (filter.getAssignee() != null) {
                    addAssigneeFilter(root, cb, predicateList);
                } else {
                    predicateList.add(cb.isNotNull(getBackofficeAssigneePath(root)));
                }
            } else {
                // pending filter
                predicateList.add(cb.isNull(getBackofficeAssigneePath(root)));
            }
        }
    }

    private void addAssigneeFilter(Root<AgreementEntity> root, CriteriaBuilder cb, List<Predicate> predicateList) {

        Path<String> backofficeAssigneePath = getBackofficeAssigneePath(root);
        if (AssigneeEnum.ME.equals(filter.getAssignee())) {
            predicateList.add(cb.equal(backofficeAssigneePath, currentUser));
        } else {
            predicateList.add(cb.notEqual(backofficeAssigneePath, currentUser));
        }
    }

    private Path<String> getBackofficeAssigneePath(Root<AgreementEntity> root) {

        return root.get("backofficeAssignee");
    }

    private <T> Order order(Expression<T> expr, CriteriaBuilder cb) {

        return order(expr, cb, isSortAscending());

    }

    private <T> Order order(Expression<T> expr, CriteriaBuilder cb, boolean ascending) {

        return ascending ? cb.asc(expr) : cb.desc(expr);

    }

}
