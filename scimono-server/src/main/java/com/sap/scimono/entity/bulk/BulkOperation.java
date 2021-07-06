package com.sap.scimono.entity.bulk;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.sap.scimono.helper.Strings;

@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkOperation implements Serializable {
  private static final String STATUS_FIELD = "status";

  private static final String RESPONSE_FIELD = "response";

  private static final String LOCATION_FIELD = "location";

  private static final String DATA_FIELD = "data";

  private static final String PATH_FIELD = "path";
  private static final String VERSION_FIELD = "version";

  private static final String BULK_ID_FIELD = "bulkId";

  private static final String METHOD_FIELD = "method";

  private static final long serialVersionUID = 6477898200760795763L;
  
  private final RequestMethod method;
  private final String bulkId;
  private final String path;
  private final String version;
  private final JsonNode data;
  private final String location;
  private final String response;
  private final String status;
  
  @JsonCreator
  public BulkOperation(@JsonProperty(METHOD_FIELD) final String method,
      @JsonProperty(BULK_ID_FIELD) final String bulkId,
      @JsonProperty(PATH_FIELD) final String path,
      @JsonProperty(VERSION_FIELD) final String version,
      @JsonProperty(DATA_FIELD) final JsonNode data) {
    this.method = RequestMethod.from(method);
    this.bulkId = bulkId;
    this.path = path;
    this.version = version;
    this.data = data;
    this.location= null;
    this.response = null;
    this.status = null;
  }
  
  private BulkOperation(final Builder builder) {
    this.method = builder.method;
    this.bulkId = builder.bulkId;
    this.path = builder.path;
    this.version = builder.version;
    this.data = builder.data;
    this.location = builder.location;
    this.response = builder.response;
    this.status = builder.status;
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

  public String getPath() {
    return path;
  }
  
  public String getVersion() {
    return version;
  }
  
  public JsonNode getData() {
    return data;
  }

  public String getLocation() {
    return location;
  }
  
  public String getResponse() {
    return response;
  }
  
  public String getStatus() {
    return status;
  }
  
  @Override
  public String toString() {
    Map<String, Object> valuesToDisplay = new LinkedHashMap<>();

    valuesToDisplay.put(METHOD_FIELD, method.toString());
    valuesToDisplay.put(BULK_ID_FIELD, bulkId);
    valuesToDisplay.put(PATH_FIELD, path);
    valuesToDisplay.put(DATA_FIELD, data.toString());
    valuesToDisplay.put(LOCATION_FIELD, location);
    valuesToDisplay.put(RESPONSE_FIELD, response);
    valuesToDisplay.put(STATUS_FIELD, status);

    return Strings.createPrettyEntityString(valuesToDisplay, this.getClass());
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(bulkId, data, location, method, path, response, status);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BulkOperation other = (BulkOperation) obj;
    return Objects.equals(bulkId, other.bulkId) && Objects.equals(data, other.data) && Objects.equals(location, other.location)
        && method == other.method && Objects.equals(path, other.path) && Objects.equals(response, other.response)
        && Objects.equals(status, other.status);
  }



  public static class Builder {
    private RequestMethod method;
    private String bulkId;
    private String path;
    private String version;
    private JsonNode data;
    private String location;
    private String response;
    private String status;
    
    public Builder setMethod(final RequestMethod method) {
      this.method = method;
      return this;
    }
    
    public Builder setBulkId(final String bulkId) {
      this.bulkId = bulkId;
      return this;
    }
    
    public Builder setPath(final String path) {
      this.path = path;
      return this;
    }
    
    public Builder setVersion(final String version) {
      this.version = version;
      return this;
    }
    
    public Builder setData(final JsonNode data) {
      this.data = data;
      return this;
    }
    
    public Builder setLocation(final String location) {
      this.location = location;
      return this;
    }
    
    public Builder setResponse(final String response) {
      this.response = response;
      return this;
    }
    
    public Builder setStatus(final String status) {
      this.status = status;
      return this;
    }
    
    public BulkOperation build() {
      return new BulkOperation(this);
    }
  }

  public static enum RequestMethod {
    POST, PUT, PATCH, DELETE;
    
    public static RequestMethod from(String methodName) {
      return Arrays.stream(values()).filter(method -> method.toString().equalsIgnoreCase(methodName)).findAny().orElse(null);
    }
  }
  
}
