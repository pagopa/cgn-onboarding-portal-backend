package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.EycaDataExportViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EycaDataExportRepository
        extends JpaRepository<EycaDataExportViewEntity, Long>, JpaSpecificationExecutor<EycaDataExportViewEntity> {

}