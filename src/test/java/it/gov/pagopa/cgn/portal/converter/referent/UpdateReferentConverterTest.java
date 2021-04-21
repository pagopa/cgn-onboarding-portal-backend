package it.gov.pagopa.cgn.portal.converter.referent;

import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgnonboardingportal.model.UpdateReferent;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class UpdateReferentConverterTest {

    private UpdateReferentConverter updateReferentConverter = new UpdateReferentConverter();

    @Test
    public void Convert_ConvertReferentEntityToDTO_Ok() {
        ReferentEntity referentEntity = TestUtils.createSampleProfileWithCommonFields().getReferent();
        UpdateReferent referentDto = updateReferentConverter.toDto(referentEntity);
        Assert.assertEquals(referentEntity.getFirstName(), referentDto.getFirstName());

        Assert.assertEquals(referentEntity.getLastName(), referentDto.getLastName());
        Assert.assertEquals(referentEntity.getEmailAddress(), referentDto.getEmailAddress());
        Assert.assertEquals(referentEntity.getTelephoneNumber(), referentDto.getTelephoneNumber());
    }

    @Test
    public void Convert_ConvertReferentDTOToEntity_Ok() {
        UpdateReferent referentDto = createSampleUpdateReferent();
        ReferentEntity referentEntity  = updateReferentConverter.toEntity(referentDto);
        Assert.assertEquals(referentDto.getFirstName(), referentEntity.getFirstName());
        Assert.assertEquals(referentDto.getLastName(), referentEntity.getLastName());
        Assert.assertEquals(referentDto.getEmailAddress(), referentEntity.getEmailAddress());
        Assert.assertEquals(referentDto.getTelephoneNumber(), referentEntity.getTelephoneNumber());
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