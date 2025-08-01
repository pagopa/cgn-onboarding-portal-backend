package it.gov.pagopa.cgn.portal.config;

import it.gov.pagopa.cgn.portal.exception.InternalErrorException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.function.Supplier;

@Configuration
public class RestTemplateConfig {

    private final ConfigProperties configProperties;

    @Autowired
    RestTemplateConfig(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return configProperties.isActiveProfileDev() ? builder.requestFactory(getFactorySupplierForDisableSSL()).build():builder.build();
    }

    private Supplier<ClientHttpRequestFactory> getFactorySupplierForDisableSSL() {
        return () -> {

            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

            SSLContext sslContext = null;
            try {
                sslContext = org.apache.http.ssl.SSLContexts.custom()
                                                            .loadTrustMaterial(null, acceptingTrustStrategy)
                                                            .build();
            } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                throw new InternalErrorException(e.getMessage());
            }
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();

            return new HttpComponentsClientHttpRequestFactory(httpClient);
        };
    }
}
