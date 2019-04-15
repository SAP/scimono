
package com.sap.scim.entity.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BulkSetting extends ProviderConfigSetting {

  private static final long serialVersionUID = -2584590832421123369L;

  @JsonProperty(required = true)
  private final int maxOperations; // NOPMD by I061675
  @JsonProperty(required = true)
  private final int maxPayloadSize; // NOPMD by I061675

  public BulkSetting(@JsonProperty(value = "supported", required = true) final boolean supported,
      @JsonProperty(value = "maxOperations", required = true) final int maxOperations,
      @JsonProperty(value = "maxPayloadSize", required = true) final int maxPayloadSize) {
    super(supported);
    this.maxOperations = maxOperations;
    this.maxPayloadSize = maxPayloadSize;
  }

  public int getMaxOperations() {
    return maxOperations;
  }

  public int getMaxPayloadSize() {
    return maxPayloadSize;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + maxOperations;
    result = prime * result + maxPayloadSize;
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
    if (!(obj instanceof BulkSetting)) {
      return false;
    }
    BulkSetting other = (BulkSetting) obj;
    if (maxOperations != other.maxOperations) {
      return false;
    }
    return maxPayloadSize == other.maxPayloadSize;
  }
}
