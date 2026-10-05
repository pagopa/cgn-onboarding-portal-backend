package it.gov.pagopa.cgn.portal.converter.backoffice;

import it.gov.pagopa.cgn.portal.converter.backoffice.approved.BackofficeApprovedAgreementConverter;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.EntityTypeEnum;
import it.gov.pagopa.cgn.portal.model.ApprovedAgreementEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ApprovedAgreement;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.ApprovedAgreementState;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
public class BackofficeApprovedAgreementConverterTest {

    @Test
    public void ToDto_ExpiredAgreementToDto_Ok() {
        BackofficeApprovedAgreementConverter converter = new BackofficeApprovedAgreementConverter();
        ApprovedAgreementEntity entity = new ApprovedAgreementEntity();
        entity.setId("agreement-id");
        entity.setState(AgreementStateEnum.EXPIRED);
        entity.setEntityType(EntityTypeEnum.PRIVATE);
        entity.setStartDate(LocalDate.now());
        entity.setInformationLastUpdateDate(LocalDate.now());
        entity.setPublishedDiscounts(0L);
        entity.setTestPending(Boolean.FALSE);

        ApprovedAgreement dto = converter.toDto(entity);

        Assert.assertNotNull(dto);
        Assert.assertEquals(ApprovedAgreementState.EXPIRED, dto.getState());
        Assert.assertEquals(EntityType.PRIVATE, dto.getEntityType());
    }
}