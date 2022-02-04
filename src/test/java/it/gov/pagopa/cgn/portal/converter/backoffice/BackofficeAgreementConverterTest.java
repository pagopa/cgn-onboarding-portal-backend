package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.Agreement;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.AgreementState;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.AssignedAgreement;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.PendingAgreement;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@RunWith(SpringRunner.class)
public class BackofficeAgreementConverterTest {


    @Test
    public void ToDto_AssignedAgreementToDto_ok() {
        BackofficeAgreementConverter converter = getBackofficeAgreementConverter();

        AgreementEntity backofficeAgreementEntity = createSampleBackofficeAgreementEntity();
        backofficeAgreementEntity.setState(AgreementStateEnum.PENDING);
        backofficeAgreementEntity.setBackofficeAssignee("user");

        Agreement agreementDto = converter.toDto.apply(backofficeAgreementEntity);
        Assert.assertTrue(agreementDto instanceof AssignedAgreement);
        Assert.assertEquals(AgreementState.ASSIGNEDAGREEMENT, agreementDto.getState());

    }

    @Test
    public void ToDto_PendingAgreementToDto_ok() {
        BackofficeAgreementConverter converter = getBackofficeAgreementConverter();

        AgreementEntity backofficeAgreementEntity = createSampleBackofficeAgreementEntity();
        backofficeAgreementEntity.setState(AgreementStateEnum.PENDING);

        Agreement agreementDto = converter.toDto.apply(backofficeAgreementEntity);
        Assert.assertTrue(agreementDto instanceof PendingAgreement);
        Assert.assertEquals(AgreementState.PENDINGAGREEMENT, agreementDto.getState());

    }

    @Test
    public void ToDto_RejectedAgreementToDto_ThrowCGNException() {
        BackofficeAgreementConverter converter = getBackofficeAgreementConverter();

        AgreementEntity backofficeAgreementEntity = createSampleBackofficeAgreementEntity();
        backofficeAgreementEntity.setState(AgreementStateEnum.REJECTED);
        Assert.assertThrows(CGNException.class, () -> converter.toDto.apply(backofficeAgreementEntity));

    }

    private BackofficeAgreementConverter getBackofficeAgreementConverter() {
        BackofficeDiscountConverter discountConverter = new BackofficeDiscountConverter();
        BackofficeDocumentConverter documentConverter = new BackofficeDocumentConverter();
        BackofficeProfileConverter profileConverter = new BackofficeProfileConverter();

        return new BackofficeAgreementConverter(discountConverter, documentConverter, profileConverter);
    }


    private AgreementEntity createSampleBackofficeAgreementEntity() {
        AgreementEntity backofficeAgreementEntity = new AgreementEntity();
        backofficeAgreementEntity.setId("agreement_id");
        backofficeAgreementEntity.setImageUrl("image12345.png");

        backofficeAgreementEntity.setStartDate(LocalDate.now());
        backofficeAgreementEntity.setEndDate(LocalDate.now().plusYears(1));

        backofficeAgreementEntity.setRequestApprovalTime(OffsetDateTime.now());
        backofficeAgreementEntity.setInformationLastUpdateDate(LocalDate.now());
        return backofficeAgreementEntity;
    }

}
