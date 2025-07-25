package it.gov.pagopa.cgn.portal.converter;

import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.EntityTypeEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgnonboardingportal.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
public class AgreementConverterTest {

    private final AgreementConverter agreementConverter = new AgreementConverter();

    @Test
    public void Convert_ConvertPendingAgreementEntityToDTO_Ok() {
        AgreementEntity agreementEntity = TestUtils.createSampleAgreementEntityWithCommonFields();
        agreementEntity.setState(AgreementStateEnum.PENDING);
        agreementEntity.setEntityType(EntityTypeEnum.PRIVATE);
        Agreement pendingDto = agreementConverter.toDto(agreementEntity);
        commonAssertionsEntityToDto(agreementEntity, pendingDto);
        Assert.assertEquals(AgreementState.PENDING_AGREEMENT, pendingDto.getState());
    }

    @Test
    public void Convert_ConvertPendingAgreementDtoToEntity_Ok() {
        PendingAgreement dto = new PendingAgreement();
        fillAgreementDtoWithCommonFields(dto);
        dto.setState(AgreementState.PENDING_AGREEMENT);
        dto.setEntityType(EntityType.PRIVATE);
        AgreementEntity entity = agreementConverter.toEntity(dto);
        commonAssertionsDtoToEntity(entity, dto);
        Assert.assertEquals(AgreementStateEnum.PENDING, entity.getState());
    }

    @Test
    public void Convert_ConvertDraftAgreementEntityToDTO_Ok() {
        AgreementEntity agreementEntity = TestUtils.createSampleAgreementEntityWithCommonFields();
        agreementEntity.setState(AgreementStateEnum.DRAFT);
        agreementEntity.setEntityType(EntityTypeEnum.PRIVATE);
        Agreement draftDto = agreementConverter.toDto(agreementEntity);
        commonAssertionsEntityToDto(agreementEntity, draftDto);
        Assert.assertEquals(AgreementState.DRAFT_AGREEMENT, draftDto.getState());
    }

    @Test
    public void Convert_ConvertDraftAgreementDtoToEntity_Ok() {
        DraftAgreement dto = new DraftAgreement();
        fillAgreementDtoWithCommonFields(dto);
        dto.setState(AgreementState.DRAFT_AGREEMENT);
        dto.setEntityType(EntityType.PRIVATE);
        AgreementEntity entity = agreementConverter.toEntity(dto);
        commonAssertionsDtoToEntity(entity, dto);
        Assert.assertEquals(AgreementStateEnum.DRAFT, entity.getState());
    }

    @Test
    public void Convert_ConvertRejectedAgreementEntityToDTO_Ok() {
        AgreementEntity agreementEntity = TestUtils.createSampleAgreementEntityWithCommonFields();
        agreementEntity.setState(AgreementStateEnum.REJECTED);
        agreementEntity.setEntityType(EntityTypeEnum.PRIVATE);
        Agreement rejectedDto = agreementConverter.toDto(agreementEntity);
        commonAssertionsEntityToDto(agreementEntity, rejectedDto);
        Assert.assertEquals(AgreementState.REJECTED_AGREEMENT, rejectedDto.getState());
    }

    @Test
    public void Convert_ConvertRejectedAgreementDtoToEntity_Ok() {
        RejectedAgreement dto = new RejectedAgreement();
        fillAgreementDtoWithCommonFields(dto);
        dto.setState(AgreementState.REJECTED_AGREEMENT);
        dto.setEntityType(EntityType.PRIVATE);
        AgreementEntity entity = agreementConverter.toEntity(dto);
        commonAssertionsDtoToEntity(entity, dto);
        Assert.assertEquals(AgreementStateEnum.REJECTED, entity.getState());
    }

    @Test
    public void Convert_ConvertApprovedAgreementEntityToDTO_Ok() {
        AgreementEntity agreementEntity = TestUtils.createSampleAgreementEntityWithCommonFields();
        agreementEntity.setState(AgreementStateEnum.APPROVED);
        agreementEntity.setStartDate(LocalDate.now());
        agreementEntity.setEntityType(EntityTypeEnum.PRIVATE);
        Agreement agreementDto = agreementConverter.toDto(agreementEntity);
        commonAssertionsEntityToDto(agreementEntity, agreementDto);
        Assert.assertEquals(AgreementState.APPROVED_AGREEMENT, agreementDto.getState());
        Assert.assertTrue(agreementDto instanceof ApprovedAgreement);
        ApprovedAgreement approvedDto = (ApprovedAgreement) agreementDto;
        Assert.assertEquals(agreementEntity.getStartDate(), approvedDto.getStartDate());
    }

    @Test
    public void Convert_ConvertApprovedAgreementDtoToEntity_Ok() {
        ApprovedAgreement dto = new ApprovedAgreement();
        dto.setId("agreement_dto_id");
        dto.setImageUrl("imageURL");
        dto.setState(AgreementState.APPROVED_AGREEMENT);
        dto.setStartDate(LocalDate.now());
        dto.setEntityType(EntityType.PRIVATE);
        AgreementEntity entity = agreementConverter.toEntity(dto);
        commonAssertionsDtoToEntity(entity, dto);
        Assert.assertEquals(AgreementStateEnum.APPROVED, entity.getState());
        Assert.assertEquals(dto.getStartDate(), entity.getStartDate());
    }

    private void commonAssertionsEntityToDto(AgreementEntity agreementEntity, Agreement agreementDto) {
        Assert.assertEquals(agreementEntity.getId(), agreementDto.getId());
        Assert.assertNotNull(agreementEntity.getState());
        Assert.assertNotNull(agreementDto.getState());
        Assert.assertEquals(agreementEntity.getImageUrl(), agreementDto.getImageUrl());
    }

    private void commonAssertionsDtoToEntity(AgreementEntity entity, Agreement dto) {
        Assert.assertEquals(dto.getId(), entity.getId());
        Assert.assertNotNull(entity.getState());
        Assert.assertEquals(dto.getImageUrl(), entity.getImageUrl());
    }

    private void fillAgreementDtoWithCommonFields(Agreement agreement) {
        agreement.setId("agreement_dto_id");
        agreement.setImageUrl("image12345.png");
    }

}
