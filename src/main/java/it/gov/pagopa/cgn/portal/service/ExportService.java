package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.converter.*;
import it.gov.pagopa.cgn.portal.converter.referent.DataExportEycaWrapper;
import it.gov.pagopa.cgn.portal.email.*;
import it.gov.pagopa.cgn.portal.email.EmailParams.Attachment;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import it.gov.pagopa.cgn.portal.repository.EycaDataExportRepository;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import javax.transaction.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.*;

@Slf4j
@Service
public class ExportService {

    public static final String LIVE_YES="Y";
    public static final String LIVE_NO="N";

    private final AgreementRepository agreementRepository;
    private final DiscountRepository discountRepository;

    private final EycaDataExportRepository eycaDataExportRepository;
    private final ConfigProperties configProperties;
    private final EycaExportService eycaExportService;
    private final DataExportEycaWrapperConverter dataExportEycaConverter;
    private final UpdateDataExportEycaWrapperConverter updateDataExportEycaConverter;
    
    private EmailNotificationFacade emailNotificationFacade;
    
    private final String[] eycaDataExportHeaders = new String[] {
    		"id","categories","profile_id","vendor","discount_id","eyca_update_id","name","start_date","end_date","name_local","text","text_local","email","phone","web","tags","image","live","location_local_id","street","city","zip","country","region","latitude","longitude","discount_type","referent"
    };

    private final String[] exportAgreementsHeaders = new String[]{"Stato Convenzione",
            "Ragione sociale",
            "Nome alternativo",
            "Nome alternativo inglese",
            "Canale di vendita",
            "Modalità di riconoscimento",
            "Sito",
            "Titolo agevolazione",
            "Titolo agevolazione inglese",
            "Descrizione agevolazione",
            "Descrizione agevolazione inglese",
            "Valore",
            "Stato agevolazione",
            "Data inizio",
            "Data fine",
            "Visibile su EYCA",
            "Condizioni",
            "Condizioni inglese",
            "Link agevolazione",
            "Categorie",
            "Codice statico",
            "Landing page",
            "Referer",
            "Id Operatore",
            "Id Agevolazione"};

    private final String[] exportEycaHeaders = new String[]{"LOCAL_ID",
            "CATEGORIES",
            "VENDOR",
            "NAME",
            "NAME_LOCAL",
            "TEXT",
            "TEXT_LOCAL",
            "EMAIL",
            "PHONE",
            "WEB",
            "TAGS",
            "IMAGE",
            "LIVE",
            "LOCATION_LOCAL_ID",
            "STREET",
            "CITY",
            "ZIP",
            "COUNTRY",
            "REGION",
            "GEO - Latitude",
            "GEO - Longitude",
            "DISCOUNT TYPE"};


