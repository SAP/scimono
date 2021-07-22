package com.sap.scimono.entity.bulk;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.sap.scimono.api.API;
import com.sap.scimono.api.helper.ScimErrorResponseParser;
import com.sap.scimono.entity.ErrorResponse;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.exception.InternalScimonoException;
import com.sap.scimono.exception.InvalidInputException;
import com.sap.scimono.exception.SCIMException;
import com.sap.scimono.helper.Strings;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseOperation extends BulkOperation {
  private static final String LOCATION_FIELD = "location";
  private static final String STATUS_FIELD = "status";
  private static final String RESPONSE_FIELD = "response";

  private final String location;
  private final String status;
  private final ErrorResponse response;

  private String resourceId;
  private String resourceType;

  @JsonCreator
  private ResponseOperation(@JsonProperty(value = METHOD_FIELD, required = true) final String method,
                            @JsonProperty(value = BULK_ID_FIELD) final String bulkId,
                            @JsonProperty(value = LOCATION_FIELD) final String location,
                            @JsonProperty(VERSION_FIELD) final String version,
                            @JsonProperty(RESPONSE_FIELD) final ErrorResponse response,
                            @JsonProperty(value = STATUS_FIELD, required = true) final String status) {
    super(method, bulkId, version);
    this.status = status;
    this.response = response;
    this.location = validateAndNormalizeLocation(getMethod(), response, location);
    initResourceTypeAndId();
  }

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
    return error(reqOperation, scimError);
  }

  static Builder error(RequestOperation reqOperation, ErrorResponse scimError) {
    return new Builder().forRequestOperation(reqOperation).withError(scimError).withStatus(scimError.getStatus());
  }

  static Builder success(RequestOperation reqOperation) {
    String statusCode = String.valueOf(reqOperation.getMethod().getSuccessfulStatus().getStatusCode());
    return new Builder().forRequestOperation(reqOperation).withStatus(statusCode);
  }

  private void initResourceTypeAndId() {
    if (this.location == null) {
      return;
    }

    String[] pathSegments = location.split("/");
    this.resourceId = pathSegments[pathSegments.length - 1];
    String endpoint = pathSegments[pathSegments.length - 2];

    if (API.USERS.equalsIgnoreCase(endpoint)) {
      this.resourceType = User.RESOURCE_TYPE_USER;
    } else if (API.GROUPS.equalsIgnoreCase(endpoint)) {
      this.resourceType = Group.RESOURCE_TYPE_GROUP;
    }
  }

  private static String validateAndNormalizeLocation(RequestMethod requestMethod, ErrorResponse errorResponse, String location) {
    if (requestMethod != RequestMethod.POST && location == null) {
      throw new InvalidInputException("Expected resource location for input method: POST");
    }

    if (requestMethod == RequestMethod.POST && errorResponse != null) {
      return null;
    }

    String normalizedLocation;
    try {
      URL locationUrl = new URL(location);
      normalizedLocation = locationUrl.toURI().normalize().toASCIIString();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new InternalScimonoException("Unable to extract resource type and id from bulk operation's location");
    }

    if (location.endsWith("/")) {
      normalizedLocation = normalizedLocation.substring(0, normalizedLocation.length() - 1);
    }

    return normalizedLocation;
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