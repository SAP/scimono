package com.sap.scimono.entity.bulk;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkOperation implements Serializable {
  private static final long serialVersionUID = 6477898200760795763L;

  public static final String VERSION_FIELD = "version";
  public static final String BULK_ID_FIELD = "bulkId";
  public static final String METHOD_FIELD = "method";

  private final RequestMethod method;
  private final String bulkId;
  private final String version;

  @JsonCreator
  public BulkOperation(@JsonProperty(value = METHOD_FIELD, required = true) final String method,
                       @JsonProperty(value = BULK_ID_FIELD) final String bulkId,
                       @JsonProperty(VERSION_FIELD) final String version) {
    this.method = RequestMethod.from(method);
    this.bulkId = bulkId;
    this.version = version;
  }

  BulkOperation(final Builder<? extends BulkOperation> builder) {
    this.method = builder.method;
    this.bulkId = builder.bulkId;
    this.version = builder.version;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public RequestMethod getMethod() {
    return method;
  }

  public String getBulkId() {
    return bulkId;
  }

  public String getVersion() {
    return version;
  }

  public abstract static class Builder<T extends BulkOperation> {
    private RequestMethod method;
    private String bulkId;
    private String version;

    public Builder() {

    }

    public Builder(BulkOperation operation) {
      this.method = operation.method;
      this.bulkId = operation.bulkId;
      this.version = operation.version;
    }

    public Builder<T> setMethod(final RequestMethod method) {
      this.method = method;
      return this;
    }

    public Builder<T> setBulkId(final String bulkId) {
      this.bulkId = bulkId;
      return this;
    }

    public Builder<T> setVersion(final String version) {
      this.version = version;
      return this;
    }

    public abstract T build();
  }
}
