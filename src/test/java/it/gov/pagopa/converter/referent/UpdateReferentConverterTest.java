package it.gov.pagopa.converter.referent;

import it.gov.pagopa.BaseTest;
import it.gov.pagopa.cgnonboardingportal.model.UpdateReferent;
import it.gov.pagopa.model.ReferentEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"dev"})
class UpdateReferentConverterTest extends BaseTest {


    @Autowired
    private UpdateReferentConverter updateReferentConverter;

    @Test
    void Convert_ConvertReferentEntityToDTO_Ok() {
        ReferentEntity referentEntity = createSampleProfileWithCommonFields().getReferent();
        UpdateReferent referentDto = updateReferentConverter.toDto(referentEntity);
        Assertions.assertEquals(referentEntity.getFirstName(), referentDto.getFirstName());

        Assertions.assertEquals(referentEntity.getLastName(), referentDto.getLastName());
        Assertions.assertEquals(referentEntity.getEmailAddress(), referentDto.getEmailAddress());
        Assertions.assertEquals(referentEntity.getTelephoneNumber(), referentDto.getTelephoneNumber());
    }

    @Test
    void Convert_ConvertReferentDTOToEntity_Ok() {
        UpdateReferent referentDto = createSampleUpdateReferent();
        ReferentEntity referentEntity  = updateReferentConverter.toEntity(referentDto);
        Assertions.assertEquals(referentDto.getFirstName(), referentEntity.getFirstName());
        Assertions.assertEquals(referentDto.getLastName(), referentEntity.getLastName());
        Assertions.assertEquals(referentDto.getEmailAddress(), referentEntity.getEmailAddress());
        Assertions.assertEquals(referentDto.getTelephoneNumber(), referentEntity.getTelephoneNumber());
    }

    private UpdateReferent createSampleUpdateReferent() {
        UpdateReferent referentDto = new UpdateReferent();
        referentDto.setFirstName("first_name_dto");
        referentDto.setLastName("last_name_dto");
        referentDto.setEmailAddress("referent.registry@pagopa.it");
        referentDto.setTelephoneNumber("+390123456789");
        return referentDto;
    }

}