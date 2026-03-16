package it.gov.pagopa.cgn.portal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OrganizationWithReferentsAttributeAuthority
 */
@JsonPropertyOrder({
  OrganizationWithReferentsAttributeAuthority.JSON_PROPERTY_KEY_ORGANIZATION_FISCAL_CODE,
  OrganizationWithReferentsAttributeAuthority.JSON_PROPERTY_ORGANIZATION_FISCAL_CODE,
  OrganizationWithReferentsAttributeAuthority.JSON_PROPERTY_ORGANIZATION_NAME,
  OrganizationWithReferentsAttributeAuthority.JSON_PROPERTY_PEC,
  OrganizationWithReferentsAttributeAuthority.JSON_PROPERTY_REFERENTS,
  OrganizationWithReferentsAttributeAuthority.JSON_PROPERTY_INSERTED_AT
})
@JsonTypeName("OrganizationWithReferents")
public class OrganizationWithReferentsAttributeAuthority implements Serializable {
  private static final long serialVersionUID = 1L;

  public static final String JSON_PROPERTY_KEY_ORGANIZATION_FISCAL_CODE = "keyOrganizationFiscalCode";
  @javax.annotation.Nonnull
  private String keyOrganizationFiscalCode;

  public static final String JSON_PROPERTY_ORGANIZATION_FISCAL_CODE = "organizationFiscalCode";
  @javax.annotation.Nonnull
  private String organizationFiscalCode;

  public static final String JSON_PROPERTY_ORGANIZATION_NAME = "organizationName";
  @javax.annotation.Nonnull
  private String organizationName;

  public static final String JSON_PROPERTY_PEC = "pec";
  @javax.annotation.Nonnull
  private String pec;

  public static final String JSON_PROPERTY_REFERENTS = "referents";
  @javax.annotation.Nonnull
  private List<String> referents = new ArrayList<>();

  public static final String JSON_PROPERTY_INSERTED_AT = "insertedAt";
  @javax.annotation.Nonnull
  private String insertedAt;

  public OrganizationWithReferentsAttributeAuthority() {
  }

  public OrganizationWithReferentsAttributeAuthority keyOrganizationFiscalCode(@javax.annotation.Nonnull String keyOrganizationFiscalCode) {
    
    this.keyOrganizationFiscalCode = keyOrganizationFiscalCode;
    return this;
  }

  /**
   * Get keyOrganizationFiscalCode
   * @return keyOrganizationFiscalCode
   */
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_KEY_ORGANIZATION_FISCAL_CODE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getKeyOrganizationFiscalCode() {
    return keyOrganizationFiscalCode;
  }


  @JsonProperty(JSON_PROPERTY_KEY_ORGANIZATION_FISCAL_CODE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setKeyOrganizationFiscalCode(@javax.annotation.Nonnull String keyOrganizationFiscalCode) {
    this.keyOrganizationFiscalCode = keyOrganizationFiscalCode;
  }

  public OrganizationWithReferentsAttributeAuthority organizationFiscalCode(@javax.annotation.Nonnull String organizationFiscalCode) {
    
    this.organizationFiscalCode = organizationFiscalCode;
    return this;
  }

  /**
   * Get organizationFiscalCode
   * @return organizationFiscalCode
   */
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_ORGANIZATION_FISCAL_CODE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getOrganizationFiscalCode() {
    return organizationFiscalCode;
  }


  @JsonProperty(JSON_PROPERTY_ORGANIZATION_FISCAL_CODE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setOrganizationFiscalCode(@javax.annotation.Nonnull String organizationFiscalCode) {
    this.organizationFiscalCode = organizationFiscalCode;
  }

  public OrganizationWithReferentsAttributeAuthority organizationName(@javax.annotation.Nonnull String organizationName) {
    
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

  public OrganizationWithReferentsAttributeAuthority pec(@javax.annotation.Nonnull String pec) {
    
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

  public OrganizationWithReferentsAttributeAuthority referents(@javax.annotation.Nonnull List<String> referents) {
    
    this.referents = referents;
    return this;
  }

  public OrganizationWithReferentsAttributeAuthority addReferentsItem(String referentsItem) {
    if (this.referents == null) {
      this.referents = new ArrayList<>();
    }
    this.referents.add(referentsItem);
    return this;
  }

  /**
   * Get referents
   * @return referents
   */
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_REFERENTS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public List<String> getReferents() {
    return referents;
  }


  @JsonProperty(JSON_PROPERTY_REFERENTS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setReferents(@javax.annotation.Nonnull List<String> referents) {
    this.referents = referents;
  }

  public OrganizationWithReferentsAttributeAuthority insertedAt(@javax.annotation.Nonnull String insertedAt) {
    
    this.insertedAt = insertedAt;
    return this;
  }

  /**
   * A date-time field in ISO-8601 format and UTC timezone.
   * @return insertedAt
   */
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_INSERTED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getInsertedAt() {
    return insertedAt;
  }


  @JsonProperty(JSON_PROPERTY_INSERTED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setInsertedAt(@javax.annotation.Nonnull String insertedAt) {
    this.insertedAt = insertedAt;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrganizationWithReferentsAttributeAuthority organizationWithReferents = (OrganizationWithReferentsAttributeAuthority) o;
    return Objects.equals(this.keyOrganizationFiscalCode, organizationWithReferents.keyOrganizationFiscalCode) &&
        Objects.equals(this.organizationFiscalCode, organizationWithReferents.organizationFiscalCode) &&
        Objects.equals(this.organizationName, organizationWithReferents.organizationName) &&
        Objects.equals(this.pec, organizationWithReferents.pec) &&
        Objects.equals(this.referents, organizationWithReferents.referents) &&
        Objects.equals(this.insertedAt, organizationWithReferents.insertedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyOrganizationFiscalCode, organizationFiscalCode, organizationName, pec, referents, insertedAt);
  }

  @Override
  public String toString() {
    return "OrganizationWithReferentsAttributeAuthority{" +
        "keyOrganizationFiscalCode='" + keyOrganizationFiscalCode + '\'' +
        ", organizationFiscalCode='" + organizationFiscalCode + '\'' +
        ", organizationName='" + organizationName + '\'' +
        ", pec='" + pec + '\'' +
        ", referents=" + referents +
        ", insertedAt='" + insertedAt + '\'' +
        '}';
  }

}

