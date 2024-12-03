package it.gov.pagopa.cgn.portal.model;

import lombok.Data;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import java.time.LocalDate;

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

    @Column(name = "landing_page_referrer")
    private String landingPageReferrer;

    @Column(name = "referent")
    private Long referent;

    @Override
    public String toString() {
        return "EycaDataExportViewEntity{" + "discountId=" + discountId + ", id=" + id + ", state='" + state + '\'' +
               ", categories='" + categories + '\'' + ", profileId=" + profileId + ", vendor='" + vendor + '\'' +
               ", eycaUpdateId='" + eycaUpdateId + '\'' + ", name='" + name + '\'' + ", startDate=" + startDate +
               ", endDate=" + endDate + ", nameLocal='" + nameLocal + '\'' + ", text='" + text + '\'' +
               ", textLocal='" + textLocal + '\'' + ", email='" + email + '\'' + ", phone='" + phone + '\'' +
               ", web='" + web + '\'' + ", tags='" + tags + '\'' + ", image='" + image + '\'' + ", live='" + live +
               '\'' + ", locationLocalId='" + locationLocalId + '\'' + ", street='" + street + '\'' + ", city='" +
               city + '\'' + ", zip='" + zip + '\'' + ", country='" + country + '\'' + ", region='" + region + '\'' +
               ", latitude='" + latitude + '\'' + ", longitude='" + longitude + '\'' + ", salesChannel='" +
               salesChannel + '\'' + ", discountType='" + discountType + '\'' + ", landingPageReferrer='" +
               landingPageReferrer + '\'' + ", referent=" + referent + '}';
    }
}

