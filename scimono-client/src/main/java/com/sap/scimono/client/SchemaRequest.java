package com.sap.scimono.client;

import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.schema.Schema;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;

import static com.sap.scimono.api.API.SCHEMAS;

public class SchemaRequest {
  private final SCIMResourceRequest<Schema> resourceRequestDelegate;

  public SchemaRequest(WebTarget targetSystem, SCIMRequest scimRequest) {
    this.resourceRequestDelegate = new SCIMResourceRequest<>(targetSystem.path(SCHEMAS), scimRequest, Schema.class);
  }

  public SCIMResponse<Schema> createSchema(Schema schema) {
    return resourceRequestDelegate.createResource(schema);
  }

  public SCIMResponse<Schema> readSingleSchema(String schemaUrn) {
    return resourceRequestDelegate.readSingleResource(schemaUrn, RequestDetails.DEFAULT);
  }

  public SCIMResponse<PagedByIndexSearchResult<Schema>> readAllSchemas() {
    return resourceRequestDelegate.readMultipleResources(new GenericType<PagedByIndexSearchResult<Schema>>(){}, RequestDetails.DEFAULT);
  }

  public SCIMResponse<Void> deleteSchema(String schemaUrn) {
    return resourceRequestDelegate.deleteResource(schemaUrn);
  }
}
