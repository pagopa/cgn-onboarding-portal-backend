package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgn.portal.repository.EycaDataExportRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExportService {

    private final AgreementRepository agreementRepository;
    private final EycaDataExportRepository eycaDataExportRepository;

    private final String[] exportAgreementsHeaders = new String[]{"Stato Convenzione",
                                                                  "Ragione sociale",
                                                                  "Nome alternativo",
                                                                  "Canale di vendita",
                                                                  "Modalità di riconoscimento",
                                                                  "Sito",
                                                                  "Titolo agevolazione",
                                                                  "Descrizione agevolazione",
                                                                  "Valore",
                                                                  "Stato agevolazione",
                                                                  "Data inizio",
                                                                  "Data fine",
                                                                  "Visibile su EYCA",
                                                                  "Condizioni",
                                                                  "Link agevolazione",
                                                                  "Categorie",
                                                                  "Codice statico",
                                                                  "Landing page",
                                                                  "Referer"};

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
                                                            "GEO - Longitude"};

    public ExportService(AgreementRepository agreementRepository, EycaDataExportRepository eycaDataExportRepository) {
        this.agreementRepository = agreementRepository;
        this.eycaDataExportRepository = eycaDataExportRepository;
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
                                                     r.getLongitude()})
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

    private final BiFunction<AgreementEntity, Optional<DiscountEntity>, String[]>
            agreementWithProfileAndDiscountToStringArray
            = (agreement, maybeDiscount) -> new String[]{agreement.getState().getCode(),
                                                         Optional.ofNullable(agreement.getProfile())
                                                                 .map(ProfileEntity::getFullName).orElse(null),
                                                         Optional.ofNullable(agreement.getProfile())
                                                                 .map(ProfileEntity::getName).orElse(null),
                                                         Optional.ofNullable(agreement.getProfile())
                                                                 .map(ProfileEntity::getSalesChannel)
                                                                 .map(SalesChannelEnum::getCode).orElse(null),
                                                         Optional.ofNullable(agreement.getProfile())
                                                                 .map(ProfileEntity::getDiscountCodeType)
                                                                 .map(DiscountCodeTypeEnum::getCode).orElse(null),
                                                         Optional.ofNullable(agreement.getProfile())
                                                                 .map(ProfileEntity::getWebsiteUrl).orElse(null),
                                                         maybeDiscount.map(DiscountEntity::getName).orElse(null),
                                                         maybeDiscount.map(DiscountEntity::getDescription).orElse(null),
                                                         maybeDiscount.map(DiscountEntity::getDiscountValue)
                                                                      .map(Objects::toString).orElse(null),
                                                         maybeDiscount.map(d -> d.getEndDate().isAfter(LocalDate.now()) ? d.getState() : "EXPIRED")
                                                                      .map(Objects::toString).orElse(null),
                                                         maybeDiscount.map(DiscountEntity::getStartDate)
                                                                      .map(Objects::toString).orElse(null),
                                                         maybeDiscount.map(DiscountEntity::getEndDate)
                                                                      .map(Objects::toString).orElse(null),
                                                         maybeDiscount.map(DiscountEntity::getVisibleOnEyca)
                                                                      .map(Objects::toString).orElse(null),
                                                         maybeDiscount.map(DiscountEntity::getCondition).orElse(null),
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
                                                                 null)};

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
