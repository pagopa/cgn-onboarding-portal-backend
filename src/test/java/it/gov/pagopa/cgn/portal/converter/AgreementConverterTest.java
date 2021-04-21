package it.gov.pagopa.cgn.portal.converter;

import it.gov.pagopa.cgnonboardingportal.model.Agreement;
import it.gov.pagopa.cgnonboardingportal.model.AgreementState;
import it.gov.pagopa.cgnonboardingportal.model.ApprovedAgreement;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
public class AgreementConverterTest {

    private AgreementConverter agreementConverter = new AgreementConverter();

    @Test
    public void Convert_ConvertPendingAgreementEntityToDTO_Ok() {
        AgreementEntity agreementEntity = createSampleAgreementEntityWithCommonFields();
        agreementEntity.setState(AgreementStateEnum.PENDING);
        Agreement pendingDto = agreementConverter.toDto(agreementEntity);
        commonAssertionsEntityToDto(agreementEntity, pendingDto);
        Assert.assertEquals(AgreementState.PENDINGAGREEMENT, pendingDto.getState());
    }

    @Test
    public void Convert_ConvertPendingAgreementDtoToEntity_Ok() {
        Agreement dto = createSampleAgreementDtoWithCommonFields();
        dto.setState(AgreementState.PENDINGAGREEMENT);
        AgreementEntity entity = agreementConverter.toEntity(dto);
        commonAssertionsDtoToEntity(entity, dto);
        Assert.assertEquals(AgreementStateEnum.PENDING, entity.getState());
    }

    @Test
    public void Convert_ConvertDraftAgreementEntityToDTO_Ok() {
        AgreementEntity agreementEntity = createSampleAgreementEntityWithCommonFields();
        agreementEntity.setState(AgreementStateEnum.DRAFT);
        Agreement draftDto = agreementConverter.toDto(agreementEntity);
        commonAssertionsEntityToDto(agreementEntity, draftDto);
        Assert.assertEquals(AgreementState.DRAFTAGREEMENT, draftDto.getState());
    }

    @Test
    public void Convert_ConvertDraftAgreementDtoToEntity_Ok() {
        Agreement dto = createSampleAgreementDtoWithCommonFields();
        dto.setState(AgreementState.DRAFTAGREEMENT);
        AgreementEntity entity = agreementConverter.toEntity(dto);
        commonAssertionsDtoToEntity(entity, dto);
        Assert.assertEquals(AgreementStateEnum.DRAFT, entity.getState());
    }

    @Test
    public void Convert_ConvertRejectedAgreementEntityToDTO_Ok() {
        AgreementEntity agreementEntity = createSampleAgreementEntityWithCommonFields();
        agreementEntity.setState(AgreementStateEnum.REJECTED);
        Agreement rejectedDto = agreementConverter.toDto(agreementEntity);
        commonAssertionsEntityToDto(agreementEntity, rejectedDto);
        Assert.assertEquals(AgreementState.REJECTEDAGREEMENT, rejectedDto.getState());
    }

    @Test
    public void Convert_ConvertRejectedAgreementDtoToEntity_Ok() {
        Agreement dto = createSampleAgreementDtoWithCommonFields();
        dto.setState(AgreementState.REJECTEDAGREEMENT);
        AgreementEntity entity = agreementConverter.toEntity(dto);
        commonAssertionsDtoToEntity(entity, dto);
        Assert.assertEquals(AgreementStateEnum.REJECTED, entity.getState());
    }

