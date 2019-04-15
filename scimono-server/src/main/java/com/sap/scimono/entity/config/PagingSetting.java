
package com.sap.scimono.entity.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PagingSetting extends ProviderConfigSetting {

  private static final long serialVersionUID = -9218504230685359210L;

  @JsonProperty(required = true)
  private final int defaultStartIndex;
  @JsonProperty(required = true)
  private final int defaultCount;
  @JsonProperty(required = true)
  private final String startIdPagingParam;
  @JsonProperty(required = true)
  private final String nextIdPagingParam;

  @JsonCreator
  public PagingSetting(@JsonProperty(value = "supported", required = true) final boolean supported,
      @JsonProperty(value = "defaultStartIndex", required = true) final int defaultStartIndex,
      @JsonProperty(value = "defaultCount", required = true) final int defaultCount,
      @JsonProperty(value = "startIdPagingParam", required = true) final String startIdPagingParam,
      @JsonProperty(value = "nextIdPagingParam", required = true) final String nextIdPagingParam) {
    super(supported);
    this.defaultStartIndex = defaultStartIndex;
    this.defaultCount = defaultCount;
    this.startIdPagingParam = startIdPagingParam;
    this.nextIdPagingParam = nextIdPagingParam;
  }

  public int getDefaultStartIndex() {
    return defaultStartIndex;
  }

  public int getDefaultCount() {
    return defaultCount;
  }

  public String getStartIdPagingParam() {
    return startIdPagingParam;
  }

  public String getNextIdPagingParam() {
    return nextIdPagingParam;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + defaultCount;
    result = prime * result + defaultStartIndex;
    result = prime * result + ((nextIdPagingParam == null) ? 0 : nextIdPagingParam.hashCode());
    result = prime * result + ((startIdPagingParam == null) ? 0 : startIdPagingParam.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PagingSetting other = (PagingSetting) obj;
    if (defaultCount != other.defaultCount) {
      return false;
    }
    if (defaultStartIndex != other.defaultStartIndex) {
      return false;
    }
    if (nextIdPagingParam == null) {
      if (other.nextIdPagingParam != null) {
        return false;
      }
    } else if (!nextIdPagingParam.equals(other.nextIdPagingParam)) {
      return false;
    }
    if (startIdPagingParam == null) {
      return other.startIdPagingParam == null;
    } else return startIdPagingParam.equals(other.startIdPagingParam);
  }

}
