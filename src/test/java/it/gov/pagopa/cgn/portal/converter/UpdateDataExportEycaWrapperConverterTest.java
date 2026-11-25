package it.gov.pagopa.cgn.portal.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.UpdateDiscountRequestEyca;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateDataExportEycaWrapperConverterTest {

    private final UpdateDataExportEycaWrapperConverter converter = new UpdateDataExportEycaWrapperConverter();

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void toDto_serializedRequestOmitsFieldsWithoutValue() throws Exception {
        EycaDataExportViewEntity entity = TestUtils.getEycaDataExportForUpdate().getFirst();
        entity.setStartDate(LocalDate.of(2026, Month.JANUARY, 1));
        entity.setEndDate(LocalDate.of(2026, Month.DECEMBER, 31));

        UpdateDiscountRequestEyca request = converter.toDto(entity).getDataExportEyca();
        JsonNode body = objectMapper.readTree(objectMapper.writeValueAsString(request));

        assertEquals("1234", body.path("id").asText());
        assertEquals("name_local_0", body.path("name_local").asText());
        List.of("live", "expiry_date", "local_id", "brand_description", "instructions", "terms_and_conditions",
                "instagram", "email", "phone")
            .forEach(field -> assertFalse(body.has(field), field));
        body.properties().forEach(field -> assertFalse(field.getValue().isNull(), field.getKey()));
        assertTrue(body.path("+tags").isArray());
    }
}
