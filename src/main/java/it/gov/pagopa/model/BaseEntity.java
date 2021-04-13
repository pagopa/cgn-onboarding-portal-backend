package it.gov.pagopa.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalTime;

@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "INSERT_DATE", nullable = false, updatable = false)
    protected LocalTime insertDate;

    @Column(name = "UPDATE_DATE")
    protected LocalTime updateDate;

    @PrePersist
    protected void onInsert() {
        insertDate = LocalTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalTime.now();
    }
}