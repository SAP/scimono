package com.sap.scimono.api;

import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.api.API.BULK;
import static com.sap.scimono.api.API.GROUPS;
import static com.sap.scimono.api.API.USERS;
import static com.sap.scimono.entity.Group.RESOURCE_TYPE_GROUP;
import static com.sap.scimono.exception.SCIMException.Type.TOO_MANY;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scimono.SCIMApplication;
import com.sap.scimono.api.helper.ObjectMapperFactory;
import com.sap.scimono.api.preprocessor.ResourcePreProcessor;
import com.sap.scimono.callback.bulk.BulkRequestCallback;
import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.callback.groups.GroupsCallback;
import com.sap.scimono.callback.resourcetype.ResourceTypesCallback;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.callback.users.UsersCallback;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.bulk.BulkBody;
import com.sap.scimono.entity.bulk.RequestMethod;
import com.sap.scimono.entity.bulk.RequestOperation;
import com.sap.scimono.entity.bulk.ResponseOperation;
import com.sap.scimono.entity.bulk.validation.ValidBulkRequest;
import com.sap.scimono.entity.config.BulkSetting;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.validation.patch.PatchValidationFramework;
import com.sap.scimono.exception.InternalScimonoException;
import com.sap.scimono.exception.SCIMException;
import com.sap.scimono.helper.ResourceLocationService;

@Path(BULK)
@Produces(APPLICATION_JSON_SCIM)
@Consumes(APPLICATION_JSON_SCIM)
@ServletRequestProvider
public class Bulk {
  private static final ObjectMapper JSON_OBJECT_MAPPER = ObjectMapperFactory.createObjectMapper();
  private final BulkRequestCallback bulkAPI;

  private final ResourcePreProcessor<User> userPreProcessor;
  private final ResourcePreProcessor<Group> groupPreProcessor;

  private final PatchValidationFramework userPatchValidator;
  private final PatchValidationFramework groupPatchValidator;

  private final ResourceLocationService usersLocationService;
  private final ResourceLocationService groupsLocationService;

  private final SCIMConfigurationCallback scimConfigurationCallback;

  public Bulk(@Context final Application appContext, @Context final UriInfo uriInfo) {
    SCIMApplication scimApplication = SCIMApplication.from(appContext);
    bulkAPI = scimApplication.getBulkRequestCallback();
    scimConfigurationCallback = scimApplication.getConfigurationCallback();

    usersLocationService = new ResourceLocationService(uriInfo, scimConfigurationCallback, USERS);
    groupsLocationService = new ResourceLocationService(uriInfo, scimConfigurationCallback, GROUPS);

    SchemasCallback schemasAPI = scimApplication.getSchemasCallback();
    ResourceTypesCallback resourceTypesAPI = scimApplication.getResourceTypesCallback();

    UsersCallback usersAPI = scimApplication.getUsersCallback();
    GroupsCallback groupsAPI = scimApplication.getGroupsCallback();

    userPreProcessor = ResourcePreProcessor.forUsers(usersLocationService, usersAPI, resourceTypesAPI, schemasAPI);
    groupPreProcessor = ResourcePreProcessor.forGroups(groupsLocationService, groupsAPI, resourceTypesAPI, schemasAPI);

    userPatchValidator = PatchValidationFramework.usersFramework(schemasAPI, resourceTypesAPI);
    groupPatchValidator = PatchValidationFramework.groupsFramework(schemasAPI, resourceTypesAPI);
  }
  
  @POST
  public Response handleBulkRequest(@ValidBulkRequest BulkBody<RequestOperation> bulkRequest) {
    validateByBulkSettings(bulkRequest);
    List<RequestOperation> normalizedBulkOperations = normalizeRequestOperations(bulkRequest.getOperations());

    bulkRequest = BulkBody.forRequest(bulkRequest.getFailOnErrors(), normalizedBulkOperations);
    BulkBody<ResponseOperation> bulkResponse = bulkAPI.handleBulkRequest(bulkRequest);

    bulkResponse = fillMissingResponseData(bulkRequest, bulkResponse);
    return Response.ok().entity(bulkResponse).build();
  }

