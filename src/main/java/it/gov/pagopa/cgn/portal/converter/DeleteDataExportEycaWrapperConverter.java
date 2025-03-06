package it.gov.pagopa.cgn.portal.converter;


import it.gov.pagopa.cgn.portal.converter.referent.DataExportEycaWrapper;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgn.portal.service.ExportService;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DataExportEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DeleteDataExportEyca;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.sql.Delete;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DeleteDataExportEycaWrapperConverter
        extends AbstractConverter<EycaDataExportViewEntity, DataExportEycaWrapper<DeleteDataExportEyca>> {

    @Value("${eyca.api.debug}")
    boolean eycaApiDebug;

    @Override
    protected Function<EycaDataExportViewEntity, DataExportEycaWrapper<DeleteDataExportEyca>> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<DataExportEycaWrapper<DeleteDataExportEyca>, EycaDataExportViewEntity> toEntityFunction() {
        return null;
    }

    protected Function<EycaDataExportViewEntity, DataExportEycaWrapper<DeleteDataExportEyca>> toDto = entity -> {

        DeleteDataExportEyca deleteDataExport = new DeleteDataExportEyca();

        deleteDataExport.setId(entity.getEycaUpdateId());

        DataExportEycaWrapper<DeleteDataExportEyca> dto = new DataExportEycaWrapper<>(deleteDataExport);
        dto.setEycaUpdateId(entity.getEycaUpdateId());
        dto.setVendor(entity.getVendor());

        dto.setStartDate(entity.getStartDate() != null
                             ? entity.getStartDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH))
                             : "N/A");

        dto.setEndDate(entity.getEndDate() != null
                           ? entity.getEndDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH))
                           : "N/A");

        dto.setLimitOfUse("No Limit");
        dto.setStaticCode(entity.getStaticCode());
        dto.setEycaLandingPageUrl(entity.getEycaLandingPageUrl());
        dto.setDiscountType(entity.getDiscountType());
        dto.setEycaEmailUpdateRequired(false);
        return dto;
    };
}
