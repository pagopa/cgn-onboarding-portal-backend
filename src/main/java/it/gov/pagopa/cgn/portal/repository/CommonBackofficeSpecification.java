package it.gov.pagopa.cgn.portal.repository;


import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import org.apache.commons.lang3.StringUtils;
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


public abstract class CommonBackofficeSpecification<T>
        implements Specification<T> {

    protected final Sort.Direction direction;
    protected final BackofficeFilter filter;
    protected final String currentUser;

    protected CommonBackofficeSpecification(final BackofficeFilter filter, final String currentUser) {
        this.direction = Sort.Direction.DESC;
        this.filter = filter;
        this.currentUser = currentUser;
    }

    public Pageable getPage() {
        if (filter.getPageSize()==null) {
            return PageRequest.of(0, 20);
        }
        return PageRequest.of(filter.getPage(), filter.getPageSize());
    }

    protected abstract void addFiltersDatePredicate(Root<T> root, CriteriaBuilder cb, List<Predicate> predicateList);

    protected abstract void addStaticFiltersPredicate(Root<T> root, CriteriaBuilder cb, List<Predicate> predicateList);

    protected abstract Order getOrder(Root<T> root, CriteriaBuilder cb);

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicateList = addFiltersPredicate(root, cb);
        addStaticFiltersPredicate(root, cb, predicateList);
        query.where(predicateList.toArray(new Predicate[predicateList.size()]));
        query.orderBy(getOrder(root, cb));
        return null;
    }

    protected List<Predicate> addFiltersPredicate(Root<T> root, CriteriaBuilder cb) {
        List<Predicate> predicateList = new ArrayList<>();
        if (StringUtils.isNotEmpty(filter.getProfileFullName())) {
            predicateList.add(cb.like(cb.upper(getProfileFullNamePath(root)),
                                      toFullLikeUpperCaseString(filter.getProfileFullName())));
        }
        addFiltersDatePredicate(root, cb, predicateList);
        return predicateList;
    }

    protected OffsetDateTime getOffsetDateTimeFromLocalDate(LocalDate localDate) {
        return OffsetDateTime.of(localDate, LocalTime.MIDNIGHT, ZoneOffset.UTC);
    }

    protected String toFullLikeString(String value) {
        return "%" + value + "%";
    }

    protected String toFullLikeUpperCaseString(String value) {
        return toFullLikeString(value).toUpperCase();
    }

    protected Path<String> getProfileFullNamePath(Root<T> root) {
        return root.get("profile").get("fullName");
    }

    protected boolean isSortAscending() {
        return Sort.Direction.ASC.equals(filter.getSortDirection());
    }
}
