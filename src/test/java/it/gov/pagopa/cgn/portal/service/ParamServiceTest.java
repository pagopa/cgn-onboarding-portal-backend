package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.enums.ParamGroupEnum;
import it.gov.pagopa.cgn.portal.model.ParamEntity;
import it.gov.pagopa.cgn.portal.repository.ParamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ParamServiceTest extends IntegrationAbstractTest {

    private final ParamService paramService;

    private final ParamRepository paramRepository;

    private final ParamGroupEnum group = ParamGroupEnum.SEND_DISCOUNTS_EYCA_JOB;

    @Autowired
    public ParamServiceTest(ParamService paramService, ParamRepository paramRepository) {
        this.paramService = paramService;
        this.paramRepository = paramRepository;
    }

    @BeforeEach
    void clean() {
        paramRepository.deleteAll();
    }

    @Test
    void createParamsWithInsertUpdateDate_Ok() {
        paramService.setParam(group, "eyca_job_key", "eyca_job_value");

        ParamEntity pe = paramService.getParamsList(group).getFirst();
        assertNotNull(pe.getInsertTime());
        assertNull(pe.getUpdateTime());

        paramService.setParam(group, "eyca_job_key", "eyca_job_value2");

        List<ParamEntity> list = paramService.getParamsList(group);
        assertEquals(1, list.size());
        assertEquals("eyca_job_value2", list.getFirst().getParamValue());
        assertNotNull(list.getFirst().getUpdateTime());
    }

    @Test
    void getParam_shouldReturnValue_whenParamExists() {
        paramService.setParam(group, "my.key", "myValue");

        String value = paramService.getParam(group, "my.key");

        assertEquals("myValue", value);
    }

    @Test
    void getParamsList_shouldReturnListOfParams() {
        paramService.setParam(group, "key1", "val1");
        paramService.setParam(group, "key2", "val2");

        List<ParamEntity> result = paramService.getParamsList(group);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getParamKey().equals("key1")));
        assertTrue(result.stream().anyMatch(p -> p.getParamKey().equals("key2")));
    }

    @Test
    void getParamsMap_shouldReturnMapOfKeyValue() {
        paramService.setParam(group, "key1", "val1");
        paramService.setParam(group, "key2", "val2");

        Map<String, String> map = paramService.getParamsMap(group);

        assertEquals(2, map.size());
        assertEquals("val1", map.get("key1"));
        assertEquals("val2", map.get("key2"));
    }
}
