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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class BackofficeAgreementToValidateSpecification
        extends CommonBackofficeSpecification<AgreementEntity> {

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
        predicateList.add(root.get("state")
                              .in(AgreementStateEnum.DRAFT, AgreementStateEnum.PENDING, AgreementStateEnum.REJECTED));
    }

    @Override
    protected Order getOrder(Root<AgreementEntity> root, CriteriaBuilder cb) {
        Path<OffsetDateTime> dateExpression = getRequestApprovalTimePath(root);

        if (filter.getRequestSortColumnEnum()!=null) {
            return getOrderByFilter(root, cb);
        }
        // order by requestApprovalTime desc
        if (StringUtils.isBlank(currentUser)) {
            return new OrderImpl(dateExpression, direction.isAscending());
        }
        return new OrderImpl(cb.selectCase()
                               // first the agreements assigned to current user
                               .when(cb.equal(getBackofficeAssigneePath(root), currentUser),
                                     LocalDate.now().minusYears(10))
                               // last the agreements assigned to others user
                               .when(cb.isNotNull(getBackofficeAssigneePath(root)), LocalDate.now().plusYears(10))
                               // after agreements assigned to current user, the agreements not assigned
                               .otherwise(dateExpression), direction.isAscending());
    }

    private Order getOrderByFilter(Root<AgreementEntity> root, CriteriaBuilder cb) {
        switch (filter.getRequestSortColumnEnum()) {
            case ASSIGNEE:
                return new OrderImpl(getBackofficeAssigneePath(root), isSortAscending());
            case STATE:
                        /* if order direction is ASC --> draft, pending not assigned, pending assigned;
                            otherwise the same groups are reversed.
                         */
                return new OrderImpl(cb.selectCase()
                                       .when(cb.equal(root.get("state"), AgreementStateEnum.DRAFT), 1)
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

    private Expression<String> getOperatorNameExpression(Root<AgreementEntity> root, CriteriaBuilder cb) {
        Join<AgreementEntity, ProfileEntity> profileJoin = root.join("profile", JoinType.LEFT);
        return cb.coalesce(profileJoin.get("fullName"), root.get("organizationName"));
    }

    private void addStatusFilter(Root<AgreementEntity> root, CriteriaBuilder cb, List<Predicate> predicateList) {
        if (StringUtils.isNotEmpty(filter.getAgreementState())) {
            AgreementStateEnum agreementStateEnum = BackofficeAgreementConverter.getAgreementStateEnumFromDtoCode(
                    filter.getAgreementState());
            if (AgreementStateEnum.DRAFT.equals(agreementStateEnum)) {
                predicateList.add(cb.equal(root.get("state"), AgreementStateEnum.DRAFT));
                return;
            }
            if (AgreementStateEnum.REJECTED.equals(agreementStateEnum)) {
                predicateList.add(cb.equal(root.get("state"), AgreementStateEnum.REJECTED));
                return;
            }
            //if assigned, database status is Pending but assignee should be used (if present or else not null)
            if (BackofficeAgreementConverter.isAgreementStateIsAssigned(filter.getAgreementState())) {
                predicateList.add(cb.equal(root.get("state"), AgreementStateEnum.PENDING));
                if (filter.getAssignee()!=null) {
                    addAssigneeFilter(root, cb, predicateList);
                } else {
                    predicateList.add(cb.isNotNull(getBackofficeAssigneePath(root)));
                }
            } else if (AgreementStateEnum.PENDING.equals(agreementStateEnum)) {
                // pending filter
                predicateList.add(cb.equal(root.get("state"), AgreementStateEnum.PENDING));
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
