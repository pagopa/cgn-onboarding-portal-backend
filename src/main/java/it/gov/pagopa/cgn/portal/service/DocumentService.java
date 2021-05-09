package it.gov.pagopa.cgn.portal.service;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import it.gov.pagopa.cgn.portal.repository.DocumentRepository;
import it.gov.pagopa.cgn.portal.repository.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Transactional(Transactional.TxType.REQUIRED)
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ProfileRepository profileRepository;
    private final DiscountRepository discountRepository;
    private final AgreementServiceLight agreementServiceLight;
    private final AzureStorage azureStorage;
    private final TemplateEngine templateEngine;

    private final ITextRenderer renderer = new ITextRenderer();

    public List<DocumentEntity> getPrioritizedDocuments(String agreementId) {
        return filterDocumentsByPriority(getAllDocuments(agreementId));
    }

    public List<DocumentEntity> getAllDocuments(String agreementId) {
        return documentRepository.findByAgreementId(agreementId);
    }

    public List<DocumentEntity> getAllDocuments(String agreementId, Predicate<DocumentEntity> documentFilter) {
        List<DocumentEntity> documents = getAllDocuments(agreementId);
        if (!CollectionUtils.isEmpty(documents)) {
            return documents.stream().filter(documentFilter).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public DocumentEntity storeDocument(String agreementId, DocumentTypeEnum documentType, InputStream content, long size) {
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

    public long deleteDocument(String agreementId, DocumentTypeEnum documentType) {
        return documentRepository.deleteByAgreementIdAndDocumentType(agreementId, documentType);
    }

    // if there are documents created by profile and backoffice user, the document made by backoffice user will be returned
    private List<DocumentEntity> filterDocumentsByPriority(List<DocumentEntity> documentEntityList) {
        if (CollectionUtils.isEmpty(documentEntityList)) {
            return documentEntityList;
        }
        return Arrays.stream(DocumentTypeEnum.Type.values())
                .map(type -> filterDocumentsByPriorityAndType(type, documentEntityList))
                .filter(t -> !Objects.isNull(t))
                .collect(Collectors.toList());
    }

    private DocumentEntity filterDocumentsByPriorityAndType(DocumentTypeEnum.Type typeEnum, List<DocumentEntity> documentEntityList) {
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


    public ByteArrayOutputStream renderDocument(String agreementId, DocumentTypeEnum documentType) {
        switch (documentType) {
            case AGREEMENT:
                return renderAgreementDocument(agreementId);
            case MANIFESTATION_OF_INTEREST:
                return renderManifestationOfInterestDocument(agreementId);
            default:
                throw new RuntimeException("Invalid document type: "  + documentType);
        }
    }

    private ByteArrayOutputStream renderAgreementDocument(String agreementId) {
        ProfileEntity profileEntity = profileRepository.findByAgreementId(agreementId).orElseThrow(() -> new RuntimeException("no profile"));

        Context context = new Context();
        context.setVariable("legal_name", profileEntity.getFullName());
        context.setVariable("merchant_tax_code", profileEntity.getTaxCodeOrVat());
        context.setVariable("legal_representative_fullname", profileEntity.getLegalRepresentativeFullName());
        context.setVariable("legal_representative_fiscal_code", profileEntity.getLegalRepresentativeTaxCode());
        context.setVariable("legal_office", profileEntity.getLegalOffice());
        context.setVariable("telephone_nr", profileEntity.getTelephoneNumber());
        context.setVariable("pec_address", profileEntity.getPecAddress());
        context.setVariable("department_reference_email", "......");  // TODO add department reference email
        context.setVariable("current_date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        String renderedContent = templateEngine.process("pdf/pe-agreement.html", context);
        return generatePdfFromHtml(renderedContent);
    }


    private ByteArrayOutputStream renderManifestationOfInterestDocument(String agreementId) {
        ProfileEntity profileEntity = profileRepository.findByAgreementId(agreementId).orElseThrow(() -> new RuntimeException("no profile"));

        List<String> addressList = profileEntity.getAddressList().stream().map(address -> address.getStreet() + ", " + address.getZipCode() + ", " + address.getCity() + " (" + address.getDistrict() + ")").collect(Collectors.toList());

        List<DiscountEntity> discounts = discountRepository.findByAgreementId(agreementId);
        List<RenderableDiscount> renderableDiscounts = discounts.stream().map(RenderableDiscount::fromEntity).collect(Collectors.toList());

        Context context = new Context();
        context.setVariable("legal_name", profileEntity.getFullName());
        context.setVariable("merchant_tax_code", profileEntity.getTaxCodeOrVat());
        context.setVariable("legal_representative_fullname", profileEntity.getLegalRepresentativeFullName());
        context.setVariable("legal_representative_fiscal_code", profileEntity.getLegalRepresentativeTaxCode());
        context.setVariable("legal_office", profileEntity.getLegalOffice());
        context.setVariable("telephone_nr", profileEntity.getTelephoneNumber());
        context.setVariable("pec_address", profileEntity.getPecAddress());
        context.setVariable("current_date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        context.setVariable("merchant_name", profileEntity.getName());
        context.setVariable("merchant_description", profileEntity.getDescription());
        context.setVariable("merchant_address_list", addressList);
        context.setVariable("merchant_website", profileEntity.getWebsiteUrl());
        context.setVariable("discounts", renderableDiscounts);

        ReferentEntity referent = profileEntity.getReferent();

        context.setVariable("referent_fullname", referent.getFirstName() + " " + referent.getLastName());
        context.setVariable("referent_role", referent.getRole());
        context.setVariable("referent_email_address", referent.getEmailAddress());
        context.setVariable("referent_telephone_nr", referent.getTelephoneNumber());

        String renderedContent = templateEngine.process("pdf/pe-manifestation-of-interest.html", context);
        return generatePdfFromHtml(renderedContent);
    }

    private ByteArrayOutputStream generatePdfFromHtml(String renderedContent) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        renderer.setDocumentFromString(renderedContent);
        renderer.layout();

        try {
            renderer.createPDF(outputStream);
        } catch (DocumentException e) {
            e.printStackTrace();
            throw new RuntimeException("Error in document rendering", e);
        }

        return outputStream;
    }


    public DocumentService(DocumentRepository documentRepository, ProfileRepository profileRepository,
                           DiscountRepository discountRepository, AzureStorage azureStorage,
                           TemplateEngine templateEngine, AgreementServiceLight agreementServiceLight) {
        this.documentRepository = documentRepository;
        this.profileRepository = profileRepository;
        this.discountRepository = discountRepository;
        this.azureStorage = azureStorage;
        this.templateEngine = templateEngine;
        this.agreementServiceLight = agreementServiceLight;

        try {
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

        } catch (IOException exc) {
            throw new RuntimeException("Failed to load fonts", exc);
        }
    }


    static private class RenderableDiscount {
        public String name;
        public String validityPeriod;
        public String discountValue;
        public String staticCode;
        public String categories;

        public static RenderableDiscount fromEntity(DiscountEntity entity) {
            RenderableDiscount discount = new RenderableDiscount();

            String categories = entity.getProducts().stream()
                    .map(p -> p.getProductCategory().name()).collect(Collectors.joining(",\n"));

            discount.name = entity.getName();
            discount.validityPeriod = entity.getStartDate() + " - \n" + entity.getEndDate();
            discount.discountValue = " " + entity.getDiscountValue() + "% ";
            discount.staticCode = entity.getStaticCode();
            discount.categories = categories;

            return discount;
        }

    }

}
