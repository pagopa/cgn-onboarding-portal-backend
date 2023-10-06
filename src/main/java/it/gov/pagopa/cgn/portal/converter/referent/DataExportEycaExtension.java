package it.gov.pagopa.cgn.portal.converter.referent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DataExportEyca;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;

@Getter
@Setter
public class DataExportEycaExtension extends DataExportEyca {

    @JsonIgnore
    private Long discountID;
    @JsonIgnore
    @Max(24)
    private String eycaUpdateId;

}
