package it.gov.pagopa.cgn.portal.facade;

import it.gov.pagopa.cgn.portal.service.OpenIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionFacade {

    private final OpenIdService openIdService;

    @Autowired
    public SessionFacade(OpenIdService openIdService) {
        this.openIdService = openIdService;
    }

    public String getToken(String code, String state, String nonce)
            throws Exception {
        return openIdService.getToken(code, state, nonce);
    }
}