    public ExportService(AgreementRepository agreementRepository, DiscountRepository discountRepository, EycaDataExportRepository eycaDataExportRepository,
                         ConfigProperties configProperties, EycaExportService eycaExportService,
                         DataExportEycaWrapperConverter dataExportEycaConverter, UpdateDataExportEycaWrapperConverter updateDataExportEycaConverter, 
                         EmailNotificationFacade emailNotificationFacade) {
		this.agreementRepository = agreementRepository;
        this.discountRepository = discountRepository;
        this.eycaDataExportRepository = eycaDataExportRepository;
        this.configProperties = configProperties;
        this.eycaExportService = eycaExportService;
        this.dataExportEycaConverter = dataExportEycaConverter;
        this.updateDataExportEycaConverter=updateDataExportEycaConverter;
        this.emailNotificationFacade = emailNotificationFacade;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponseEntity<Resource> exportAgreements() {
        log.info("exportAgreements start");
        List<AgreementEntity> agreementEntities = agreementRepository.findAll();
        StringWriter writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            printerConsumer.apply(printer).accept(exportAgreementsHeaders);
            agreementEntities.stream()
                    .map(expandAgreementToList)
                    .forEach(agreementData -> agreementData.forEach(printerConsumer.apply(printer)));

            byte[] export = writer.toString().getBytes(StandardCharsets.UTF_8);
            String filename = "export-" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv";

            log.info("exportAgreements end success");
            return ResponseEntity.ok()
                    .contentLength(export.length)
                    .contentType(MediaType.TEXT_PLAIN)
                    .cacheControl(CacheControl.noCache().mustRevalidate())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .body(new ByteArrayResource(export));
        } catch (Exception ex) {
            log.error("exportAgreements end failure: " + ex.getMessage());
            log.error(Arrays.stream(ex.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n")));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponseEntity<Resource> exportEycaDiscounts() {
        log.info("exportEycaDiscounts start");
        List<EycaDataExportViewEntity> exportViewEntities = eycaDataExportRepository.findAll();
        exportViewEntities = exportViewEntities.stream().distinct().collect(Collectors.toList());
        
        StringWriter writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            printerConsumer.apply(printer).accept(exportEycaHeaders);
            exportViewEntities.stream()
                    .map(r -> new String[]{r.getId().toString(),
                            r.getCategories(),
                            r.getVendor(),
                            r.getName(),
                            r.getNameLocal(),
                            r.getText(),
                            r.getTextLocal(),
                            r.getEmail(),
                            r.getPhone(),
                            r.getWeb(),
                            r.getTags(),
                            r.getImage(),
                            r.getLive(),
                            r.getLocationLocalId(),
                            r.getStreet(),
                            r.getCity(),
                            r.getZip(),
                            r.getCountry(),
                            r.getRegion(),
                            r.getLatitude(),
                            r.getLongitude(),
                            r.getDiscountType()})
                    .forEach(printerConsumer.apply(printer));

            byte[] export = writer.toString().getBytes(StandardCharsets.UTF_8);
            String filename = "export-eyca-" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv";

            log.info("exportEycaDiscounts end success");
            return ResponseEntity.ok()
                    .contentLength(export.length)
                    .contentType(MediaType.TEXT_PLAIN)
                    .cacheControl(CacheControl.noCache().mustRevalidate())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .body(new ByteArrayResource(export));
        } catch (Exception ex) {
            log.error("exportEycaDiscounts end failure: " + ex.getMessage());
            log.error(Arrays.stream(ex.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n")));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    @Transactional(Transactional.TxType.REQUIRED)
    public ByteArrayResource buildEycaCsv(List<EycaDataExportViewEntity> exportViewEntities) {
        StringWriter writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL)) {
        	
        	//"id","categories","profile_id","vendor","discount_id","eyca_update_id","name","start_date","end_date","name_local","text","text_local","email","phone","web","tags","image","live",
        	///"location_local_id","street","city","zip","country","region","latitude","longitude","discount_type","referent"
        	DateTimeFormatter ddMMyyyy = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        	
        	printerConsumer.apply(printer).accept(eycaDataExportHeaders);
            exportViewEntities.stream()
            .map(r -> new String[]{
            		r.getId().toString(),
                    r.getCategories(),
                    Optional.ofNullable(r.getProfileId()).orElse(Long.valueOf(0L)).toString(),
                    r.getVendor(),
                    Optional.ofNullable(r.getDiscountId()).orElse(Long.valueOf(0L)).toString(),
                    r.getEycaUpdateId(),
                    r.getName(),
                    Optional.ofNullable(r.getStartDate().format(ddMMyyyy)).orElse(""),
                    Optional.ofNullable(r.getEndDate().format(ddMMyyyy)).orElse(""),
                    r.getNameLocal(),
                    r.getText(),
                    r.getTextLocal(),
                    r.getEmail(),
                    r.getPhone(),
                    r.getWeb(),
                    r.getTags(),
                    r.getImage(),
                    r.getLive(),
                    r.getLocationLocalId(),
                    r.getStreet(),
                    r.getCity(),
                    r.getZip(),
                    r.getCountry(),
                    r.getRegion(),
                    r.getLatitude(),
                    r.getLongitude(),
                    r.getDiscountType(),
                    Optional.ofNullable(r.getReferent()).orElse(Long.valueOf(0L)).toString(),
                    })
            .forEach(printerConsumer.apply(printer));


            log.info("buildEycaCsv end success");
            return new ByteArrayResource(writer.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            log.error("buildEycaCsv end failure: " + ex.getMessage());
        }
        return new ByteArrayResource(new byte[] {});
    }
    
    
    @Transactional(Transactional.TxType.REQUIRED)
    public ResponseEntity<String> sendDiscountsToEyca() {
        
    	try {    	
    	    	
	        Optional<Boolean> eycaExportEnabled = Optional.ofNullable(configProperties.getEycaExportEnabled());
	        if (eycaExportEnabled.isEmpty() || Boolean.FALSE.equals(eycaExportEnabled.get())) {
	            log.info("sendDiscountsToEyca aborted - eyca.export.enabled is FALSE");
	            return null;
	        }
	
	        log.info("sendDiscountsToEyca start");
	        List<EycaDataExportViewEntity> exportViewEntities = eycaDataExportRepository.findAll();
            //per ovviare al problema delle righe duplicate in vista
            exportViewEntities = exportViewEntities.stream().distinct().collect(Collectors.toList());

	        if (exportViewEntities.isEmpty()) {
	            log.info("No EYCA data to export");
	            return null;
	        }	
	        
	    	//Tutte le agevolazioni (attive E in stato PUBLISHED: => Live=Y),con le sole LENDING PAGE con referent vuoto
	    	List<DataExportEycaWrapper<DataExportEyca>> entitiesToCreateOnEyca = getWrappersToCreateOnEyca(exportViewEntities);

	    	//Tutte le agevolazioni che avevano valorizzato il campo eycaUpdateId, precedentemente alla create.
			List<DataExportEycaWrapper<UpdateDataExportEyca>> entitiesToUpdateOnEyca = getWrappersToUpdateOnEyca(exportViewEntities);
			
			//Tutte le agevolazioni che sono andate in Live=N
			List<DeleteDataExportEyca> entitiesToDeleteOnEyca = getItemsToDeleteOnEyca(exportViewEntities);

	        log.info("EYCA_LOG_CREATE:");
	        createDiscountsOnEyca(entitiesToCreateOnEyca);
	            
	        log.info("EYCA_LOG_UPDATE:");
		    updateDiscountsOnEyca(entitiesToUpdateOnEyca);
		    
		    log.info("EYCA_LOG_DELETE:");
         	deleteDiscountsOnEyca(entitiesToDeleteOnEyca);
         	
            List<Attachment> attachments = new ArrayList<>();
            if(exportViewEntities.size() > 0) {
            	attachments.add(new Attachment("all.csv", buildEycaCsv(exportViewEntities)));
            }
            
            if(entitiesToCreateOnEyca.size() > 0) {
            	attachments.add(new Attachment("createOnEyca.csv", buildEycaCsv(createOnEycaStream(exportViewEntities).collect(Collectors.toList()))));
            }
            
            if(entitiesToUpdateOnEyca.size() > 0) {
            	attachments.add(new Attachment("updateOnEyca.csv", buildEycaCsv(updateOnEycaStream(exportViewEntities).collect(Collectors.toList()))));
            }
            
            if(entitiesToDeleteOnEyca.size() > 0) {
            	attachments.add(new Attachment("deleteOnEyca.csv", buildEycaCsv(deleteOnEycaStream(exportViewEntities).collect(Collectors.toList()))));
            }
             
            String body = "All discounts for Eyca excluding duplicates: "+exportViewEntities.size()
            			  +"<br /> Discounts to create: "+entitiesToCreateOnEyca.size()
            			  +"<br /> Discounts to update: "+entitiesToUpdateOnEyca.size()
            			  +"<br /> Discounts to delete: "+entitiesToDeleteOnEyca.size();
            
            System.out.println("MAIL-BODY: "+body);
            
            emailNotificationFacade.notifyAdminForJobEyca(attachments,body);
            
            return ResponseEntity.status(HttpStatus.OK).build();

        } catch (Exception ex) {
            log.error("sendDiscountsToEyca end failure: " + ex.getMessage());
            log.error(Arrays.stream(ex.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n")));
        }
    	
        log.info("sendDiscountsToEyca end success");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  
    
//  private List<DataExportEycaWrapper> getWrappersToDeleteOnEyca(List<EycaDataExportViewEntity> exportViewEntities) {
//		return exportViewEntities.stream()
//				.filter(e -> e.getLive().equalsIgnoreCase("N"))
//	//1*        			.filter(e -> e.getLive().equalsIgnoreCase("N") && !e.getDiscountType().equals(DiscountCodeTypeEnum.BUCKET.getEycaDataCode()))        			
////				.filter(e -> !e.getEndDate().isBefore(LocalDate.now().minusDays(1)))
////              .collect(Collectors.groupingBy(EycaDataExportViewEntity::getDiscountId))
////              .entrySet().stream()
////              .map(dataExportEycaConverter::groupedEntityToDto)					
//		        .map(dataExportEycaConverter::toDto)
//		        .collect(Collectors.toList());		
//  }	
    
    private List<DeleteDataExportEyca> getItemsToDeleteOnEyca(List<EycaDataExportViewEntity> exportViewEntities) {
		return deleteOnEycaStream(exportViewEntities)
	//1*        			.filter(e -> e.getLive().equalsIgnoreCase("N") && !e.getDiscountType().equals(DiscountCodeTypeEnum.BUCKET.getEycaDataCode()))        			
		        .map(this::convertToDeleteDataExportEyca)
		        .collect(Collectors.toList());		
    }	
    
    public DeleteDataExportEyca convertToDeleteDataExportEyca(EycaDataExportViewEntity entity) {
        DeleteDataExportEyca deleteDataExportEyca = new DeleteDataExportEyca();
        Optional<String> optDiscountId = Optional.ofNullable(entity.getEycaUpdateId());
        deleteDataExportEyca.setId(optDiscountId.orElseThrow(() -> new CGNException("Error during viewEntity to delete conversion, eycaUpdateId is empty")));
        return deleteDataExportEyca;
    }

	
	private List<DataExportEycaWrapper<UpdateDataExportEyca>> getWrappersToUpdateOnEyca(List<EycaDataExportViewEntity> exportViewEntities) {
		return updateOnEycaStream(exportViewEntities)
		        .map(updateDataExportEycaConverter::toDto)
		        .collect(Collectors.toList());
	}

	private List<DataExportEycaWrapper<DataExportEyca>> getWrappersToCreateOnEyca(
			List<EycaDataExportViewEntity> exportViewEntities) {
		
		return createOnEycaStream(exportViewEntities)
		.filter(entity->StringUtils.isEmpty(entity.getEycaUpdateId()))
//        .collect(Collectors.groupingBy(EycaDataExportViewEntity::getDiscountId))
//        .entrySet().stream()
//        .map(dataExportEycaConverter::groupedEntityToDto)	            		
		.map(dataExportEycaConverter::toDto)
		.collect(Collectors.toList());
	}
	

    private Stream<EycaDataExportViewEntity> deleteOnEycaStream(List<EycaDataExportViewEntity> exportViewEntities) {
    	return exportViewEntities.stream()
				.filter(e -> LIVE_NO.equalsIgnoreCase(e.getLive()))
				.filter(e -> !StringUtils.isEmpty(e.getEycaUpdateId()));
    }    
    
    private Stream<EycaDataExportViewEntity> updateOnEycaStream(List<EycaDataExportViewEntity> exportViewEntities) {
    	return exportViewEntities.stream().filter(
    			e-> LIVE_YES.equalsIgnoreCase(e.getLive()) && !StringUtils.isEmpty(e.getEycaUpdateId())
    		   );
    }
	
	private Stream<EycaDataExportViewEntity> createOnEycaStream(List<EycaDataExportViewEntity> exportViewEntities) {
		return exportViewEntities.stream().filter(
		 		e ->
//1*        					 		e.getLive().equalsIgnoreCase("N") && e.getDiscountType().equals(DiscountCodeTypeEnum.BUCKET.getEycaDataCode())
//			 		||
                        (LIVE_YES.equalsIgnoreCase(e.getLive())  && !DiscountCodeTypeEnum.LANDINGPAGE.getEycaDataCode().equals(e.getDiscountType()))
				|| 
				(LIVE_YES.equalsIgnoreCase(e.getLive()) && DiscountCodeTypeEnum.LANDINGPAGE.getEycaDataCode().equals(e.getDiscountType())
						&& e.getReferent() == null)
		);
	}	

    private void createDiscountsOnEyca(List<DataExportEycaWrapper<DataExportEyca>> exportEycaList){

        eycaExportService.authenticateOnEyca();

        log.info("creating new discount on EYCA");

        if (exportEycaList.isEmpty()) {
            log.info("List of EYCA Discounts to be created is empty");
            return;
        }
        eycaExportService.authenticateOnEyca();

        exportEycaList.forEach(exportEycaWrapper -> {
            DataExportEyca exportEyca = exportEycaWrapper.getDataExportEyca();

            log.info("CREATE DataExportEyca: " + CGNUtils.toJson(exportEyca));
            ApiResponseEyca response = null;
            try {
                response = eycaExportService.createDiscount(exportEyca, "json");
                
                DiscountEntity entity = discountRepository.findById(exportEycaWrapper.getDiscountID())
                		.orElseThrow( () -> new CGNException("discountId from Eyca " 
                					+ exportEycaWrapper.getDiscountID()+" not found on Discount table"));

                if(response !=null &&
                		response.getApiResponse() != null &&
                		response.getApiResponse().getData() != null &&
                		response.getApiResponse().getData().getDiscount() != null){
                	entity.setEycaUpdateId(response.getApiResponse().getData().getDiscount().get(0).getId());
                    discountRepository.save(entity);
                }                
            } catch (RestClientException | CGNException e) {
                log.info("CREATE eycaApi.createDiscount Exception>>: " + e.getMessage());
            }
        });
    }

    private void updateDiscountsOnEyca(List<DataExportEycaWrapper<UpdateDataExportEyca>> exportEycaList) {
        log.info("updating old discount on EYCA");

        if (exportEycaList.isEmpty()) {
            log.info("List of EYCA Discounts to be updated is empty");
            return;
        }
        eycaExportService.authenticateOnEyca();

        exportEycaList.forEach(exportEycaWrapper -> {  
	        UpdateDataExportEyca exportEyca = exportEycaWrapper.getDataExportEyca();
	        
	    	log.info("UPDATE UpdateDataExportEyca: " + CGNUtils.toJson(exportEyca));
	        ApiResponseEyca response = null;
	        try {
	        	
	        	response = eycaExportService.updateDiscount(exportEyca, "json");
	        	
	            if (Objects.nonNull(response)){
	                log.info(response.toString());
	            }
	        }
	        catch (RestClientException | CGNException e) {
	            log.info("UPDATE eycaApi.updateDiscount Exception: " + e.getMessage());
	        }
	    });
    }


    private void deleteDiscountsOnEyca(List<DeleteDataExportEyca> exportEycaList) {
        log.info("deleting discount on EYCA");
//        List<DeleteDataExportEyca> deleteList = exportEycaList.stream()
//                .map(dataExportEycaConverter::convertToDeleteDataExportEyca).collect(Collectors.toList());

        if (exportEycaList.isEmpty()) {
            log.info("List of EYCA Discounts to be deleted is empty");
            return;
        }
        eycaExportService.authenticateOnEyca();

        exportEycaList.forEach(exportEyca -> {
        	
			log.info("DELETE DeleteDataExportEyca: " + exportEyca.toString());
            DeleteApiResponseEyca apiResponse = null;
            try {
                apiResponse = eycaExportService.deleteDiscount(exportEyca, "json");
                
                if (Objects.nonNull(apiResponse)){
                    log.info(apiResponse.toString());
                }

                String eycaUpdateId = exportEyca.getId();
	            DiscountEntity entity = discountRepository.findByEycaUpdateId(eycaUpdateId)
	            		.orElseThrow( () -> new CGNException("Discount with EycaUpdateId: "+eycaUpdateId+" from eyca not found on Discount table"));
	        	entity.setEycaUpdateId(null);
	            discountRepository.save(entity);
            }  
            catch (RestClientException rce) {
                log.info("DELETE eycaApi.deleteDiscount Exception: " + rce.getMessage());
            }
        });
    }



    private final BiFunction<AgreementEntity, Optional<DiscountEntity>, String[]>
            agreementWithProfileAndDiscountToStringArray
            = (agreement, maybeDiscount) -> new String[]{agreement.getState().getCode(),
            Optional.ofNullable(agreement.getProfile())
                    .map(ProfileEntity::getFullName).orElse(null),
            Optional.ofNullable(agreement.getProfile())
                    .map(ProfileEntity::getName).orElse(null),
            Optional.ofNullable(agreement.getProfile())
                    .map(ProfileEntity::getNameEn).orElse(null),
            Optional.ofNullable(agreement.getProfile())
                    .map(ProfileEntity::getSalesChannel)
                    .map(SalesChannelEnum::getCode).orElse(null),
            Optional.ofNullable(agreement.getProfile())
                    .map(ProfileEntity::getDiscountCodeType)
                    .map(DiscountCodeTypeEnum::getCode).orElse(null),
            Optional.ofNullable(agreement.getProfile())
                    .map(ProfileEntity::getWebsiteUrl).orElse(null),
            maybeDiscount.map(DiscountEntity::getName).orElse(null),
            maybeDiscount.map(DiscountEntity::getNameEn).orElse(null),
            maybeDiscount.map(DiscountEntity::getDescription).orElse(null),
            maybeDiscount.map(DiscountEntity::getDescriptionEn).orElse(null),
            maybeDiscount.map(DiscountEntity::getDiscountValue)
                    .map(Objects::toString).orElse(null),
            maybeDiscount.map(d -> DiscountStateEnum.PUBLISHED.equals(d.getState())
                            && d.getEndDate().compareTo(LocalDate.now()) < 0 ? "EXPIRED" : d.getState())
                    .map(Objects::toString).orElse(null),
            maybeDiscount.map(DiscountEntity::getStartDate)
                    .map(Objects::toString).orElse(null),
            maybeDiscount.map(DiscountEntity::getEndDate)
                    .map(Objects::toString).orElse(null),
            maybeDiscount.map(DiscountEntity::getVisibleOnEyca)
                    .map(Objects::toString).orElse(null),
            maybeDiscount.map(DiscountEntity::getCondition).orElse(null),
            maybeDiscount.map(DiscountEntity::getConditionEn).orElse(null),
            maybeDiscount.map(DiscountEntity::getDiscountUrl).orElse(null),
            maybeDiscount.map(DiscountEntity::getProducts)
                    .map(l -> l.stream()
                            .map(e -> e.getProductCategory()
                                    .getDescription())
                            .collect(Collectors.joining(", "))).orElse(
                    null),
            maybeDiscount.map(DiscountEntity::getStaticCode).orElse(null),
            maybeDiscount.map(DiscountEntity::getLandingPageUrl).orElse(
                    null),
            maybeDiscount.map(DiscountEntity::getLandingPageReferrer).orElse(
                    null),
            Optional.ofNullable(agreement.getProfile()).map(ProfileEntity::getId)
                    .map(Objects::toString).orElse(null),
            maybeDiscount.map(DiscountEntity::getId).map(Objects::toString).orElse(null)
    };

    private final Function<AgreementEntity, List<String[]>> expandAgreementToList = agreement -> {
        List<String[]> agreementRows = agreement.getDiscountList()
                .stream()
                .map(d -> agreementWithProfileAndDiscountToStringArray.apply(agreement,
                        Optional.of(
                                d)))
                .collect(Collectors.toList());

        if (agreementRows.isEmpty()) {
            agreementRows.add(agreementWithProfileAndDiscountToStringArray.apply(agreement, Optional.empty()));
        }

        return agreementRows;
    };

    private final Function<CSVPrinter, Consumer<String[]>> printerConsumer = printer -> row -> {
        try {
            printer.printRecord(row);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    };

    private final Function<String, List<String>> listFromCommaSeparatedString= value-> Optional.ofNullable(value)
            .map(str -> Arrays.stream(str.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList()))
            .orElse(new ArrayList<>());
}
