package it.gov.pagopa.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.io.Serializable;
import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    @Column(name = "insert_time", nullable = false, updatable = false)
    protected OffsetDateTime insertTime;

    @Column(name = "update_time")
    protected OffsetDateTime updateTime;

    @PrePersist
    protected void onInsert() {
        insertTime = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = OffsetDateTime.now();
    }
}