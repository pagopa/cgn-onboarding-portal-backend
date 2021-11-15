package it.gov.pagopa.cgn.portal.repository.custom;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import it.gov.pagopa.cgn.portal.model.DiscountBucketCode;

@Repository
@Transactional
public class DiscountBucketCodeRepositoryCustomImpl implements DiscountBucketCodeRepositoryCustom {

    @Autowired
    private JdbcTemplate template;

    @Override
    public void bulkPersist(List<DiscountBucketCode> entities) {
        template.batchUpdate("insert into discount_bucket_code (code, used, discount_fk) values (?, ?, ?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        DiscountBucketCode toInsert = entities.get(i);
                        ps.setLong(1, toInsert.getId());
                        ps.setBoolean(2, toInsert.getIsUsed());
                        ps.setLong(3, toInsert.getDiscount().getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return entities.size();
                    }
                });
    }

}
