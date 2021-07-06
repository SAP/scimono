
package com.sap.scimono.entity.bulk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkRequest {
  public static final String BULK_REQUEST_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:BulkRequest";
  public static final String BULK_RESPONSE_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:BulkResponse";
  
  private final Set<String> schemas;
  private final Integer failOnErrors;
  private final List<BulkOperation> operations = new ArrayList<>();

  @JsonCreator
  public BulkRequest(@JsonProperty(value = "schemas", required = true) final Set<String> schemas,
      @JsonProperty("failOnErrors") final Integer failOnErrors,
      @JsonProperty(value = "Operations", required = true) final List<BulkOperation> operations) {
    this.schemas = schemas;
    this.failOnErrors = failOnErrors;
    this.operations.addAll(operations);
  }

  public Set<String> getSchemas() {
    return schemas;
  }
  
  public Integer getFailOnErrors() {
    return failOnErrors;
  }
  
  public List<BulkOperation> getOperations() {
    return Collections.unmodifiableList(operations);
  }

  @Override
  public String toString() {
    return "BulkRequest [schemas=" + schemas + ", failOnErrors=" + failOnErrors + ", operations=" + operations + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(failOnErrors, operations, schemas);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BulkRequest other = (BulkRequest) obj;
    return Objects.equals(failOnErrors, other.failOnErrors) && Objects.equals(operations, other.operations) && Objects.equals(schemas, other.schemas);
  }

}
