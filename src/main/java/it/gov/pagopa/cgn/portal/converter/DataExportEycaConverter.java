package it.gov.pagopa.cgn.portal.converter;


import it.gov.pagopa.cgn.portal.converter.referent.DataExportEycaExtension;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.CountryEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DataExportEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.LocationEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.UpdateDataExportEyca;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.sql.Update;
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

                DataExportEyca dataExport = new DataExportEyca();

                Optional<Integer> optIntLiveValue = Optional.ofNullable(entity.getLive())
                        .map(val->val.equals("Y")?1:0);
                dataExport.setLive(optIntLiveValue.orElse(0));
                dataExport.setEmail(entity.getEmail());
                dataExport.setLocalId(entity.getLocationLocalId());
                dataExport.setPhone(entity.getPhone());
                dataExport.setVendor(entity.getVendor());
                dataExport.setText(entity.getText());
                dataExport.setName(entity.getName());
                dataExport.setNameLocal(entity.getNameLocal());
                dataExport.setTextLocal(entity.getTextLocal());
                dataExport.setWeb(entity.getWeb());
                dataExport.setPlusCategories(Arrays.stream(entity.getCategories().split(","))
                        .collect(Collectors.toList()));
                dataExport.setImageSourceFile(entity.getImage());
                if (!StringUtils.isBlank(entity.getStreet())) {
                    LocationEyca locationEyca = new LocationEyca();
                    locationEyca.setStreet(entity.getStreet());
                    dataExport.setPlusLocations(Collections.singletonList(locationEyca));
                }

                DataExportEycaExtension dto = new DataExportEycaExtension(dataExport);
                dto.setEycaUpdateId(entity.getEycaUpdateId());
                dto.setDiscountID(entity.getDiscountId());

                return dto;
            };


    public DataExportEycaExtension groupedEntityToDto(Map.Entry<Long, List<EycaDataExportViewEntity>> entry) {
        Optional<EycaDataExportViewEntity> entity = entry.getValue().stream().findFirst();
        DataExportEycaExtension dto = toDto(entity.orElseThrow());
        dto.setPlusLocations(entry.getValue().stream()
                .map(en -> {
                    LocationEyca loc = new LocationEyca();
                    loc.setStreet(en.getStreet());
                    loc.setCity(en.getCity());
                  //  loc.setCountry(new ;
                    return loc;
                })
                .collect(Collectors.toList()));
        return dto;

    }



    public UpdateDataExportEyca convertDataExportEycaExtension(DataExportEycaExtension dto) {

        UpdateDataExportEyca updateDataExportEyca = new UpdateDataExportEyca();
                 updateDataExportEyca.setId(dto.getEycaUpdateId());
                    updateDataExportEyca.setEmail(dto.getEmail());
                updateDataExportEyca.setFiles(dto.getFiles());
                updateDataExportEyca.setName(dto.getName());
                updateDataExportEyca.setLive(dto.getLive());
                updateDataExportEyca.setPhone(dto.getPhone());
                updateDataExportEyca.setNameLocal(dto.getNameLocal());
                updateDataExportEyca.setPlusCategories(dto.getPlusCategories());
                updateDataExportEyca.setPlusLocations(dto.getPlusLocations());
                updateDataExportEyca.setImageSourceFile(dto.getImageSourceFile());
                updateDataExportEyca.setPlusTags(dto.getPlusTags());
                updateDataExportEyca.setVendor(dto.getVendor());
                updateDataExportEyca.setWeb(dto.getWeb());
   return updateDataExportEyca;

    }



}