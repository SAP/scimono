
package com.sap.scimono.callback.schemas;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.Schema;

import java.util.List;

public class DefaultSchemasCallback implements SchemasCallback {


  @Override
  public Schema getCustomSchema(String schemaId) {
    throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
  }

  @Override
  public void createCustomSchema(Schema schema) {
    throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
  }

  @Override
  public List<Schema> getCustomSchemas() {
    throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
  }

  @Override
  public void deleteCustomSchema(String schemaId) {
    throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
  }

  @Override
  public boolean isValidSchemaName(String schemaName) {
    throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
  }

  @Override
  public Attribute getAttribute(String path) {
    throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
  }

}
