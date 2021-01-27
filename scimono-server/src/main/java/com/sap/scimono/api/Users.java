
package com.sap.scimono.api;

import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.api.API.ATTRIBUTES_PARAM;
import static com.sap.scimono.api.API.COUNT_PARAM;
import static com.sap.scimono.api.API.EXCLUDED_ATTRIBUTES_PARAM;
import static com.sap.scimono.api.API.FILTER_PARAM;
import static com.sap.scimono.api.API.START_ID_PARAM;
import static com.sap.scimono.api.API.START_INDEX_PARAM;
import static com.sap.scimono.api.API.USERS;
import static com.sap.scimono.entity.User.RESOURCE_TYPE_USER;
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
import com.sap.scimono.api.request.RequestedResourceAttributesParser;
import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.callback.resourcetype.ResourceTypesCallback;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.callback.users.UsersCallback;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.User;
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
import com.sap.scimono.helper.UnnecessarySchemasEraser;

@Path(USERS)
@Produces(APPLICATION_JSON_SCIM)
@Consumes(APPLICATION_JSON_SCIM)
@ServletRequestProvider
public class Users {
  private static final Logger logger = LoggerFactory.getLogger(Users.class);

  private final UriInfo uriInfo;
  private final UsersCallback usersAPI;
  private final SchemasCallback schemaAPI;
  private final ResourceTypesCallback resourceTypesAPI;
  private final SCIMConfigurationCallback scimConfig;
  private final ResourceLocationService resourceLocationService;

  public Users(@Context final Application appContext, @Context final UriInfo uriInfo) {
    this.uriInfo = uriInfo;
    SCIMApplication scimApplication = SCIMApplication.from(appContext);

    usersAPI = scimApplication.getUsersCallback();
    schemaAPI = scimApplication.getSchemasCallback();
    resourceTypesAPI = scimApplication.getResourceTypesCallback();
    scimConfig = scimApplication.getConfigurationCallback();
    resourceLocationService = new ResourceLocationService(uriInfo, scimConfig, USERS);
  }

  @GET
  @Path("/Me")
  public Response getMe(@Context final SecurityContext sec) {
    String userName = sec.getUserPrincipal().getName();
    logger.trace("Reading data for current user {}", userName);

    User userFromDb = usersAPI.getUserByUsername(userName);

    if (userFromDb == null) {
      throw new ResourceNotFoundException(RESOURCE_TYPE_USER, userName);
    }

    UriBuilder location = uriInfo.getBaseUriBuilder();
    List<PathSegment> pathSegments = uriInfo.getPathSegments(false);
    for (int i = 0; i < pathSegments.size() - 1; ++i) {
      location.path(pathSegments.get(i).toString());
    }
    location.path(userFromDb.getId());

    User user = resourceLocationService.addLocation(userFromDb, location.build());
    user = resourceLocationService.addRelationalEntitiesLocation(user);
    return Response.ok(user).tag(user.getMeta().getVersion()).location(location.build()).build();
  }

  @GET
  @Path("{id}")
  // @formatter:off
  public Response getUser(@PathParam("id") @ValidId final String userId,
                          @QueryParam(ATTRIBUTES_PARAM) final String attributes,
                          @QueryParam(EXCLUDED_ATTRIBUTES_PARAM) final String excludedAttributes) {
    // @formatter:on
    logger.trace("Reading user {}", userId);
    User userFromDb = usersAPI.getUser(userId, RequestedResourceAttributesParser.parse(attributes, excludedAttributes));

    if (userFromDb == null) {
      throw new ResourceNotFoundException(RESOURCE_TYPE_USER, userId);
    }

    User user = resourceLocationService.addLocation(userFromDb, userId);
    user = resourceLocationService.addRelationalEntitiesLocation(user);
    return Response.ok(user).tag(user.getMeta().getVersion()).location(resourceLocationService.getLocation(userId)).build();
  }

