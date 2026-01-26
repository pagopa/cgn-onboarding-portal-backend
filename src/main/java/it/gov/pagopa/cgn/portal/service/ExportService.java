package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.converter.DataExportEycaWrapperConverter;
import it.gov.pagopa.cgn.portal.converter.DeleteDataExportEycaWrapperConverter;
import it.gov.pagopa.cgn.portal.converter.UpdateDataExportEycaWrapperConverter;
import it.gov.pagopa.cgn.portal.converter.referent.DataExportEycaWrapper;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClientException;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@Slf4j
@Service
public class ExportService {

    /*
    This is a security flag for avoid potential delete of items on CCDB production on eyca during integration tests
     */
    @Value("${eyca.api.delete.debug}")
    boolean eycaApiDeleteDebug;

    public static final String LIVE_YES = "Y";
    public static final String LIVE_NO = "N";
    public static final Integer LIVE_YES_INT = 1;
    public static final Integer LIVE_NO_INT = 0;
    public static final String JSON = "json";

    private final AgreementRepository agreementRepository;
    private final DiscountRepository discountRepository;

    private final EycaDataExportRepository eycaDataExportRepository;
    private final ConfigProperties configProperties;
    private final EycaExportService eycaExportService;
    private final DataExportEycaWrapperConverter dataExportEycaConverter;
    private final UpdateDataExportEycaWrapperConverter updateDataExportEycaConverter;
    private final DeleteDataExportEycaWrapperConverter deleteDataExportEycaConverter;
    private final EmailNotificationFacade emailNotificationFacade;

    public record EycaManualRowView(
            String initiativeId,
            String discountId,
            String provider,
            String genericCodeOrUrl,
            String limitOfUse,
            String validityStart,
            String validityEnd
    ) {

        public static EycaManualRowView of(String initiativeId,String discountId,String provider,
                String genericCodeOrUrl,String limitOfUse,String validityStart,String validityEnd
        ) {
            return new EycaManualRowView(initiativeId,discountId,provider,genericCodeOrUrl,limitOfUse,validityStart,
                    validityEnd
            );
        }
    }


