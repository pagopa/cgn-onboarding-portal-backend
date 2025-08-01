package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.facade.SessionFacade;
import it.gov.pagopa.cgnonboardingportal.publicapi.api.SessionApi;
import it.gov.pagopa.cgnonboardingportal.publicapi.model.ActiveDirectoryData;
import it.gov.pagopa.cgnonboardingportal.publicapi.model.CreateJwtSessionTokenRequest;
import it.gov.pagopa.cgnonboardingportal.publicapi.model.OneIdentityData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;

@RestController
@Slf4j
public class PublicSessionApiController
        implements SessionApi {

    private final SessionFacade sessionFacade;

    @Autowired
    public PublicSessionApiController(SessionFacade sessionFacade) {
        this.sessionFacade = sessionFacade;
    }

    @Override
    public ResponseEntity<String> createJwtSessionToken(CreateJwtSessionTokenRequest createJwtSessionTokenRequest) {
        try {
            String token = null;
            if (createJwtSessionTokenRequest instanceof OneIdentityData oiData) {
                token = sessionFacade.getOperatorToken(oiData.getCode(), oiData.getNonce());
            }
            if (createJwtSessionTokenRequest instanceof ActiveDirectoryData adData) {
                token = sessionFacade.getAdminToken(adData.getToken(), adData.getNonce());
            }
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            log.error("get session token failure: {}",
                      Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(joining("\n")));
            throw new SecurityException(e.getMessage());
        }
    }
}
