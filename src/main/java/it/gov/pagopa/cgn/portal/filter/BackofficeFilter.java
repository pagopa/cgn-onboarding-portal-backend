package it.gov.pagopa.cgn.portal.filter;

import it.gov.pagopa.cgn.portal.enums.AssigneeEnum;
import it.gov.pagopa.cgn.portal.enums.BackofficeApprovedSortColumnEnum;
import it.gov.pagopa.cgn.portal.enums.BackofficeRequestSortColumnEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class BackofficeFilter
        implements Serializable {

    private String agreementState;

    private String profileFullName;

    private AssigneeEnum assignee;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private Integer pageSize;

    private Integer page;

    private BackofficeRequestSortColumnEnum requestSortColumnEnum;

    private BackofficeApprovedSortColumnEnum approvedSortColumnEnum;

    private Sort.Direction sortDirection;


    public static BackofficeFilter getRequestFilter(String state,
                                                    String profileFullName,
                                                    String assignee,
                                                    LocalDate startDateFrom,
                                                    LocalDate startDateTo,
                                                    Integer pageSize,
                                                    Integer page,
                                                    String sortColumn,
                                                    String sortDirection) {

        return BackofficeFilter.builder()
                               .agreementState(state)
                               .profileFullName(profileFullName)
                               .assignee(AssigneeEnum.fromValue(assignee))
                               .dateFrom(startDateFrom)
                               .dateTo(startDateTo)
                               .page(page)
                               .pageSize(pageSize)
                               .requestSortColumnEnum(BackofficeRequestSortColumnEnum.fromValue(sortColumn))
                               .sortDirection(Sort.Direction.valueOf(sortDirection))
                               .build();
    }

    public static BackofficeFilter getApprovedFilter(String profileFullName,
                                                     LocalDate requestDateFrom,
                                                     LocalDate requestDateTo,
                                                     Integer pageSize,
                                                     Integer page,
                                                     String sortColumn,
                                                     String sortDirection) {
        return BackofficeFilter.builder()
                               .profileFullName(profileFullName)
                               .dateFrom(requestDateFrom)
                               .dateTo(requestDateTo)
                               .page(page)
                               .pageSize(pageSize)
                               .approvedSortColumnEnum(BackofficeApprovedSortColumnEnum.fromValue(sortColumn))
                               .sortDirection(Sort.Direction.valueOf(sortDirection))
                               .build();
    }

}
