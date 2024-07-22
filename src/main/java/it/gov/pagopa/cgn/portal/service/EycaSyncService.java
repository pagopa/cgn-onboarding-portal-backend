package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.exception.CGNException;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.SearchApiResponseEyca;
import it.gov.pagopa.cgnonboardingportal.eycadataexport.model.SearchDataExportEyca;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class EycaSyncService {
    @Autowired
    EycaExportService eycaExportService;

    @Autowired
    DiscountRepository discountRepository;

    @org.springframework.transaction.annotation.Transactional
    public void syncEycaUpdateIdOnEyca(List<SearchDataExportEyca> exportEycaList, List<EycaDataExportViewEntity> exportViewEntities){

        if (exportEycaList.isEmpty()) {
            log.info("No discounts to search");
            return;
        }

        eycaExportService.authenticateOnEyca();

        log.info("Searching discounts on EYCA...");

        exportEycaList.forEach(exportEyca -> {

            log.info("SEARCH SearchDataExportEyca: " + exportEyca.toString());
            SearchApiResponseEyca response = null;
            try {
                response = eycaExportService.searchDiscount(exportEyca,"json");

                if (Objects.nonNull(response)){
                    log.info("Search Response:");
                    log.info(response.toString());
                }

                //
                if(response !=null &&
                        response.getApiResponse() != null &&
                        response.getApiResponse().getData() != null &&
                        response.getApiResponse().getData().getDiscounts() != null &&
                        ObjectUtils.isEmpty(response.getApiResponse().getData().getDiscounts().getData())) {

                    String eycaUpdateId = exportEyca.getId();
                    DiscountEntity entity = discountRepository.findByEycaUpdateId(eycaUpdateId)
                            .orElseThrow( () -> new CGNException("Discount with EycaUpdateId: "+eycaUpdateId+" from eyca not found on Discount table"));

                    EycaDataExportViewEntity viewItem = exportViewEntities.stream().filter(d -> d.getEycaUpdateId().equals(entity.getEycaUpdateId())).findFirst().get();
                    entity.setEycaUpdateId(null);
                    discountRepository.saveAndFlush(entity);
                    viewItem.setEycaUpdateId(null);
                }
            }
            catch (RestClientException rce) {
                log.info("SEARCH eycaApi.searchDiscount Exception: " + rce.getMessage());
            }
        });
    }

}
