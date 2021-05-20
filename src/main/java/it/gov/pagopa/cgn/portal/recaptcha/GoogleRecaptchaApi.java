package it.gov.pagopa.cgn.portal.recaptcha;

import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleRecaptchaApi {

    private final ConfigProperties configProperties;
    private final RestTemplate restTemplate;

    public GoogleRecaptchaApi(ConfigProperties configProperties, RestTemplate restTemplate) {
        this.configProperties = configProperties;
        this.restTemplate = restTemplate;
    }

    public boolean isTokenValid(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("secret", configProperties.getRecaptchaSecretKey());
        map.add("response", token);

        GoogleRecaptchaResponse response = restTemplate.postForObject(
                configProperties.getRecaptchaGoogleHost() + "/recaptcha/api/siteverify",
                new HttpEntity<>(map, headers),
                GoogleRecaptchaResponse.class
        );

        return response != null && response.isSuccess();
    }

}
