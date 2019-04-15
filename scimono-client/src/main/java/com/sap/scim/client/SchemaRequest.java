package com.sap.scim.client;

import com.sap.scim.entity.paging.PagedByIndexSearchResult;
import com.sap.scim.entity.schema.Schema;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import static com.sap.scim.api.API.SCHEMAS;

public class SchemaRequest {
  private SCIMResourceRequest<Schema> resourceRequestDelegate;

  public SchemaRequest(WebTarget targetSystem, SCIMRequest scimRequest) {
    this.resourceRequestDelegate = new SCIMResourceRequest<>(targetSystem.path(SCHEMAS), scimRequest, Schema.class);
  }

  public SCIMResponse<Schema> createSchema(Schema schema) {
    return resourceRequestDelegate.createResource(schema);
  }

  public SCIMResponse<Schema> readSingleSchema(String schemaUrn) {
    return resourceRequestDelegate.readSingleResource(schemaUrn);
  }

  public SCIMResponse<PagedByIndexSearchResult<Schema>> readAllSchemas() {
    return resourceRequestDelegate.readMultipleResourcesWithoutPaging(new GenericType<PagedByIndexSearchResult<Schema>>(){});
  }

  public SCIMResponse<Void> deleteSchema(String schemaUrn) {
    return resourceRequestDelegate.deleteResource(schemaUrn);
  }
}
