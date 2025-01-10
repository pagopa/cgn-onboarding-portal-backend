package it.gov.pagopa.cgn.portal.converter;


import it.gov.pagopa.cgn.portal.converter.referent.DataExportEycaWrapper;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.repository.ProfileRepository;
import it.gov.pagopa.cgn.portal.service.ExportService;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DataExportEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.UpdateDataExportEyca;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Function;

@Service
public class UpdateDataExportEycaWrapperConverter
        extends AbstractConverter<EycaDataExportViewEntity, DataExportEycaWrapper<UpdateDataExportEyca>> {

    @Autowired
    ProfileRepository profile;

    @Override
    protected Function<EycaDataExportViewEntity, DataExportEycaWrapper<UpdateDataExportEyca>> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<DataExportEycaWrapper<UpdateDataExportEyca>, EycaDataExportViewEntity> toEntityFunction() {
        throw new NotImplementedException();
    }

    protected Function<EycaDataExportViewEntity, DataExportEycaWrapper<UpdateDataExportEyca>> toDto = entity -> {


        UpdateDataExportEyca updateDataExportEyca = new UpdateDataExportEyca();
        updateDataExportEyca.setId(entity.getEycaUpdateId());
        updateDataExportEyca.setEmail(entity.getEmail());
        updateDataExportEyca.setName(entity.getName());
        updateDataExportEyca.setPhone(entity.getPhone());
        updateDataExportEyca.setNameLocal(entity.getNameLocal());
        updateDataExportEyca.setVendor(entity.getVendor());
        updateDataExportEyca.setWeb(entity.getWeb());
        updateDataExportEyca.setText(entity.getText());
        updateDataExportEyca.setTextLocal(entity.getTextLocal());

        DataExportEycaWrapper<UpdateDataExportEyca> dto = new DataExportEycaWrapper<>(
                updateDataExportEyca);
        dto.setEycaUpdateId(entity.getEycaUpdateId());
        dto.setDiscountID(entity.getDiscountId());
        dto.setDiscountType(entity.getDiscountType());
        dto.setVendor(entity.getVendor());
        dto.setStartDate(entity.getStartDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)));
        dto.setEndDate(entity.getEndDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)));
        dto.setLimitOfUse("No Limit");

        dto.setStaticCode(entity.getStaticCode());
        dto.setEycaLandingPageUrl(entity.getEycaLandingPageUrl());
        dto.setEycaEmailUpdateRequired(entity.getEycaEmailUpdateRequired());

        return dto;
    };
}
