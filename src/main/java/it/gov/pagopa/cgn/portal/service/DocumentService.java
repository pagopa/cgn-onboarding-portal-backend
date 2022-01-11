package it.gov.pagopa.cgn.portal.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.AddressEntity;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import it.gov.pagopa.cgn.portal.repository.DocumentRepository;
import it.gov.pagopa.cgn.portal.repository.ProfileRepository;
import it.gov.pagopa.cgn.portal.util.CsvUtils;
import lombok.extern.slf4j.Slf4j;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ProfileRepository profileRepository;
    private final DiscountRepository discountRepository;
    private final AgreementServiceLight agreementServiceLight;
    private final AzureStorage azureStorage;
    private final TemplateEngine templateEngine;
    private final ConfigProperties configProperties;

    private static final int MAX_ALLOWED_BUCKET_CODE_LENGTH = 20;

    public List<DocumentEntity> getPrioritizedDocuments(String agreementId) {
        return filterDocumentsByPriority(getAllDocuments(agreementId));
    }

    @Transactional
    public List<DocumentEntity> getAllDocuments(String agreementId) {
        return documentRepository.findByAgreementId(agreementId);
    }

    @Transactional(readOnly = true)
    public List<DocumentEntity> getAllDocuments(String agreementId, Predicate<DocumentEntity> documentFilter) {
        List<DocumentEntity> documents = getAllDocuments(agreementId);
        if (!CollectionUtils.isEmpty(documents)) {
            documents = documents.stream().filter(documentFilter).collect(Collectors.toList());
            documents.forEach(azureStorage::setSecureDocumentUrl);
            return documents;
        }
        return Collections.emptyList();
    }

    @Transactional
    public DocumentEntity storeDocument(String agreementId, DocumentTypeEnum documentType, InputStream content,
                                        long size) {
        AgreementEntity agreementEntity = agreementServiceLight.findById(agreementId);
        String url = azureStorage.storeDocument(agreementId, documentType, content, size);
        // Delete old document if exists
        long deleted = documentRepository.deleteByAgreementIdAndDocumentType(agreementId, documentType);
        if (deleted > 0) {
            documentRepository.flush();
            log.debug(String.format("delete document for agreement id %s and with type %s", agreementId, documentType));
        }
        DocumentEntity document = new DocumentEntity();
        document.setDocumentType(documentType);
        document.setAgreement(agreementEntity);
        document.setDocumentUrl(url);
        return documentRepository.save(document);
    }

    @Transactional
    public String storeBucket(String agreementId, InputStream inputStream, long size) {
        ProfileEntity profileEntity = profileRepository.findByAgreementId(agreementId)
                .orElseThrow(() -> new InvalidRequestException("Profile not found. Bucket not uploadable"));
        if (!profileEntity.getDiscountCodeType().equals(DiscountCodeTypeEnum.BUCKET)) {
            throw new InvalidRequestException("Cannot load bucket for Discount Code type not equals to BUCKET");
        }
        try {
            byte[] content = inputStream.readAllBytes();
            long csvRecordCount = countCsvRecord(content);
            if (csvRecordCount < configProperties.getBucketMinCsvRows()) {
                throw new InvalidRequestException(
                        "Cannot load bucket because number of rows (" + csvRecordCount + ") does not respect minimum bound (" + configProperties.getBucketMinCsvRows() + ") on loaded content of length (" + content.length + ")");
            }
            try (ByteArrayInputStream contentIs = new ByteArrayInputStream(content)) {
                Stream<CSVRecord> csvRecordStream = CsvUtils.getCsvRecordStream(contentIs);
                if (content.length == 0
                        || csvRecordStream.anyMatch(line -> line.get(0).length() > MAX_ALLOWED_BUCKET_CODE_LENGTH
                        || StringUtils.isBlank(line.get(0)))) {
                    throw new InvalidRequestException(
                            "Cannot load bucket because of empty file or number of rows does not respect minimum or one or more codes do not respect "
                                    + MAX_ALLOWED_BUCKET_CODE_LENGTH + " code size");
                }
            } catch (IOException e) {
                throw new CGNException(e.getMessage());
            }

            String bucketLoadUID = UUID.randomUUID().toString();
            try (ByteArrayInputStream in = new ByteArrayInputStream(content)) {
                azureStorage.uploadCsv(in, bucketLoadUID, size);
            } catch (IOException e) {
                throw new CGNException(e.getMessage());
            }

            return bucketLoadUID;
        } catch (IOException e) {
            throw new CGNException(e.getMessage());
        }
    }

    private long countCsvRecord(byte[] content) {
        long recordCount = 0;
        try (ByteArrayInputStream contentIs = new ByteArrayInputStream(content)) {
            recordCount = CsvUtils.countCsvLines(contentIs);
        } catch (IOException e) {
            throw new CGNException(e.getMessage());
        }
        return recordCount;
    }

    @Transactional
    public long deleteDocument(String agreementId, DocumentTypeEnum documentType) {
        return documentRepository.deleteByAgreementIdAndDocumentType(agreementId, documentType);
    }

    @Transactional
    public void resetMerchantDocuments(String agreementId) {
        documentRepository.deleteByAgreementIdAndDocumentType(agreementId, DocumentTypeEnum.AGREEMENT);
        documentRepository.deleteByAgreementIdAndDocumentType(agreementId, DocumentTypeEnum.ADHESION_REQUEST);
    }

    @Transactional
    public void resetAllDocuments(String agreementId) {
        resetMerchantDocuments(agreementId);
        documentRepository.deleteByAgreementIdAndDocumentType(agreementId, DocumentTypeEnum.BACKOFFICE_AGREEMENT);
        documentRepository.deleteByAgreementIdAndDocumentType(agreementId,
                DocumentTypeEnum.BACKOFFICE_ADHESION_REQUEST);
    }

    // if there are documents created by profile and backoffice user, the document
    // made by backoffice user will be returned
    private List<DocumentEntity> filterDocumentsByPriority(List<DocumentEntity> documentEntityList) {
        if (CollectionUtils.isEmpty(documentEntityList)) {
            return documentEntityList;
        }
        return Arrays.stream(DocumentTypeEnum.Type.values())
                .map(type -> filterDocumentsByPriorityAndType(type, documentEntityList)).filter(t -> !Objects.isNull(t))
                .collect(Collectors.toList());
    }

    private DocumentEntity filterDocumentsByPriorityAndType(DocumentTypeEnum.Type typeEnum,
                                                            List<DocumentEntity> documentEntityList) {
        DocumentEntity toReturn = null;
        for (DocumentEntity documentEntity : documentEntityList) {
            if (typeEnum.equals(documentEntity.getDocumentType().getType())) {
                if (documentEntity.getDocumentType().isBackoffice()) {
                    return documentEntity;
                }
                toReturn = documentEntity;
            }
        }
        return toReturn;
    }

    @Transactional(readOnly = true)
    public ByteArrayOutputStream renderDocument(String agreementId, DocumentTypeEnum documentType) {
        switch (documentType) {
            case AGREEMENT:
                return renderAgreementDocument(agreementId);
            case ADHESION_REQUEST:
                return renderAdhesionRequestDocument(agreementId);
            default:
                throw new RuntimeException("Invalid document type: " + documentType);
        }
    }

    private ByteArrayOutputStream renderAgreementDocument(String agreementId) {
        ProfileEntity profileEntity = profileRepository.findByAgreementId(agreementId)
                .orElseThrow(() -> new RuntimeException("no profile"));

        Context context = new Context();
        context.setVariable("legal_name", profileEntity.getFullName());
        context.setVariable("merchant_tax_code", profileEntity.getTaxCodeOrVat());
        context.setVariable("legal_representative_fullname", profileEntity.getLegalRepresentativeFullName());
        context.setVariable("legal_representative_fiscal_code", profileEntity.getLegalRepresentativeTaxCode());
        context.setVariable("legal_office", profileEntity.getLegalOffice());
        context.setVariable("telephone_nr", profileEntity.getTelephoneNumber());
        context.setVariable("pec_address", profileEntity.getPecAddress());
        context.setVariable("department_reference_email", "cartagiovaninazionale@governo.it");
        context.setVariable("department_pec_address", "giovanieserviziocivile@pec.governo.it");
        context.setVariable("current_date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        String renderedContent = templateEngine.process("pdf/pe-agreement.html", context);
        return generatePdfFromHtml(renderedContent);
    }

    private ByteArrayOutputStream renderAdhesionRequestDocument(String agreementId) {
        ProfileEntity profileEntity = profileRepository.findByAgreementId(agreementId)
                .orElseThrow(() -> new RuntimeException("no profile"));

        List<String> addressList = profileEntity.getAddressList().stream().map(AddressEntity::getFullAddress)
                .collect(Collectors.toList());

        List<DiscountEntity> discounts = discountRepository.findByAgreementId(agreementId);
        List<RenderableDiscount> renderableDiscounts = discounts.stream().map(RenderableDiscount::fromEntity)
                .collect(Collectors.toList());

        String discountMode = null;
        if (SalesChannelEnum.OFFLINE.equals(profileEntity.getSalesChannel())) {
            discountMode = "Negozio fisico";
        } else {
            switch (profileEntity.getDiscountCodeType()) {
                case STATIC:
                    discountMode = "Codice sconto statico";
                    break;
                case API:
                    discountMode = "API";
                    break;
                case LANDINGPAGE:
                    discountMode = "Landing page";
                    break;
                case BUCKET:
                    discountMode = "Lista di codici sconto";
                    break;
            }
        }

        Context context = new Context();
        context.setVariable("legal_name", profileEntity.getFullName());
        context.setVariable("merchant_tax_code", profileEntity.getTaxCodeOrVat());
        context.setVariable("legal_representative_fullname", profileEntity.getLegalRepresentativeFullName());
        context.setVariable("legal_representative_fiscal_code", profileEntity.getLegalRepresentativeTaxCode());
        context.setVariable("legal_office", profileEntity.getLegalOffice());
        context.setVariable("telephone_nr", profileEntity.getTelephoneNumber());
        context.setVariable("pec_address", profileEntity.getPecAddress());
        context.setVariable("current_date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        context.setVariable("merchant_name", ObjectUtils.firstNonNull(Stream.of(profileEntity.getName(), profileEntity.getFullName())).findFirst().orElse(null));
        context.setVariable("merchant_description", profileEntity.getDescription());
        context.setVariable("merchant_address_list", addressList);
        context.setVariable("merchant_website", profileEntity.getWebsiteUrl());
        context.setVariable("discounts", renderableDiscounts);
        context.setVariable("discount_mode", discountMode);

        ReferentEntity referent = profileEntity.getReferent();

        context.setVariable("referent_fullname", referent.getFirstName() + " " + referent.getLastName());
        context.setVariable("referent_role", referent.getRole());
        context.setVariable("referent_email_address", referent.getEmailAddress());
        context.setVariable("referent_telephone_nr", referent.getTelephoneNumber());

        String renderedContent = templateEngine.process("pdf/pe-adhesion-request.html", context);
        return generatePdfFromHtml(renderedContent);
    }

    private ByteArrayOutputStream generatePdfFromHtml(String renderedContent) {
        var outputStream = new ByteArrayOutputStream();

        try {
            var renderer = new ITextRenderer();

            ITextFontResolver fontResolver = renderer.getFontResolver();
            fontResolver.addFont("fonts/TitilliumWeb-Black.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            fontResolver.addFont("fonts/TitilliumWeb-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            fontResolver.addFont("fonts/TitilliumWeb-BoldItalic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            fontResolver.addFont("fonts/TitilliumWeb-ExtraLight.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            fontResolver.addFont("fonts/TitilliumWeb-ExtraLightItalic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            fontResolver.addFont("fonts/TitilliumWeb-Italic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            fontResolver.addFont("fonts/TitilliumWeb-Light.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            fontResolver.addFont("fonts/TitilliumWeb-LightItalic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            fontResolver.addFont("fonts/TitilliumWeb-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            fontResolver.addFont("fonts/TitilliumWeb-SemiBold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            fontResolver.addFont("fonts/TitilliumWeb-SemiBoldItalic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            renderer.setDocumentFromString(renderedContent);
            renderer.layout();

            renderer.createPDF(outputStream);
        } catch (DocumentException | IOException e) {
            throw new CGNException("Error in document rendering", e);
        }

        return outputStream;
    }

    public DocumentService(DocumentRepository documentRepository, ProfileRepository profileRepository,
                           DiscountRepository discountRepository, AgreementServiceLight agreementServiceLight,
                           AzureStorage azureStorage, TemplateEngine templateEngine, ConfigProperties configProperties) {
        this.documentRepository = documentRepository;
        this.profileRepository = profileRepository;
        this.discountRepository = discountRepository;
        this.agreementServiceLight = agreementServiceLight;
        this.azureStorage = azureStorage;
        this.templateEngine = templateEngine;
        this.configProperties = configProperties;
    }

    private static class RenderableDiscount {
        public String name;
        public String validityPeriod;
        public String discountValue;
        public String condition;
        public String modeValue;
        public String categories;

        public static RenderableDiscount fromEntity(DiscountEntity entity) {
            RenderableDiscount discount = new RenderableDiscount();

            String categories = entity.getProducts().stream().map(p -> p.getProductCategory().getDescription())
                    .collect(Collectors.joining(",\n"));

            discount.name = entity.getName();
            discount.validityPeriod = entity.getStartDate() + " - \n" + entity.getEndDate();
            discount.discountValue = entity.getDiscountValue() != null ? "" + entity.getDiscountValue() + "% " : "";
            discount.condition = entity.getCondition();
            discount.modeValue = Stream.of(entity.getStaticCode(), entity.getLandingPageUrl()).filter(Objects::nonNull).findFirst().orElse("-");
            discount.categories = categories;

            return discount;
        }

    }

}
