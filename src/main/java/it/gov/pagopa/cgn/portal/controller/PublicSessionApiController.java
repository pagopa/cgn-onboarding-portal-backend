package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.facade.SessionFacade;
import it.gov.pagopa.cgnonboardingportal.publicapi.api.SessionApi;
import it.gov.pagopa.cgnonboardingportal.publicapi.model.ActiveDirectoryData;
import it.gov.pagopa.cgnonboardingportal.publicapi.model.CreateJwtSessionTokenRequest;
import it.gov.pagopa.cgnonboardingportal.publicapi.model.OneIdentityData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
            if (createJwtSessionTokenRequest instanceof OneIdentityData oidata) {
                String token = sessionFacade.getToken(oidata.getCode(), oidata.getState(), oidata.getNonce());
                return ResponseEntity.ok(token);
            }
            if (createJwtSessionTokenRequest instanceof ActiveDirectoryData addata) {
                //TODO: next
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            log.error("get session token failure: " + e.getMessage());
            log.error(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(joining("\n")));

        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
