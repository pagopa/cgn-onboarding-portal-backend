package it.gov.pagopa.cgn.portal.repository;


import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.BackofficeApprovedSortColumnEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.ApprovedAgreementEntity;
import org.hibernate.query.criteria.internal.OrderImpl;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class BackofficeApprovedAgreementSpecification
        extends CommonBackofficeSpecification<ApprovedAgreementEntity> {

    private static final String AGREEMENT_ID_FIELD = "id";
    private static final String PUBLISHED_DISCOUNTS_FIELD = "publishedDiscounts";
    private static final String START_DATE_FIELD = "startDate";
    private static final String STATE_FIELD = "state";
    private static final String TEST_PENDING_FIELD = "testPending";

    public BackofficeApprovedAgreementSpecification(BackofficeFilter filter, String currentUser) {
        super(filter, currentUser);
    }

    @Override
    protected void addFiltersDatePredicate(Root<ApprovedAgreementEntity> root,
                                           CriteriaBuilder cb,
                                           List<Predicate> predicateList) {
        if (!Objects.isNull(filter.getDateFrom())) {
            predicateList.add(cb.greaterThanOrEqualTo(getLastUpdateDatePath(root), filter.getDateFrom()));
        }
        if (!Objects.isNull(filter.getDateTo())) {
            predicateList.add(cb.lessThanOrEqualTo(getLastUpdateDatePath(root), filter.getDateTo()));
        }
    }

    @Override
    protected void addStaticFiltersPredicate(Root<ApprovedAgreementEntity> root,
                                             CriteriaBuilder cb,
                                             List<Predicate> predicateList) {
        predicateList.add(root.get(STATE_FIELD)
                              .in(AgreementStateEnum.APPROVED,
                                  AgreementStateEnum.ACTIVE,
                                  AgreementStateEnum.EXPIRED,
                                  AgreementStateEnum.INACTIVE,
                                  AgreementStateEnum.TERMINATION_REMINDER_SENT,
                                  AgreementStateEnum.TERMINATION_IN_PROGRESS,
                                  AgreementStateEnum.TERMINATED));
    }

    @Override
    protected Order getOrder(Root<ApprovedAgreementEntity> root, CriteriaBuilder cb) {
        if (filter.getApprovedSortColumnEnum()!=null) {
            return getOrderByFilter(root);
        }
        return new OrderImpl(getLastUpdateDatePath(root), false);
    }

    @Override
    protected List<Order> getOrders(Root<ApprovedAgreementEntity> root, CriteriaBuilder cb) {
        List<Order> orders = new ArrayList<>();
        orders.add(getOrder(root, cb));
        addStableTieBreakers(root, orders);
        return orders;
    }

    private Order getOrderByFilter(Root<ApprovedAgreementEntity> root) {
        switch (filter.getApprovedSortColumnEnum()) {
            case OPERATOR:
                return new OrderImpl(getProfileFullNamePath(root), isSortAscending());
            case AGREEMENT_DATE:
                return new OrderImpl(root.get(START_DATE_FIELD), isSortAscending());
            case LAST_MODIFY_DATE:
                return new OrderImpl(getLastUpdateDatePath(root), isSortAscending());
            case PUBLISHED_DISCOUNTS:
                return new OrderImpl(root.get(PUBLISHED_DISCOUNTS_FIELD), isSortAscending());
            case TEST:
                return new OrderImpl(root.get(TEST_PENDING_FIELD), isSortAscending());
            case STATE:
                return new OrderImpl(root.get(STATE_FIELD), isSortAscending());
            default:
                throw new InvalidRequestException("Invalid sort column");
        }
    }

    private void addStableTieBreakers(Root<ApprovedAgreementEntity> root, List<Order> orders) {
        if (!isSortingByLastModifyDate()) {
            orders.add(new OrderImpl(getLastUpdateDatePath(root), false));
        }
        orders.add(new OrderImpl(root.get(AGREEMENT_ID_FIELD), true));
    }

    private boolean isSortingByLastModifyDate() {
        return filter.getApprovedSortColumnEnum() == null ||
               BackofficeApprovedSortColumnEnum.LAST_MODIFY_DATE.equals(filter.getApprovedSortColumnEnum());
    }

    private Path<LocalDate> getLastUpdateDatePath(Root<ApprovedAgreementEntity> root) {
        return root.get("informationLastUpdateDate");
    }

}
