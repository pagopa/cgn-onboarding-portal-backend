package it.gov.pagopa.cgn.portal.converter;


import it.gov.pagopa.cgn.portal.converter.referent.DataExportEycaWrapper;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgn.portal.service.ExportService;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.CategoryCodeEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.CreateDiscountRequestEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.TagCodeEyca;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

@Service
public class DataExportEycaWrapperConverter
        extends AbstractConverter<EycaDataExportViewEntity, DataExportEycaWrapper<CreateDiscountRequestEyca>> {

    @Value("${eyca.api.debug}")
    boolean eycaApiDebug;

    @Override
    protected Function<EycaDataExportViewEntity, DataExportEycaWrapper<CreateDiscountRequestEyca>> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<DataExportEycaWrapper<CreateDiscountRequestEyca>, EycaDataExportViewEntity> toEntityFunction() {
        return null;
    }

    protected Function<EycaDataExportViewEntity, DataExportEycaWrapper<CreateDiscountRequestEyca>> toDto = entity -> {

        CreateDiscountRequestEyca dataExport = new CreateDiscountRequestEyca();

        int result = 0;

        if (!eycaApiDebug && !(DiscountCodeTypeEnum.LANDINGPAGE.getEycaDataCode().equals(entity.getDiscountType()) ||
                               DiscountCodeTypeEnum.STATIC.getEycaDataCode().equals(entity.getDiscountType())) &&
            ExportService.LIVE_YES.equals(entity.getLive()) && !(StringUtils.isBlank(entity.getEycaUpdateId()) &&
                                                                 entity.getDiscountType()
                                                                       .equals(DiscountCodeTypeEnum.BUCKET.getEycaDataCode()))) {
            result = 1;
        }

        dataExport.setLive(CreateDiscountRequestEyca.LiveEnum.fromValue(result));
        dataExport.setEmail(entity.getEmail());
        dataExport.setLocalId(entity.getLocationLocalId());
        dataExport.setPhone(entity.getPhone());
        dataExport.setVendor(entity.getVendor());
        dataExport.setText(entity.getText());
        dataExport.setName(entity.getName());
        dataExport.setNameLocal(entity.getNameLocal());
        dataExport.setTextLocal(entity.getTextLocal());
        dataExport.setWeb(entity.getWeb());
        if (!StringUtils.isEmpty(entity.getTags())) {
            dataExport.setPlusTags(Arrays.stream(entity.getTags().split(",")).map(TagCodeEyca::fromValue).toList());
        }
        if (!StringUtils.isEmpty(entity.getCategories())) {
            dataExport.setPlusCategories(Arrays.stream(entity.getCategories().split(","))
                                               .map(CategoryCodeEyca::fromValue)
                                               .toList());
        }
        dataExport.setImageSourceWeb(entity.getImage());

        DataExportEycaWrapper<CreateDiscountRequestEyca> dto = new DataExportEycaWrapper<>(dataExport);
        dto.setEycaUpdateId(entity.getEycaUpdateId());
        dto.setDiscountID(entity.getDiscountId());
        dto.setDiscountType(entity.getDiscountType());
        dto.setVendor(entity.getVendor());
        dto.setStartDate(entity.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ITALIAN)));
        dto.setEndDate(entity.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ITALIAN)));
        dto.setLimitOfUse("No Limit");
        dto.setStaticCode(entity.getStaticCode());
        dto.setEycaLandingPageUrl(entity.getEycaLandingPageUrl());
        dto.setEycaEmailUpdateRequired(entity.getEycaEmailUpdateRequired());
        dto.setCcdbId(entity.getCcdbId());
        return dto;
    };
}
