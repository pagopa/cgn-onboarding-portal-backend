package it.gov.pagopa.cgn.portal.type;

import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@RunWith(SpringRunner.class)
public class ProductCategoryArrayTypeTest {

    @Test
    public void ProductCategoryArrayType_Methods_Ok() throws SQLException {
        var productCategoryArrayType = new ProductCategoryArrayType();

        var mockArray = Mockito.mock(Array.class);
        Mockito.when(mockArray.getArray()).thenReturn((Object[]) new String[]{ProductCategoryEnum.LEARNING.name()});

        var resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.getArray(Mockito.anyString())).thenReturn(mockArray);

        // assert that implemented methods returns the expected result
        Assertions.assertEquals(Types.ARRAY, productCategoryArrayType.sqlTypes()[0]);
        Assertions.assertFalse(productCategoryArrayType.isMutable());
        Assertions.assertEquals(ProductCategoryEnum.LEARNING, productCategoryArrayType.nullSafeGet(resultSet, new String[]{"any_name"}, null, null)[0]);

        // assert that not implemented methods throw a NotImplementedException
        Assertions.assertThrows(NotImplementedException.class, productCategoryArrayType::returnedClass);
        Assertions.assertThrows(NotImplementedException.class, () -> productCategoryArrayType.equals(null, null));
        Assertions.assertThrows(NotImplementedException.class, () -> productCategoryArrayType.hashCode(null));
        Assertions.assertThrows(NotImplementedException.class, () -> productCategoryArrayType.nullSafeSet(null, null, 0, null));
        Assertions.assertThrows(NotImplementedException.class, () -> productCategoryArrayType.deepCopy(null));
        Assertions.assertThrows(NotImplementedException.class, () -> productCategoryArrayType.disassemble(null));
        Assertions.assertThrows(NotImplementedException.class, () -> productCategoryArrayType.assemble(null, null));
        Assertions.assertThrows(NotImplementedException.class, () -> productCategoryArrayType.replace(null, null, null));
    }
}
