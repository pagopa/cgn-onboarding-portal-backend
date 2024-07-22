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

}
