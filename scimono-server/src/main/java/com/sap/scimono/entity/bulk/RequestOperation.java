package com.sap.scimono.entity.bulk;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.sap.scimono.api.API;
import com.sap.scimono.entity.ErrorResponse;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.exception.InternalScimonoException;
import com.sap.scimono.exception.SCIMException;
import com.sap.scimono.helper.Strings;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestOperation extends BulkOperation {
  private static final String DATA_FIELD = "data";
  private static final String PATH_FIELD = "path";

  private static final long serialVersionUID = 6477898200760795763L;

  private final String path;
  private final JsonNode rawData;

  private Object data;

  @JsonCreator
  private RequestOperation(@JsonProperty(value = METHOD_FIELD, required = true) final String method,
                       @JsonProperty(value = BULK_ID_FIELD) final String bulkId,
                       @JsonProperty(value = PATH_FIELD, required = true) final String path,
                       @JsonProperty(VERSION_FIELD) final String version,
                       @JsonProperty(DATA_FIELD) final JsonNode data) {
    super(method, bulkId, version);
    this.path = path;
    this.rawData = data;
  }

  private RequestOperation(final Builder builder) {
    super(builder);
    this.path = builder.path;
    this.data = builder.data;
    this.rawData = builder.rawData;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public String getPath() {
    return path;
  }

  public Object getData() {
    return data != null ? data : rawData;
  }



  @JsonIgnore
  public boolean isDataAvailable() {
    return data != null || rawData != null;
  }

  @JsonIgnore
  public Optional<String> getResourceId() {
    if (RequestMethod.POST == getMethod() && data == null) {
      return Optional.empty();
    }

    if (RequestMethod.POST == getMethod() && data instanceof User) {
      return Optional.of(getDataAsUser().getId());
    }

    if (RequestMethod.POST == getMethod() && data instanceof Group) {
      return Optional.of(getDataAsGroup().getId());
    }

    String normalizedPath = path;
    if (path.endsWith("/")) {
      normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
    }

    int lastDelimiterIndex = normalizedPath.lastIndexOf('/');
    return lastDelimiterIndex == -1 ? Optional.empty() : Optional.of(normalizedPath.substring(lastDelimiterIndex + 1));
  }

  @JsonIgnore
  public PatchBody getDataAsPatch() {
    if (data instanceof PatchBody) {
      return (PatchBody) data;
    }

    throw new BulkOperationCastingException(super.getBulkId(), PatchBody.class);
  }

  @JsonIgnore
  public User getDataAsUser() {
    if (data instanceof User) {
      return (User) data;
    }

    throw new BulkOperationCastingException(super.getBulkId(), User.class);
  }

  @JsonIgnore
  public Group getDataAsGroup() {
    if (data instanceof Group) {
      return (Group) data;
    }

    throw new BulkOperationCastingException(super.getBulkId(), Group.class);
  }

  @JsonIgnore
  public ErrorResponse getDataAsScimError() {
    if (data instanceof ErrorResponse) {
      return (ErrorResponse) data;
    }

    throw new BulkOperationCastingException(super.getBulkId(), ErrorResponse.class);
  }

  @JsonIgnore
  public JsonNode getRawData() {
    return rawData;
  }

  @JsonIgnore
  public boolean hasValidationError() {
    if (data == null) {
      return false;
    }

    return data instanceof ErrorResponse;
  }

  @Override
  public String toString() {
    Map<String, Object> valuesToDisplay = new LinkedHashMap<>();

    valuesToDisplay.put(METHOD_FIELD, super.getMethod().toString());
    valuesToDisplay.put(VERSION_FIELD, super.getMethod().toString());
    valuesToDisplay.put(BULK_ID_FIELD, super.getBulkId());
    valuesToDisplay.put(PATH_FIELD, path);
    valuesToDisplay.put(DATA_FIELD, data.toString());

    return Strings.createPrettyEntityString(valuesToDisplay, this.getClass());
  }

  @JsonIgnore
  public String getResourceType() {
    
    if (hasValidationError()) {
      return null;
    }
    
    String extractedEndpoint = extractRootFromPath(path);
    if (extractedEndpoint.equalsIgnoreCase(API.USERS)) {
      return User.RESOURCE_TYPE_USER;
    }

    if (extractedEndpoint.equalsIgnoreCase(API.GROUPS)) {
      return Group.RESOURCE_TYPE_GROUP;
    }

    throw new InternalScimonoException(String.format("Invalid path for bulk operation with bulkId: %s", super.getBulkId()));
  }

  public Builder builder() {
    return new Builder(this);
  }

  public static String extractRootFromPath(String path) {
    String normalizedPath = Strings.stripStart(path, "/");
    int firstPathDelimiterIndex = normalizedPath.indexOf("/");
    return firstPathDelimiterIndex == -1 ? normalizedPath : normalizedPath.substring(0, firstPathDelimiterIndex);
  }

  public ResponseOperation.Builder errorResponseFromExistingValidationError() {
    if (!hasValidationError()) {
      throw new InternalScimonoException("Validation error is not present");
    }

    return ResponseOperation.error(this, getDataAsScimError());
  }
  public ResponseOperation.Builder errorResponse(SCIMException scimException) {
    return ResponseOperation.error(this, scimException);
  }

  public ResponseOperation.Builder successfulResponse() {
    return ResponseOperation.success(this);
  }

  public static class Builder extends BulkOperation.Builder<RequestOperation> {
    private String path;
    private Object data;
    private SCIMException validationError;
    private JsonNode rawData;

    public Builder() {

    }

    public Builder(RequestOperation operation) {
      super(operation);
      this.path = operation.path;
      this.data = operation.data;
      this.rawData = operation.rawData;
    }

    public Builder setMethod(final RequestMethod method) {
      super.setMethod(method);
      return this;
    }

    public Builder setBulkId(final String bulkId) {
      super.setBulkId(bulkId);
      return this;
    }

    public Builder setVersion(final String version) {
      super.setVersion(version);
      return this;
    }

    public Builder setPath(final String path) {
      this.path = path;
      return this;
    }

    public Builder setData(final Object data) {
      this.data = data;
      return this;
    }

    public RequestOperation build() {
      return new RequestOperation(this);
    }
  }
}