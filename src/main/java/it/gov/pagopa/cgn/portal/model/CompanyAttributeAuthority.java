package it.gov.pagopa.cgn.portal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.Objects;

/**
 * CompanyAttributeAuthority
 */
@JsonPropertyOrder({
  CompanyAttributeAuthority.JSON_PROPERTY_FISCAL_CODE,
  CompanyAttributeAuthority.JSON_PROPERTY_ORGANIZATION_NAME,
  CompanyAttributeAuthority.JSON_PROPERTY_PEC
})
@JsonTypeName("Company")
public class CompanyAttributeAuthority implements Serializable {
  private static final long serialVersionUID = 1L;

  public static final String JSON_PROPERTY_FISCAL_CODE = "fiscalCode";
  @javax.annotation.Nonnull
  private String fiscalCode;

  public static final String JSON_PROPERTY_ORGANIZATION_NAME = "organizationName";
  @javax.annotation.Nonnull
  private String organizationName;

  public static final String JSON_PROPERTY_PEC = "pec";
  @javax.annotation.Nonnull
  private String pec;

  public CompanyAttributeAuthority() {
  }

  public CompanyAttributeAuthority fiscalCode(@javax.annotation.Nonnull String fiscalCode) {
    
    this.fiscalCode = fiscalCode;
    return this;
  }

  /**
   * Get fiscalCode
   * @return fiscalCode
   */
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_FISCAL_CODE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getFiscalCode() {
    return fiscalCode;
  }


  @JsonProperty(JSON_PROPERTY_FISCAL_CODE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setFiscalCode(@javax.annotation.Nonnull String fiscalCode) {
    this.fiscalCode = fiscalCode;
  }

  public CompanyAttributeAuthority organizationName(@javax.annotation.Nonnull String organizationName) {
    
    this.organizationName = organizationName;
    return this;
  }

  /**
   * Get organizationName
   * @return organizationName
   */
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_ORGANIZATION_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getOrganizationName() {
    return organizationName;
  }


  @JsonProperty(JSON_PROPERTY_ORGANIZATION_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setOrganizationName(@javax.annotation.Nonnull String organizationName) {
    this.organizationName = organizationName;
  }

  public CompanyAttributeAuthority pec(@javax.annotation.Nonnull String pec) {
    
    this.pec = pec;
    return this;
  }

  /**
   * Get pec
   * @return pec
   */
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PEC)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getPec() {
    return pec;
  }


  @JsonProperty(JSON_PROPERTY_PEC)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPec(@javax.annotation.Nonnull String pec) {
    this.pec = pec;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CompanyAttributeAuthority company = (CompanyAttributeAuthority) o;
    return Objects.equals(this.fiscalCode, company.fiscalCode) &&
        Objects.equals(this.organizationName, company.organizationName) &&
        Objects.equals(this.pec, company.pec);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fiscalCode, organizationName, pec);
  }

  @Override
  public String toString() {
    return "CompanyAttributeAuthority{" +
        "fiscalCode='" + fiscalCode + '\'' +
        ", organizationName='" + organizationName + '\'' +
        ", pec='" + pec + '\'' +
        '}';
  }

}

