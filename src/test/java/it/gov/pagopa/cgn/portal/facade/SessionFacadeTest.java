package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.security.ActiveDirectoryUser;
import it.gov.pagopa.cgn.portal.security.JwtUtils;
import it.gov.pagopa.cgn.portal.security.OneIdentityUser;
import it.gov.pagopa.cgn.portal.service.ActiveDirectoryService;
import it.gov.pagopa.cgn.portal.service.OneIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionFacadeTest {

    @Mock
    private OneIdentityService oneIdentityService;

    @Mock
    private ActiveDirectoryService activeDirectoryService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private SessionFacade sessionFacade;

    @Test
    void testGetOperatorToken_success()
            throws Exception {
        String code = "test-code";
        String nonce = "test-nonce";

        OneIdentityUser user = new OneIdentityUser("Mario", "Rossi", "MRARSS80A01F205X");

        when(oneIdentityService.getOneIdentityUser(code, nonce)).thenReturn(user);
        when(jwtUtils.buildJwtToken(anyMap())).thenReturn("mocked-token");

        String result = sessionFacade.getOperatorToken(code, nonce);

        assertEquals("mocked-token", result);
        verify(oneIdentityService).getOneIdentityUser(code, nonce);
        verify(jwtUtils).buildJwtToken(anyMap());
    }

    @Test
    void testGetAdminToken_success()
            throws Exception {
        String token = "test-token";
        String nonce = "test-nonce";

        ActiveDirectoryUser user = new ActiveDirectoryUser("Giulia", "Verdi");

        when(activeDirectoryService.getActiveDirectoryUser(token, nonce)).thenReturn(user);
        when(jwtUtils.buildJwtToken(anyMap())).thenReturn("mocked-admin-token");

        String result = sessionFacade.getAdminToken(token, nonce);

        assertEquals("mocked-admin-token", result);
        verify(activeDirectoryService).getActiveDirectoryUser(token, nonce);
        verify(jwtUtils).buildJwtToken(anyMap());
    }

    @Test
    void testGetOperatorToken_exception()
            throws Exception {
        when(oneIdentityService.getOneIdentityUser(anyString(), anyString())).thenThrow(new IOException("IO error"));

        assertThrows(GeneralSecurityException.class, () -> sessionFacade.getOperatorToken("code", "nonce"));
    }

    @Test
    void testGetAdminToken_exception()
            throws Exception {
        when(activeDirectoryService.getActiveDirectoryUser(anyString(), anyString())).thenThrow(new IOException(
                "IO error"));

        assertThrows(GeneralSecurityException.class, () -> sessionFacade.getAdminToken("token", "nonce"));
    }
}