    private final String[] eycaDataExportHeaders = new String[]{"ccdb_id",
                                                                "discount_id",
                                                                "id",
                                                                "state",
                                                                "categories",
                                                                "profile_id",
                                                                "vendor",
                                                                "eyca_update_id",
                                                                "name",
                                                                "start_date",
                                                                "end_date",
                                                                "name_local",
                                                                "text",
                                                                "text_local",
                                                                "email",
                                                                "phone",
                                                                "web",
                                                                "tags",
                                                                "image",
                                                                "live",
                                                                "location_local_id",
                                                                "street",
                                                                "city",
                                                                "zip",
                                                                "country",
                                                                "region",
                                                                "latitude",
                                                                "longitude",
                                                                "SalesChannel",
                                                                "discount_type",
                                                                "static_code",
                                                                "landing_page_url",
                                                                "landing_page_referrer",
                                                                "referent",
                                                                "eyca_landing_page_url"};

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
                                                                  "Eyca Landing page",
                                                                  "Id Operatore",
                                                                  "Id Agevolazione"};

    private final String[] exportEycaHeaders = new String[]{"DISCOUNT_ID",
                                                            "LOCAL_ID",
                                                            "STATE",
                                                            "CATEGORIES",
                                                            "VENDOR",
                                                            "EYCA_UPDATE_ID",
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
                                                            "SALES CHANNEL",
                                                            "DISCOUNT TYPE",
                                                            "STATIC_CODE",
                                                            "LANDING_PAGE_URL",
                                                            "LANDING PAGE REFERRER",
                                                            "EYCA_LANDING_PAGE_URL",};
    private final Predicate<SearchApiResponseEyca> notExistsOnEycaPredicate = sae -> sae.getApiResponse()!=null &&
                                                                                     sae.getApiResponse().getData()!=
                                                                                     null && sae.getApiResponse()
                                                                                                .getData()
                                                                                                .getDiscounts()!=null &&
                                                                                     ObjectUtils.isEmpty(sae.getApiResponse()
                                                                                                            .getData()
                                                                                                            .getDiscounts()
                                                                                                            .getData());

    private final Predicate<ListApiResponseEyca> hasDiscountsListApiPredicate =
            r -> r != null
                 && r.getApiResponse() != null
                 && r.getApiResponse().getData() != null
                 && r.getApiResponse().getData().getDiscount() != null
                 && !r.getApiResponse().getData().getDiscount().isEmpty();

    private final Predicate<ApiResponseEyca> hasDiscountsApiPredicate =
            r -> r != null
                 && r.getApiResponse() != null
                 && r.getApiResponse().getData() != null
                 && r.getApiResponse().getData().getDiscount() != null
                 && !r.getApiResponse().getData().getDiscount().isEmpty();


    public ExportService(AgreementRepository agreementRepository,
                         DiscountRepository discountRepository,
                         EycaDataExportRepository eycaDataExportRepository,
                         ConfigProperties configProperties,
                         EycaExportService eycaExportService,
                         DataExportEycaWrapperConverter dataExportEycaConverter,
                         UpdateDataExportEycaWrapperConverter updateDataExportEycaConverter,
                         DeleteDataExportEycaWrapperConverter deleteDataExportEycaConverter,
                         EmailNotificationFacade emailNotificationFacade) {
        this.agreementRepository = agreementRepository;
        this.discountRepository = discountRepository;
        this.eycaDataExportRepository = eycaDataExportRepository;
        this.configProperties = configProperties;
        this.eycaExportService = eycaExportService;
        this.dataExportEycaConverter = dataExportEycaConverter;
        this.updateDataExportEycaConverter = updateDataExportEycaConverter;
        this.deleteDataExportEycaConverter = deleteDataExportEycaConverter;
        this.emailNotificationFacade = emailNotificationFacade;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponseEntity<Resource> exportAgreements() {
        log.info("exportAgreements start");
        List<AgreementEntity> agreementEntities = agreementRepository.findAll();
        StringWriter writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL.builder().setDelimiter(';').build())) {
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
            log.error("exportAgreements end failure: {}", ex.getMessage());
            log.error(Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString).collect(joining("\n")));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponseEntity<Resource> exportEycaDiscounts() {
        log.info("exportEycaDiscounts start");
        List<EycaDataExportViewEntity> exportViewEntities = eycaDataExportRepository.findAll();

        StringWriter writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL.builder().setDelimiter(';').build())) {
            printerConsumer.apply(printer).accept(exportEycaHeaders);
            exportViewEntities.stream()
                              .map(r -> new String[]{Optional.ofNullable(r.getDiscountId()).orElse(0L).toString(),
                                                     Optional.ofNullable(r.getId()).orElse(0L).toString(),
                                                     r.getState(),
                                                     r.getCategories(),
                                                     r.getVendor(),
                                                     r.getEycaUpdateId(),
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
                                                     r.getSalesChannel(),
                                                     r.getDiscountType(),
                                                     r.getStaticCode(),
                                                     r.getLandingPageUrl(),
                                                     r.getLandingPageReferrer(),
                                                     r.getEycaLandingPageUrl()})
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
            log.error("exportEycaDiscounts end failure: {}", ex.getMessage());
            log.error(Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString).collect(joining("\n")));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ByteArrayResource buildEycaCsv(List<EycaDataExportViewEntity> exportViewEntities) {
        StringWriter writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL.builder().setDelimiter(';').build())) {

            DateTimeFormatter ddMMyyyy = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            printerConsumer.apply(printer).accept(eycaDataExportHeaders);
            exportViewEntities.stream()
                              .map(r -> new String[]{
                                                     Optional.ofNullable(r.getCcdbId()).orElse("-"),
                                                     Optional.ofNullable(r.getDiscountId()).orElse(0L).toString(),
                                                     Optional.ofNullable(r.getId()).orElse(0L).toString(),
                                                     r.getState(),
                                                     r.getCategories(),
                                                     Optional.ofNullable(r.getProfileId()).orElse(0L).toString(),
                                                     r.getVendor(),
                                                     r.getEycaUpdateId(),
                                                     r.getName(),
                                                     Optional.ofNullable(r.getStartDate())
                                                             .map(ld -> ld.format(ddMMyyyy)).orElse(""),
                                                     Optional.ofNullable(r.getEndDate())
                                                             .map(ld -> ld.format(ddMMyyyy)).orElse(""),
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
                                                     r.getSalesChannel(),
                                                     r.getDiscountType(),
                                                     r.getStaticCode(),
                                                     r.getLandingPageUrl(),
                                                     r.getLandingPageReferrer(),
                                                     Optional.ofNullable(r.getReferent()).orElse(0L).toString(),
                                                     r.getEycaLandingPageUrl()})
                              .forEach(printerConsumer.apply(printer));


            log.info("buildEycaCsv end success");
            return new ByteArrayResource(writer.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            log.error("buildEycaCsv end failure: {}", ex.getMessage());
        }
        return new ByteArrayResource(new byte[]{});
    }


    @Transactional(Transactional.TxType.REQUIRED)
    public void sendDiscountsToEyca() {

        try {

            Optional<Boolean> eycaExportEnabled = Optional.ofNullable(configProperties.getEycaExportEnabled());
            if (!eycaExportEnabled.orElse(false)) {
                log.info("sendDiscountsToEyca aborted - eyca.export.enabled is FALSE");
                return;
            }

            log.info("sendDiscountsToEyca start");
            List<EycaDataExportViewEntity> exportViewEntities = eycaDataExportRepository.findAll();

            syncEycaUpdateIdOnEyca(exportViewEntities);

            List<DataExportEycaWrapper<DataExportEyca>> entitiesToCreateOnEyca = getWrappersToCreateOnEyca(
                    exportViewEntities);

            List<DataExportEycaWrapper<UpdateDataExportEyca>> entitiesToUpdateOnEyca = getWrappersToUpdateOnEyca(
                    exportViewEntities);

            List<DataExportEycaWrapper<DeleteDataExportEyca>> entitiesToDeleteOnEyca = getWrappersToDeleteOnEyca(
                    exportViewEntities);

            log.info("EYCA_LOG_CREATE:");
            createDiscountsOnEyca(entitiesToCreateOnEyca);

            log.info("EYCA_LOG_UPDATE:");
            updateDiscountsOnEyca(entitiesToUpdateOnEyca);

            log.info("EYCA_LOG_DELETE:");
            deleteDiscountsOnEyca(entitiesToDeleteOnEyca);

            List<Attachment> attachments = createAttachments(exportViewEntities,
                                                             entitiesToCreateOnEyca,
                                                             entitiesToUpdateOnEyca,
                                                             entitiesToDeleteOnEyca);

            emailNotificationFacade.notifyAdminForJobEyca(attachments,
                                                          entitiesToCreateOnEyca.size(),
                                                          entitiesToUpdateOnEyca.size(),
                                                          entitiesToDeleteOnEyca.size());
            log.info("sendDiscountsToEyca end success");

            if (!entitiesToCreateOnEyca.isEmpty() || !entitiesToUpdateOnEyca.isEmpty() ||
                !entitiesToDeleteOnEyca.isEmpty()) {

                List<EycaManualRowView> rowsToCreate = new ArrayList<>(getListForStaticCode(entitiesToCreateOnEyca, false));
                rowsToCreate.addAll(getListForLandingPage(entitiesToCreateOnEyca, false));

                List<EycaManualRowView> rowsToUpdate = new ArrayList<>(getListForStaticCode(entitiesToUpdateOnEyca, true));
                rowsToUpdate.addAll(getListForLandingPage(entitiesToUpdateOnEyca, true));

                //only for those that could not be deleted
                entitiesToDeleteOnEyca = entitiesToDeleteOnEyca.stream()
                                                               .filter(row -> Boolean.TRUE.equals(row.getToDeleteFromEycaAdmin()))
                                                               .toList();

                List<EycaManualRowView> rowsToDelete = new ArrayList<>(getListForStaticCode(entitiesToDeleteOnEyca, false));
                rowsToDelete.addAll(getListForLandingPage(entitiesToDeleteOnEyca, false));

                if (!rowsToCreate.isEmpty() || !rowsToUpdate.isEmpty() || !rowsToDelete.isEmpty()) {

                    emailNotificationFacade.notifyEycaAdmin(rowsToCreate, rowsToUpdate, rowsToDelete);
                    log.info("notifyEycaAdmin end success");

                    entitiesToUpdateOnEyca.stream()
                                          .filter(row -> Boolean.TRUE.equals(row.getEycaEmailUpdateRequired()))
                                          .forEach(row -> {
                                              Optional<DiscountEntity> dbEntityOpt = discountRepository.findByEycaUpdateId(
                                                      row.getEycaUpdateId());
                                              if (dbEntityOpt.isPresent()) {
                                                  DiscountEntity dbEntity = dbEntityOpt.get();
                                                  dbEntity.setEycaEmailUpdateRequired(false);
                                                  discountRepository.save(dbEntity);
                                              } else {
                                                  log.info(
                                                          "EycaEmailUpdateRequired not setted to false, discount not found on CGN with eyca_update_id: " +
                                                          row.getEycaUpdateId());
                                              }
                                          });
                } else {
                    log.info("MAIL-BODY-ADMIN-EYCA: no data to send");
                    log.info("notifyEycaAdmin not sended");
                }
            }
        } catch (Exception ex) {
            log.error("sendDiscountsToEyca end failure: " + ex.getMessage());
            log.error(Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString).collect(joining("\n")));
        }
    }

    private List<Attachment> createAttachments(List<EycaDataExportViewEntity> exportViewEntities,
                                               List<DataExportEycaWrapper<DataExportEyca>> entitiesToCreateOnEyca,
                                               List<DataExportEycaWrapper<UpdateDataExportEyca>> entitiesToUpdateOnEyca,
                                               List<DataExportEycaWrapper<DeleteDataExportEyca>> entitiesToDeleteOnEyca) {
        List<Attachment> attachments = new ArrayList<>();

        if (!exportViewEntities.isEmpty()) {
            attachments.add(new Attachment("all.csv", buildEycaCsv(exportViewEntities)));
        }

        if (!entitiesToCreateOnEyca.isEmpty()) {
            attachments.add(new Attachment("createOnEyca.csv",
                                           buildEycaCsv(createOnEycaStream(exportViewEntities).toList())));
        }

        if (!entitiesToUpdateOnEyca.isEmpty()) {
            attachments.add(new Attachment("updateOnEyca.csv",
                                           buildEycaCsv(updateOnEycaStream(exportViewEntities).toList())));
        }

        if (!entitiesToDeleteOnEyca.isEmpty()) {
            attachments.add(new Attachment("deleteOnEyca.csv",
                                           buildEycaCsv(deleteOnEycaStream(exportViewEntities).toList())));
        }

        return attachments;
    }

    private <T> List<EycaManualRowView> getListForStaticCode(List<DataExportEycaWrapper<T>> entitiesForEyca,
                                                    Boolean eycaEmailUpdateRequired) {
        return entitiesForEyca.stream()
                              .filter(dew -> (DiscountCodeTypeEnum.STATIC.getEycaDataCode()
                                                                         .equals(dew.getDiscountType()) ||
                                              "N/A".equals(dew.getDiscountType())) &&
                                             eycaEmailUpdateRequired.equals(dew.getEycaEmailUpdateRequired()))
                              .map(dew -> EycaManualRowView.of(
                                                       dew.getCcdbId(),
                                                       dew.getEycaUpdateId(),
                                                       dew.getVendor(),
                                                       dew.getStaticCode(),
                                                       dew.getLimitOfUse(),
                                                       dew.getStartDate(),
                                                       dew.getEndDate())
                              )
                              .toList();
    }

    private <T> List<EycaManualRowView> getListForLandingPage(List<DataExportEycaWrapper<T>> entitiesForEyca,
                                                     Boolean eycaEmailUpdateRequired) {
        return entitiesForEyca.stream()
                              .filter(dew -> DiscountCodeTypeEnum.LANDINGPAGE.getEycaDataCode()
                                                                             .equals(dew.getDiscountType()) &&
                                             eycaEmailUpdateRequired.equals(dew.getEycaEmailUpdateRequired()))
                              .map(dew -> EycaManualRowView.of(
                                                       dew.getCcdbId(),
                                                       dew.getEycaUpdateId(),
                                                       dew.getVendor(),
                                                       dew.getEycaLandingPageUrl(),
                                                       dew.getLimitOfUse(),
                                                       dew.getStartDate(),
                                                       dew.getEndDate())
                              )
                              .toList();
    }

    public void syncEycaUpdateIdOnEyca(List<EycaDataExportViewEntity> exportViewEntities) {

        List<SearchDataExportEyca> exportEycaList = getItemsToSearchOnEyca(exportViewEntities);

        if (exportEycaList.isEmpty()) {
            log.info("No discounts to search for sync eycaUpdateId");
        }

        eycaExportService.authenticateOnEyca();

        log.info("Searching discounts on EYCA for sync eycaUpdateId...");

        exportEycaList.forEach(exportEyca -> {

            log.info("SEARCH SearchDataExportEyca: " + exportEyca.toString());

            try {
                String ccdbId = getCcdbIdOnEyca(exportEyca);
                if(ccdbId == null) {
                    String eycaUpdateId = exportEyca.getId();
                    DiscountEntity entity = discountRepository.findByEycaUpdateId(eycaUpdateId)
                                                              .orElseThrow(() -> new CGNException(
                                                                      "Discount with EycaUpdateId: " + eycaUpdateId +
                                                                      " from eyca not found on Discount table"));

                    //remove eycaUpdateId from db and from item with same eycaUpdateId on memory
                    EycaDataExportViewEntity viewItem = exportViewEntities.stream()
                                                                          .filter(d -> entity.getEycaUpdateId()
                                                                                             .equals(d.getEycaUpdateId()))
                                                                          .findFirst()
                                                                          .orElse(null);
                    if (viewItem!=null) {
                        entity.setEycaUpdateId(null);
                        discountRepository.saveAndFlush(entity);
                        viewItem.setEycaUpdateId(null);
                    }
                }
                else {
                    exportViewEntities.stream()
                                      .filter(d -> exportEyca.getId().equals(d.getEycaUpdateId()))
                                      .findFirst()
                                      .ifPresent(viewItem -> viewItem.setCcdbId(ccdbId));
                }
            } catch (RestClientException rce) {
                log.info("SEARCH eycaApi.searchDiscount Exception: " + rce.getMessage());
            }
        });

        ListApiResponseEyca response = eycaExportService.listDiscounts(1, 1000, JSON);

        log.info("Verifying that discounts to delete on Eyca...");

        /*
            IMPORTANT: foreach item present on eyca, verify presence on discounts table. If not, builds simple viewEntity just for delete on CCDB.
            Delete not occur here but after this sync, the viewEntity is only inserted on the exportViewEntities as to be deleted
        */

        List<DiscountItemEyca> discountItemsEyca = getDiscountItemsOnEyca(response);
        discountItemsEyca.forEach(discountItemEyca -> {
            if (discountRepository.findByEycaUpdateId(discountItemEyca.getId()).isEmpty()) {
                EycaDataExportViewEntity entityToDelete = new EycaDataExportViewEntity();
                entityToDelete.setEycaUpdateId(discountItemEyca.getId());
                entityToDelete.setId(0L);
                entityToDelete.setLive(LIVE_NO);
                entityToDelete.setVendor(discountItemEyca.getVendor());
                entityToDelete.setDiscountType("N/A");
                entityToDelete.setCcdbId(discountItemEyca.getCcdbId());
                exportViewEntities.add(entityToDelete);
            }
        });


    }

    public List<DiscountItemEyca> getDiscountItemsOnEyca(ListApiResponseEyca response) {

        if (hasDiscountsListApiPredicate.test(response)) {

            List<DiscountItemEyca> discountItemsEyca = response.getApiResponse()
                                                               .getData()
                                                               .getDiscount()
                                                               .stream()
                                                               .toList();

            if (!discountItemsEyca.isEmpty()) {
                log.info(response.getApiResponse()
                                 .getData()
                                 .getDiscount()
                                 .stream()
                                 .map(DiscountItemEyca::getId)
                                 .toList()
                                 .toString());
            }
            return discountItemsEyca;
        } else {
            return Collections.emptyList();
        }
    }

    String getCcdbIdOnEyca(SearchDataExportEyca exportEyca)
            throws RestClientException {
        SearchApiResponseEyca sae = eycaExportService.searchDiscount(exportEyca, JSON, false);
        if(notExistsOnEycaPredicate.test(sae)){
            sae = eycaExportService.searchDiscount(exportEyca, JSON, true);
            if(notExistsOnEycaPredicate.test(sae)){
                return null;
            }

        }
        return sae.getApiResponse().getData().getDiscounts().getData().getFirst().getCcdbId();
    }


    private List<SearchDataExportEyca> getItemsToSearchOnEyca(List<EycaDataExportViewEntity> exportViewEntities) {
        return updateOnEycaStream(exportViewEntities).map(this::convertToSearchDataExportEyca).toList();
    }

    public SearchDataExportEyca convertToSearchDataExportEyca(EycaDataExportViewEntity entity) {
        SearchDataExportEyca searchDataExportEyca = new SearchDataExportEyca();
        Optional<String> optDiscountId = Optional.ofNullable(entity.getEycaUpdateId());
        searchDataExportEyca.setId(optDiscountId.orElseThrow(() -> new CGNException(
                "Error during viewEntity to search conversion, eycaUpdateId is empty")));
        searchDataExportEyca.setLive(1);
        return searchDataExportEyca;
    }


    private List<DataExportEycaWrapper<UpdateDataExportEyca>> getWrappersToUpdateOnEyca(List<EycaDataExportViewEntity> exportViewEntities) {
        return updateOnEycaStream(exportViewEntities).map(updateDataExportEycaConverter::toDto).toList();
    }

    private List<DataExportEycaWrapper<DataExportEyca>> getWrappersToCreateOnEyca(List<EycaDataExportViewEntity> exportViewEntities) {

        return createOnEycaStream(exportViewEntities).map(dataExportEycaConverter::toDto).toList();
    }

    public List<DataExportEycaWrapper<DeleteDataExportEyca>> getWrappersToDeleteOnEyca(List<EycaDataExportViewEntity> exportViewEntities) {

        return deleteOnEycaStream(exportViewEntities).map(deleteDataExportEycaConverter::toDto).toList();
    }

    private Stream<EycaDataExportViewEntity> deleteOnEycaStream(List<EycaDataExportViewEntity> exportViewEntities) {
        return exportViewEntities.stream()
                                 .filter(e -> LIVE_NO.equalsIgnoreCase(e.getLive()))
                                 .filter(e -> !StringUtils.isEmpty(e.getEycaUpdateId()));
    }

    private Stream<EycaDataExportViewEntity> updateOnEycaStream(List<EycaDataExportViewEntity> exportViewEntities) {
        return exportViewEntities.stream()
                                 .filter(e -> LIVE_YES.equalsIgnoreCase(e.getLive()))
                                 .filter(e -> !StringUtils.isEmpty(e.getEycaUpdateId()));
    }

    private Stream<EycaDataExportViewEntity> createOnEycaStream(List<EycaDataExportViewEntity> exportViewEntities) {
        return exportViewEntities.stream()
                                 .filter(e -> LIVE_YES.equalsIgnoreCase(e.getLive()))
                                 .filter(e -> StringUtils.isEmpty(e.getEycaUpdateId()));
    }

    private void createDiscountsOnEyca(List<DataExportEycaWrapper<DataExportEyca>> exportEycaList) {

        if (exportEycaList.isEmpty()) {
            log.info("List of EYCA Discounts to be created is empty");
            return;
        }

        eycaExportService.authenticateOnEyca();

        log.info("Creating new discount on EYCA...");

        exportEycaList.forEach(exportEycaWrapper -> {
            DataExportEyca exportEyca = exportEycaWrapper.getDataExportEyca();

            log.info("CREATE DataExportEyca: {}", CGNUtils.toJson(exportEyca));
            ApiResponseEyca response = null;
            try {
                response = eycaExportService.createDiscount(exportEyca, JSON);

                if (Objects.nonNull(response)) {
                    log.info("Create Response:");
                    log.info(response.toString());
                }

                DiscountEntity entity = discountRepository.findById(exportEycaWrapper.getDiscountID())
                                                          .orElseThrow(() -> new CGNException("discountId from Eyca " +
                                                                                              exportEycaWrapper.getDiscountID() +
                                                                                              " not found on Discount table"));

                if (hasDiscountsApiPredicate.test(response)) {

                    DiscountItemEyca die = response.getApiResponse().getData().getDiscount().getFirst();

                    entity.setEycaUpdateId(die.getId());
                    discountRepository.save(entity);
                    exportEycaWrapper.setEycaUpdateId(die.getId());
                    exportEycaWrapper.setCcdbId(die.getCcdbId());
                }
            } catch (RestClientException | CGNException e) {
                log.info("CREATE eycaApi.createDiscount Exception>>: {}", e.getMessage());
            }
        });
    }

    private void updateDiscountsOnEyca(List<DataExportEycaWrapper<UpdateDataExportEyca>> exportEycaList) {

        if (exportEycaList.isEmpty()) {
            log.info("List of EYCA Discounts to be updated is empty");
            return;
        }
        eycaExportService.authenticateOnEyca();

        log.info("Updating old discount on EYCA...");

        exportEycaList.forEach(exportEycaWrapper -> {
            UpdateDataExportEyca exportEyca = exportEycaWrapper.getDataExportEyca();

            log.info("UPDATE UpdateDataExportEyca: " + CGNUtils.toJson(exportEyca));
            ApiResponseEyca response = null;
            try {

                response = eycaExportService.updateDiscount(exportEyca, JSON);

                if (Objects.nonNull(response)) {
                    log.info("Update Response:");
                    log.info(response.toString());
                }
            } catch (RestClientException | CGNException e) {
                log.info("UPDATE eycaApi.updateDiscount Exception: {}", e.getMessage());
            }
        });
    }


    public void deleteDiscountsOnEyca(List<DataExportEycaWrapper<DeleteDataExportEyca>> exportEycaList) {

        if (exportEycaList.isEmpty()) {
            log.info("List of EYCA Discounts to be deleted is empty");
            return;
        }
        eycaExportService.authenticateOnEyca();

        log.info("Deleting discount on EYCA...");

        exportEycaList.forEach(exportEycaWrapper -> {
            DeleteDataExportEyca exportEyca = exportEycaWrapper.getDataExportEyca();

            log.info("DELETE DeleteDataExportEyca: {}", exportEyca.toString());
            DeleteApiResponseEyca response = null;
            try {
                if (!eycaApiDeleteDebug) {
                    response = eycaExportService.deleteDiscount(exportEyca, JSON);
                }

                if (Objects.nonNull(response)) {
                    log.info("Delete Response:");
                    log.info(response.toString());
                }

                String eycaUpdateId = exportEyca.getId();
                DiscountEntity entity = discountRepository.findByEycaUpdateId(eycaUpdateId).orElse(null);
                //the entity can be null for opportunities to be deleted on eyca but not present on CGN.
                if (entity!=null) {
                    entity.setEycaUpdateId(null);
                    discountRepository.save(entity);
                }

            } catch (RestClientException rce) {
                log.info("DELETE eycaApi.deleteDiscount Exception: " + rce.getMessage());
                if (rce.getMessage()!=null &&
                    rce.getMessage().contains(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())) {
                    exportEycaWrapper.setToDeleteFromEycaAdmin(true);
                }
            }
        });
    }

    private final BiFunction<AgreementEntity, Optional<DiscountEntity>, String[]> agreementWithProfileAndDiscountToStringArray = (agreement, maybeDiscount) -> new String[]{
            agreement.getState().getCode(),
            Optional.ofNullable(agreement.getProfile())
                    .map(ProfileEntity::getFullName).orElse(agreement.getOrganizationName()),
            Optional.ofNullable(agreement.getProfile()).map(ProfileEntity::getName).orElse(null),
            Optional.ofNullable(agreement.getProfile()).map(ProfileEntity::getNameEn).orElse(null),
            Optional.ofNullable(agreement.getProfile())
                    .map(ProfileEntity::getSalesChannel)
                    .map(SalesChannelEnum::getCode).orElse(null),
            Optional.ofNullable(agreement.getProfile())
                    .map(ProfileEntity::getDiscountCodeType)
                    .map(DiscountCodeTypeEnum::getCode).orElse(null),
            Optional.ofNullable(agreement.getProfile()).map(ProfileEntity::getWebsiteUrl).orElse(null),
            maybeDiscount.map(DiscountEntity::getName).orElse(null),
            maybeDiscount.map(DiscountEntity::getNameEn).orElse(null),
            maybeDiscount.map(DiscountEntity::getDescription).orElse(null),
            maybeDiscount.map(DiscountEntity::getDescriptionEn).orElse(null),
            maybeDiscount.map(DiscountEntity::getDiscountValue).map(Objects::toString).orElse(null),
            maybeDiscount.map(d -> DiscountStateEnum.PUBLISHED.equals(d.getState()) &&
                                   d.getEndDate().compareTo(LocalDate.now()) < 0 ? "EXPIRED":d.getState())
                         .map(Objects::toString).orElse(null),
            maybeDiscount.map(DiscountEntity::getStartDate).map(Objects::toString).orElse(null),
            maybeDiscount.map(DiscountEntity::getEndDate).map(Objects::toString).orElse(null),
            maybeDiscount.map(DiscountEntity::getVisibleOnEyca).map(Objects::toString).orElse(null),
            maybeDiscount.map(DiscountEntity::getCondition).orElse(null),
            maybeDiscount.map(DiscountEntity::getConditionEn).orElse(null),
            maybeDiscount.map(DiscountEntity::getDiscountUrl).orElse(null),
            maybeDiscount.map(DiscountEntity::getProducts)
                         .map(l -> l.stream()
                                    .map(e -> e.getProductCategory().getDescription())
                                    .collect(joining(", "))).orElse(null),
            maybeDiscount.map(DiscountEntity::getStaticCode).orElse(null),
            maybeDiscount.map(DiscountEntity::getLandingPageUrl).orElse(null),
            maybeDiscount.map(DiscountEntity::getLandingPageReferrer).orElse(null),
            maybeDiscount.map(DiscountEntity::getEycaLandingPageUrl).orElse(null),
            Optional.ofNullable(agreement.getProfile()).map(ProfileEntity::getId).map(Objects::toString).orElse(null),
            maybeDiscount.map(DiscountEntity::getId).map(Objects::toString).orElse(null)};

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
}