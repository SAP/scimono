
package com.sap.scimono.api;

import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.api.API.ATTRIBUTES_PARAM;
import static com.sap.scimono.api.API.COUNT_PARAM;
import static com.sap.scimono.api.API.FILTER_PARAM;
import static com.sap.scimono.api.API.GROUPS;
import static com.sap.scimono.api.API.START_ID_PARAM;
import static com.sap.scimono.api.API.START_INDEX_PARAM;
import static com.sap.scimono.entity.Group.RESOURCE_TYPE_GROUP;
import static com.sap.scimono.entity.paging.PagedByIndexSearchResult.DEFAULT_COUNT;
import static com.sap.scimono.entity.paging.PagedByIndexSearchResult.DEFAULT_START_INDEX;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.api.patch.PATCH;
import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.callback.groups.GroupsCallback;
import com.sap.scimono.callback.resourcetype.ResourceTypesCallback;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.paging.PageInfo;
import com.sap.scimono.entity.paging.PagedResult;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.schema.validation.ValidId;
import com.sap.scimono.entity.schema.validation.ValidStartId;
import com.sap.scimono.entity.validation.ResourceCustomAttributesValidator;
import com.sap.scimono.entity.validation.patch.PatchValidationFramework;
import com.sap.scimono.exception.InvalidInputException;
import com.sap.scimono.exception.ResourceNotFoundException;
import com.sap.scimono.helper.ReadOnlyAttributesEraser;
import com.sap.scimono.helper.ResourceLocationService;

@Path(API.GROUPS)
@Produces(APPLICATION_JSON_SCIM)
@Consumes(APPLICATION_JSON_SCIM)
@ServletRequestProvider
public class Groups {
  private static final Logger logger = LoggerFactory.getLogger(Groups.class);

  private final GroupsCallback groupAPI;
  private final SchemasCallback schemaAPI;
  private final ResourceTypesCallback resourceTypesAPI;
  private final SCIMConfigurationCallback scimConfig;
  private final ResourceLocationService resourceLocationService;

  public Groups(@Context Application appContext, @Context UriInfo uriInfo) {
    SCIMApplication scimApplication = SCIMApplication.from(appContext);

    groupAPI = scimApplication.getGroupsCallback();
    schemaAPI = scimApplication.getSchemasCallback();
    resourceTypesAPI = scimApplication.getResourceTypesCallback();
    scimConfig = scimApplication.getConfigurationCallback();
    resourceLocationService = new ResourceLocationService(uriInfo, scimApplication.getConfigurationCallback(), GROUPS);
  }

  @GET
  @Path("{id}")
  public Response getGroup(@PathParam("id") @ValidId final String groupId) {
    logger.trace("Reading group {}", groupId);

    Group groupFromDb = groupAPI.getGroup(groupId);

    if (groupFromDb == null) {
      throw new ResourceNotFoundException(RESOURCE_TYPE_GROUP, groupId);
    }

    Group group = resourceLocationService.addLocation(groupFromDb, groupId);
    group = resourceLocationService.addMembersLocation(group);
    return Response.ok(group).tag(group.getMeta().getVersion()).location(resourceLocationService.getLocation(groupId)).build();
  }

  @GET
  public Response getGroups(@QueryParam(START_INDEX_PARAM) @DefaultValue(DEFAULT_START_INDEX) String startIndexParam,
                            @QueryParam(COUNT_PARAM) @DefaultValue(DEFAULT_COUNT) String countParam,
                            @QueryParam(START_ID_PARAM) @ValidStartId String startId,
                            @QueryParam(FILTER_PARAM) final String filter,
                            @QueryParam(ATTRIBUTES_PARAM) final String attributes) {

    logger.trace("Reading groups with paging parameters startIndex {} startId {} count {}", startIndexParam, startId, countParam);

    int startIndex = PagingParamsParser.parseStartIndex(startIndexParam);
    int count = PagingParamsParser.parseCount(countParam);

    int maxCount = scimConfig.getMaxResourcesPerPage();
    logger.trace("Configured max count of returned resources is {}", maxCount);
    if (count > maxCount) {
      count = maxCount;
    }

    PageInfo pageInfo = PageInfo.getInstance(count, startIndex - 1, startId);
    PagedResult<Group> groups = groupAPI.getGroups(pageInfo, filter);

    List<Group> groupsToReturn = new ArrayList<>();
    for (Group group : groups.getResources()) {
      group = resourceLocationService.addLocation(group, group.getId());
      group = resourceLocationService.addMembersLocation(group);
      groupsToReturn.add(group);
    }

    return ListResponseBuilder.forGroups(groupsToReturn)
        .withPagingStartParameters(startId, startIndex)
        .withRequestedCount(count)
        .withTotalResultsCount(groups.getTotalResourceCount())
        .build();
  }

