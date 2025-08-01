package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.enums.ParamGroupEnum;
import it.gov.pagopa.cgn.portal.model.ParamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParamRepository
        extends JpaRepository<ParamEntity, Integer> {

    List<ParamEntity> findByParamGroup (ParamGroupEnum paramGroup);

    Optional<ParamEntity> findByParamGroupAndParamKey (ParamGroupEnum paramGroup, String paramKey);

}