package it.gov.pagopa.cgn.portal.filestorage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.sas.SasProtocol;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.exception.ImageException;
import it.gov.pagopa.cgn.portal.model.DocumentEntity;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;

@Component
public class AzureStorage {

    private final ConfigProperties configProperties;

    private BlobContainerClient documentContainerClient;
    private BlobContainerClient imagesContainerClient;

    @Autowired
    public AzureStorage(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @PostConstruct
    protected void init(){

        documentContainerClient = new BlobContainerClientBuilder()
                .connectionString(configProperties.getAzureConnectionString())
                .containerName(configProperties.getDocumentsContainerName())
                .buildClient();

        imagesContainerClient = new BlobContainerClientBuilder()
                .connectionString(configProperties.getAzureConnectionString())
                .containerName(configProperties.getImagesContainerName())
                .buildClient();
    }

    public String storeDocument(String agreementId, DocumentTypeEnum documentType, InputStream content, long size) {
        String blobName = agreementId + "/" + documentType.getCode().toLowerCase() + ".pdf";

        BlobClient blobClient = documentContainerClient.getBlobClient(blobName);
        try (ByteArrayInputStream contentIs = new ByteArrayInputStream(IOUtils.toByteArray(content))) {
            blobClient.upload(contentIs, size, true);
        } catch (IOException e) {
           throw new CGNException(e);
        }
        return configProperties.getDocumentsContainerName() + "/" + blobName;
    }


    public String storeImage(String agreementId, MultipartFile image) {
        String blobName = "image-" + agreementId + "." + FilenameUtils.getExtension(image.getOriginalFilename());

        BlobClient blobClient = imagesContainerClient.getBlobClient(blobName);
        try (ByteArrayInputStream contentIs = new ByteArrayInputStream(IOUtils.toByteArray(image.getInputStream()))) {
            blobClient.upload(contentIs, image.getSize(), true);
        } catch (IOException e) {
            throw new ImageException(ImageException.ImageErrorCodeEnum.GENERIC, e.getMessage());
        }

        return configProperties.getImagesContainerName() + "/" + blobName;
    }

    public String getDocumentSasFileUrl(String documentUrl) {
        BlobClient blobClient = documentContainerClient.getBlobClient(getBlobName(documentUrl));
        BlobServiceSasSignatureValues blobServiceSasSignatureValues = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plusHours(configProperties.getSasExpiryTimeHours()),
                new BlobSasPermission().setReadPermission(true)).setProtocol(SasProtocol.HTTPS_ONLY);
        return String.format("%s?%s", blobClient.getBlobUrl(), blobClient.generateSas(blobServiceSasSignatureValues));

    }

    public void setSecureDocumentUrl(DocumentEntity documentEntity) {
        documentEntity.setDocumentUrl(getDocumentSasFileUrl(documentEntity.getDocumentUrl()));
    }

    public void setSecureDocumentUrl(List<DocumentEntity> documentList) {
        if (!CollectionUtils.isEmpty(documentList)) {
            documentList.forEach(this::setSecureDocumentUrl);
        }
    }

    private String getBlobName(String documentUrl) {
        if (documentUrl.contains("/")) {
            return documentUrl.substring(documentUrl.indexOf("/")+1);
        }
        return documentUrl;
    }

}
