package it.gov.pagopa.cgn.portal.converter;


import it.gov.pagopa.cgn.portal.converter.referent.DataExportEycaWrapper;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.repository.ProfileRepository;
import it.gov.pagopa.cgn.portal.service.ExportService;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.UpdateDataExportEyca;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.function.Function;

@Service
public class UpdateDataExportEycaWrapperConverter extends AbstractConverter<EycaDataExportViewEntity, DataExportEycaWrapper<UpdateDataExportEyca>> {
	
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
        //updateDataExportEyca.setFiles(entity.getFiles());
        updateDataExportEyca.setName(entity.getName());
        updateDataExportEyca.setPhone(entity.getPhone());
        updateDataExportEyca.setNameLocal(entity.getNameLocal());
        //updateDataExportEyca.setPlusCategories(entity.getPlusCategories());
        //updateDataExportEyca.setImageSourceFile(entity.getImageSourceFile());
        //updateDataExportEyca.setPlusTags(entity.getPlusTags());
        updateDataExportEyca.setVendor(entity.getVendor());
        updateDataExportEyca.setWeb(entity.getWeb());
        updateDataExportEyca.setText(entity.getText());
      
        DataExportEycaWrapper<UpdateDataExportEyca> dto = new DataExportEycaWrapper<UpdateDataExportEyca>(updateDataExportEyca);
        dto.setEycaUpdateId(entity.getEycaUpdateId());
        dto.setDiscountID(entity.getDiscountId());
        dto.setVendor(entity.getVendor());
        dto.setStartDate(entity.getStartDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        dto.setEndDate(entity.getEndDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        dto.setLimitOfUse("No Limit");
        dto.setStaticCode(entity.getStaticCode());
        dto.setEycaLandingPageUrl(entity.getEycaLandingPageUrl());

        return dto;
    };
}