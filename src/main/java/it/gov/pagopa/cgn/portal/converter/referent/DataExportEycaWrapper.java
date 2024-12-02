package it.gov.pagopa.cgn.portal.converter.referent;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Max;

@ToString
@Getter
@Setter
public class DataExportEycaWrapper<T>{

    @JsonIgnore
    private Long discountID;
    @JsonIgnore
    @Max(24)
    private String eycaUpdateId;
    @JsonIgnore
    private String discountType;
    @JsonIgnore
    private String staticCode;
    @JsonIgnore
    private String landingPageUrl;

    @ToString.Exclude
    private T dataExportEyca;
    
    public DataExportEycaWrapper(T dataExportEyca) {
        this.dataExportEyca = dataExportEyca;
    }

}
