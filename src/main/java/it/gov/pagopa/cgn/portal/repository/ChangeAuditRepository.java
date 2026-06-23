package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.ChangeAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.Optional;

public interface ChangeAuditRepository
        extends JpaRepository<ChangeAuditEntity, Long> {

        @SuppressWarnings("java:S2143")
        @Query(value = """
                        WITH ordered_audits AS (
                                SELECT
                                        ca.change_audit_k,
                                        ca.insert_time,
                                        ca.value ->> 'state' AS audit_state,
                                        LAG(ca.value ->> 'state') OVER (ORDER BY ca.change_audit_k ASC) AS previous_audit_state
                                FROM change_audit ca
                                WHERE ca.subject_type = CAST('AGREEMENT' AS change_audit_subject_type_enum)
                                  AND ca.subject_id = :agreementId
                                  AND ca.value ->> 'state' IS NOT NULL
                        )
                        SELECT oa.insert_time
                        FROM ordered_audits oa
                        WHERE oa.audit_state = :currentState
                          AND oa.previous_audit_state IS DISTINCT FROM oa.audit_state
                          AND :currentState = (
                                SELECT audit_state
                                FROM ordered_audits
                                ORDER BY change_audit_k DESC
                                LIMIT 1
                          )
                        ORDER BY oa.change_audit_k DESC
                        LIMIT 1
                        """, nativeQuery = true)
        Optional<Timestamp> findAgreementStateSince(@Param("agreementId") String agreementId,
                                                                                                @Param("currentState") String currentState);
}