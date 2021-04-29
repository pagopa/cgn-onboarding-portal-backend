package it.gov.pagopa.cgn.portal.security;


import java.io.IOException;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.gov.pagopa.cgn.portal.service.AgreementUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {


    @Autowired
    AgreementUserService agreementUserService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.header}")
    private String tokenHeader;

    @Value("${cgn.role.header}")
    private String cgnRoleHeader;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        Optional<String> authToken = Optional.ofNullable(request.getHeader(this.tokenHeader)).map(t -> t.replace("Bearer ", ""));

        String cgnRole = request.getHeader(this.cgnRoleHeader);

        JwtUser userDetails = null;

        if(authToken.isPresent()){
            userDetails = jwtTokenUtil.getUserDetails(authToken.get(), cgnRole);
        }

        if (userDetails != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // TODO validate permission to access resource based on path
            //agreementUserService.findCurrentAgreementUser()

            /*
             checks:
              - if role is admin -> check the user is accessing admin resources
              - if role is merchant -> check the user is accessing merchant resources
                    - if path is agreement-creation, proceed (it will create or retrieve the correct agreement
                    - if path has an agreementId, check if the merchant har rights to operate on that agreement
             */

            JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }
}