    @Test
    public void Convert_ConvertApprovedAgreementEntityToDTO_Ok() {
        AgreementEntity agreementEntity = createSampleAgreementEntityWithCommonFields();
        agreementEntity.setState(AgreementStateEnum.APPROVED);
        agreementEntity.setEndDate(LocalDate.of(2021, 12, 31));
        agreementEntity.setStartDate(LocalDate.now());
        Agreement agreementDto = agreementConverter.toDto(agreementEntity);
        commonAssertionsEntityToDto(agreementEntity, agreementDto);
        Assert.assertEquals(AgreementState.APPROVEDAGREEMENT, agreementDto.getState());
        Assert.assertTrue(agreementDto instanceof ApprovedAgreement);
        ApprovedAgreement approvedDto = (ApprovedAgreement) agreementDto;
        Assert.assertEquals(agreementEntity.getStartDate(), approvedDto.getStartDate());
        Assert.assertEquals(agreementEntity.getEndDate(), approvedDto.getEndDate());
    }

    @Test
    public void Convert_ConvertApprovedAgreementDtoToEntity_Ok() {
        Agreement dto = createSampleAgreementDtoWithCommonFields(new ApprovedAgreement());
        dto.setState(AgreementState.APPROVEDAGREEMENT);
        ApprovedAgreement approvedDto = (ApprovedAgreement) dto;
        approvedDto.setStartDate(LocalDate.now());
        approvedDto.setEndDate(LocalDate.of(2021, 12, 31));
        AgreementEntity entity = agreementConverter.toEntity(approvedDto);
        commonAssertionsDtoToEntity(entity, dto);
        Assert.assertEquals(AgreementStateEnum.APPROVED, entity.getState());
        Assert.assertEquals(approvedDto.getStartDate(), entity.getStartDate());
        Assert.assertEquals(approvedDto.getEndDate(), entity.getEndDate());

    }

    private void commonAssertionsEntityToDto(AgreementEntity agreementEntity, Agreement agreementDto) {
        Assert.assertEquals(agreementEntity.getId(), agreementDto.getId());
        Assert.assertNotNull(agreementEntity.getState());
        Assert.assertNotNull(agreementDto.getState());
        Assert.assertEquals(agreementEntity.getDiscountsModifiedDate(), agreementDto.getDiscountsLastModifiedDate());
        Assert.assertEquals(agreementEntity.getDocumentsModifiedDate(), agreementDto.getDocumentsLastModifiedDate());
        Assert.assertEquals(agreementEntity.getProfileModifiedDate(), agreementDto.getProfileLastModifiedDate());
    }

    private void commonAssertionsDtoToEntity(AgreementEntity entity, Agreement dto) {
        Assert.assertEquals(dto.getId(), entity.getId());
        Assert.assertNotNull(entity.getState());
        Assert.assertEquals(dto.getDiscountsLastModifiedDate(), entity.getDiscountsModifiedDate());
        Assert.assertEquals(dto.getDocumentsLastModifiedDate(), entity.getDocumentsModifiedDate());
        Assert.assertEquals(dto.getProfileLastModifiedDate(), entity.getProfileModifiedDate());
    }

    private AgreementEntity createSampleAgreementEntityWithCommonFields() {
        AgreementEntity agreementEntity = new AgreementEntity();
        agreementEntity.setId("agreement_id");
        agreementEntity.setDiscountsModifiedDate(LocalDate.now());
        agreementEntity.setDocumentsModifiedDate(LocalDate.now());
        agreementEntity.setProfileModifiedDate(LocalDate.now());
        return agreementEntity;
    }

    private Agreement createSampleAgreementDtoWithCommonFields() {
        Agreement dto = new Agreement();
        dto.setId("agreement_dto_id");
        dto.setDiscountsLastModifiedDate(LocalDate.now());
        dto.setProfileLastModifiedDate(LocalDate.now());
        dto.setDocumentsLastModifiedDate(LocalDate.now());
        return dto;
    }

    private Agreement createSampleAgreementDtoWithCommonFields(Agreement dto) {
        dto.setId("agreement_dto_id");
        dto.setDiscountsLastModifiedDate(LocalDate.now());
        dto.setProfileLastModifiedDate(LocalDate.now());
        dto.setDocumentsLastModifiedDate(LocalDate.now());
        return dto;
    }
}
