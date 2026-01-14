package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import javax.validation.constraints.Max;
import java.time.LocalDate;

@ToString
@Data
@Entity
@Immutable
@Table(name = "eyca_data_export")
public class EycaDataExportViewEntity {

    @Column(name = "discount_id")
    private Long discountId;

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "state")
    private String state;

    @Column(name = "categories")
    private String categories;

    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "vendor")
    private String vendor;

    @Column(name = "eyca_update_id")
    @Max(24)
    private String eycaUpdateId;

    @Column(name = "name")
    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

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

    @Column(name = "sales_channel")
    private String salesChannel;

    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "static_code")
    private String staticCode;

    @Column(name = "landing_page_url")
    private String landingPageUrl;

    @Column(name = "landing_page_referrer")
    private String landingPageReferrer;

    @Column(name = "referent")
    private Long referent;

    @Column(name = "eyca_landing_page_url")
    private String eycaLandingPageUrl;

    @Column(name = "eyca_email_update_required")
    private Boolean eycaEmailUpdateRequired;

    @Transient
    private String ccdbId;
}
