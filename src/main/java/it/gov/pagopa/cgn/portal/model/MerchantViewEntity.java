package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@Entity
@Immutable
@Table(name = "merchant")
public class MerchantViewEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "description")
    private String description;

    @Column(name = "description_en")
    private String descriptionEn;

    @Column(name = "searchable_name")
    private String searchableName;

    @Column(name = "searchable_name_en")
    private String searchableNameEn;

    @Column(name = "searchable_description")
    private String searchableDescription;

    @Column(name = "searchable_description_en")
    private String searchableDescriptionEn;


}
