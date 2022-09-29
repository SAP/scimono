package com.sap.scimono.entity.bulk;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.scimono.api.API;
import com.sap.scimono.api.helper.ScimErrorResponseParser;
import com.sap.scimono.entity.ErrorResponse;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.exception.InternalScimonoException;
import com.sap.scimono.exception.InvalidInputException;
import com.sap.scimono.exception.SCIMException;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseOperation extends BulkOperation {

  private static final long serialVersionUID = 2729999986283853529L;
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

    String validatedLocation = getValidatedLocation(getMethod(), response, location);
    this.location = getNormalizedLocation(getMethod(), response, validatedLocation);
    this.resourceId = extractResourceId(this.location);
    this.resourceType = extractResourceType(this.location);
    this.status = status;
    this.response = response;
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
    return new Builder().forRequestOperation(reqOperation).withError(scimError).withStatus(scimError.getStatus())
        .withLocation(reqOperation.getPath());
  }

  static Builder success(RequestOperation reqOperation) {
    String statusCode = String.valueOf(reqOperation.getMethod().getSuccessfulStatus().getStatusCode());
    return new Builder().forRequestOperation(reqOperation).withStatus(statusCode);
  }

  private String extractResourceId(String normalizedLocation) {
    if (normalizedLocation != null) {
      String[] pathSegments = normalizedLocation.split("/");
      return pathSegments[pathSegments.length - 1];
    }
    return null;
  }

  private String extractResourceType(String normalizedLocation) {
    if (normalizedLocation != null) {
      String[] pathSegments = normalizedLocation.split("/");
      String endpoint = pathSegments[pathSegments.length - 2];

      if (API.USERS.equalsIgnoreCase(endpoint)) {
        return User.RESOURCE_TYPE_USER;
      } else if (API.GROUPS.equalsIgnoreCase(endpoint)) {
        return Group.RESOURCE_TYPE_GROUP;
      }
    }
    return null;
  }
 
  private static String getValidatedLocation(RequestMethod requestMethod, ErrorResponse errorResponse, String initLocation) {
    boolean hasErrorResponse = errorResponse != null;
    boolean isPostMethod = requestMethod == RequestMethod.POST;
    String location = removeEndingSlash(initLocation);
    boolean isMissingLocation = location == null;
    boolean isLocationMissingFromNonPostMethod = !isPostMethod && isMissingLocation && !hasErrorResponse;
    boolean isPostMethodWithError = isPostMethod && hasErrorResponse;

    if (isLocationMissingFromNonPostMethod) {
      throw new InvalidInputException("Expected resource location for request method: " + requestMethod);
    }

    if (isPostMethodWithError || isMissingLocation) {
      return null;
    }
    return location;
  }

  private static String getNormalizedLocation(RequestMethod requestMethod, ErrorResponse errorResponse, String location) {
    boolean hasErrorResponse = errorResponse != null;

    try {
      URL locationUrl = new URL(location);
      return locationUrl.toURI().normalize().toASCIIString();
    } catch (MalformedURLException | URISyntaxException e) {

      if (!hasErrorResponse) {
        throw new InternalScimonoException(String.format(
            "The resource's location could not be normalized in order to extract the resource type and id. Location is: %s, method is: %s", location,
            requestMethod));
      }
    }
    return null;
  }

  private static String removeEndingSlash(String location) {
    if (location != null && location.endsWith("/")) {
      location = location.substring(0, location.length() - 1);
    }
    return location;
  }

  public static class Builder extends BulkOperation.Builder<ResponseOperation> {
    private String status;
    private ErrorResponse errorResponse;
    private String resourceId;
    private String resourceType;

    private String location;

    public Builder() {

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

    public Builder withError(ErrorResponse error) {
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

    public Builder setResourceType(String resourceType) {
      this.resourceType = resourceType;
      return this;
    }

    @Override
    public ResponseOperation build() {
      return new ResponseOperation(this);
    }

  }

}