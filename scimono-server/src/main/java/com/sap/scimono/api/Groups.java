
package com.sap.scimono.api;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.api.patch.PATCH;
import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.callback.groups.GroupsCallback;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.paging.PageInfo;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.paging.PagedResult;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.schema.validation.ValidId;
import com.sap.scimono.entity.schema.validation.ValidStartId;
import com.sap.scimono.entity.validation.patch.PatchValidationFramework;
import com.sap.scimono.exception.InvalidInputException;
import com.sap.scimono.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.api.API.COUNT_PARAM;
import static com.sap.scimono.api.API.FILTER_PARAM;
import static com.sap.scimono.api.API.START_ID_PARAM;
import static com.sap.scimono.api.API.START_INDEX_PARAM;
import static com.sap.scimono.entity.Group.RESOURCE_TYPE_GROUP;
import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_END_PARAM;
import static com.sap.scimono.entity.paging.PagedByIndexSearchResult.DEFAULT_COUNT;
import static com.sap.scimono.entity.paging.PagedByIndexSearchResult.DEFAULT_START_INDEX;
import static com.sap.scimono.helper.Resources.addLocation;
import static com.sap.scimono.helper.Resources.addMembersLocation;
import static com.sap.scimono.helper.Strings.isNotNullOrEmpty;

@Path(API.GROUPS)
@Produces(APPLICATION_JSON_SCIM)
@Consumes(APPLICATION_JSON_SCIM)
@ServletRequestProvider
public class Groups {
  private static final Logger logger = LoggerFactory.getLogger(Groups.class);

  @Context
  private UriInfo uriInfo;

  private final GroupsCallback groupAPI;
  private final SchemasCallback schemaAPI;
  private final SCIMConfigurationCallback scimConfig;

  public Groups(@Context Application appContext) {
    SCIMApplication scimApplication = SCIMApplication.from(appContext);

    groupAPI = scimApplication.getGroupsCallback();
    schemaAPI = scimApplication.getSchemasCallback();
    scimConfig = scimApplication.getConfigurationCallback();
  }

  @GET
  @Path("{id}")
  public Response getGroup(@PathParam("id") @ValidId final String groupId) {
    logger.trace("Reading group {}", groupId);

    Group groupFromDb = groupAPI.getGroup(groupId);

    if (groupFromDb != null) {
      Group group = addLocation(groupFromDb, uriInfo.getAbsolutePath());
      group = addMembersLocation(group, uriInfo);
      return Response.ok(group).tag(group.getMeta().getVersion()).location(uriInfo.getAbsolutePath()).build();
    }

    throw new ResourceNotFoundException(RESOURCE_TYPE_GROUP, groupId);
  }

  @GET
  public Response getGroups(@DefaultValue(DEFAULT_START_INDEX) @QueryParam(START_INDEX_PARAM) int startIndex,
      @DefaultValue(DEFAULT_COUNT) @QueryParam(COUNT_PARAM) int count, @QueryParam(START_ID_PARAM) @ValidStartId String startId,
      @QueryParam(FILTER_PARAM) final String filter) {
    logger.trace("Reading groups with paging parameters startIndex {} startId {} count {}", startIndex, startId, count);
    if (startIndex < 1) {
      startIndex = 1;
    }

    if (count < 0) {
      count = 0;
    }

    int maxCount = scimConfig.getMaxResourcesPerPage();
    logger.trace("Configured max count of returned resources is {}", maxCount);
    if (count > maxCount) {
      count = maxCount;
    }

    PageInfo pageInfo = PageInfo.getInstance(count, startIndex - 1, startId);
    PagedResult<Group> groups = groupAPI.getGroups(pageInfo, filter);

    List<Group> groupsToReturn = new ArrayList<>();
    for (Group group : groups.getResources()) {
      group = addLocation(group, uriInfo.getAbsolutePathBuilder().path(group.getId()));
      group = addMembersLocation(group, uriInfo);
      groupsToReturn.add(group);
    }

    if (isNotNullOrEmpty(startId)) {
      if (groupsToReturn.size() <= count) {
        return Response.ok(new PagedByIdentitySearchResult<>(groupsToReturn, groups.getTotalResourceCount(), groups.getResourcesCount(), startId, PAGINATION_BY_ID_END_PARAM)).build();
      }

      int indexOfLastGroup = groupsToReturn.size() - 1;
      Group nextGroup = groupsToReturn.remove(indexOfLastGroup);

      return Response.ok(new PagedByIdentitySearchResult<>(groupsToReturn, groups.getTotalResourceCount(), groupsToReturn.size(), startId, nextGroup.getId())).build();
    }

    return Response.ok(new PagedByIndexSearchResult<>(groupsToReturn, groups.getTotalResourceCount(), groupsToReturn.size(), new Long(startIndex))).build();
  }

  @POST
  public Response createGroup(final Group newGroup) {
    if (newGroup == null) {
      throw new InvalidInputException("One of the request inputs is not valid.");
    }

    String version = UUID.randomUUID().toString();
    Meta groupMeta = new Meta.Builder().setVersion(version).setResourceType(RESOURCE_TYPE_GROUP).build();
    Group.Builder groupWithMeta = newGroup.builder().setMeta(groupMeta);
    groupAPI.generateId().ifPresent(groupWithMeta::setId);

    Group createdGroup = groupAPI.createGroup(groupWithMeta.build());

    UriBuilder location = uriInfo.getAbsolutePathBuilder().path(createdGroup.getId());
    createdGroup = addLocation(createdGroup, location);

    logger.trace("Created group {} with version {}", createdGroup.getId(), version);
    return Response.created(location.build()).tag(version).entity(createdGroup).build();
  }

  @PUT
  @Path("{id}")
  public Response updateGroup(@PathParam("id") @ValidId final String groupId, final Group groupToUpdate) {
    String newVersion = UUID.randomUUID().toString();
    Meta.Builder lastUpdatedMeta = new Meta.Builder(groupToUpdate.getMeta());
    lastUpdatedMeta.setLastModified(Instant.now()).setVersion(newVersion);

    Group updatedGroup = groupToUpdate.builder().setId(groupId).setMeta(lastUpdatedMeta.build()).build();
    groupAPI.updateGroup(updatedGroup);

    logger.trace("Updated group {}, new version is {}", groupId, newVersion);
    return Response.ok(updatedGroup).tag(newVersion).location(uriInfo.getAbsolutePath()).build();
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
    PatchValidationFramework validationFramework = PatchValidationFramework.groupsFramework(schemaAPI);
    validationFramework.validate(patchBody);

    Meta meta = new Meta.Builder(null, Instant.now()).setVersion(UUID.randomUUID().toString()).build();
    groupAPI.patchGroup(groupId, patchBody, meta);

    logger.trace("Updated group {}", groupId);
    return Response.status(Response.Status.NO_CONTENT).build();
  }
}
