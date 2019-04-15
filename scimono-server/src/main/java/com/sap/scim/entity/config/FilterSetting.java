
package com.sap.scim.entity.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FilterSetting extends ProviderConfigSetting {

  private static final long serialVersionUID = -9218504230685359210L;

  @JsonProperty(required = true)
  private final int maxResults;

  public FilterSetting(@JsonProperty(value = "supported", required = true) final boolean supported,
      @JsonProperty(value = "maxResults", required = true) final int maxResults) {
    super(supported);
    this.maxResults = maxResults;
  }

  public int getMaxResults() {
    return maxResults;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + maxResults;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof FilterSetting)) {
      return false;
    }
    FilterSetting other = (FilterSetting) obj;
    return maxResults == other.maxResults;
  }

}
