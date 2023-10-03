package it.gov.pagopa.cgn.portal.converter;


import it.gov.pagopa.cgn.portal.converter.referent.DataExportEycaExtension;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DataExportEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.LocationEyca;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DataExportEycaConverter extends AbstractConverter<EycaDataExportViewEntity, DataExportEycaExtension> {


    @Override
    protected Function<EycaDataExportViewEntity, DataExportEycaExtension> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<DataExportEycaExtension, EycaDataExportViewEntity> toEntityFunction() {
        return null;
    }

    protected Function<EycaDataExportViewEntity, DataExportEycaExtension> toDto =
            entity -> {
                DataExportEycaExtension dto = new DataExportEycaExtension();
                Optional<Integer> optIntLiveValue = Optional.ofNullable(entity.getLive())
                        .map(val->val.equals("Y")?1:0);
                dto.setLive(optIntLiveValue.orElse(0));
                dto.setEmail(entity.getEmail());
                dto.setLocalId(entity.getLocationLocalId());
                dto.setPhone(entity.getPhone());
                dto.setVendor(entity.getVendor());
                dto.setText(entity.getText());
                dto.setName(entity.getName());
                dto.setNameLocal(entity.getNameLocal());
                dto.setTextLocal(entity.getTextLocal());
                dto.setWeb(entity.getWeb());
                dto.setPlusCategories(Arrays.stream(entity.getCategories().split(","))
                        .collect(Collectors.toList()));
                dto.setImageSourceFile(entity.getImage());
                dto.setCreate(entity.getStartDate().isAfter(LocalDate.now().minusDays(1)));
                if (!StringUtils.isBlank(entity.getStreet())) {
                    LocationEyca locationEyca = new LocationEyca();
                    locationEyca.setStreet(entity.getStreet());
                    dto.setPlusLocations(Collections.singletonList(locationEyca));
                }

                return dto;
            };


    public DataExportEycaExtension groupedEntityToDto(Map.Entry<Long, List<EycaDataExportViewEntity>> entry) {
        Optional<EycaDataExportViewEntity> entity = entry.getValue().stream().findFirst();
        DataExportEycaExtension dto = toDto(entity.orElseThrow());
        dto.setPlusLocations(entry.getValue().stream()
                .map(en -> {
                    LocationEyca loc = new LocationEyca();
                    loc.setStreet(en.getStreet());
                    return loc;
                })
                .collect(Collectors.toList()));
        return dto;

    }

}