  @POST
  public Response createGroup(@Valid Group newGroup) {
    if (newGroup == null) {
      throw new InvalidInputException("One of the request inputs is not valid.");
    }

    ReadOnlyAttributesEraser<Group> readOnlyAttributesEraser = new ReadOnlyAttributesEraser<>(schemaAPI);
    newGroup = readOnlyAttributesEraser.eraseAllFormCustomExtensions(newGroup);

    String version = UUID.randomUUID().toString();
    Meta groupMeta = new Meta.Builder().setVersion(version).setResourceType(RESOURCE_TYPE_GROUP).build();
    Group.Builder groupWithMetaBuilder = newGroup.builder().setMeta(groupMeta);
    groupAPI.generateId().ifPresent(groupWithMetaBuilder::setId);

    Group groupWithMeta = groupWithMetaBuilder.build();

    ResourceCustomAttributesValidator<Group> customAttributesValidator = ResourceCustomAttributesValidator.forPost(schemaAPI, resourceTypesAPI);
    customAttributesValidator.validate(groupWithMeta);

    Group createdGroup = groupAPI.createGroup(groupWithMeta);
    createdGroup = resourceLocationService.addMembersLocation(createdGroup);
    createdGroup = resourceLocationService.addLocation(createdGroup, createdGroup.getId());

    logger.trace("Created group {} with version {}", createdGroup.getId(), version);
    return Response.created(resourceLocationService.getLocation(createdGroup.getId())).tag(version).entity(createdGroup).build();
  }

  @PUT
  @Path("{id}")
  public Response updateGroup(@PathParam("id") @ValidId final String groupId, @Valid Group groupToUpdate) {
    ReadOnlyAttributesEraser<Group> readOnlyAttributesEraser = new ReadOnlyAttributesEraser<>(schemaAPI);
    groupToUpdate = readOnlyAttributesEraser.eraseAllFormCustomExtensions(groupToUpdate);

    String newVersion = UUID.randomUUID().toString();
    Meta.Builder lastUpdatedMeta = new Meta.Builder(groupToUpdate.getMeta());

    URI groupLocation = resourceLocationService.getLocation(groupId);
    lastUpdatedMeta.setLastModified(Instant.now()).setVersion(newVersion).setLocation(groupLocation.toString());

    Group updatedGroup = groupToUpdate.builder().setId(groupId).setMeta(lastUpdatedMeta.build()).build();

    ResourceCustomAttributesValidator<Group> customAttributesValidator = ResourceCustomAttributesValidator.forPut(schemaAPI, resourceTypesAPI);
    customAttributesValidator.validate(groupToUpdate);

    updatedGroup = groupAPI.updateGroup(updatedGroup);
    updatedGroup = resourceLocationService.addMembersLocation(updatedGroup);

    logger.trace("Updated group {}, new version is {}", groupId, newVersion);
    return Response.ok(updatedGroup).tag(newVersion).location(groupLocation).build();
  }

  @DELETE
  @Path("{id}")
  public void deleteGroup(@PathParam("id") @ValidId final String groupId) {
    groupAPI.deleteGroup(groupId);

    logger.trace("Deleted group {}", groupId);
    Response.noContent().build();
  }

  @PATCH
  @Path("{id}")
  public Response patchGroup(@PathParam("id") @ValidId final String groupId, final PatchBody patchBody) {
    PatchValidationFramework validationFramework = PatchValidationFramework.groupsFramework(schemaAPI, resourceTypesAPI);
    validationFramework.validate(patchBody);

    Meta meta = new Meta.Builder(null, Instant.now()).setVersion(UUID.randomUUID().toString()).build();
    groupAPI.patchGroup(groupId, patchBody, meta);

    logger.trace("Updated group {}", groupId);
    return Response.status(Response.Status.NO_CONTENT).build();
  }
}
