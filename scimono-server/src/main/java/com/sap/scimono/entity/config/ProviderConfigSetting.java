
package com.sap.scimono.entity.config;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a provider configuration setting - whether the particular functionality is supported.
 *
 * @author i061675
 *
 */
public class ProviderConfigSetting implements Serializable {

  private static final long serialVersionUID = -8703305394103673455L;

  @JsonProperty(required = true)
  private final boolean supported;

  @JsonCreator
  public ProviderConfigSetting(@JsonProperty(value = "supported", required = true) final boolean supported) {
    this.supported = supported;
  }

  public boolean isSupported() {
    return supported;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    return prime + Boolean.hashCode(supported);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ProviderConfigSetting)) {
      return false;
    }
    ProviderConfigSetting other = (ProviderConfigSetting) obj;
    return supported == other.supported;
  }

}
