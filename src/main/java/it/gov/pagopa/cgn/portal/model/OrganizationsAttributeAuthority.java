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
 * OrganizationsAttributeAuthority
 */
@JsonPropertyOrder({
  OrganizationsAttributeAuthority.JSON_PROPERTY_ITEMS,
  OrganizationsAttributeAuthority.JSON_PROPERTY_COUNT
})
@JsonTypeName("Organizations")
public class OrganizationsAttributeAuthority implements Serializable {
  private static final long serialVersionUID = 1L;

  public static final String JSON_PROPERTY_ITEMS = "items";
  @javax.annotation.Nullable
  private List<OrganizationWithReferentsAttributeAuthority> items = new ArrayList<>();

  public static final String JSON_PROPERTY_COUNT = "count";
  @javax.annotation.Nullable
  private Integer count;

  public OrganizationsAttributeAuthority() {
  }

  public OrganizationsAttributeAuthority items(@javax.annotation.Nullable List<OrganizationWithReferentsAttributeAuthority> items) {
    
    this.items = items;
    return this;
  }

  public OrganizationsAttributeAuthority addItemsItem(OrganizationWithReferentsAttributeAuthority itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }
    this.items.add(itemsItem);
    return this;
  }

  /**
   * Get items
   * @return items
   */
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ITEMS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public List<OrganizationWithReferentsAttributeAuthority> getItems() {
    return items;
  }


  @JsonProperty(JSON_PROPERTY_ITEMS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setItems(@javax.annotation.Nullable List<OrganizationWithReferentsAttributeAuthority> items) {
    this.items = items;
  }

  public OrganizationsAttributeAuthority count(@javax.annotation.Nullable Integer count) {
    
    this.count = count;
    return this;
  }

  /**
   * Get count
   * @return count
   */
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_COUNT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public Integer getCount() {
    return count;
  }


  @JsonProperty(JSON_PROPERTY_COUNT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setCount(@javax.annotation.Nullable Integer count) {
    this.count = count;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrganizationsAttributeAuthority organizations = (OrganizationsAttributeAuthority) o;
    return Objects.equals(this.items, organizations.items) &&
        Objects.equals(this.count, organizations.count);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items, count);
  }

  @Override
  public String toString() {
    return "OrganizationsAttributeAuthority{" +
        "items=" + items +
        ", count=" + count +
        '}';
  }

}

