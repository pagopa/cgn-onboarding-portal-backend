package it.gov.pagopa.cgn.portal.converter;


import it.gov.pagopa.cgn.portal.converter.referent.DataExportEycaWrapper;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DataExportEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.DeleteDataExportEyca;
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
                        .map(val -> {
                            if (val.equals("Y")) {
                                if (entity.getDiscountType().equals("BUCKET")) {
                                    return 0;
                                } else {
                                    return 1;
                                }
                            } else {
                                return 0;
                            }
                        });

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
                if (!StringUtils.isEmpty(entity.getTags())) {
                    dataExport.setPlusTags(Arrays.stream(entity.getTags().split(","))
                            .collect(Collectors.toList()));
                }
                if (!StringUtils.isEmpty(entity.getCategories())) {
                    dataExport.setPlusCategories(Arrays.stream(entity.getCategories().split(","))
                            .collect(Collectors.toList()));
                }
                dataExport.setImageSourceWeb(entity.getImage());

                DataExportEycaWrapper dto = new DataExportEycaWrapper(dataExport);
                dto.setEycaUpdateId(entity.getEycaUpdateId());
                dto.setDiscountID(entity.getDiscountId());

                return dto;
            };


    public DataExportEycaWrapper groupedEntityToDto(Map.Entry<Long, List<EycaDataExportViewEntity>> entry) {
        Optional<EycaDataExportViewEntity> entity = entry.getValue().stream().findFirst();
        DataExportEycaWrapper dto = toDto(entity.orElseThrow());

        List<LocationEyca>  locationEycaList = entry.getValue().stream()
                .map(en -> {
                    if (!StringUtils.isEmpty(en.getCountry())&&
                            !StringUtils.isEmpty(en.getCity())&&
                            !StringUtils.isEmpty(en.getStreet())&&
                            !StringUtils.isEmpty(en.getLatitude())&&
                            !StringUtils.isEmpty(en.getLongitude())
                    ) {
                        LocationEyca loc = new LocationEyca();
                        loc.setCountry((en.getCountry()));
                        loc.setStreet((en.getStreet()));
                        loc.setCity((en.getCity()));
                        loc.setPointY((en.getLatitude()));
                        loc.setPointX((en.getLongitude()));
                        return loc;
                    } else {
                        return null;
                    }
                })
                .collect(Collectors.toList());

       locationEycaList = locationEycaList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!locationEycaList.isEmpty()) {
            dto.getDataExportEyca().setPlusLocations(locationEycaList);
        }

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

    public DeleteDataExportEyca convertToDeleteDataExportEyca(DataExportEycaWrapper dto) {

        DeleteDataExportEyca deleteDataExportEyca = new DeleteDataExportEyca();
        deleteDataExportEyca.setId(dto.getEycaUpdateId());
        return deleteDataExportEyca;
    }

}