  @GET
  // @formatter:off
  public Response getUsers(@QueryParam(START_INDEX_PARAM) @DefaultValue(DEFAULT_START_INDEX)  String startIndexParam,
                           @QueryParam(COUNT_PARAM) @DefaultValue(DEFAULT_COUNT) String countParam,
                           @QueryParam(START_ID_PARAM) @ValidStartId final String startId,
                           @QueryParam(FILTER_PARAM) final String filter,
                           @QueryParam(ATTRIBUTES_PARAM) final String attributes,
                           @QueryParam(EXCLUDED_ATTRIBUTES_PARAM) final String excludedAttributes) {
    // @formatter:on
    logger.trace("Reading users with paging parameters startIndex {} startId {} count {}", startIndexParam, startId, countParam);

    int startIndex = PagingParamsParser.parseStartIndex(startIndexParam);
    int count = PagingParamsParser.parseCount(countParam);

    int maxCount = scimConfig.getMaxResourcesPerPage();
    logger.trace("Configured max count of returned resources is {}", maxCount);
    if (count > maxCount) {
      count = maxCount;
    }

    PageInfo pageInfo = PageInfo.getInstance(count, startIndex - 1, startId);
    PagedResult<User> users = usersAPI.getUsers(pageInfo, filter, RequestedResourceAttributesParser.parse(attributes, excludedAttributes));

    List<User> usersToReturn = new ArrayList<>();
    for (User user : users.getResources()) {
      user = resourceLocationService.addLocation(user, user.getId());
      user = resourceLocationService.addRelationalEntitiesLocation(user);
      usersToReturn.add(user);
    }

    return ListResponseBuilder.forUsers(usersToReturn)
        .withPagingStartParameters(startId, startIndex)
        .withRequestedCount(count)
        .withTotalResultsCount(users.getTotalResourceCount())
        .build();
  }

  @POST
  public Response createUser(@Valid final User newUser) {
    if (newUser == null) {
      throw new InvalidInputException("One of the request inputs is not valid.");
    }

    ReadOnlyAttributesEraser<User> readOnlyAttributesEraser = new ReadOnlyAttributesEraser<>(schemaAPI);
    User userWithoutReadOnlyAttributes = readOnlyAttributesEraser.eraseAllFormCustomExtensions(newUser);

    UnnecessarySchemasEraser<User> unnecessarySchemasEraser = new UnnecessarySchemasEraser<>();
    User user = unnecessarySchemasEraser.eraseAllUnnecessarySchemas(userWithoutReadOnlyAttributes, User.SCHEMA);

    String version = UUID.randomUUID().toString();
    Meta userMeta = new Meta.Builder().setVersion(version).setResourceType(RESOURCE_TYPE_USER).build();

    User.Builder userWithMetaBuilder = user.builder().setMeta(userMeta);
    usersAPI.generateId().ifPresent(userWithMetaBuilder::setId);

    User userWithMeta = userWithMetaBuilder.build();

    ResourceCustomAttributesValidator<User> userCustomAttributesValidator = ResourceCustomAttributesValidator.forPost(schemaAPI, resourceTypesAPI);
    userCustomAttributesValidator.validate(userWithMeta);

    User createdUser = usersAPI.createUser(userWithMeta);

    createdUser = resourceLocationService.addLocation(createdUser, createdUser.getId());
    createdUser = resourceLocationService.addRelationalEntitiesLocation(createdUser);

    logger.trace("Created user {} with version {}", createdUser.getId(), version);
    return Response.created(resourceLocationService.getLocation(createdUser.getId())).tag(version).entity(createdUser).build();
  }

  @PUT
  @Path("{id}")
  public Response updateUser(@PathParam("id") @ValidId final String userId, @Valid final User userToUpdate) {
    ReadOnlyAttributesEraser<User> readOnlyAttributesEraser = new ReadOnlyAttributesEraser<>(schemaAPI);
    User userWithoutReadOnlyAttributes = readOnlyAttributesEraser.eraseAllFormCustomExtensions(userToUpdate);
    
    UnnecessarySchemasEraser<User> unnecessarySchemasEraser = new UnnecessarySchemasEraser<>();
    User user = unnecessarySchemasEraser.eraseAllUnnecessarySchemas(userWithoutReadOnlyAttributes, User.SCHEMA);

    String newVersion = UUID.randomUUID().toString();

    Meta.Builder lastModifiedMeta = new Meta.Builder();

    URI userLocation = resourceLocationService.getLocation(userId);
    lastModifiedMeta.setLastModified(Instant.now()).setVersion(newVersion).setLocation(userLocation.toString()).setResourceType(RESOURCE_TYPE_USER);
    User updatedUser = user.builder().setId(userId).setMeta(lastModifiedMeta.build()).build();

    ResourceCustomAttributesValidator<User> userCustomAttributesValidator = ResourceCustomAttributesValidator.forPut(schemaAPI, resourceTypesAPI);
    userCustomAttributesValidator.validate(updatedUser);

    updatedUser = usersAPI.updateUser(updatedUser);
    updatedUser = resourceLocationService.addRelationalEntitiesLocation(updatedUser);

    logger.trace("Updated user {}, new version is {}", userId, newVersion);
    return Response.ok(updatedUser).tag(newVersion).location(userLocation).build();
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
    PatchValidationFramework validationFramework = PatchValidationFramework.usersFramework(schemaAPI, resourceTypesAPI);
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
    return getUsers("0", "0", null, null, null, null);
  }
}
