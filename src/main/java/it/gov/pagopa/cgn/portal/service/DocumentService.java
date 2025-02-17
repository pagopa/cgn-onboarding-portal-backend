package it.gov.pagopa.cgn.portal.service;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.enums.EntityTypeEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.exception.InternalErrorException;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import it.gov.pagopa.cgn.portal.repository.DocumentRepository;
import it.gov.pagopa.cgn.portal.repository.ProfileRepository;
import it.gov.pagopa.cgn.portal.util.CsvUtils;
import it.gov.pagopa.cgnonboardingportal.model.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class DocumentService {

    private static final int MAX_ALLOWED_BUCKET_CODE_LENGTH = 20;
    private final DocumentRepository documentRepository;
    private final ProfileRepository profileRepository;
    private final DiscountRepository discountRepository;
    private final AgreementServiceLight agreementServiceLight;
    private final AzureStorage azureStorage;
    private final TemplateEngine templateEngine;
    private final ConfigProperties configProperties;

    public DocumentService(DocumentRepository documentRepository,
                           ProfileRepository profileRepository,
                           DiscountRepository discountRepository,
                           AgreementServiceLight agreementServiceLight,
                           AzureStorage azureStorage,
                           TemplateEngine templateEngine,
                           ConfigProperties configProperties) {
        this.documentRepository = documentRepository;
        this.profileRepository = profileRepository;
        this.discountRepository = discountRepository;
        this.agreementServiceLight = agreementServiceLight;
        this.azureStorage = azureStorage;
        this.templateEngine = templateEngine;
        this.configProperties = configProperties;
    }

    public List<DocumentEntity> getPrioritizedDocuments(String agreementId) {
        return filterDocumentsByPriority(documentRepository.findByAgreementId(agreementId));
    }

    @Transactional
    public List<DocumentEntity> getAllDocuments(String agreementId) {
        return documentRepository.findByAgreementId(agreementId);
    }

    @Transactional(readOnly = true)
    public List<DocumentEntity> getAllDocuments(String agreementId, Predicate<DocumentEntity> documentFilter) {
        List<DocumentEntity> documents = documentRepository.findByAgreementId(agreementId);
        if (!CollectionUtils.isEmpty(documents)) {
            documents = documents.stream().filter(documentFilter).toList();
            documents.forEach(azureStorage::setSecureDocumentUrl);
            return documents;
        }
        return Collections.emptyList();
    }

    @Transactional
    public DocumentEntity storeDocument(String agreementId,
                                        DocumentTypeEnum documentType,
                                        InputStream content,
                                        long size) {
        AgreementEntity agreementEntity = agreementServiceLight.findAgreementById(agreementId);
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
    public String storeBucket(String agreementId, InputStream inputStream, long size)
            throws IOException {
        ProfileEntity profileEntity = profileRepository.findByAgreementId(agreementId)
                                                       .orElseThrow(() -> new InvalidRequestException(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));
        if (!profileEntity.getDiscountCodeType().equals(DiscountCodeTypeEnum.BUCKET)) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_LOAD_BUCKET_CODE_FOR_DISCOUNT_NO_BUCKET.getValue());
        }

        byte[] content = inputStream.readAllBytes();
        long csvRecordCount = countCsvRecord(content);
        if (csvRecordCount < configProperties.getBucketMinCsvRows()) {
            throw new InvalidRequestException(ErrorCodeEnum.CANNOT_LOAD_BUCKET_FOR_NOT_RESPECTED_MINIMUM_BOUND.getValue());
        }
        try (ByteArrayInputStream contentIs = new ByteArrayInputStream(content)) {
            Stream<CSVRecord> csvRecordStream = CsvUtils.getCsvRecordStream(contentIs);
            if (content.length==0) {
                throw new InternalErrorException(ErrorCodeEnum.CSV_DATA_NOT_VALID.getValue());
            }
            if (csvRecordStream.anyMatch(line -> line.get(0).length() > MAX_ALLOWED_BUCKET_CODE_LENGTH ||
                                                 StringUtils.isBlank(line.get(0)))) {
                throw new InvalidRequestException(ErrorCodeEnum.MAX_ALLOWED_BUCKET_CODE_LENGTH_NOT_RESPECTED.getValue());
            }
        }

        Pattern pDigits = Pattern.compile("\\d"); //[0-9]
        Pattern pAlphab = Pattern.compile("[A-Za-z]");
        Pattern SpChars = Pattern.compile("^(?=.*\\d)[a-zA-Z0-9][-a-zA-Z0-9]+$");

        try (ByteArrayInputStream contentIs = new ByteArrayInputStream(content)) {
            Stream<CSVRecord> csvRecordStream = CsvUtils.getCsvRecordStream(contentIs);
            if (csvRecordStream.anyMatch(line -> !(pDigits.matcher(line.get(0)).find() //at least one digit
                                                            && pAlphab.matcher(line.get(0)).find() //at least on alphab. char
            ))) {
                throw new InvalidRequestException(ErrorCodeEnum.BUCKET_CODES_MUST_BE_ALPHANUM_WITH_AT_LEAST_ONE_DIGIT_AND_ONE_CHAR.getValue());
            }
        }

        try (ByteArrayInputStream contentIs = new ByteArrayInputStream(content)) {
            Stream<CSVRecord> csvRecordStream = CsvUtils.getCsvRecordStream(contentIs);
            if (csvRecordStream.anyMatch((line) ->  {
                return !(SpChars.matcher(line.get(0)).find());
            })) { //can contains only hypen
                throw new InvalidRequestException(ErrorCodeEnum.NOT_ALLOWED_SPECIAL_CHARS.getValue());
            }
        }

        String bucketLoadUID = UUID.randomUUID().toString();
        azureStorage.uploadCsv(content, bucketLoadUID, size);

        return bucketLoadUID;
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
        documentRepository.deleteByAgreementIdAndDocumentType(agreementId, DocumentTypeEnum.AGREEMENT);
        documentRepository.deleteByAgreementIdAndDocumentType(agreementId, DocumentTypeEnum.ADHESION_REQUEST);
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
                     .map(type -> filterDocumentsByPriorityAndType(type, documentEntityList))
                     .filter(t -> !Objects.isNull(t))
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
                throw new InvalidRequestException(ErrorCodeEnum.DOCUMENT_TYPE_NOT_VALID.getValue());
        }
    }

    private ByteArrayOutputStream renderAgreementDocument(String agreementId) {
        ProfileEntity profileEntity = profileRepository.findByAgreementId(agreementId)
                                                       .orElseThrow(() -> new InvalidRequestException(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));
        String docPath = "pdf/pe-agreement-public.html";

        Context context = new Context();
        context.setVariable("legal_name", profileEntity.getFullName());
        context.setVariable("merchant_tax_code", profileEntity.getTaxCodeOrVat());
        if (profileEntity.getAgreement().getEntityType().equals(EntityTypeEnum.PRIVATE)) {
            docPath = "pdf/pe-agreement.html";
            context.setVariable("legal_representative_fullname", profileEntity.getLegalRepresentativeFullName());
            context.setVariable("legal_representative_fiscal_code", profileEntity.getLegalRepresentativeTaxCode());
            context.setVariable("legal_office", profileEntity.getLegalOffice());
            context.setVariable("telephone_nr", profileEntity.getTelephoneNumber());
            context.setVariable("pec_address", profileEntity.getPecAddress());
        }
        context.setVariable("department_reference_email", "cartagiovaninazionale@governo.it");
        context.setVariable("department_pec_address", "giovanieserviziocivile@pec.governo.it");
        context.setVariable("current_date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        String renderedContent = templateEngine.process(docPath, context);
        return generatePdfFromHtml(renderedContent);
    }

    private ByteArrayOutputStream renderAdhesionRequestDocument(String agreementId) {
        ProfileEntity profileEntity = profileRepository.findByAgreementId(agreementId)
                                                       .orElseThrow(() -> new InvalidRequestException(ErrorCodeEnum.PROFILE_NOT_FOUND.getValue()));

        if (profileEntity!=null && profileEntity.getAgreement()!=null &&
            profileEntity.getAgreement().getEntityType().equals(EntityTypeEnum.PUBLIC_ADMINISTRATION)) {
            throw new InvalidRequestException(ErrorCodeEnum.ADHESION_DOCUMENT_NOT_REQUIRED_FOR_PA.getValue());
        }

        List<String> addressList = profileEntity.getAddressList()
                                                .stream()
                                                .map(AddressEntity::getFullAddress)
                                                .collect(Collectors.toList());

        List<DiscountEntity> discounts = discountRepository.findByAgreementId(agreementId);
        List<RenderableDiscount> renderableDiscounts = discounts.stream()
                                                                .map(RenderableDiscount::fromEntity)
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

        String merchantName = profileEntity.getName()==null || profileEntity.getName().isEmpty() ||
                              profileEntity.getName().isBlank() ? profileEntity.getFullName():profileEntity.getName();

        Context context = new Context();
        context.setVariable("legal_name", profileEntity.getFullName());
        context.setVariable("merchant_tax_code", profileEntity.getTaxCodeOrVat());
        context.setVariable("legal_representative_fullname", profileEntity.getLegalRepresentativeFullName());
        context.setVariable("legal_representative_fiscal_code", profileEntity.getLegalRepresentativeTaxCode());
        context.setVariable("legal_office", profileEntity.getLegalOffice());
        context.setVariable("telephone_nr", profileEntity.getTelephoneNumber());
        context.setVariable("pec_address", profileEntity.getPecAddress());
        context.setVariable("current_date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        context.setVariable("merchant_name", merchantName);
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
            log.error("Error during Rendering PDF:", e);
            throw new InvalidRequestException(ErrorCodeEnum.PDF_RENDERING_ERROR.getValue());
        }

        return outputStream;
    }

    private static class RenderableDiscount {
        public String name;
        public String validityPeriod;
        public String discountValue;
        public String condition;
        public String modeValue;
        public String categories;
        public String description;

        public static RenderableDiscount fromEntity(DiscountEntity entity) {
            RenderableDiscount discount = new RenderableDiscount();

            String categories = entity.getProducts()
                                      .stream()
                                      .map(p -> p.getProductCategory().getDescription())
                                      .collect(Collectors.joining(",\n"));

            discount.name = entity.getName();
            discount.validityPeriod = entity.getStartDate() + " - \n" + entity.getEndDate();
            discount.discountValue = entity.getDiscountValue()!=null ? "" + entity.getDiscountValue() + "% ":"";
            discount.condition = entity.getCondition();
            discount.modeValue = Stream.of(entity.getStaticCode(), entity.getLandingPageUrl())
                                       .filter(Objects::nonNull)
                                       .findFirst()
                                       .orElse("-");
            discount.categories = categories;
            discount.description = entity.getDescription();

            return discount;
        }

    }

}
