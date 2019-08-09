
package com.sap.scimono.api;

import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.api.API.COUNT_PARAM;
import static com.sap.scimono.api.API.FILTER_PARAM;
import static com.sap.scimono.api.API.START_ID_PARAM;
import static com.sap.scimono.api.API.START_INDEX_PARAM;
import static com.sap.scimono.api.API.USERS;
import static com.sap.scimono.entity.User.RESOURCE_TYPE_USER;
import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_END_PARAM;
import static com.sap.scimono.entity.paging.PagedByIndexSearchResult.DEFAULT_COUNT;
import static com.sap.scimono.entity.paging.PagedByIndexSearchResult.DEFAULT_START_INDEX;
import static com.sap.scimono.helper.Resources.addLocation;
import static com.sap.scimono.helper.Resources.addRelationalEntitiesLocation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.api.patch.PATCH;
import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.callback.users.UsersCallback;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.User;
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

@Path(USERS)
@Produces(APPLICATION_JSON_SCIM)
@Consumes(APPLICATION_JSON_SCIM)
@ServletRequestProvider
public class Users {
  private static final Logger logger = LoggerFactory.getLogger(Users.class);

  @Context
  private UriInfo uriInfo;

  private final UsersCallback usersAPI;
  private final SchemasCallback schemaAPI;
  private final SCIMConfigurationCallback scimConfig;

  public Users(@Context final Application appContext) {
    SCIMApplication scimApplication = SCIMApplication.from(appContext);

    usersAPI = scimApplication.getUsersCallback();
    schemaAPI = scimApplication.getSchemasCallback();
    scimConfig = scimApplication.getConfigurationCallback();
  }

  @GET
  @Path("/Me")
  public Response getMe(@Context final SecurityContext sec) {
    String userName = sec.getUserPrincipal().getName();
    logger.trace("Reading data for current user {}", userName);

    User userFromDb = usersAPI.getUserByUsername(userName);

    if (userFromDb != null) {
      UriBuilder location = uriInfo.getBaseUriBuilder();
      List<PathSegment> pathSegments = uriInfo.getPathSegments(false);
      for (int i = 0; i < pathSegments.size() - 1; ++i) {
        location.path(pathSegments.get(i).toString());
      }
      location.path(userFromDb.getId());

      User user = addLocation(userFromDb, location);
      user = addRelationalEntitiesLocation(user, uriInfo);
      return Response.ok(user).tag(user.getMeta().getVersion()).location(location.build()).build();
    }

    throw new ResourceNotFoundException(RESOURCE_TYPE_USER, userName);
  }

  @GET
  @Path("{id}")
  public Response getUser(@PathParam("id") @ValidId final String userId) {
    logger.trace("Reading user {}", userId);
    User userFromDb = usersAPI.getUser(userId);

    if (userFromDb != null) {
      User user = addLocation(userFromDb, uriInfo.getAbsolutePath());
      user = addRelationalEntitiesLocation(user, uriInfo);
      return Response.ok(user).tag(user.getMeta().getVersion()).location(uriInfo.getAbsolutePath()).build();
    }

    throw new ResourceNotFoundException(RESOURCE_TYPE_USER, userId);
  }

  @GET
  //@formatter:off
  public Response getUsers(@DefaultValue(DEFAULT_START_INDEX) @QueryParam(START_INDEX_PARAM) int startIndex,
      @DefaultValue(DEFAULT_COUNT) @QueryParam(COUNT_PARAM) int count, @QueryParam(START_ID_PARAM) @ValidStartId String startId,
      @QueryParam(FILTER_PARAM) final String filter) {
    // @formatter:on
    logger.trace("Reading users with paging parameters startIndex {} startId {} count {}", startIndex, startId, count);

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
    PagedResult<User> users = usersAPI.getUsers(pageInfo, filter);

    List<User> usersToReturn = new ArrayList<>();
    for (User user : users.getResources()) {
      user = addLocation(user, uriInfo.getAbsolutePathBuilder().path(user.getId()));
      user = addRelationalEntitiesLocation(user, uriInfo);
      usersToReturn.add(user);
    }

    // TODO maybe move this paging logic inside the PagedByX classes, what will remain here is whether to return paged by id or paged by index results
    if (startId != null) {
      if (usersToReturn.size() <= count) {
        return Response.ok(new PagedByIdentitySearchResult<>(usersToReturn, users.getTotalResourceCount(), count, startId, PAGINATION_BY_ID_END_PARAM)).build();
      }

      int indexOfLastUser = usersToReturn.size() - 1;
      User nextUser = usersToReturn.remove(indexOfLastUser);

      return Response.ok(new PagedByIdentitySearchResult<>(usersToReturn, users.getTotalResourceCount(), count, startId, nextUser.getId())).build();
    }

    return Response.ok(new PagedByIndexSearchResult<>(usersToReturn, users.getTotalResourceCount(), count, startIndex)).build();
  }

  @POST
  public Response createUser(final User newUser) {
    if (newUser == null) {
      throw new InvalidInputException("One of the request inputs is not valid.");
    }

    String version = UUID.randomUUID().toString();
    Meta userMeta = new Meta.Builder().setVersion(version).setResourceType(RESOURCE_TYPE_USER).build();
    
    User.Builder userWithMeta = newUser.builder().setMeta(userMeta);
    usersAPI.generateId().ifPresent(userWithMeta::setId);

    User createdUser = usersAPI.createUser(userWithMeta.build());
    
    UriBuilder location = uriInfo.getAbsolutePathBuilder().path(createdUser.getId());
    createdUser = addLocation(createdUser, location);
    
    logger.trace("Created user {} with version {}", createdUser.getId(), version);
    return Response.created(location.build()).tag(version).entity(createdUser).build();
  }

  @PUT
  @Path("{id}")
  public Response updateUser(@PathParam("id") @ValidId final String userId, final User userToUpdate) {
    String newVersion = UUID.randomUUID().toString();
    Meta.Builder lastModifiedMeta = new Meta.Builder(userToUpdate.getMeta());
    lastModifiedMeta.setLastModified(Instant.now()).setVersion(newVersion);

    User updatedUser = userToUpdate.builder().setId(userId).setMeta(lastModifiedMeta.build()).build();
    usersAPI.updateUser(updatedUser);

    logger.trace("Updated user {}, new version is {}", userId, newVersion);
    addLocation(updatedUser, uriInfo.getAbsolutePath());
    return Response.ok(updatedUser).tag(newVersion).location(uriInfo.getAbsolutePath()).build();
  }

  @DELETE
  @Path("{id}")
  public void deleteUser(@PathParam("id") @ValidId final String userId) {
    usersAPI.deleteUser(userId);

    logger.trace("Deleted user {}", userId);
    Response.noContent().build();
  }

  @PATCH
  @Path("{id}")
  public Response patchUser(@PathParam("id") @ValidId final String userId, final PatchBody patchBody) {
    PatchValidationFramework validationFramework = PatchValidationFramework.usersFramework(schemaAPI);
    validationFramework.validate(patchBody);

    String newVersion = UUID.randomUUID().toString();
    Meta meta = new Meta.Builder(null, Instant.now()).setVersion(newVersion).build();
    usersAPI.patchUser(userId, patchBody, meta);

    logger.trace("Updated user {}, new version is {}", userId, newVersion);
    return Response.status(Status.NO_CONTENT).build();
  }

  @POST
  @Path(".query")
  public Response queryUsers() {
    return getUsers(0, 0, null, null);
  }

}
