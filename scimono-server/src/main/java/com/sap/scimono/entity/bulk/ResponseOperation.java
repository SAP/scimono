package com.sap.scimono.entity.bulk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.scimono.api.helper.ScimErrorResponseParser;
import com.sap.scimono.entity.ErrorResponse;
import com.sap.scimono.exception.SCIMException;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseOperation extends BulkOperation {
  private final String location;
  private final String status;
  private final ErrorResponse response;

  private final String resourceId;
  private final String resourceType;

  private ResponseOperation(Builder builder) {
    super(builder);
    this.location = builder.location;
    this.status = builder.status;
    this.response = builder.errorResponse;
    this.resourceType = builder.resourceType;
    this.resourceId = builder.resourceId;
  }

  public String getLocation() {
    return location;
  }

  public String getStatus() {
    return status;
  }

  public ErrorResponse getResponse() {
    return response;
  }

  @JsonIgnore
  public String getResourceId() {
    return resourceId;
  }

  @JsonIgnore
  public String getResourceType() {
    return resourceType;
  }

  @JsonIgnore
  public boolean isSuccessful() {
    return response == null;
  }

  @JsonIgnore
  public Builder builder() {
    return new Builder(this);
  }

  static Builder error(RequestOperation reqOperation, SCIMException scimException) {
    ErrorResponse scimError = ScimErrorResponseParser.parseException(scimException);
    return new Builder().forRequestOperation(reqOperation).withError(scimError).withStatus(scimError.getStatus());
  }

  static Builder success(RequestOperation reqOperation) {
    String statusCode = String.valueOf(reqOperation.getMethod().getSuccessfulStatus().getStatusCode());
    return new Builder().forRequestOperation(reqOperation).withStatus(statusCode);
  }

  public static class Builder extends BulkOperation.Builder<ResponseOperation> {
    private String status;
    private ErrorResponse errorResponse;
    private String resourceId;
    private String resourceType;

    private String location;

    private Builder() {

    }
    private Builder(ResponseOperation operation) {
      super(operation);
      this.location = operation.location;
      this.errorResponse = operation.response;
      this.resourceId = operation.resourceId;
      this.resourceType = operation.resourceType;
      this.status = operation.getStatus();
    }

    private Builder forRequestOperation(RequestOperation requestOperation) {
      super.setBulkId(requestOperation.getBulkId()).setMethod(requestOperation.getMethod());
      this.resourceType = requestOperation.getResourceType();
      this.resourceId = requestOperation.getResourceId().orElse(null);


      return this;
    }

    private Builder withError(ErrorResponse error) {
      this.errorResponse = error;
      return this;
    }

    private Builder withStatus(String status) {
      this.status = status;
      return this;
    }

    public Builder withVersion(String version) {
      super.setVersion(version);
      return this;
    }

    public Builder withLocation(String location) {
      this.location = location;
      return this;
    }

    @Override
    public ResponseOperation build() {
      return new ResponseOperation(this);
    }
  }

}