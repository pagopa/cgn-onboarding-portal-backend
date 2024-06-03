package it.gov.pagopa.cgn.portal.converter;


import it.gov.pagopa.cgn.portal.converter.referent.DataExportEycaWrapper;
import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgn.portal.service.ExportService;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DataExportEyca;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DataExportEycaWrapperConverter extends AbstractConverter<EycaDataExportViewEntity, DataExportEycaWrapper<DataExportEyca>> {
	
	@Value("eyca.api.debug")
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

        Optional<Integer> optIntLiveValue = Optional.ofNullable(entity.getLive())
                .map(val -> {
//                	if (eycaApiDebug) {
//                		return 0;
//                	}
//                    if (ExportService.LIVE_YES.equals(val)) {
//                        if (StringUtils.isBlank(entity.getEycaUpdateId()) && entity.getDiscountType().equals(DiscountCodeTypeEnum.BUCKET.getEycaDataCode())) {
//                            return 0;
//                        } else {
//                            return 1;
//                        }
//                    } else {
//                        return 0;
//                    }
                    
                    int result = 0;

                    if (!eycaApiDebug) {
                        if (ExportService.LIVE_YES.equals(val)) {
                            if ( ! (StringUtils.isBlank(entity.getEycaUpdateId()) && entity.getDiscountType().equals(DiscountCodeTypeEnum.BUCKET.getEycaDataCode()))) {
                                result = 1;
                            }
                        }
                    }

                    return result;                    
                });

        dataExport.setLive(optIntLiveValue.get());
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
            dataExport.setPlusTags(Arrays.stream(entity.getTags().split(","))
                    .collect(Collectors.toList()));
        }
        if (!StringUtils.isEmpty(entity.getCategories())) {
            dataExport.setPlusCategories(Arrays.stream(entity.getCategories().split(","))
                    .collect(Collectors.toList()));
        }
        dataExport.setImageSourceWeb(entity.getImage());

        DataExportEycaWrapper<DataExportEyca> dto = new DataExportEycaWrapper<DataExportEyca>(dataExport);
        dto.setEycaUpdateId(entity.getEycaUpdateId());
        dto.setDiscountID(entity.getDiscountId());

        return dto;
    };
}