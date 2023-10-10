package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.converter.DataExportEycaConverter;
import it.gov.pagopa.cgn.portal.converter.referent.DataExportEycaExtension;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import it.gov.pagopa.cgn.portal.repository.EycaDataExportRepository;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.ApiResponseEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DataExportEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.UpdateDataExportEyca;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

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
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExportService {

    private final AgreementRepository agreementRepository;
    private final DiscountRepository discountRepository;

    private final EycaDataExportRepository eycaDataExportRepository;
    private final ConfigProperties configProperties;
    private final EycaExportService eycaExportService;
    private final DataExportEycaConverter dataExportEycaConverter;


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


    private static final String LANDING_PAGE = "LANDINGPAGE";

    public ExportService(AgreementRepository agreementRepository, DiscountRepository discountRepository, EycaDataExportRepository eycaDataExportRepository,
                         ConfigProperties configProperties, EycaExportService eycaExportService,
                         DataExportEycaConverter dataExportEycaConverter) {
        this.agreementRepository = agreementRepository;
        this.discountRepository = discountRepository;
        this.eycaDataExportRepository = eycaDataExportRepository;
        this.configProperties = configProperties;
        this.eycaExportService = eycaExportService;
        this.dataExportEycaConverter = dataExportEycaConverter;
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
    public ResponseEntity<String> sendDiscountsToEyca() {

    //    Optional<Boolean> eycaExportEnabled = Optional.ofNullable(configProperties.getEycaExportEnabled());
    //    if (eycaExportEnabled.isEmpty() || Boolean.FALSE.equals(eycaExportEnabled.get())) {
    //        log.info("sendDiscountsToEyca aborted - eyca.export.enabled is FALSE");
    //        return null;
     //   }

        log.info("sendDiscountsToEyca start");
        List<EycaDataExportViewEntity> exportViewEntities = getFakeEntityList();
               // eycaDataExportRepository.findAll();

        if (exportViewEntities.isEmpty()) {
            log.info("No EYCA data to export");
            return null;
        }

        String eycaNotAllowedDiscountModes = configProperties.getEycaNotAllowedDiscountModes();

        try {
            List<DataExportEycaExtension> exportEycaList = exportViewEntities.stream()
                    .filter(entity -> !StringUtils.isBlank(entity.getDiscountType()))
                    .filter(entity -> !listFromCommaSeparatedString.apply(eycaNotAllowedDiscountModes)
                            .contains(entity.getDiscountType()))
                    .filter(entity -> !(entity.getDiscountType().equals(LANDING_PAGE) && !Objects.isNull(entity.getReferent())))
                    .filter(entity -> !StringUtils.isBlank(entity.getLive()) && entity.getLive().equals("Y"))
                    .collect(Collectors.groupingBy(EycaDataExportViewEntity::getProfileId))
                    .entrySet().stream()
                    .map(dataExportEycaConverter::groupedEntityToDto)
                    .collect(Collectors.toList());

            createNewDicountsOnEyca(exportEycaList);
            updateOldDiscountsOnEyca(exportEycaList);

            log.info("sendDiscountsToEyca end success");

            return ResponseEntity.status(HttpStatus.OK).build();

        } catch (Exception ex) {
            log.error("sendDiscountsToEyca end failure: " + ex.getMessage());
            log.error(Arrays.stream(ex.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n")));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    private List<EycaDataExportViewEntity>  getFakeEntityList(){
        EycaDataExportViewEntity entity_0 = new EycaDataExportViewEntity();
        entity_0.setId(1L);
        entity_0.setText("textetext");
        entity_0.setCategories("SV");
        entity_0.setProfileId(1L);
        entity_0.setVendor("vendor_0");
        entity_0.setName("name_0");
        entity_0.setNameLocal("name_local_0");
        entity_0.setStreet("address0");
        entity_0.setDiscountType("STATIC");
        entity_0.setReferent(2L);
        entity_0.setLive("Y");

        EycaDataExportViewEntity entity_1 = new EycaDataExportViewEntity();
        entity_1.setId(2L);
        entity_1.setText("textetext");
        entity_1.setCategories("SV");
        entity_1.setProfileId(2L);
        entity_1.setVendor("vendor_1");
        entity_1.setName("name_1");
        entity_1.setNameLocal("name_local_1");
        entity_1.setStreet("address1");
        entity_1.setDiscountType("STATIC");
        entity_1.setLive("N");

        EycaDataExportViewEntity entity_2 = new EycaDataExportViewEntity();
        entity_2.setId(3L);
        entity_2.setText("textetext");
        entity_2.setCategories("SV");
        entity_2.setProfileId(3L);
        entity_2.setVendor("vendor_2");
        entity_2.setName("name_2");
        entity_2.setNameLocal("name_local_2");
        entity_2.setStreet("address2");
        entity_2.setDiscountType("STATIC");
        entity_2.setLive("Y");

        EycaDataExportViewEntity entity_3 = new EycaDataExportViewEntity();
        entity_3.setId(4L);
        entity_3.setText("textetext");
        entity_3.setCategories("SV");
        entity_3.setProfileId(4L);
        entity_3.setVendor("vendor_3");
        entity_3.setName("name_3");
        entity_3.setNameLocal("name_local_3");
        entity_3.setStreet("address3");
        entity_3.setDiscountType("STATIC");
        entity_3.setReferent(3L);

        EycaDataExportViewEntity entity_4 = new EycaDataExportViewEntity();
        entity_4.setId(5L);
        entity_4.setText("textetext");
        entity_4.setCategories("FD");
        entity_4.setProfileId(5L);
        entity_4.setVendor("vendor_4");
        entity_4.setName("name_4");
        entity_4.setNameLocal("name_local_4");
        entity_4.setStreet("address4");
        entity_4.setDiscountType("STATIC");
        entity_4.setLive("Y");

        EycaDataExportViewEntity entity_5 = new EycaDataExportViewEntity();
        entity_5.setId(6L);
        entity_5.setText("textetext");
        entity_5.setCategories("FD");
        entity_5.setProfileId(6L);
        entity_5.setVendor("vendor_5");
        entity_5.setName("name_5");
        entity_5.setNameLocal("name_local_5");
        entity_5.setStreet("address5");
        entity_5.setDiscountType("STATIC");
        entity_5.setReferent(4L);
        entity_5.setLive("N");

        EycaDataExportViewEntity entity_6 = new EycaDataExportViewEntity();
        entity_6.setId(7L);
        entity_6.setText("textetext");
        entity_6.setCategories("FD");
        entity_6.setProfileId(7L);
        entity_6.setVendor("vendor_6");
        entity_6.setName("name_6");
        entity_6.setNameLocal("name_local_6");
        entity_6.setStreet("address6");
        entity_6.setDiscountType("STATIC");
        entity_6.setReferent(4L);

        EycaDataExportViewEntity entity_7 = new EycaDataExportViewEntity();
        entity_7.setId(8L);
        entity_7.setText("textetext");
        entity_7.setCategories("FD");
        entity_7.setProfileId(7L);
        entity_7.setVendor("vendor_6");
        entity_7.setName("name_6");
        entity_7.setNameLocal("name_local_6");
        entity_7.setStreet("address6");
        entity_7.setDiscountType("STATIC");
        entity_7.setLive("Y");

        return Arrays.asList(entity_0,entity_1, entity_2, entity_3, entity_4, entity_5, entity_6, entity_7);

    }


    private void createNewDicountsOnEyca(List<DataExportEycaExtension> exportEycaList){
        List<DataExportEycaExtension> createList = exportEycaList.stream().
                filter(entity->entity.getEycaUpdateId()==null).collect(Collectors.toList());

        createList.forEach(exportEycaExtension -> {
                     DataExportEyca exportEyca = exportEycaExtension.getDataExportEyca();

            ApiResponseEyca response = eycaExportService.createDiscountWithAuthorization(exportEyca, "json");
            Optional<DiscountEntity> discountEntity = discountRepository.findById(exportEycaExtension.getDiscountID());

            discountEntity.ifPresent(entity -> {
                assert response.getApiResponse() != null &&
                        response.getApiResponse().getData() != null &&
                        response.getApiResponse().getData().getDiscounts() != null &&
                        response.getApiResponse().getData().getDiscounts().getData() != null;

                entity.setEycaUpdateId(response.getApiResponse().getData().getDiscounts().getData().get(0).getId());
                discountRepository.save(entity);
            });
            // Gestione del caso in cui discountEntity è vuoto (Optional vuoto)
        });

    }


    private void updateOldDiscountsOnEyca (List<DataExportEycaExtension> exportEycaList){

        List<UpdateDataExportEyca> updateList = exportEycaList.stream().
                filter(entity->!entity.getEycaUpdateId().isEmpty())
                    .map(dataExportEycaConverter::convertDataExportEycaExtension).collect(Collectors.toList());

        updateList.forEach(exportEyca -> eycaExportService.updateDiscountWithAuthorization(exportEyca, "json"));
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
