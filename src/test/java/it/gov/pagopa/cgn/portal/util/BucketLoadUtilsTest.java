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

@RunWith(SpringRunner.class)
public class BucketLoadUtilsTest {

    @Mock
    private BucketService bucketService = Mockito.mock(BucketService.class);

    @Test
    public void StoreCodesBucket_Ok() {
        BucketLoadUtils bucketLoadUtils = new BucketLoadUtils(bucketService);
        bucketLoadUtils.storeCodesBucket(1L);
        verify(bucketService, times(1)).setRunningBucketLoad(anyLong());
        verify(bucketService, times(1)).performBucketLoad(anyLong());
    }

}
