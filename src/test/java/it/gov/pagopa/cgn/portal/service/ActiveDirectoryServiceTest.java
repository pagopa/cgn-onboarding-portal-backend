package it.gov.pagopa.cgn.portal.service;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.security.ActiveDirectoryUser;
import it.gov.pagopa.cgn.portal.security.OidcJwtValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActiveDirectoryServiceTest {

    @Mock
    private ConfigProperties configProperties;

    @Mock
    private JWT jwt;

    @Test
    void testGetActiveDirectoryUser_Ok()
            throws Exception {
        ActiveDirectoryService service = new ActiveDirectoryService(configProperties);

        String token = "dummy-token";
        String nonce = "dummy-nonce";

        when(configProperties.getActiveDirectoryWellKnown()).thenReturn("https://mock-well-known");
        when(configProperties.getActiveDirectoryId()).thenReturn("mock-client-id");

        try (MockedStatic<JWTParser> staticMock = mockStatic(JWTParser.class)) {
            staticMock.when(() -> JWTParser.parse(token)).thenReturn(jwt);

            try (MockedConstruction<OidcJwtValidation> construction = mockConstruction(OidcJwtValidation.class,
                                                                                       (mock, context) -> {
                                                                                           IDTokenClaimsSet claims = mock(
                                                                                                   IDTokenClaimsSet.class);
                                                                                           when(claims.getStringClaim(
                                                                                                   "given_name")).thenReturn(
                                                                                                   "Anna");
                                                                                           when(claims.getStringClaim(
                                                                                                   "family_name")).thenReturn(
                                                                                                   "Neri");
                                                                                           when(mock.validateIdTokenAndGetClaims(
                                                                                                   jwt,
                                                                                                   nonce)).thenReturn(
                                                                                                   claims);
                                                                                       })) {

                ActiveDirectoryUser result = service.getActiveDirectoryUser(token, nonce);

                assertNotNull(result);
                assertEquals("Anna", result.getFirstName());
                assertEquals("Neri", result.getLastName());
            }
        }
    }

    @Test
    void testGetActiveDirectoryUser_invalidToken_throwsParseException() {
        ActiveDirectoryService service = new ActiveDirectoryService(configProperties);

        when(configProperties.getActiveDirectoryWellKnown()).thenReturn("https://mock-well-known");
        when(configProperties.getActiveDirectoryId()).thenReturn("mock-client-id");

        try (MockedStatic<JWTParser> parserMockedStatic = mockStatic(JWTParser.class); MockedConstruction<OidcJwtValidation> validationMock = mockConstruction(
                OidcJwtValidation.class,
                (mock, context) -> {
                })) {

            parserMockedStatic.when(() -> JWTParser.parse("bad-token"))
                              .thenThrow(new ParseException("Invalid token", 0));

            assertThrows(ParseException.class, () -> service.getActiveDirectoryUser("bad-token", "nonce"));
        }
    }
}

