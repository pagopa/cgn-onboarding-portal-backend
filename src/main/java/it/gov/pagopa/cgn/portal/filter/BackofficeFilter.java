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

    private LocalDate requestDateFrom;

    private LocalDate requestDateTo;

    private Integer pageSize;

    private Integer page;

}
