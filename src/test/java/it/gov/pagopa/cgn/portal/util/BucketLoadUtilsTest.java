package it.gov.pagopa.cgn.portal.util;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import it.gov.pagopa.cgn.portal.service.BucketService;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class)
public class BucketLoadUtilsTest {

    @Mock
    private BucketService bucketService = Mockito.mock(BucketService.class);

    @Mock
    private EntityManager entityManager = Mockito.mock(EntityManager.class);

    @Test
    public void StoreCodesBucket_Ok() {
        BucketLoadUtils bucketLoadUtils = new BucketLoadUtils(bucketService, entityManager);
        bucketLoadUtils.storeCodesBucket(1L);
        verify(bucketService, times(1)).setRunningBucketLoad(anyLong());
        verify(bucketService, times(1)).performBucketLoad(anyLong());
    }

}
