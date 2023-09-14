
package com.sap.scimono.api;

import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.api.API.SCHEMAS;
import static com.sap.scimono.helper.Strings.stripStart;

import java.net.URI;
import java.util.*;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import com.sap.scimono.entity.schema.validation.ValidSchemaId;
import com.sap.scimono.helper.ResourceLocationService;
import com.sap.scimono.helper.UnnecessarySchemasEraser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.entity.schema.validation.ValidSchema;
import com.sap.scimono.exception.InvalidInputException;
import com.sap.scimono.exception.ResourceNotFoundException;

@Path(API.SCHEMAS)
@Produces(APPLICATION_JSON_SCIM)
@Consumes(APPLICATION_JSON_SCIM)
public class Schemas {
  private static final Logger logger = LoggerFactory.getLogger(Schemas.class);

  private static final int CUSTOM_SCHEMAS_MAX_COUNT = 20;

  private final SchemasCallback schemaAPI;
  private final SCIMConfigurationCallback scimConfig;
  private final ResourceLocationService resourceLocationService;

  public Schemas(@Context Application appContext, @Context UriInfo uriInfo) {
    SCIMApplication scimApplication = SCIMApplication.from(appContext);

    schemaAPI = scimApplication.getSchemasCallback();
    scimConfig = scimApplication.getConfigurationCallback();
    resourceLocationService = new ResourceLocationService(uriInfo, scimApplication.getConfigurationCallback(), SCHEMAS);
  }

  @GET
  public Response getSchemas() {
    logger.trace("Reading all schemas");
    List<Schema> schemas = schemaAPI.getSchemas();

    List<Schema> schemasWithLocation = new ArrayList<>();
    for (Schema schema : schemas) {
      schema = resourceLocationService.addLocation(schema, schema.getId());
      schemasWithLocation.add(schema);
    }

    return Response.ok(new PagedByIndexSearchResult<>(schemasWithLocation, schemasWithLocation.size(), scimConfig.getMaxResourcesPerPage(), 1)).build();
  }

  @GET
  @Path("{id}")
  public Response getSchema(@PathParam("id") @ValidSchemaId final String schemaId) {
    logger.trace("Reading schema {}", schemaId);

    Schema schema =  schemaAPI.getSchema(schemaId);
    if (schema == null) {
      throw new ResourceNotFoundException(Schema.RESOURCE_TYPE_SCHEMA, schemaId);
    }
    Schema schemaWithLocation = resourceLocationService.addLocation(schema, schemaId);
    return Response.ok(schemaWithLocation).tag(schemaWithLocation.getMeta().getVersion()).location(resourceLocationService.getLocation(schemaId)).build();
  }

  @POST
  public Response createSchema(@Valid @ValidSchema final Schema newSchema) {
    validateSchema(newSchema);

    UnnecessarySchemasEraser<Schema> unnecessarySchemasEraser = new UnnecessarySchemasEraser<>();
    Schema schema = unnecessarySchemasEraser.eraseAllUnnecessarySchemas(newSchema, Schema.SCHEMA);

    URI location = resourceLocationService.getLocation(schema.getId());
    String version = UUID.randomUUID().toString();
    Meta schemaMeta = new Meta.Builder().setResourceType(Schema.RESOURCE_TYPE_SCHEMA).setLocation(location.toString()).setVersion(version).build();
    Schema schemaWithMeta = schema.builder().setMeta(schemaMeta).build();

    schemaAPI.createCustomSchema(schemaWithMeta);

    logger.trace("Created schema {} with version {}", schema.getId(), version);
    return Response.created(location).tag(version).entity(schemaWithMeta).build();
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

    int customSchemasCount = schemaAPI.getCustomSchemas().size();
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
