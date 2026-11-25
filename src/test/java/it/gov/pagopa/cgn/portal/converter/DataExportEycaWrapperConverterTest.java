package it.gov.pagopa.cgn.portal.converter;

import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.CategoryCodeEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.CreateDiscountRequestEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.TagCodeEyca;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataExportEycaWrapperConverterTest {

    private final DataExportEycaWrapperConverter converter = new DataExportEycaWrapperConverter();

    @Test
    void toDto_mapsCategoriesAndTagsToEycaCodes() {
        EycaDataExportViewEntity entity = TestUtils.getEycaDataExportForCreate().getFirst();
        entity.setCategories("SV,LR");
        entity.setTags("tag_mobility,tag_accessible");

        CreateDiscountRequestEyca request = converter.toDto(entity).getDataExportEyca();

        assertEquals(List.of(CategoryCodeEyca.SV, CategoryCodeEyca.LR), request.getPlusCategories());
        assertEquals(List.of(TagCodeEyca.TAG_MOBILITY, TagCodeEyca.TAG_ACCESSIBLE), request.getPlusTags());
        assertEquals(CreateDiscountRequestEyca.LiveEnum.NUMBER_0, request.getLive());
    }

    @Test
    void toDto_unknownCategory_throwsIllegalArgumentException() {
        EycaDataExportViewEntity entity = TestUtils.getEycaDataExportForCreate().getFirst();
        entity.setCategories("XX");

        assertThrows(IllegalArgumentException.class, () -> converter.toDto(entity));
    }

    @Test
    void toDto_unknownTag_throwsIllegalArgumentException() {
        EycaDataExportViewEntity entity = TestUtils.getEycaDataExportForCreate().getFirst();
        entity.setTags("tag_unknown");

        assertThrows(IllegalArgumentException.class, () -> converter.toDto(entity));
    }
}
