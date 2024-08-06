
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.api.patch.PATCH;
import com.sap.scimono.api.preprocessor.ResourcePreProcessor;
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
import com.sap.scimono.entity.schema.validation.ValidStartId;
import com.sap.scimono.entity.validation.patch.PatchValidationFramework;
import com.sap.scimono.exception.InvalidInputException;
import com.sap.scimono.exception.ResourceNotFoundException;
import com.sap.scimono.helper.ResourceLocationService;

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
  private final ResourcePreProcessor<User> userPreProcessor;

  private static final String NOT_VALID_INPUTS = "One of the request inputs is not valid.";

  public Users(@Context final Application appContext, @Context final UriInfo uriInfo) {
    this.uriInfo = uriInfo;
    SCIMApplication scimApplication = SCIMApplication.from(appContext);

    usersAPI = scimApplication.getUsersCallback();
    schemaAPI = scimApplication.getSchemasCallback();
    resourceTypesAPI = scimApplication.getResourceTypesCallback();
    scimConfig = scimApplication.getConfigurationCallback();
    resourceLocationService = new ResourceLocationService(uriInfo, scimConfig, USERS);
    userPreProcessor = ResourcePreProcessor.forUsers(resourceLocationService, usersAPI, resourceTypesAPI, schemaAPI);
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
  public Response getUser(@PathParam("id")  final String userId,
                          @QueryParam(FILTER_PARAM) final String filter,
                          @QueryParam(ATTRIBUTES_PARAM) final String attributes,
                          @QueryParam(EXCLUDED_ATTRIBUTES_PARAM) final String excludedAttributes) {
    // @formatter:on
    logger.trace("Reading user {}", userId);
    User userFromDb = usersAPI.getUser(userId, RequestedResourceAttributesParser.parse(attributes, excludedAttributes), filter);

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
      throw new InvalidInputException(NOT_VALID_INPUTS);
    }

    User preparedUser = userPreProcessor.prepareForCreate(newUser);
    User createdUser = usersAPI.createUser(preparedUser);

    createdUser = resourceLocationService.addLocation(createdUser, createdUser.getId());
    createdUser = resourceLocationService.addRelationalEntitiesLocation(createdUser);

    String version = preparedUser.getMeta().getVersion();
    logger.trace("Created user {} with version {}", createdUser.getId(), version);
    return Response.created(resourceLocationService.getLocation(createdUser.getId())).tag(version).entity(createdUser).build();
  }

  @PUT
  @Path("{id}")
  public Response updateUser(@PathParam("id") final String userId, @Valid final User userToUpdate) {
    if (userToUpdate == null) {
      throw new InvalidInputException(NOT_VALID_INPUTS);
    }
    User preparedUser = userPreProcessor.prepareForUpdate(userToUpdate, userId);

    User updatedUser = usersAPI.updateUser(preparedUser);

    updatedUser = resourceLocationService.addLocation(updatedUser, updatedUser.getId());
    updatedUser = resourceLocationService.addRelationalEntitiesLocation(updatedUser);

    String version = preparedUser.getMeta().getVersion();

    logger.trace("Updated user {}, new version is {}", userId, version);
    return Response.ok(updatedUser).tag(version).location(resourceLocationService.getLocation(userId)).build();
  }

  @DELETE
  @Path("{id}")
  public void deleteUser(@PathParam("id") final String userId) {
    usersAPI.deleteUser(userId);

    logger.trace("Deleted user {}", userId);
    Response.noContent().build();
  }

  @PATCH
  @Path("{id}")
  public Response patchUser(@PathParam("id") final String userId, final PatchBody patchBody) {
    if (patchBody == null) {
      throw new InvalidInputException(NOT_VALID_INPUTS);
    }
    PatchValidationFramework validationFramework = PatchValidationFramework.usersFramework(schemaAPI, resourceTypesAPI, usersAPI);
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
