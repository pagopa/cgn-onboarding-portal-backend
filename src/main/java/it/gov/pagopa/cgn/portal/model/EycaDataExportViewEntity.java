package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Immutable
@Table(name = "eyca_data_export")
public class EycaDataExportViewEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "categories")
    private String categories;

    @Column(name = "vendor")
    private String vendor;

    @Column(name = "name")
    private String name;

    @Column(name = "name_local")
    private String nameLocal;

    @Column(name = "text")
    private String text;

    @Column(name = "text_local")
    private String textLocal;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "web")
    private String web;

    @Column(name = "tags")
    private String tags;

    @Column(name = "image")
    private String image;

    @Column(name = "live")
    private String live;

    @Column(name = "location_local_id")
    private String locationLocalId;

    @Column(name = "street")
    private String street;

    @Column(name = "city")
    private String city;

    @Column(name = "zip")
    private String zip;

    @Column(name = "country")
    private String country;

    @Column(name = "region")
    private String region;

    @Column(name = "latitude")
    private String latitude;

    @Column(name = "longitude")
    private String longitude;
    
    @Column(name = "discount_type")
    private String discountType;

}

