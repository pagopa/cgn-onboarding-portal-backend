package it.gov.pagopa.cgn.portal.converter.referent;

import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DataExportEyca;

public class DataExportEycaExtension extends DataExportEyca {

   private Boolean create;

    public Boolean getCreate() {
        return create;
    }

    public void setCreate(Boolean create) {
       this.create = create;
    }
}
