package it.gov.pagopa.cgn.portal.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;

import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.*;
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

    @Value("${spring.profiles.active:Unknown}")
    private String activeProfile;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {

        System.out.println("isDev=" + isDev());
        return isDev() ? builder.requestFactory(getFactorySupplierForDisableSSL()).build() : builder.build();
    }

    private Supplier<ClientHttpRequestFactory> getFactorySupplierForDisableSSL() {

        return () -> {

            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

            SSLContext sslContext = null;
            try {
                sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

            PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(csf).build();

            HttpClient httpClient = HttpClientBuilder.create().setConnectionManager(connectionManager).build();

            return new HttpComponentsClientHttpRequestFactory(httpClient);
        };
    }

    private boolean isDev() {

        return activeProfile.equalsIgnoreCase("dev");
    }
}
