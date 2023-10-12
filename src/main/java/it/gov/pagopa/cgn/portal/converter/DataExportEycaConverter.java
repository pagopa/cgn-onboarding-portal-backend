package it.gov.pagopa.cgn.portal.converter;


import it.gov.pagopa.cgn.portal.converter.referent.DataExportEycaWrapper;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DataExportEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.LocationEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.UpdateDataExportEyca;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DataExportEycaConverter extends AbstractConverter<EycaDataExportViewEntity, DataExportEycaWrapper> {


    @Override
    protected Function<EycaDataExportViewEntity, DataExportEycaWrapper> toDtoFunction() {
        return toDto;
    }

    @Override
    protected Function<DataExportEycaWrapper, EycaDataExportViewEntity> toEntityFunction() {
        return null;
    }

    protected Function<EycaDataExportViewEntity, DataExportEycaWrapper> toDto =
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
               if (!StringUtils.isEmpty(entity.getCategories())){
                   dataExport.setPlusCategories(Arrays.stream(entity.getCategories().split(","))
                           .collect(Collectors.toList()));
               }
                dataExport.setImageSourceFile(entity.getImage());

                DataExportEycaWrapper dto = new DataExportEycaWrapper(dataExport);
                dto.setEycaUpdateId(entity.getEycaUpdateId());
                dto.setDiscountID(entity.getDiscountId());

                return dto;
            };


    public DataExportEycaWrapper groupedEntityToDto(Map.Entry<Long, List<EycaDataExportViewEntity>> entry) {
        Optional<EycaDataExportViewEntity> entity = entry.getValue().stream().findFirst();
        DataExportEycaWrapper dto = toDto(entity.orElseThrow());
        dto.getDataExportEyca().setPlusLocations(entry.getValue().stream()
                .map(en -> {
                    LocationEyca loc = new LocationEyca();
                    loc.setStreet(en.getStreet());
                    loc.setCity(en.getCity());
                    loc.setPointY(en.getLatitude());
                    loc.setPointX(en.getLongitude());
                    loc.setCountry(en.getCountry());
                    return loc;
                })
                .collect(Collectors.toList()));
        return dto;

    }


    public UpdateDataExportEyca convertToUpdateDataExportEyca(DataExportEycaWrapper dto) {

        UpdateDataExportEyca updateDataExportEyca = new UpdateDataExportEyca();
                 updateDataExportEyca.setId(dto.getEycaUpdateId());
                 updateDataExportEyca.setEmail(dto.getDataExportEyca().getEmail());
                updateDataExportEyca.setFiles(dto.getDataExportEyca().getFiles());
                updateDataExportEyca.setName(dto.getDataExportEyca().getName());
                updateDataExportEyca.setLive(dto.getDataExportEyca().getLive());
                updateDataExportEyca.setPhone(dto.getDataExportEyca().getPhone());
                updateDataExportEyca.setNameLocal(dto.getDataExportEyca().getNameLocal());
                updateDataExportEyca.setPlusCategories(dto.getDataExportEyca().getPlusCategories());
                updateDataExportEyca.setImageSourceFile(dto.getDataExportEyca().getImageSourceFile());
                updateDataExportEyca.setPlusTags(dto.getDataExportEyca().getPlusTags());
                updateDataExportEyca.setVendor(dto.getDataExportEyca().getVendor());
                updateDataExportEyca.setWeb(dto.getDataExportEyca().getWeb());
                updateDataExportEyca.setText(dto.getDataExportEyca().getText());
   return updateDataExportEyca;

    }



}