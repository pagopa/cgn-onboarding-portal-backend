package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgnonboardingportal.api.GeolocationTokenApi;
import it.gov.pagopa.cgnonboardingportal.model.GeolocationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeolocationTokenController
        implements GeolocationTokenApi {

    private final ConfigProperties configProperties;

    @Autowired
    public GeolocationTokenController(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Override
    public ResponseEntity<GeolocationToken> getGeolocationToken() {
        var geolocationToken = new GeolocationToken();
        geolocationToken.setToken(configProperties.getGeolocationToken());
        return ResponseEntity.ok(geolocationToken);
    }
}
