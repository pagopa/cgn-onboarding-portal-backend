package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.service.DocumentService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class DocumentFacadeTest {

    @Mock
    private DocumentService documentService;

    @Mock
    private MultipartFile multipartFile;

    @Test
    public void UploadDocument_UploadDocumentThrowCGNExceptionIfIOExceptionWasThrown_ThrowCGNException() throws IOException {
        when(multipartFile.getInputStream()).thenThrow(new IOException());
        DocumentFacade documentFacade = new DocumentFacade(documentService, null, null);
        String documentTypeCode = DocumentTypeEnum.AGREEMENT.getCode();
        Assert.assertThrows(CGNException.class, ()
                -> documentFacade.uploadDocument("fake_agreement", documentTypeCode, multipartFile));

    }



}
