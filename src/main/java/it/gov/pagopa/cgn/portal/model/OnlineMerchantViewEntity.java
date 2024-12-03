package it.gov.pagopa.cgn.portal.model;

import it.gov.pagopa.cgn.portal.enums.DiscountCodeTypeEnum;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Entity
@Immutable
@Table(name = "online_merchant")
@Data
public class OnlineMerchantViewEntity
        extends BaseMerchantViewEntity {

    @Column(name = "website_url")
    private String websiteUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_code_type")
    private DiscountCodeTypeEnum discountCodeType;

}
