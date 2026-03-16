package it.gov.pagopa.cgn.portal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.Objects;

/**
 * ReferentFiscalCodeAttributeAuthority
 */
@JsonPropertyOrder({
  ReferentFiscalCodeAttributeAuthority.JSON_PROPERTY_REFERENT_FISCAL_CODE
})
@JsonTypeName("ReferentFiscalCode")
public class ReferentFiscalCodeAttributeAuthority implements Serializable {
  private static final long serialVersionUID = 1L;

  public static final String JSON_PROPERTY_REFERENT_FISCAL_CODE = "referentFiscalCode";
  @javax.annotation.Nonnull
  private String referentFiscalCode;

  public ReferentFiscalCodeAttributeAuthority() {
  }

  public ReferentFiscalCodeAttributeAuthority referentFiscalCode(@javax.annotation.Nonnull String referentFiscalCode) {
    
    this.referentFiscalCode = referentFiscalCode;
    return this;
  }

  /**
   * User&#39;s fiscal code.
   * @return referentFiscalCode
   */
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_REFERENT_FISCAL_CODE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getReferentFiscalCode() {
    return referentFiscalCode;
  }


  @JsonProperty(JSON_PROPERTY_REFERENT_FISCAL_CODE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setReferentFiscalCode(@javax.annotation.Nonnull String referentFiscalCode) {
    this.referentFiscalCode = referentFiscalCode;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReferentFiscalCodeAttributeAuthority referentFiscalCode = (ReferentFiscalCodeAttributeAuthority) o;
    return Objects.equals(this.referentFiscalCode, referentFiscalCode.referentFiscalCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(referentFiscalCode);
  }

  @Override
  public String toString() {
    return "ReferentFiscalCodeAttributeAuthority{" +
        "referentFiscalCode='" + referentFiscalCode + '\'' +
        '}';
  }

}

