package it.gov.pagopa.cgn.portal.repository;


import it.gov.pagopa.cgn.portal.converter.backoffice.BackofficeAgreementConverter;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.AssigneeEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.internal.OrderImpl;

import javax.persistence.criteria.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class BackofficeAgreementToValidateSpecification
        extends CommonBackofficeSpecification<AgreementEntity> {

    private static final String STATE_FIELD = "state";

    private enum DefaultAgreementOrder {
        PENDING_AGREEMENT,
        ASSIGNED_TO_CURRENT_USER_AGREEMENT,
        ASSIGNED_TO_OTHER_USER_AGREEMENT,
        DRAFT_AGREEMENT,
        REJECTED_AGREEMENT,
        FALLBACK_AGREEMENT
    }

    public BackofficeAgreementToValidateSpecification(BackofficeFilter filter, String currentUser) {
        super(filter, currentUser);
    }

    @Override
    protected List<Predicate> addFiltersPredicate(Root<AgreementEntity> root, CriteriaBuilder cb) {
        List<Predicate> predicateList = new ArrayList<>();
        if (StringUtils.isNotEmpty(filter.getProfileFullName())) {
            predicateList.add(cb.like(cb.upper(getOperatorNameExpression(root, cb)),
                                      toFullLikeUpperCaseString(filter.getProfileFullName())));
        }
        addFiltersDatePredicate(root, cb, predicateList);
        return predicateList;
    }

    @Override
    protected void addFiltersDatePredicate(Root<AgreementEntity> root,
                                           CriteriaBuilder cb,
                                           List<Predicate> predicateList) {
        addStatusFilter(root, cb, predicateList);
        if (!Objects.isNull(filter.getDateFrom())) {
            predicateList.add(cb.greaterThanOrEqualTo(getRequestApprovalTimePath(root),
                                                      getOffsetDateTimeFromLocalDate(filter.getDateFrom())));
        }
        if (!Objects.isNull(filter.getDateTo())) {
            predicateList.add(cb.lessThanOrEqualTo(getRequestApprovalTimePath(root),
                                                   getOffsetDateTimeFromLocalDate(filter.getDateTo())));
        }
    }

    @Override
    protected void addStaticFiltersPredicate(Root<AgreementEntity> root,
                                             CriteriaBuilder cb,
                                             List<Predicate> predicateList) {
        predicateList.add(root.get(STATE_FIELD)
                              .in(AgreementStateEnum.DRAFT, AgreementStateEnum.PENDING, AgreementStateEnum.REJECTED));
    }

    @Override
    protected Order getOrder(Root<AgreementEntity> root, CriteriaBuilder cb) {
        if (filter.getRequestSortColumnEnum()!=null) {
            return getOrderByFilter(root, cb);
        }
        return getDefaultOrders(root, cb).get(0);
    }

    @Override
    protected List<Order> getOrders(Root<AgreementEntity> root, CriteriaBuilder cb) {
        if (filter.getRequestSortColumnEnum()!=null) {
            return List.of(getOrderByFilter(root, cb));
        }
        return getDefaultOrders(root, cb);
    }

    private List<Order> getDefaultOrders(Root<AgreementEntity> root, CriteriaBuilder cb) {
        return List.of(new OrderImpl(getDefaultAgreementPriorityExpression(root, cb), true),
                       new OrderImpl(getDefaultAgreementDateExpression(root, cb), false),
                       new OrderImpl(root.get("id"), true));
    }

    private Expression<Integer> getDefaultAgreementPriorityExpression(Root<AgreementEntity> root, CriteriaBuilder cb) {
        Path<AgreementStateEnum> statePath = root.get(STATE_FIELD);
        Path<String> assigneePath = getBackofficeAssigneePath(root);
        Predicate pendingAgreement = cb.equal(statePath, AgreementStateEnum.PENDING);

        CriteriaBuilder.Case<Integer> priority = cb.<Integer>selectCase()
                                                   .when(cb.and(pendingAgreement, cb.isNull(assigneePath)),
                                                         DefaultAgreementOrder.PENDING_AGREEMENT.ordinal());

        if (StringUtils.isBlank(currentUser)) {
            priority = priority.when(cb.and(cb.equal(statePath, AgreementStateEnum.PENDING),
                                            cb.isNotNull(assigneePath)),
                                     DefaultAgreementOrder.ASSIGNED_TO_CURRENT_USER_AGREEMENT.ordinal());
        } else {
            priority = priority.when(cb.and(cb.equal(statePath, AgreementStateEnum.PENDING),
                                            cb.equal(assigneePath, currentUser)),
                                      DefaultAgreementOrder.ASSIGNED_TO_CURRENT_USER_AGREEMENT.ordinal())
                               .when(cb.and(cb.equal(statePath, AgreementStateEnum.PENDING),
                                            cb.isNotNull(assigneePath),
                                            cb.notEqual(assigneePath, currentUser)),
                                     DefaultAgreementOrder.ASSIGNED_TO_OTHER_USER_AGREEMENT.ordinal());
        }

        return priority.when(cb.equal(statePath, AgreementStateEnum.DRAFT),
                             DefaultAgreementOrder.DRAFT_AGREEMENT.ordinal())
                       .when(cb.equal(statePath, AgreementStateEnum.REJECTED),
                             DefaultAgreementOrder.REJECTED_AGREEMENT.ordinal())
                       .otherwise(DefaultAgreementOrder.FALLBACK_AGREEMENT.ordinal());
    }

    private Expression<OffsetDateTime> getDefaultAgreementDateExpression(Root<AgreementEntity> root,
                                                                         CriteriaBuilder cb) {
        return cb.<OffsetDateTime>selectCase()
                 .when(cb.equal(root.get(STATE_FIELD), AgreementStateEnum.DRAFT), getInsertTimePath(root))
                 .otherwise(getRequestApprovalTimePath(root));
    }

    private Order getOrderByFilter(Root<AgreementEntity> root, CriteriaBuilder cb) {
        switch (filter.getRequestSortColumnEnum()) {
            case ASSIGNEE:
                return new OrderImpl(getBackofficeAssigneePath(root), isSortAscending());
            case STATE:
                return new OrderImpl(cb.selectCase()
                                                                             .when(cb.equal(root.get(STATE_FIELD), AgreementStateEnum.DRAFT), 1)
                                       .when(cb.isNull(getBackofficeAssigneePath(root)), 2)
                                       .otherwise(3),
                                     isSortAscending());
            case OPERATOR:
                return new OrderImpl(getOperatorNameExpression(root, cb), isSortAscending());
            case REQUEST_DATE:
                return new OrderImpl(getRequestApprovalTimePath(root), isSortAscending());
        }
        throw new InvalidRequestException("Invalid sort column");
    }


    private Path<OffsetDateTime> getRequestApprovalTimePath(Root<AgreementEntity> root) {
        return root.get("requestApprovalTime");
    }

    private Path<OffsetDateTime> getInsertTimePath(Root<AgreementEntity> root) {
        return root.get("insertTime");
    }

    private Expression<String> getOperatorNameExpression(Root<AgreementEntity> root, CriteriaBuilder cb) {
        Join<AgreementEntity, ProfileEntity> profileJoin = root.join("profile", JoinType.LEFT);
        return cb.coalesce(profileJoin.get("fullName"), root.get("organizationName"));
    }

    private void addStatusFilter(Root<AgreementEntity> root, CriteriaBuilder cb, List<Predicate> predicateList) {
        if (StringUtils.isNotEmpty(filter.getAgreementState())) {
            AgreementStateEnum agreementStateEnum = BackofficeAgreementConverter.getAgreementStateEnumFromDtoCode(
                    filter.getAgreementState());
            if (AgreementStateEnum.DRAFT.equals(agreementStateEnum)) {
                predicateList.add(cb.equal(root.get(STATE_FIELD), AgreementStateEnum.DRAFT));
                return;
            }
            if (AgreementStateEnum.REJECTED.equals(agreementStateEnum)) {
                predicateList.add(cb.equal(root.get(STATE_FIELD), AgreementStateEnum.REJECTED));
                return;
            }
            //if assigned, database status is Pending but assignee should be used (if present or else not null)
            if (BackofficeAgreementConverter.isAgreementStateIsAssigned(filter.getAgreementState())) {
                predicateList.add(cb.equal(root.get(STATE_FIELD), AgreementStateEnum.PENDING));
                if (filter.getAssignee()!=null) {
                    addAssigneeFilter(root, cb, predicateList);
                } else {
                    predicateList.add(cb.isNotNull(getBackofficeAssigneePath(root)));
                }
            } else if (AgreementStateEnum.PENDING.equals(agreementStateEnum)) {
                // pending filter
                predicateList.add(cb.equal(root.get(STATE_FIELD), AgreementStateEnum.PENDING));
                predicateList.add(cb.isNull(getBackofficeAssigneePath(root)));
            } else {
                predicateList.add(cb.disjunction());
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

}
