
package com.sap.scimono.api;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.callback.resourcetype.ResourceTypesCallback;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.paging.PagedResult;
import com.sap.scimono.entity.schema.ResourceType;
import com.sap.scimono.entity.schema.ResourceType.Builder;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.exception.ResourceNotFoundException;
import com.sap.scimono.helper.ResourceLocationService;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.api.API.RESOURCE_TYPES;

@Path(RESOURCE_TYPES)
@Produces(APPLICATION_JSON_SCIM)
@Consumes(APPLICATION_JSON_SCIM)
public class ResourceTypes {
  private static final Instant now = Instant.now();

  //@formatter:off
  private static final ResourceType RESOURCE_TYPE_USER = new Builder()
      .setId(User.RESOURCE_TYPE_USER)
      .name(User.RESOURCE_TYPE_USER)
      .description("User Account")
      .endpoint(API.USERS)
      .schema(User.SCHEMA)
      .setMeta(new Meta.Builder(now, now)
                 .setResourceType(ResourceType.RESOURCE_TYPE_RESOURCE_TYPE).build())
      .build();

  private static final ResourceType RESOURCE_TYPE_GROUP = new Builder()
      .setId(Group.RESOURCE_TYPE_GROUP)
      .name(Group.RESOURCE_TYPE_GROUP)
      .description("Group")
      .endpoint(API.GROUPS)
      .schema(Group.SCHEMA)
      .setMeta(new Meta.Builder(now, now)
                  .setResourceType(ResourceType.RESOURCE_TYPE_RESOURCE_TYPE).build())
      .build();

  private static final ResourceType RESOURCE_TYPE_SCHEMA = new Builder()
      .setId(Schema.RESOURCE_TYPE_SCHEMA)
      .name(Schema.RESOURCE_TYPE_SCHEMA)
      .description("Schema")
      .endpoint(API.SCHEMAS)
      .schema(Schema.SCHEMA)
      .setMeta(new Meta.Builder(now, now)
                  .setResourceType(ResourceType.RESOURCE_TYPE_RESOURCE_TYPE).build())
      .build();
  //@formatter:on

  private final SCIMConfigurationCallback scimConfig;
  private final ResourceTypesCallback resourceTypesCallback;
  private final ResourceLocationService resourceLocationService;

  public ResourceTypes(@Context Application appContext, @Context UriInfo uriInfo) {
    SCIMApplication scimApplication = SCIMApplication.from(appContext);

    resourceTypesCallback = scimApplication.getResourceTypesCallback();
    scimConfig = scimApplication.getConfigurationCallback();
    resourceLocationService = new ResourceLocationService(uriInfo, scimApplication.getConfigurationCallback(), RESOURCE_TYPES);
  }

  @GET
  public PagedByIndexSearchResult<ResourceType> getResourceTypes() {
    List<ResourceType> resources = new ArrayList<>();

    ResourceType userResourceType = resourceLocationService.addLocation(RESOURCE_TYPE_USER, User.RESOURCE_TYPE_USER);
    resources.add(this.withSchemaExtensions(userResourceType));

    ResourceType groupResourceType = resourceLocationService.addLocation(RESOURCE_TYPE_GROUP, Group.RESOURCE_TYPE_GROUP);
    resources.add(this.withSchemaExtensions(groupResourceType));

    ResourceType schemaResourceType = resourceLocationService.addLocation(RESOURCE_TYPE_SCHEMA, Schema.RESOURCE_TYPE_SCHEMA);
    resources.add(this.withSchemaExtensions(schemaResourceType));

    PagedResult<ResourceType> customResourceTypes = resourceTypesCallback.getCustomResourceTypes();
    resourceLocationService.addLocation(customResourceTypes);

    for (ResourceType resourceType : customResourceTypes.getResources()) {
      resources.add(this.withSchemaExtensions(resourceType));
    }

    return new PagedByIndexSearchResult<>(resources, resources.size(), scimConfig.getMaxResourcesPerPage(), 1);
  }

  @GET
  @Path("{id}")
  public ResourceType getResourceType(@PathParam("id") final String typeId) {
    ResourceType resourceType;

    switch (typeId) {
      case User.RESOURCE_TYPE_USER:
        resourceType = RESOURCE_TYPE_USER;
        break;

      case Group.RESOURCE_TYPE_GROUP:
        resourceType = RESOURCE_TYPE_GROUP;
        break;

      case Schema.RESOURCE_TYPE_SCHEMA:
        resourceType = RESOURCE_TYPE_SCHEMA;
        break;

      default:
        resourceType = resourceTypesCallback.getCustomResourceType(typeId);
    }

    if (resourceType == null) {
      throw new ResourceNotFoundException(ResourceType.RESOURCE_TYPE_RESOURCE_TYPE, typeId);
    }

    resourceType = this.withSchemaExtensions(resourceType);

    return resourceLocationService.addLocation(resourceType, typeId);
  }

  private ResourceType withSchemaExtensions(ResourceType resourceType) {
    Builder builder = resourceType.builder();
    builder.clearSchemaExtensions();
    builder.addSchemaExtensions(resourceTypesCallback.getSchemaExtensions(resourceType.getId()));
    return builder.build();
  }
}
