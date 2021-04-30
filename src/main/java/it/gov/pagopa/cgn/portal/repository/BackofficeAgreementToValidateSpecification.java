package it.gov.pagopa.cgn.portal.repository;


import it.gov.pagopa.cgn.portal.converter.backoffice.BackofficeAgreementConverter;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.internal.OrderImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class BackofficeAgreementToValidateSpecification implements Specification<AgreementEntity>{

    private final Sort.Direction direction;
    private final BackofficeFilter filter;
    private final String currentUser;


    public BackofficeAgreementToValidateSpecification(final BackofficeFilter filter, final String currentUser) {
        this.direction = Sort.Direction.DESC;
        this.filter = filter;
        this.currentUser = currentUser;
    }

    public Pageable getPage() {
        if (filter.getPageSize() == null) {
            return PageRequest.of(0, 20);
        }
        return PageRequest.of(filter.getPage(), filter.getPageSize());
    }


    @Override
    public Predicate toPredicate(Root<AgreementEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        root.fetch("profile");
        List<Predicate> predicateList = addFiltersPredicate(root, cb);
        predicateList.add(cb.equal(root.get("state"), AgreementStateEnum.PENDING));
        query.where(predicateList.toArray(new Predicate[predicateList.size()]));
        query.orderBy(getOrder(root, cb));
        return null;
    }

    private Order getOrder(Root<AgreementEntity> root, CriteriaBuilder cb) {
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

    private List<Predicate> addFiltersPredicate(Root<AgreementEntity> root, CriteriaBuilder cb) {
        List<Predicate> predicateList = new ArrayList<>();
        if (StringUtils.isNotEmpty(filter.getAgreementState())) {
            AgreementStateEnum agreementStateEnum;
            agreementStateEnum = BackofficeAgreementConverter.getAgreementStateEnumFromDtoCode(filter.getAgreementState());
            predicateList.add(cb.equal(root.get("state").as(String.class), agreementStateEnum.getCode()));
        }
        if (filter.getAssignee() != null) {
            Path<String> backofficeAssigneePath = root.get("backofficeAssignee");
            if (AssigneeEnum.ME.equals(filter.getAssignee())) {
                predicateList.add(cb.equal(backofficeAssigneePath, currentUser));
            } else {
                predicateList.add(cb.notEqual(backofficeAssigneePath, currentUser));
            }
        }
        if (!Objects.isNull(filter.getRequestDateFrom())) {
            predicateList.add(cb.greaterThanOrEqualTo(root.get("requestApprovalTime"),
                    getOffsetDateTimeFromLocalDate(filter.getRequestDateFrom())));
        }
        if (!Objects.isNull(filter.getRequestDateTo())) {
            predicateList.add(cb.lessThanOrEqualTo(root.get("requestApprovalTime"),
                    getOffsetDateTimeFromLocalDate(filter.getRequestDateTo())));
        }
        if (StringUtils.isNotEmpty(filter.getProfileFullName())) {
            predicateList.add(cb.like(cb.upper(root.get("profile").get("fullName")),
                    toFullLikeUpperCaseString(filter.getProfileFullName())));
        }
        return predicateList;

    }

    private OffsetDateTime getOffsetDateTimeFromLocalDate(LocalDate localDate) {
        return OffsetDateTime.of(localDate, LocalTime.MIDNIGHT, ZoneOffset.UTC);
    }

    private String toFullLikeString(String value) {
        return "%" + value + "%";
    }

    private String toFullLikeUpperCaseString(String value) {
        return toFullLikeString(value).toUpperCase();
    }
}
