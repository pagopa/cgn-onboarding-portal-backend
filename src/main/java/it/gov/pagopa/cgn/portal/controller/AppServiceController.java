package it.gov.pagopa.cgn.portal.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This API is used by App service
 */
@RestController
public class AppServiceController {

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> appServiceCheck() {
        return ResponseEntity.noContent().build();
    }
}
