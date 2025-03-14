package it.gov.pagopa.cgn.portal.repository.custom;

import it.gov.pagopa.cgn.portal.model.DiscountBucketCodeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@Transactional
public class DiscountBucketCodeRepositoryCustomImpl
        implements DiscountBucketCodeRepositoryCustom {

    @Autowired
    private JdbcTemplate template;

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void bulkPersist(List<DiscountBucketCodeEntity> entities) {
        template.batchUpdate(
                "insert into discount_bucket_code (code, used, discount_fk, bucket_code_load_id) values (?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i)
                            throws SQLException {
                        DiscountBucketCodeEntity toInsert = entities.get(i);
                        ps.setString(1, toInsert.getCode());
                        ps.setBoolean(2, toInsert.getIsUsed());
                        ps.setLong(3, toInsert.getDiscount().getId());
                        ps.setLong(4, toInsert.getBucketCodeLoadId());
                    }

                    @Override
                    public int getBatchSize() {
                        return entities.size();
                    }
                });
    }

}
