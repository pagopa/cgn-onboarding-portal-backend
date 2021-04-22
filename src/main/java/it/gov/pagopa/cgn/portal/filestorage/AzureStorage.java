package it.gov.pagopa.cgn.portal.filestorage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class AzureStorage {

    @Value("${cgn.pe.storage.azure.default-endpoints-protocol}")
    private String defaultEndpointsProtocol;

    @Value("${cgn.pe.storage.azure.account-name}")
    private String accountName;

    @Value("${cgn.pe.storage.azure.account-key}")
    private String accountKey;

    @Value("${cgn.pe.storage.azure.blob-endpoint}")
    private String blobEndpoint;


    @Value("${cgn.pe.storage.azure.documents-container-name}")
    private String documentsContainerName;

    @Value("${cgn.pe.storage.azure.images-container-name}")
    private String imagesContainerName;

    private BlobContainerClient documentContainerClient;
    private BlobContainerClient imagesContainerClient;

    @PostConstruct
    protected void init(){
        var connectionString = "DefaultEndpointsProtocol=" + defaultEndpointsProtocol + ";AccountName=" + accountName + ";AccountKey=" + accountKey + ";BlobEndpoint=" + blobEndpoint + ";";

        documentContainerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(documentsContainerName)
                .buildClient();

        imagesContainerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(imagesContainerName)
                .buildClient();
    }

    public String storeDocument(String agreementId, DocumentTypeEnum documentType, InputStream content, long size) throws IOException {
        String blobName = agreementId + "/" + documentType.getCode().toLowerCase() + ".pdf";

        BlobClient blobClient = documentContainerClient.getBlobClient(blobName);
        ByteArrayInputStream contentIs = new ByteArrayInputStream(IOUtils.toByteArray(content));
        blobClient.upload(contentIs, size, true);

        return documentsContainerName + "/" + blobName;
    }


    public String storeImage(String agreementId, String extension, InputStream content, long size) {
        String blobName = "image-" + agreementId + "." + extension;

        BlobClient blobClient = imagesContainerClient.getBlobClient(blobName);
        blobClient.upload(content, size, true);

        return imagesContainerName + "/" + blobName;
    }

}
