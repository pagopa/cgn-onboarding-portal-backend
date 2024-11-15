package it.gov.pagopa.cgn.portal.model;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "notification")
@Data
public class NotificationEntity implements Serializable {

    @Id
    @NotNull
    @NotBlank
    @Size(max = 255)
    @Column(name = "notification_k", length = 255)
    private String key;

    @NotNull
    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Size(max = 255)
    @Column(name = "error_message", length = 255)
    private String errorMessage;

    public NotificationEntity() {
    }

    public NotificationEntity(@NotNull @NotBlank @Size(max = 255) String key) {
        this.key = key;
    }

}
