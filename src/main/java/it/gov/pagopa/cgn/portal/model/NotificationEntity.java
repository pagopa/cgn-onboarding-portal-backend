package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(name = "notification")
@Data
public class NotificationEntity implements Serializable {

    @Id
    @Column(name = "notification_k")
    @SequenceGenerator(name = "notification_k_seq", sequenceName = "notification_k_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_k_seq")
    @Exclude
    @ToString.Exclude
    private Long id;

    @NotNull
    @NotBlank
    @Size(max = 255)
    @Column(name = "key", length = 255)
    private String key;

    @Size(max = 255)
    @Column(name = "error_message", length = 255)
    private String errorMessage;

    public NotificationEntity() {
    }

    public NotificationEntity(@NotNull @NotBlank @Size(max = 128) String key) {
        this.key = key;
    }

}
