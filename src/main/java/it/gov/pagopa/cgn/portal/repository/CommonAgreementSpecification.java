package it.gov.pagopa.cgn.portal.repository;


import it.gov.pagopa.cgn.portal.converter.backoffice.BackofficeAgreementConverter;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.AssigneeEnum;
import it.gov.pagopa.cgn.portal.filter.BackofficeFilter;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
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


public abstract class CommonAgreementSpecification implements Specification<AgreementEntity>{

    protected final Sort.Direction direction;
    protected final BackofficeFilter filter;
    protected final String currentUser;


    protected CommonAgreementSpecification(final BackofficeFilter filter, final String currentUser) {
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

    protected abstract void addFiltersDatePredicate(
            Root<AgreementEntity> root, CriteriaBuilder cb, List<Predicate> predicateList);

    protected abstract void addStaticFiltersPredicate(
            Root<AgreementEntity> root, CriteriaBuilder cb, List<Predicate> predicateList);

    protected abstract Order getOrder(Root<AgreementEntity> root, CriteriaBuilder cb);

    @Override
    public Predicate toPredicate(Root<AgreementEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        List<Predicate> predicateList = addFiltersPredicate(root, cb);
        addStaticFiltersPredicate(root, cb, predicateList);
        query.where(predicateList.toArray(new Predicate[predicateList.size()]));
        query.orderBy(getOrder(root, cb));
        return null;
    }

    protected List<Predicate> addFiltersPredicate(Root<AgreementEntity> root, CriteriaBuilder cb) {
        List<Predicate> predicateList = new ArrayList<>();
        if (StringUtils.isNotEmpty(filter.getProfileFullName())) {
            predicateList.add(cb.like(cb.upper(root.get("profile").get("fullName")),
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
}
