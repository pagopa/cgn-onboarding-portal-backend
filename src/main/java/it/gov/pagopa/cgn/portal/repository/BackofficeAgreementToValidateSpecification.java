package it.gov.pagopa.cgn.portal.repository;


import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.internal.OrderImpl;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;


public class BackofficeAgreementToValidateSpecification extends CommonAgreementSpecification {

    public BackofficeAgreementToValidateSpecification(BackofficeFilter filter, String currentUser) {
        super(filter, currentUser);
    }

    @Override
    protected void addFiltersDatePredicate(Root<AgreementEntity> root, CriteriaBuilder cb, List<Predicate> predicateList) {
        if (!Objects.isNull(filter.getDateFrom())) {
            predicateList.add(cb.greaterThanOrEqualTo(root.get("requestApprovalTime"),
                    getOffsetDateTimeFromLocalDate(filter.getDateFrom())));
        }
        if (!Objects.isNull(filter.getDateTo())) {
            predicateList.add(cb.lessThanOrEqualTo(root.get("requestApprovalTime"),
                    getOffsetDateTimeFromLocalDate(filter.getDateTo())));
        }
    }

    @Override
    protected void addStaticFiltersPredicate(Root<AgreementEntity> root, CriteriaBuilder cb, List<Predicate> predicateList) {
        root.fetch("profile");
        predicateList.add(cb.equal(root.get("state"), AgreementStateEnum.PENDING));
    }

    @Override
    protected Order getOrder(Root<AgreementEntity> root, CriteriaBuilder cb) {
        Expression<String> dateExpression = root.get("requestApprovalTime");
        // order by requestApprovalTime desc
        if (StringUtils.isBlank(currentUser)) {
            return new OrderImpl(dateExpression, direction.isAscending());
        }
        return new OrderImpl(cb.selectCase()
                // first the agreements assigned to current user
                .when(cb.equal(root.get("backofficeAssignee"), currentUser), LocalDate.now().minusYears(10))
                // last the agreements assigned to others user
                .when(cb.isNotNull(root.get("backofficeAssignee")), LocalDate.now().plusYears(10))
                // after agreements assigned to current user, the agreements not assigned
                .otherwise(dateExpression), direction.isAscending());
    }

}
