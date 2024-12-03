package it.gov.pagopa.cgn.portal.type;

import it.gov.pagopa.cgn.portal.enums.ProductCategoryEnum;
import org.apache.commons.lang3.NotImplementedException;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.*;
import java.util.stream.Stream;


public class ProductCategoryArrayType
        implements UserType {

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.ARRAY};
    }

    @Override
    public Class returnedClass() {
        throw new NotImplementedException();
    }

    @Override
    public boolean equals(Object o, Object o1)
            throws HibernateException {
        throw new NotImplementedException();
    }

    @Override
    public int hashCode(Object o)
            throws HibernateException {
        throw new NotImplementedException();
    }

    @Override
    public ProductCategoryEnum[] nullSafeGet(ResultSet rs,
                                             String[] names,
                                             SharedSessionContractImplementor session,
                                             Object owner)
            throws HibernateException, SQLException {
        Array productCategoriesResultSet = rs.getArray(names[0]);
        if (productCategoriesResultSet==null) return new ProductCategoryEnum[]{};
        String[] productCategories = (String[]) productCategoriesResultSet.getArray();
        return Stream.of(productCategories).map(ProductCategoryEnum::valueOf).toArray(ProductCategoryEnum[]::new);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
            throws HibernateException, SQLException {
        throw new NotImplementedException();
    }

    @Override
    public Object deepCopy(Object o)
            throws HibernateException {
        throw new NotImplementedException();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object o)
            throws HibernateException {
        throw new NotImplementedException();
    }

    @Override
    public Object assemble(Serializable serializable, Object o)
            throws HibernateException {
        throw new NotImplementedException();
    }

    @Override
    public Object replace(Object o, Object o1, Object o2)
            throws HibernateException {
        throw new NotImplementedException();
    }

}
