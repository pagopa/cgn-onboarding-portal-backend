package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.security.JwtUtils;
import it.gov.pagopa.cgn.portal.service.AttributeAuthorityService;
import it.gov.pagopa.cgn.portal.util.CGNUtils;
import it.gov.pagopa.cgnonboardingportal.model.Organizations;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@SpringBootTest
class AttributeAuthorityFacadeTest
        extends IntegrationAbstractTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AttributeAuthorityService attributeAuthorityService;

    @InjectMocks
    private AttributeAuthorityFacade attributeAuthorityFacade;

    @Test
    void getOrganizations_shouldReturnEmptyList_whenHttpClientErrorExceptionThrown() {

        try (MockedStatic<CGNUtils> mockedStatic = mockStatic(CGNUtils.class)) {
            mockedStatic.when(CGNUtils::getJwtOperatorFiscalCode).thenReturn("ABCDEF12G34H567I");

            when(attributeAuthorityService.getAgreementOrganizations(anyString())).thenThrow(new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST));

            ResponseEntity<Organizations> response = attributeAuthorityFacade.getOrganizations();

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().getItems().isEmpty());
        }
    }
}
