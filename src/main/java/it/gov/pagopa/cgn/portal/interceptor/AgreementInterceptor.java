package it.gov.pagopa.cgn.portal.interceptor;

import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AgreementUserEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementUserRepository;
import it.gov.pagopa.cgn.portal.security.JwtAuthenticationToken;
import it.gov.pagopa.cgn.portal.security.JwtOperatorUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AgreementInterceptor implements HandlerInterceptor {

    @Autowired
    private AgreementUserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String agreementIdParam = getAgreementIdFromParams(request);
        AgreementUserEntity agreementUserEntity = userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new InvalidRequestException("Current user doesn't exist"));
        if (agreementUserEntity.getAgreementId().equals(agreementIdParam)) {
            return true;
        }
        throw new SecurityException("Current user trying to use different agreementId");


    }

    private String getCurrentUserId() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        JwtOperatorUser user = (JwtOperatorUser) authentication.getPrincipal();
        return user.getUserTaxCode();
    }

    private String getAgreementIdFromParams(HttpServletRequest request) {
        Map<String, String> paramsMap = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return paramsMap.get("agreementId");
    }
}
