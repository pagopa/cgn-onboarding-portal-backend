package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.api.AttributeAuthorityApi;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.client.ApiClient;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationWithReferentsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.OrganizationsAttributeAuthority;
import it.gov.pagopa.cgnonboardingportal.attributeauthority.model.ReferentFiscalCodeAttributeAuthority;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
@ActiveProfiles("dev")
class AttributeAuthorityServiceTest extends IntegrationAbstractTest {

    private AttributeAuthorityApi attributeAuthorityApi;

    private AttributeAuthorityService attributeAuthorityService;

    @BeforeEach
    void init() {
        attributeAuthorityApi = Mockito.mock(AttributeAuthorityApi.class);
        Mockito.when(attributeAuthorityApi.getApiClient()).thenReturn(Mockito.mock(ApiClient.class));
        attributeAuthorityService = new AttributeAuthorityService(configProperties, attributeAuthorityApi);
    }

    @Test
    void GetOrganizations_Ok() {
        Mockito.when(attributeAuthorityApi.getOrganizationsWithHttpInfo(Mockito.any(),
                                                                        Mockito.any(),
                                                                        Mockito.any(),
                                                                        Mockito.any(),
                                                                        Mockito.any()))
               .thenReturn(ResponseEntity.ok(Mockito.mock(OrganizationsAttributeAuthority.class)));
        ResponseEntity<OrganizationsAttributeAuthority> response =
                attributeAuthorityService.getOrganizations(Mockito.any(),
                                                           Mockito.any(),
                                                           Mockito.any(),
                                                           Mockito.any(),
                                                           Mockito.any());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void UpsertOrganization_Ok() {
        Mockito.when(attributeAuthorityApi.upsertOrganizationWithHttpInfo(Mockito.any()))
               .thenReturn(ResponseEntity.ok(Mockito.mock(OrganizationWithReferentsAttributeAuthority.class)));
        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response =
                attributeAuthorityService.upsertOrganization(Mockito.any());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void GetOrganization_Ok() {
        Mockito.when(attributeAuthorityApi.getOrganizationWithHttpInfo(Mockito.any()))
               .thenReturn(ResponseEntity.ok(Mockito.mock(OrganizationWithReferentsAttributeAuthority.class)));
        ResponseEntity<OrganizationWithReferentsAttributeAuthority> response =
                attributeAuthorityService.getOrganization("1234567890");
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void DeleteOrganization_Ok() {
        Mockito.when(attributeAuthorityApi.deleteOrganizationWithHttpInfo(Mockito.any()))
               .thenReturn(ResponseEntity.noContent().build());
        ResponseEntity<Void> response = attributeAuthorityService.deleteOrganization("1234567890");
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void GetReferents_Ok() {
        Mockito.when(attributeAuthorityApi.getReferentsWithHttpInfo(Mockito.any()))
               .thenReturn(ResponseEntity.ok(Stream.of("AAAAAA00A00A000A").collect(Collectors.toList())));
        ResponseEntity<List<String>> response = attributeAuthorityService.getReferents(Mockito.any());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void InsertReferent_Ok() {
        Mockito.when(attributeAuthorityApi.insertReferentWithHttpInfo(Mockito.any(), Mockito.any()))
               .thenReturn(ResponseEntity.noContent().build());
        ReferentFiscalCodeAttributeAuthority referentFiscalCodeAttributeAuthority =
                new ReferentFiscalCodeAttributeAuthority();
        referentFiscalCodeAttributeAuthority.setReferentFiscalCode("AAAAAA00A00A000A");
        ResponseEntity<Void> response =
                attributeAuthorityService.insertReferent("1234567890", referentFiscalCodeAttributeAuthority);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void DeleteReferent_Ok() {
        Mockito.when(attributeAuthorityApi.deleteReferentWithHttpInfo(Mockito.any(), Mockito.any()))
               .thenReturn(ResponseEntity.noContent().build());
        ResponseEntity<Void> response = attributeAuthorityService.deleteReferent("1234567890", "AAAAAA00A00A000A");
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

}