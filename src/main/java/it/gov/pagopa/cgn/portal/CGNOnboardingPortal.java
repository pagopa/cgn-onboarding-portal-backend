package it.gov.pagopa.cgn.portal;

import it.gov.pagopa.cgn.portal.service.EycaExportService;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.RequestEycaDataExport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class CGNOnboardingPortal {

    public static void main(String[] args) {


        ApplicationContext applicationContext =SpringApplication.run(CGNOnboardingPortal.class, args);
        EycaExportService service = applicationContext.getBean(EycaExportService.class);
        RequestEycaDataExport requestEycaDataExport = new RequestEycaDataExport();
        requestEycaDataExport.setLive(0);
        requestEycaDataExport.setEmail("email@email.com");
        requestEycaDataExport.setName("name");
        requestEycaDataExport.setLocalId("localId");
        requestEycaDataExport.setPhone("944954893");
        requestEycaDataExport.setNameLocal("nameLocal");

        service.createDiscountWithAuthorization(requestEycaDataExport);


    }




}