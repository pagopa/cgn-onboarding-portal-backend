package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.converter.DataExportEycaConverter;
import it.gov.pagopa.cgn.portal.converter.referent.DataExportEycaWrapper;
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

        Optional<Boolean> eycaExportEnabled = Optional.ofNullable(configProperties.getEycaExportEnabled());
        if (eycaExportEnabled.isEmpty() || Boolean.FALSE.equals(eycaExportEnabled.get())) {
            log.info("sendDiscountsToEyca aborted - eyca.export.enabled is FALSE");
            return null;
        }

        log.info("sendDiscountsToEyca start");
        List<EycaDataExportViewEntity> exportViewEntities = eycaDataExportRepository.findAll();

        exportViewEntities.forEach(exportViewEntity -> log.info(
                "<<EYCA_LOG>><<exportViewEntity<<: " +
                        exportViewEntity.toString()));

        if (exportViewEntities.isEmpty()) {
            log.info("No EYCA data to export");
            return null;
        }

        String eycaNotAllowedDiscountModes = configProperties.getEycaNotAllowedDiscountModes();

        try {
            List<DataExportEycaWrapper> upsertOnEycaList = exportViewEntities.stream()
                    .filter(entity -> !StringUtils.isBlank(entity.getDiscountType()))
                    .filter(entity -> !listFromCommaSeparatedString.apply(eycaNotAllowedDiscountModes)
                            .contains(entity.getDiscountType()))
                    .filter(entity -> !(entity.getDiscountType().equals(DiscountCodeTypeEnum.LANDINGPAGE.getEycaDataCode())
                            && !Objects.isNull(entity.getReferent())))
                    .filter(entity -> !StringUtils.isBlank(entity.getLive()) && entity.getLive().equals("Y"))
                    .collect(Collectors.groupingBy(EycaDataExportViewEntity::getDiscountId))
                    .entrySet().stream()
                    .map(dataExportEycaConverter::groupedEntityToDto)
                    .collect(Collectors.toList());

            if (upsertOnEycaList.isEmpty()){
                log.info("List to be sent to EYCA is empty");
                return ResponseEntity.status(HttpStatus.OK).build();
            }

            createNewDiscountsOnEyca(upsertOnEycaList);
            updateOldDiscountsOnEyca(upsertOnEycaList);

            List<DataExportEycaWrapper> deleteOnEycaList = exportViewEntities.stream()
                    .filter(entity -> StringUtils.isBlank(entity.getLive()) || entity.getLive().equals("N"))
                    .filter(entity -> !entity.getEndDate().isBefore(LocalDate.now().minusDays(1)))
                    .collect(Collectors.groupingBy(EycaDataExportViewEntity::getProfileId))
                    .entrySet().stream()
                    .map(dataExportEycaConverter::groupedEntityToDto)
                    .collect(Collectors.toList());

            updateOldDiscountsOnEyca(deleteOnEycaList);

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

    private void createNewDiscountsOnEyca(List<DataExportEycaWrapper> exportEycaList){
        eycaExportService.authenticateOnEyca();

        log.info("creating new discount on EYCA");

        List<DataExportEycaWrapper> createList = exportEycaList.stream().
                filter(entity->entity.getEycaUpdateId()==null).collect(Collectors.toList());

        createList.forEach(exportEycaWrapper -> {
            DataExportEyca exportEyca = exportEycaWrapper.getDataExportEyca();

            log.info("<<EYCA_LOG>><<CREATE_exportEyca<<: " + exportEyca.toString());
            ApiResponseEyca response = null;
            try {
                response = eycaExportService.createDiscount(exportEyca, "json");
            } catch (RestClientException rce) {
                log.info("<<EYCA_LOG>><<eycaApi.createDiscount Exception>>: " + rce.getMessage());
            }
            Optional<DiscountEntity> discountEntity = discountRepository.findById(exportEycaWrapper.getDiscountID());

            ApiResponseEyca finalResponse = response;
            discountEntity.ifPresent(entity -> {
                if(finalResponse !=null &&
                        finalResponse.getApiResponse() != null &&
                        finalResponse.getApiResponse().getData() != null &&
                        finalResponse.getApiResponse().getData().getDiscount() != null){
                    entity.setEycaUpdateId(finalResponse.getApiResponse().getData().getDiscount().get(0).getId());
                    discountRepository.save(entity);
                }

            });
        });

    }


    private void updateOldDiscountsOnEyca (List<DataExportEycaWrapper> exportEycaList) {
        eycaExportService.authenticateOnEyca();
        log.info("updating old discount on EYCA");
        List<UpdateDataExportEyca> updateList = exportEycaList.stream()
                .filter(entity->!StringUtils.isEmpty(entity.getEycaUpdateId()))
                .map(dataExportEycaConverter::convertToUpdateDataExportEyca).collect(Collectors.toList());

        updateList.forEach(exportEyca ->
                {  log.info("<<EYCA_LOG>><<UPDATE_exportEyca<<: " + exportEyca.toString());
                    ApiResponseEyca apiResponse = null;
                    try {
                        apiResponse = eycaExportService.updateDiscount(exportEyca, "json");
                    }  catch (RestClientException rce) {
                        log.info("<<EYCA_LOG>><<eycaApi.updateDiscount Exception>>: " + rce.getMessage());
                    }

                    if (Objects.nonNull(apiResponse)){
                        log.info(apiResponse.toString());
                    }
                }
        );
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
