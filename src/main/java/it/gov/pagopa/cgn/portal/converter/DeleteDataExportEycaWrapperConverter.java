package it.gov.pagopa.cgn.portal.converter;


import it.gov.pagopa.cgn.portal.converter.referent.DataExportEycaWrapper;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.IdRequestEyca;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Function;

@Service
public class DeleteDataExportEycaWrapperConverter
        extends AbstractConverter<EycaDataExportViewEntity, DataExportEycaWrapper<IdRequestEyca>> {

    @Value("${eyca.api.debug}")
    boolean eycaApiDebug;

    @Override
    protected Function<EycaDataExportViewEntity, DataExportEycaWrapper<IdRequestEyca>> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<DataExportEycaWrapper<IdRequestEyca>, EycaDataExportViewEntity> toEntityFunction() {
        return null;
    }

    protected Function<EycaDataExportViewEntity, DataExportEycaWrapper<IdRequestEyca>> toDto = entity -> {

        IdRequestEyca deleteDataExport = new IdRequestEyca();

        deleteDataExport.setId(entity.getEycaUpdateId());

        DataExportEycaWrapper<IdRequestEyca> dto = new DataExportEycaWrapper<>(deleteDataExport);
        dto.setEycaUpdateId(entity.getEycaUpdateId());
        dto.setVendor(entity.getVendor());

        dto.setStartDate(entity.getStartDate() != null
                             ? entity.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ITALIAN))
                             : "N/A");

        dto.setEndDate(entity.getEndDate() != null
                           ? entity.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ITALIAN))
                           : "N/A");

        dto.setLimitOfUse("No Limit");
        dto.setStaticCode(entity.getStaticCode());
        dto.setEycaLandingPageUrl(entity.getEycaLandingPageUrl());
        dto.setDiscountType(entity.getDiscountType());
        dto.setEycaEmailUpdateRequired(false);
        dto.setCcdbId(entity.getCcdbId());
        return dto;
    };
}
