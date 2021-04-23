package it.gov.pagopa.cgn.portal.service;

import com.lowagie.text.DocumentException;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.filestorage.AzureStorage;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.repository.*;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(Transactional.TxType.REQUIRED)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ProfileRepository profileRepository;
    private final DiscountRepository discountRepository;
    private final AzureStorage azureStorage;
    private final TemplateEngine templateEngine;

    private final ITextRenderer renderer = new ITextRenderer();

    public List<DocumentEntity> getDocuments(String agreementId) {
        return documentRepository.findByAgreementId(agreementId);
    }

    public DocumentEntity storeDocument(String agreementId, DocumentTypeEnum documentType, InputStream content, long size) throws IOException {
        String url = azureStorage.storeDocument(agreementId, documentType, content, size);

        DocumentEntity document = new DocumentEntity();
        document.setDocumentUrl(url);
        document.setDocumentType(documentType);
        document.setAgreementId(agreementId);

        return documentRepository.save(document);
    }

    public void deleteDocument(String agreementId, DocumentTypeEnum documentType) {
        documentRepository.deleteByAgreementIdAndDocumentType(agreementId, documentType.toString());
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
        context.setVariable("merchant_tax_code", ".....");
        context.setVariable("legal_representative_fullname", profileEntity.getLegalRepresentativeFullName());
        context.setVariable("legal_representative_fiscal_code", profileEntity.getLegalRepresentativeTaxCode());
        context.setVariable("legal_office", profileEntity.getLegalOffice());
        context.setVariable("telephone_nr", profileEntity.getTelephoneNumber());
        context.setVariable("pec_address", profileEntity.getPecAddress());
        context.setVariable("department_reference_email", "......");
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
        context.setVariable("merchant_tax_code", ".....");
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


    public DocumentService(DocumentRepository documentRepository, ProfileRepository profileRepository, DiscountRepository discountRepository, AzureStorage azureStorage, TemplateEngine templateEngine) {
        this.documentRepository = documentRepository;
        this.profileRepository = profileRepository;
        this.discountRepository = discountRepository;
        this.azureStorage = azureStorage;
        this.templateEngine = templateEngine;
    }


    static private class RenderableDiscount {
        public String name;
        public String validityPeriod;
        public String discountValue;
        public String staticCode;
        public String categories;

        public static RenderableDiscount fromEntity(DiscountEntity entity) {
            RenderableDiscount discount = new RenderableDiscount();

            String categories = String.join(",\n", entity.getProducts().stream().map(DiscountProductEntity::getProductCategory).collect(Collectors.toList()));

            discount.name = entity.getName();
            discount.validityPeriod = entity.getStartDate() + " - \n" + entity.getEndDate();
            discount.discountValue = " " + entity.getDiscountValue() + "% ";
            discount.staticCode = entity.getStaticCode();
            discount.categories = categories;

            return discount;
        }

    }

}
