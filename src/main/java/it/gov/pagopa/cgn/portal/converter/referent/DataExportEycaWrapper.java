package it.gov.pagopa.cgn.portal.converter.referent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;

@Getter
@Setter
public class DataExportEycaWrapper<T> {

    @JsonIgnore
    private Long discountID;
    @JsonIgnore
    @Max(24)
    private String eycaUpdateId;

    private T dataExportEyca;

    public DataExportEycaWrapper(T dataExportEyca) {
        this.dataExportEyca = dataExportEyca;
    }

    @Override
    public String toString() {
        return "DataExportEycaWrapper [discountID=" + discountID + ", eycaUpdateId=" + eycaUpdateId +
               ", dataExportEyca=" + dataExportEyca + "]";
    }
}