  private void validateByBulkSettings(BulkBody<RequestOperation> bulkRequest) {
    BulkSetting bulkSetting = scimConfigurationCallback.getBulkSetting();
    if (bulkSetting == null || !bulkSetting.isSupported()) {
      String msg = "Service provider does not support bulk operations. Please check the bulk settings.";
      throw new WebApplicationException(msg, Response.Status.NOT_IMPLEMENTED);
    }

    if (bulkRequest.getOperations().size() > bulkSetting.getMaxOperations()) {
      String msg = "Bulk operations count exceeded the maximum value supported.";
      throw new SCIMException(TOO_MANY, msg, Response.Status.REQUEST_ENTITY_TOO_LARGE);
    }

    try {
      byte[] bytes = JSON_OBJECT_MAPPER.writeValueAsBytes(bulkRequest);
      int payloadSize = bulkSetting.getMaxPayloadSize();
      if (bytes.length > bulkSetting.getMaxPayloadSize()) {
        String msg = String.format("The size of the bulk operation exceeds the maxPayloadSize (%d).", payloadSize);
        throw new SCIMException(TOO_MANY, msg, Response.Status.REQUEST_ENTITY_TOO_LARGE);
      }
    } catch (JsonProcessingException e) {
      throw new InternalScimonoException("Unable to parse bulk body to bytes");
    }
  }

  private List<RequestOperation> normalizeRequestOperations(List<RequestOperation> requestOperations) {
    return requestOperations.stream().map(operation -> {
      String resourceType = operation.getResourceType();
      if (User.RESOURCE_TYPE_USER.equalsIgnoreCase(resourceType)) {
        return prepareUserBulkOperation(operation);
      }

      if (RESOURCE_TYPE_GROUP.equalsIgnoreCase(resourceType)) {
        return prepareGroupBulkOperation(operation);
      }

      return operation;
    }).collect(Collectors.toList());
  }

  private BulkBody<ResponseOperation> fillMissingResponseData(BulkBody<RequestOperation> bulkRequest, BulkBody<ResponseOperation> bulkResponse) {
    Map<String, RequestOperation> requestOperations = bulkRequest.getOperations().stream()
        .collect(Collectors.toMap(RequestOperation::getBulkId, Function.identity()));


    List<ResponseOperation> responseOperations = bulkResponse.getOperations().stream()
        .map(respOperation -> {
          ResponseOperation.Builder builder = respOperation.builder();
          RequestOperation reqOperation = requestOperations.get(respOperation.getBulkId());

          builder.withLocation(getResponseLocation(reqOperation, respOperation));
          return builder.build();
        }).collect(Collectors.toList());

    return BulkBody.forResponse(responseOperations);
  }

  public String getResponseLocation(RequestOperation reqOperation, ResponseOperation respOperation) {
    if (reqOperation.getMethod() == RequestMethod.POST && !respOperation.isSuccessful()) {
      return null;
    }

    String location = respOperation.getLocation();
    String resourceType = respOperation.getResourceType();

    if (location == null && User.RESOURCE_TYPE_USER.equalsIgnoreCase(resourceType)) {
      location = usersLocationService.getLocation(respOperation.getResourceId()).toString();
    }

    if (location == null && RESOURCE_TYPE_GROUP.equalsIgnoreCase(resourceType)) {
      location = groupsLocationService.getLocation(respOperation.getResourceId()).toString();
    }

    return location;
  }

  private RequestOperation prepareUserBulkOperation(RequestOperation operation) {
    RequestOperation.Builder builder = operation.builder();
    switch (operation.getMethod()) {
      case POST:
        builder.setData(userPreProcessor.prepareForCreate(operation.getDataAsUser()));
        break;
      case PUT:
        builder.setData(userPreProcessor.prepareForUpdate(operation.getDataAsUser(), requireResourceId(operation)));
        break;
      case PATCH:
        requireResourceId(operation);
        builder.setData(preparePatchBodyWithMeta(userPatchValidator, operation));
        break;
    }

    return builder.build();
  }

  private RequestOperation prepareGroupBulkOperation(RequestOperation operation) {
    RequestOperation.Builder builder = operation.builder();
    switch (operation.getMethod()) {
      case POST:
        builder.setData(groupPreProcessor.prepareForCreate(operation.getDataAsGroup()));
        break;
      case PUT:
        builder.setData(groupPreProcessor.prepareForUpdate(operation.getDataAsGroup(), requireResourceId(operation)));
        break;
      case PATCH:
        requireResourceId(operation);
        builder.setData(preparePatchBodyWithMeta(groupPatchValidator, operation));
        break;
    }

    return builder.build();
  }

  private String requireResourceId(RequestOperation bulkOperation) {
    return bulkOperation.getResourceId()
        .orElseThrow(() -> new InternalScimonoException("resource id is required for this bulk operation..."));
  }

  private PatchBody preparePatchBodyWithMeta(PatchValidationFramework validator, RequestOperation operation) {
    String version = UUID.randomUUID().toString();
    Meta meta = new Meta.Builder(null, Instant.now()).setVersion(version).build();

    PatchBody patchBody = operation.getDataAsPatch();
    validator.validate(patchBody);
    return new PatchBody.Builder(patchBody).setMeta(meta).build();
  }
}
