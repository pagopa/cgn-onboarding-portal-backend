package it.gov.pagopa.converter;

import it.gov.pagopa.cgnonboardingportal.model.Agreement;
import it.gov.pagopa.cgnonboardingportal.model.AgreementState;
import it.gov.pagopa.cgnonboardingportal.model.ApprovedAgreement;
import it.gov.pagopa.enums.AgreementStateEnum;
import it.gov.pagopa.model.AgreementEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles({"dev"})
class AgreementConverterTest {

    @Autowired
    private AgreementConverter agreementConverter;

    @Test
    void Convert_ConvertPendingAgreementEntityToDTO_Ok() {
        AgreementEntity agreementEntity = createSampleAgreementEntityWithCommonFields();
        agreementEntity.setState(AgreementStateEnum.PENDING);
        Agreement pendingDto = agreementConverter.toDto(agreementEntity);
        commonAssertionsEntityToDto(agreementEntity, pendingDto);
        Assertions.assertEquals(AgreementState.PENDINGAGREEMENT, pendingDto.getState());
    }

    @Test
    void Convert_ConvertPendingAgreementDtoToEntity_Ok() {
        Agreement dto = createSampleAgreementDtoWithCommonFields();
        dto.setState(AgreementState.PENDINGAGREEMENT);
        AgreementEntity entity = agreementConverter.toEntity(dto);
        commonAssertionsDtoToEntity(entity, dto);
        Assertions.assertEquals(AgreementStateEnum.PENDING, entity.getState());
    }

    @Test
    void Convert_ConvertDraftAgreementEntityToDTO_Ok() {
        AgreementEntity agreementEntity = createSampleAgreementEntityWithCommonFields();
        agreementEntity.setState(AgreementStateEnum.DRAFT);
        Agreement draftDto = agreementConverter.toDto(agreementEntity);
        commonAssertionsEntityToDto(agreementEntity, draftDto);
        Assertions.assertEquals(AgreementState.DRAFTAGREEMENT, draftDto.getState());
    }

    @Test
    void Convert_ConvertDraftAgreementDtoToEntity_Ok() {
        Agreement dto = createSampleAgreementDtoWithCommonFields();
        dto.setState(AgreementState.DRAFTAGREEMENT);
        AgreementEntity entity = agreementConverter.toEntity(dto);
        commonAssertionsDtoToEntity(entity, dto);
        Assertions.assertEquals(AgreementStateEnum.DRAFT, entity.getState());
    }

    @Test
    void Convert_ConvertRejectedAgreementEntityToDTO_Ok() {
        AgreementEntity agreementEntity = createSampleAgreementEntityWithCommonFields();
        agreementEntity.setState(AgreementStateEnum.REJECTED);
        Agreement rejectedDto = agreementConverter.toDto(agreementEntity);
        commonAssertionsEntityToDto(agreementEntity, rejectedDto);
        Assertions.assertEquals(AgreementState.REJECTEDAGREEMENT, rejectedDto.getState());
    }

    @Test
    void Convert_ConvertRejectedAgreementDtoToEntity_Ok() {
        Agreement dto = createSampleAgreementDtoWithCommonFields();
        dto.setState(AgreementState.REJECTEDAGREEMENT);
        AgreementEntity entity = agreementConverter.toEntity(dto);
        commonAssertionsDtoToEntity(entity, dto);
        Assertions.assertEquals(AgreementStateEnum.REJECTED, entity.getState());
    }

    @Test
    void Convert_ConvertApprovedAgreementEntityToDTO_Ok() {
        AgreementEntity agreementEntity = createSampleAgreementEntityWithCommonFields();
        agreementEntity.setState(AgreementStateEnum.APPROVED);
        agreementEntity.setEndDate(LocalDate.of(2021, 12, 31));
        agreementEntity.setStartDate(LocalDate.now());
        Agreement agreementDto = agreementConverter.toDto(agreementEntity);
        commonAssertionsEntityToDto(agreementEntity, agreementDto);
        Assertions.assertEquals(AgreementState.APPROVEDAGREEMENT, agreementDto.getState());
        Assertions.assertTrue(agreementDto instanceof ApprovedAgreement);
        ApprovedAgreement approvedDto = (ApprovedAgreement) agreementDto;
        Assertions.assertEquals(agreementEntity.getStartDate(), approvedDto.getStartDate());
        Assertions.assertEquals(agreementEntity.getEndDate(), approvedDto.getEndDate());
    }

    @Test
    void Convert_ConvertApprovedAgreementDtoToEntity_Ok() {
        Agreement dto = createSampleAgreementDtoWithCommonFields(new ApprovedAgreement());
        dto.setState(AgreementState.APPROVEDAGREEMENT);
        ApprovedAgreement approvedDto = (ApprovedAgreement) dto;
        approvedDto.setStartDate(LocalDate.now());
        approvedDto.setEndDate(LocalDate.of(2021, 12, 31));
        AgreementEntity entity = agreementConverter.toEntity(approvedDto);
        commonAssertionsDtoToEntity(entity, dto);
        Assertions.assertEquals(AgreementStateEnum.APPROVED, entity.getState());
        Assertions.assertEquals(approvedDto.getStartDate(), entity.getStartDate());
        Assertions.assertEquals(approvedDto.getEndDate(), entity.getEndDate());

    }

    private void commonAssertionsEntityToDto(AgreementEntity agreementEntity, Agreement agreementDto) {
        Assertions.assertEquals(agreementEntity.getId(), agreementDto.getId());
        Assertions.assertNotNull(agreementEntity.getState());
        Assertions.assertNotNull(agreementDto.getState());
        Assertions.assertEquals(agreementEntity.getDiscountsModifiedDate(), agreementDto.getDiscountsLastModifiedDate());
        Assertions.assertEquals(agreementEntity.getDocumentsModifiedDate(), agreementDto.getDocumentsLastModifiedDate());
        Assertions.assertEquals(agreementEntity.getProfileModifiedDate(), agreementDto.getProfileLastModifiedDate());
    }

    private void commonAssertionsDtoToEntity(AgreementEntity entity, Agreement dto) {
        Assertions.assertEquals(dto.getId(), entity.getId());
        Assertions.assertNotNull(entity.getState());
        Assertions.assertEquals(dto.getDiscountsLastModifiedDate(), entity.getDiscountsModifiedDate());
        Assertions.assertEquals(dto.getDocumentsLastModifiedDate(), entity.getDocumentsModifiedDate());
        Assertions.assertEquals(dto.getProfileLastModifiedDate(), entity.getProfileModifiedDate());
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
