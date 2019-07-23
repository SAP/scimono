package com.sap.scimono.scim.system.tests.extensions;

import com.sap.scimono.client.SchemaRequest;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.schema.Schema;

import java.util.List;

public class SchemaFailSafeClient implements ResourceFailSafeClient<Schema>{
  private final SchemaRequest schemaRequest;

  public SchemaFailSafeClient(SchemaRequest schemaRequest) {
    this.schemaRequest = schemaRequest;
  }

  @Override
  public Schema create(Schema schema) {
    return verifyAndGetResponse(schemaRequest.createSchema(schema));
  }

  @Override
  public Schema update(String schemaId, Schema resource) {
    // Not implemented
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(String schemaId) {
    verifyAndGetResponse(schemaRequest.deleteSchema(schemaId));
  }

  @Override
  public void patch(String schemaId, PatchBody patchBody) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Schema getSingle(String schemaId) {
    return verifyAndGetResponse(schemaRequest.readSingleSchema(schemaId));
  }

  @Override
  public List<Schema> getAllWithIdPaging() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Schema> getAllWithIndexPaging() {
    return verifyAndGetResponse(schemaRequest.readAllSchemas()).getResources();
  }

  @Override
  public List<Schema> getAllByFilter(String filterExpression) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PagedByIndexSearchResult<Schema> getPagedByIndex(int startIndex, int count) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PagedByIdentitySearchResult<Schema> getPagedById(String startId, int count) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PagedByIndexSearchResult<Schema> getByFilteredAndPagedByIndex(int startIndex, int count, String filter) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PagedByIdentitySearchResult<Schema> getByFilteredAndPagedById(String startId, int count, String filter) {
    throw new UnsupportedOperationException();
  }
}
