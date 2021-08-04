
package com.sap.scimono.entity.bulk;

import static com.sap.scimono.entity.definition.ResourceConstants.SCHEMAS_FIELD;

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
import com.sap.scimono.entity.definition.ResourceConstants;

@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkBody<T extends BulkOperation> {
  public static final String BULK_REQUEST_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:BulkRequest";
  public static final String BULK_RESPONSE_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:BulkResponse";

  private static final String OPERATIONS_FIELD = "Operations";
  private static final String FAILS_ON_ERRORS_FIELD = "failOnErrors";


  private final Set<String> schemas;
  private final Integer failOnErrors;
  private final List<T> operations = new ArrayList<>();

  @JsonCreator
  private BulkBody(@JsonProperty(value = SCHEMAS_FIELD, required = true) final Set<String> schemas,
                  @JsonProperty(FAILS_ON_ERRORS_FIELD) final Integer failOnErrors,
                  @JsonProperty(value = OPERATIONS_FIELD, required = true) final List<T> operations) {
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

  @JsonProperty(OPERATIONS_FIELD)
  public List<T> getOperations() {
    return Collections.unmodifiableList(operations);
  }

  public static BulkBody<RequestOperation> forRequest(List<RequestOperation> operations) {
    return forRequest(null, operations);
  }

  public static BulkBody<RequestOperation> forRequest(final Integer failOnErrors, List<RequestOperation> operations) {
    return new BulkBody<>(Collections.singleton(BULK_REQUEST_SCHEMA), failOnErrors, operations);
  }

  public static BulkBody<ResponseOperation> forResponse(List<ResponseOperation> operations) {
    return new BulkBody<>(Collections.singleton(BULK_RESPONSE_SCHEMA), null, operations);
  }

  @Override
  public String toString() {
    return "BulkRequest [schemas=" + schemas + ", failOnErrors=" + failOnErrors + ", operations=" + operations + "]";
  }
}
