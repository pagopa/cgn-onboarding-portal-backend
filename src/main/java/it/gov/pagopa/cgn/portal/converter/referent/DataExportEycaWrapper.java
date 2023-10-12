package it.gov.pagopa.cgn.portal.converter.referent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DataExportEyca;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;

@Getter
@Setter
public class DataExportEycaWrapper {

    @JsonIgnore
    private Long discountID;
    @JsonIgnore
    @Max(24)
    private String eycaUpdateId;
    private DataExportEyca dataExportEyca;

    public DataExportEycaWrapper(DataExportEyca dataExportEyca) {
        this.dataExportEyca = dataExportEyca;
    }

    public DataExportEyca getDataExportEyca (){
        return this.dataExportEyca;
    }

}
