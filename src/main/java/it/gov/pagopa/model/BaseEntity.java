package it.gov.pagopa.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.io.Serializable;
import java.time.LocalTime;

@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    @Column(name = "INSERT_TIME", nullable = false, updatable = false)
    protected LocalTime insertTime;

    @Column(name = "UPDATE_TIME")
    protected LocalTime updateTime;

    @PrePersist
    protected void onInsert() {
        insertTime = LocalTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalTime.now();
    }
}