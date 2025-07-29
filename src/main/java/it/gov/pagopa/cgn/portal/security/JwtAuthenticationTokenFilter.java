package it.gov.pagopa.cgn.portal.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class JwtAuthenticationTokenFilter
        extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Value("${jwt.header}")
    private String tokenHeader;

    @Autowired
    public JwtAuthenticationTokenFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Optional<String> authToken = Optional.ofNullable(request.getHeader(this.tokenHeader))
                                             .map(t -> t.replace("Bearer ", ""));

        JwtUser userDetails = null;

        if (authToken.isPresent()) {
            try {
                userDetails = jwtUtils.getUserDetails(authToken.get());
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }
        }

        if (userDetails!=null && SecurityContextHolder.getContext().getAuthentication()==null) {
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }
}
