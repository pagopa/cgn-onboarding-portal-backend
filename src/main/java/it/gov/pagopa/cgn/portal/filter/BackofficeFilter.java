package it.gov.pagopa.cgn.portal.filter;

import it.gov.pagopa.cgn.portal.enums.AssigneeEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class BackofficeFilter implements Serializable {

    private String agreementState;

    private String profileFullName;

    private AssigneeEnum assignee;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private Integer pageSize;

    private Integer page;


    public static BackofficeFilter getFilter(
            String state, String profileFullName, String assignee, LocalDate startDateFrom, LocalDate startDateTo,
            Integer pageSize, Integer page) {

        return BackofficeFilter.builder()
                .agreementState(state)
                .profileFullName(profileFullName)
                .assignee(AssigneeEnum.fromValue(assignee))
                .dateFrom(startDateFrom)
                .dateTo(startDateTo)
                .page(page)
                .pageSize(pageSize).build();
    }

    public static BackofficeFilter getFilter(
            String profileFullName, LocalDate requestDateFrom, LocalDate requestDateTo,Integer pageSize, Integer page) {
        return BackofficeFilter.builder()
                .profileFullName(profileFullName)
                .dateFrom(requestDateFrom)
                .dateTo(requestDateTo)
                .page(page)
                .pageSize(pageSize).build();
    }

}
