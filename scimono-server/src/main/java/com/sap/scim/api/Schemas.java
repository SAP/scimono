
package com.sap.scim.api;

import static com.sap.scim.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scim.helper.Resources.addLocation;
import static com.sap.scim.helper.Strings.stripStart;

import java.util.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.sap.scim.entity.schema.validation.ValidSchemaId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.scim.SCIMApplication;
import com.sap.scim.callback.config.SCIMConfigurationCallback;
import com.sap.scim.callback.schemas.SchemasCallback;
import com.sap.scim.entity.Meta;
import com.sap.scim.entity.paging.PagedByIndexSearchResult;
import com.sap.scim.entity.schema.Schema;
import com.sap.scim.entity.schema.validation.ValidSchema;
import com.sap.scim.exception.InvalidInputException;
import com.sap.scim.exception.ResourceNotFoundException;

@Path(API.SCHEMAS)
@Produces(APPLICATION_JSON_SCIM)
@Consumes(APPLICATION_JSON_SCIM)
public class Schemas {
  private static final Logger logger = LoggerFactory.getLogger(Schemas.class);

  public static final int PREDEFINED_SCHEMAS_COUNT = 6;
  private static final int CUSTOM_SCHEMAS_MAX_COUNT = 20;

  @Context
  private UriInfo uriInfo;

  private final SchemasCallback schemaAPI;
  private final SCIMConfigurationCallback scimConfig;

  public Schemas(@Context Application appContext) {
    SCIMApplication scimApplication = SCIMApplication.from(appContext);

    schemaAPI = scimApplication.getSchemasCallback();
    scimConfig = scimApplication.getConfigurationCallback();
  }

  @GET
  public Response getSchemas() {
    logger.trace("Reading all schemas");
    List<Schema> schemas = schemaAPI.getSchemas();

    List<Schema> schemasWithLocation = new ArrayList<>();
    for (Schema schema : schemas) {
      schema = addLocation(schema, uriInfo.getAbsolutePathBuilder().path(schema.getId()));
      schemasWithLocation.add(schema);
    }

    return Response.ok(new PagedByIndexSearchResult<>(schemasWithLocation, schemasWithLocation.size(), scimConfig.getMaxResourcesPerPage(), 1L)).build();
  }

  @GET
  @Path("{id}")
  public Response getSchema(@PathParam("id") @ValidSchemaId final String schemaId) {
    logger.trace("Reading schema {}", schemaId);

    Schema schema =  schemaAPI.getSchema(schemaId);

    if (schema != null) {
      Schema schemaWithLocation = addLocation(schema, uriInfo.getAbsolutePath());

      return Response.ok(schemaWithLocation).tag(schemaWithLocation.getMeta().getVersion()).location(uriInfo.getAbsolutePath()).build();
    }

    throw new ResourceNotFoundException(Schema.RESOURCE_TYPE_SCHEMA, schemaId);
  }

  @POST
  public Response createSchema(@ValidSchema final Schema newSchema) {
    validateSchema(newSchema);

    UriBuilder location = uriInfo.getAbsolutePathBuilder().path(newSchema.getId());
    String version = UUID.randomUUID().toString();
    Meta schemaMeta = new Meta.Builder().setResourceType(Schema.RESOURCE_TYPE_SCHEMA).setLocation(location.build().toString()).setVersion(version).build();
    Schema schemaWithMeta = newSchema.builder().setMeta(schemaMeta).build();

    schemaAPI.createCustomSchema(schemaWithMeta);

    logger.trace("Created schema {} with version {}", newSchema.getId(), version);
    return Response.created(location.build()).tag(version).entity(schemaWithMeta).build();
  }

  @DELETE
  @Path("{id}")
  public void deleteSchema(@PathParam("id") @ValidSchemaId final String schemaId) {
    validateSchemaId(schemaId);

    schemaAPI.deleteCustomSchema(schemaId);

    logger.trace("Deleted schema {}", schemaId);
    Response.noContent().build();
  }

  private void validateSchemaId(final String schemaId) {
    if (!SchemasCallback.isCustomSchema(schemaId)) {
      throw new InvalidInputException(String.format("Invalid schema name. Expected an extension schema name starting with the extension schema URN: '%s'",
          Schema.EXTENSION_SCHEMA_URN));
    }

    String schemaName = stripStart(schemaId, Schema.EXTENSION_SCHEMA_URN);
    if (!schemaAPI.isValidSchemaName(schemaName)) {
      throw new InvalidInputException("Invalid schema name. Expected format is alphanumeric.");
    }
  }

  private void validateSchema(final Schema schema) {
    validateSchemaId(schema.getId());

    int customSchemasCount = schemaAPI.getCustomSchemas().size() - PREDEFINED_SCHEMAS_COUNT;
    if (customSchemasCount >= CUSTOM_SCHEMAS_MAX_COUNT) {
      throw new InvalidInputException("Custom schema maximum count reached!");
    }

    if (!schemaAPI.isValidSchemaName(schema.getName())) {
      throw new InvalidInputException("Invalid schema name. Expected format is alphanumeric.");
    }

    schema.getAttributes().forEach(attribute -> {
      if (!schemaAPI.isValidSchemaName(attribute.getName())) {
        throw new InvalidInputException("Invalid attribute name. Expected format is alphanumeric.");
      }
    });

  }
}
