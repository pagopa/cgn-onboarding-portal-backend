package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EycaSyncService {
    @Autowired
    EycaExportService eycaExportService;

    @Autowired
    DiscountRepository discountRepository;

}
