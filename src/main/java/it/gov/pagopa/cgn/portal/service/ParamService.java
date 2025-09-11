package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.enums.ParamGroupEnum;
import it.gov.pagopa.cgn.portal.model.ParamEntity;
import it.gov.pagopa.cgn.portal.repository.ParamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ParamService {

    private final ParamRepository paramRepository;

    @Transactional
    public  String getParam(ParamGroupEnum paramGroup, String paramKey) {
        ParamEntity pe = paramRepository.findByParamGroupAndParamKey(paramGroup, paramKey).orElseThrow();
        return pe.getParamValue();
    }

    @Autowired
    public ParamService(ParamRepository paramRepository) {
        this.paramRepository = paramRepository;
    }

    @Transactional
    public List<ParamEntity> getParamsList(ParamGroupEnum paramGroup) {
        return paramRepository.findByParamGroup(paramGroup);
    }

    @Transactional
    public Map<String,String> getParamsMap(ParamGroupEnum paramGroup) {
        return paramRepository.findByParamGroup(paramGroup) .stream()
                              .collect(Collectors.toMap(
                                      ParamEntity::getParamKey,
                                      ParamEntity::getParamValue
                              ));
    }

    @Transactional
    public void setParam(ParamGroupEnum paramGroup, String paramKey, String paramValue) {

        Optional<ParamEntity> ope = paramRepository.findByParamGroupAndParamKey(paramGroup, paramKey);

        ope.ifPresentOrElse(pe -> {
            pe.setParamValue(paramValue);
            paramRepository.save(pe);

        },
        () -> {
            ParamEntity pe = new ParamEntity();

            pe.setParamGroup(paramGroup);
            pe.setParamKey(paramKey);
            pe.setParamValue(paramValue);

            paramRepository.save(pe);
        });
    }
}

