package it.gov.pagopa.cgn.portal.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditOperationTypeEnum;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditSubjectTypeEnum;
import it.gov.pagopa.cgn.portal.util.PostgreSQLEnumType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "change_audit")
@Data
@TypeDef(name = "change_audit_subject_type_enum", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "change_audit_operation_type_enum", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class ChangeAuditEntity {

    @Id
    @Column(name = "change_audit_k")
    @SequenceGenerator(name = "change_audit_change_audit_k_seq",
                       sequenceName = "change_audit_change_audit_k_seq",
                       allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "change_audit_change_audit_k_seq")
    private Long id;

    @Column(name = "subject_id", nullable = false, length = 100)
    private String subjectId;

    @Column(name = "partner_full_name", nullable = false, length = 100)
    private String partnerFullName;

    @Column(name = "actor_ref", length = 200)
    private String actorRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false)
    @Type(type = "change_audit_subject_type_enum")
    private ChangeAuditSubjectTypeEnum subjectType;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    @Type(type = "change_audit_operation_type_enum")
    private ChangeAuditOperationTypeEnum operationType;

    @Column(name = "insert_time", nullable = false)
    private OffsetDateTime insertTime;

    @Type(type = "jsonb")
    @Column(name = "value", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> value;
}