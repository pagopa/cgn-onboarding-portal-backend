package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.ParamGroupEnum;
import it.gov.pagopa.cgn.portal.util.PostgreSQLEnumType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import javax.validation.constraints.NotNull;


@Entity
@Table(name = "param")
@Data
@EqualsAndHashCode(callSuper = true)
@TypeDef(name = "param_group_enum", typeClass = PostgreSQLEnumType.class)
public class ParamEntity
        extends BaseEntity {

    @Id
    @Column(name = "param_k")
    @SequenceGenerator(name = "param_k_seq",
                       sequenceName = "param_k_seq",
                       allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "param_k_seq")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "param_group", length = 40)
    @Type(type = "param_group_enum")
    @NotNull
    private ParamGroupEnum paramGroup;

    @Column(name = "param_key", length = 100)
    @NotNull
    private String paramKey;

    @Column(name = "param_value")
    @NotNull
    private String paramValue;
}

