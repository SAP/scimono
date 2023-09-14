
package com.sap.scimono.api;

import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.api.API.BULK;
import static com.sap.scimono.api.API.GROUPS;
import static com.sap.scimono.api.API.USERS;
import static com.sap.scimono.entity.Group.RESOURCE_TYPE_GROUP;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scimono.SCIMApplication;
import com.sap.scimono.api.helper.ObjectMapperFactory;
import com.sap.scimono.api.helper.ScimErrorResponseParser;
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
import com.sap.scimono.entity.bulk.BulkResponseOperationLocationService;
import com.sap.scimono.entity.bulk.RequestOperation;
import com.sap.scimono.entity.bulk.ResponseOperation;
import com.sap.scimono.entity.bulk.validation.BulkOperationsValidator;
import com.sap.scimono.entity.bulk.validation.ValidBulkRequest;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.validation.patch.PatchValidationFramework;
import com.sap.scimono.exception.InternalScimonoException;
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

    userPatchValidator = PatchValidationFramework.usersFramework(schemasAPI, resourceTypesAPI, usersAPI);
    groupPatchValidator = PatchValidationFramework.groupsFramework(schemasAPI, resourceTypesAPI, groupsAPI);
  }

  @POST
  public Response handleBulkRequest(@ValidBulkRequest BulkBody<RequestOperation> bulkRequest) {
    BulkOperationsValidator operationsValidator = new BulkOperationsValidator(scimConfigurationCallback);
    BulkResponseOperationLocationService responseService = new BulkResponseOperationLocationService(usersLocationService, groupsLocationService);
    List<RequestOperation> validatedOperations = operationsValidator.getValidBulkOperations(bulkRequest);
    List<RequestOperation> formattedBulkOperations = normalizeRequestOperations(validatedOperations);

    bulkRequest = BulkBody.forRequest(bulkRequest.getFailOnErrors(), formattedBulkOperations);
    BulkBody<ResponseOperation> bulkResponse = bulkAPI.handleBulkRequest(bulkRequest);

    bulkResponse = responseService.rebuildWithLocations(bulkResponse);
    return Response.ok().entity(bulkResponse).build();
  }

  private List<RequestOperation> normalizeRequestOperations(List<RequestOperation> requestOperations) {
    return requestOperations.stream().map(operation -> {
      if (!operation.hasValidationError()) {
        String resourceType = operation.getResourceType();
        if (User.RESOURCE_TYPE_USER.equalsIgnoreCase(resourceType)) {
          return prepareUserBulkOperation(operation);
        }

        if (RESOURCE_TYPE_GROUP.equalsIgnoreCase(resourceType)) {
          return prepareGroupBulkOperation(operation);
        }
      }

      return operation;
    }).collect(Collectors.toList());
  }

  private RequestOperation prepareUserBulkOperation(RequestOperation operation) {
    RequestOperation.Builder builder = operation.builder();
    switch (operation.getMethod()) {
      case POST:
        builder.setData(parseAndPreprocessUserData(userPreProcessor::prepareForCreate, operation));
        break;
      case PUT:
        String resourceId = requireResourceId(operation);
        PreProcessorExecutor<User> updateProcessor = group -> userPreProcessor.prepareForUpdate(group, resourceId);
        builder.setData(parseAndPreprocessUserData(updateProcessor, operation));
        break;
      case PATCH:
        requireResourceId(operation);
        PreProcessorExecutor<PatchBody> patchProcessor = data -> preparePatchBodyWithMeta(userPatchValidator, data);
        builder.setData(parseAndPreprocessPatchData(patchProcessor, operation));
        break;
    }

    return builder.build();
  }

  private RequestOperation prepareGroupBulkOperation(RequestOperation operation) {
    RequestOperation.Builder builder = operation.builder();
    switch (operation.getMethod()) {
      case POST:
        builder.setData(parseAndPreprocessGroupData(groupPreProcessor::prepareForCreate, operation));
        break;
      case PUT:
        String resourceId = requireResourceId(operation);
        PreProcessorExecutor<Group> updateProcessor = group -> groupPreProcessor.prepareForUpdate(group, resourceId);
        builder.setData(parseAndPreprocessGroupData(updateProcessor, operation));
        break;
      case PATCH:
        requireResourceId(operation);
        PreProcessorExecutor<PatchBody> patchProcessor = data -> preparePatchBodyWithMeta(groupPatchValidator, data);
        builder.setData(parseAndPreprocessPatchData(patchProcessor, operation));
        break;
    }

    return builder.build();
  }

  private Object parseAndPreprocessGroupData(PreProcessorExecutor<Group> preProcessor, RequestOperation reqOp) {
    return parseAndPreprocessData(preProcessor, () -> JSON_OBJECT_MAPPER.treeToValue(reqOp.getRawData(), Group.class));
  }

  private Object parseAndPreprocessUserData(PreProcessorExecutor<User> preProcessor, RequestOperation reqOp) {
    return parseAndPreprocessData(preProcessor, () -> JSON_OBJECT_MAPPER.treeToValue(reqOp.getRawData(), User.class));
  }

  private Object parseAndPreprocessPatchData(PreProcessorExecutor<PatchBody> preProcessor, RequestOperation reqOp) {
    return parseAndPreprocessData(preProcessor, () -> JSON_OBJECT_MAPPER.treeToValue(reqOp.getRawData(), PatchBody.class));
  }

  private <T> Object parseAndPreprocessData(PreProcessorExecutor<T> preProcessor, JsonMappingExecutor<T> resourceMapping) {
    try {
      return preProcessor.execute(resourceMapping.execute());
    } catch (Throwable e) {
      return ScimErrorResponseParser.parseException(e);
    }
  }

  private String requireResourceId(RequestOperation bulkOperation) {
    return bulkOperation.getResourceId().orElseThrow(() -> new InternalScimonoException("resource id is required for this bulk operation..."));
  }

  private PatchBody preparePatchBodyWithMeta(PatchValidationFramework validator, PatchBody patchBody) {
    String version = UUID.randomUUID().toString();
    Meta meta = new Meta.Builder(null, Instant.now()).setVersion(version).build();

    validator.validate(patchBody);
    return new PatchBody.Builder(patchBody).setMeta(meta).build();
  }

  @FunctionalInterface
  private interface PreProcessorExecutor<T> {

    T execute(T data);
  }

  @FunctionalInterface
  private interface JsonMappingExecutor<T> {

    T execute() throws JsonProcessingException;
  }
}