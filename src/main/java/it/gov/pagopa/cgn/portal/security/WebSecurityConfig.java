package it.gov.pagopa.cgn.portal.security;


import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig
        extends WebSecurityConfigurerAdapter {

    @Value("${spring.profiles.active:Unknown}")
    private String activeProfile;

    private final JwtAuthenticationEntryPoint unauthorizedHandler;

    private final ConfigProperties configProperties;

    private final JwtAuthenticationTokenFilter jwtAuthTokenFilter;

    @Autowired
    public WebSecurityConfig(JwtAuthenticationEntryPoint unauthorizedHandler,
                             ConfigProperties configProperties,
                             JwtAuthenticationTokenFilter jwtAuthTokenFilter) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.configProperties = configProperties;
        this.jwtAuthTokenFilter = jwtAuthTokenFilter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin(configProperties.getCORSOrigin());
        configuration.setAllowedMethods(Collections.singletonList("POST, PUT, GET, OPTIONS, DELETE"));
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // we suppress "Disabling CSRF protections is security-sensitive"
    // because JWT token prevents CSRF attack to the portal
    @SuppressWarnings("java:S4502")
    @Override
    protected void configure(HttpSecurity httpSecurity)
            throws Exception {
        httpSecurity.csrf()
                    .disable()
                    .exceptionHandling()
                    .authenticationEntryPoint(unauthorizedHandler)
                    .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .cors()
                    .and()
                    .authorizeRequests()
                    .antMatchers(getAntMatchers())
                    .permitAll()
                    .anyRequest()
                    .authenticated();

        // UsernamePasswordAuthenticationFilter isn't properly need, we should rewrite the filter chain
        httpSecurity.addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class);

        httpSecurity.headers().cacheControl();
    }

    private String[] getAntMatchers() {
        return ("dev".equals(activeProfile) ?
                List.of("/actuator/**",
                        "/session",
                        "/help",
                        "/",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"):
                List.of("/actuator/**", "/session", "/help", "/")).toArray(String[]::new);
    }
}
