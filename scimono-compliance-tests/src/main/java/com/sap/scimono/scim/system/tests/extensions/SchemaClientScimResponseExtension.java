package com.sap.scimono.scim.system.tests.extensions;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.client.SchemaRequest;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.schema.Schema;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class SchemaClientScimResponseExtension implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {
  private static final Logger logger = LoggerFactory.getLogger(SchemaClientScimResponseExtension.class);

  private final Map<String, Schema> managedResources = new HashMap<>();
  private final SchemaRequest schemaRequest;
  private final SchemaFailSafeClient schemaFailSafeClient;

  public SchemaClientScimResponseExtension(SchemaRequest schemaRequest) {
    this.schemaRequest = schemaRequest;
    this.schemaFailSafeClient = new SchemaFailSafeClient(this);
  }

  public void after(ExtensionContext extensionContext) {
    logger.info("Deleting managed resources by test: {}", extensionContext.getDisplayName());
    clearManagedResources();
  }

  public void before(ExtensionContext extensionContext) {

  }

  public void clearManagedResources() {
    new HashSet<>(managedResources.keySet()).forEach(schemaFailSafeClient::delete);
    managedResources.clear();
  }

  public SchemaFailSafeClient getFailSafeClient() {
    return schemaFailSafeClient;
  }

  public SCIMResponse<Schema> createSchema(Schema schema) {
    SCIMResponse<Schema> createdSchemaResponse = schemaRequest.createSchema(schema);
    if (createdSchemaResponse.isSuccess()) {
      Schema createdSchema = createdSchemaResponse.get();
      managedResources.put(createdSchema.getId(), createdSchema);
    }
    return createdSchemaResponse;
  }

  public SCIMResponse<Schema> readSingleSchema(String schemaUrn) {
    return schemaRequest.readSingleSchema(schemaUrn);
  }

  public SCIMResponse<PagedByIndexSearchResult<Schema>> readAllSchemas() {
    return schemaRequest.readAllSchemas();
  }

  public SCIMResponse<Void> deleteSchema(String schemaUrn) {
    SCIMResponse<Void> scimResponse = schemaRequest.deleteSchema(schemaUrn);
    if(scimResponse.isSuccess()) {
      managedResources.remove(schemaUrn);
    }
    return scimResponse;
  }

  public static SchemaClientScimResponseExtension forClearingAfterAllExecutions(SchemaRequest schemaRequest) {
    return new SchemaClientScimResponseExtensionAroundAll(schemaRequest);
  }

  public static SchemaClientScimResponseExtension forClearingAfterEachExecutions(SchemaRequest schemaRequest) {
    return new SchemaClientScimResponseExtensionAroundEach(schemaRequest);
  }

  public static  class SchemaClientScimResponseExtensionAroundEach extends SchemaClientScimResponseExtension {
    private SchemaClientScimResponseExtensionAroundEach(SchemaRequest schemaRequest) {
      super(schemaRequest);
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
      // Not implemented
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
      // Not implemented
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
      super.before(extensionContext);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
      super.after(extensionContext);
    }
  }

  public static class SchemaClientScimResponseExtensionAroundAll extends SchemaClientScimResponseExtension {
    private SchemaClientScimResponseExtensionAroundAll(SchemaRequest schemaRequest) {
      super(schemaRequest);
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
      super.before(extensionContext);
    }
    @Override
    public void afterAll(ExtensionContext extensionContext) {
      super.after(extensionContext);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
      // Not implemented
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
      // Not implemented
    }
  }
}
