package it.gov.pagopa.cgn.portal.converter;


import it.gov.pagopa.cgn.portal.converter.referent.DataExportEycaWrapper;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgn.portal.service.ExportService;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DataExportEyca;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DataExportEycaWrapperConverter
        extends AbstractConverter<EycaDataExportViewEntity, DataExportEycaWrapper<DataExportEyca>> {

    @Value("${eyca.api.debug}")
    boolean eycaApiDebug;

    @Override
    protected Function<EycaDataExportViewEntity, DataExportEycaWrapper<DataExportEyca>> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<DataExportEycaWrapper<DataExportEyca>, EycaDataExportViewEntity> toEntityFunction() {
        return null;
    }

    protected Function<EycaDataExportViewEntity, DataExportEycaWrapper<DataExportEyca>> toDto = entity -> {

        DataExportEyca dataExport = new DataExportEyca();

        int result = 0;

        if (!eycaApiDebug && !(DiscountCodeTypeEnum.LANDINGPAGE.getEycaDataCode().equals(entity.getDiscountType()) ||
                               DiscountCodeTypeEnum.STATIC.getEycaDataCode().equals(entity.getDiscountType())) &&
            ExportService.LIVE_YES.equals(entity.getLive()) && !(StringUtils.isBlank(entity.getEycaUpdateId()) &&
                                                                 entity.getDiscountType()
                                                                       .equals(DiscountCodeTypeEnum.BUCKET.getEycaDataCode()))) {
            result = 1;
        }

        dataExport.setLive(result);
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
            dataExport.setPlusTags(Arrays.stream(entity.getTags().split(",")).collect(Collectors.toList()));
        }
        if (!StringUtils.isEmpty(entity.getCategories())) {
            dataExport.setPlusCategories(Arrays.stream(entity.getCategories().split(",")).collect(Collectors.toList()));
        }
        dataExport.setImageSourceWeb(entity.getImage());

        DataExportEycaWrapper<DataExportEyca> dto = new DataExportEycaWrapper<>(dataExport);
        dto.setEycaUpdateId(entity.getEycaUpdateId());
        dto.setDiscountID(entity.getDiscountId());
        dto.setDiscountType(entity.getDiscountType());
        dto.setVendor(entity.getVendor());
        dto.setStartDate(entity.getStartDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        dto.setEndDate(entity.getEndDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        dto.setLimitOfUse("No Limit");
        dto.setStaticCode(entity.getStaticCode());
        dto.setEycaLandingPageUrl(entity.getEycaLandingPageUrl());
        return dto;
    };
}
