package it.gov.pagopa.cgn.portal;

import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgn.portal.service.ExportService;
import it.gov.pagopa.cgn.portal.service.EycaExportService;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DataExportEyca;
import jdk.dynalink.linker.LinkerServices;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class CGNOnboardingPortal {

    public static void main(String[] args) {
        ApplicationContext applicationContext =SpringApplication.run(CGNOnboardingPortal.class, args);

        ExportService exportService = applicationContext.getBean(ExportService.class);

        exportService.sendDiscountsToEyca();


/*
        EycaExportService service = applicationContext.getBean(EycaExportService.class);
        DataExportEyca requestEycaDataExport = new DataExportEyca();
        requestEycaDataExport.setLive(0);
        requestEycaDataExport.setEmail("email@email.com");
        requestEycaDataExport.setName("name");
        requestEycaDataExport.setLocalId("localId_007");
       requestEycaDataExport.setPhone("944954893");
        requestEycaDataExport.setNameLocal("nameLocal");
       requestEycaDataExport.setVendor("vendor");
        requestEycaDataExport.setText("dudatext");
       service.createDiscountWithAuthorization(requestEycaDataExport, "json");
*/




        SpringApplication.run(CGNOnboardingPortal.class, args);
    }

}