package com.sap.scimono.client;

import static com.sap.scimono.api.API.BULK;
import static com.sap.scimono.client.ResourceAction.CREATE_BULK;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import com.sap.scimono.api.API;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.bulk.BulkBody;
import com.sap.scimono.entity.bulk.RequestMethod;
import com.sap.scimono.entity.bulk.RequestOperation;
import com.sap.scimono.entity.bulk.ResponseOperation;
import com.sap.scimono.entity.patch.PatchBody;

public class BulkRequest {
  private final WebTarget targetSystem;
  private final SCIMRequest scimRequest;

  private final BulkBody<RequestOperation> body;

  private BulkRequest(WebTarget targetSystem, SCIMRequest scimRequest, BulkBody<RequestOperation> body) {
    this.targetSystem = targetSystem;
    this.scimRequest = scimRequest;
    this.body = body;
  }

  public SCIMResponse<BulkBody<ResponseOperation>> execute() {
    Response response = scimRequest.post(targetSystem, body);

    GenericType<BulkBody<ResponseOperation>> responseTpe = new GenericType<BulkBody<ResponseOperation>>() {};
    return SCIMResponse.newInstance(responseTpe, response, scimRequest.getScimActionResponseStatusConfig(CREATE_BULK));
  }


  public static class Operations {
    private static final String USERS_PATH = "/" + API.USERS;
    private static final String GROUPS_PATH = "/" + API.GROUPS;

    public static RequestOperation createUser(User user) {
      return createUser(generateBulkId(), user);
    }

    // @formatter:off
    public static RequestOperation createUser(String bulkId, User user) {
      return new RequestOperation.Builder()
          .setBulkId(bulkId)
          .setData(user)
          .setMethod(RequestMethod.POST)
          .setPath(USERS_PATH)
          .build();
    }

    public static RequestOperation updateUser(User user) {
      return updateUser(generateBulkId(), user);
    }

    public static RequestOperation updateUser(String bulkId, User user) {
      return new RequestOperation.Builder()
          .setBulkId(bulkId)
          .setData(user)
          .setMethod(RequestMethod.PUT)
          .setPath(USERS_PATH + "/" + user.getId())
          .build();
    }

    public static RequestOperation deleteUser(String userId) {
      return deleteUser(generateBulkId(), userId);
    }

    public static RequestOperation deleteUser(String bulkId, String userId) {
      return new RequestOperation.Builder()
          .setBulkId(bulkId)
          .setMethod(RequestMethod.DELETE)
          .setPath(USERS_PATH + "/" + userId)
          .build();
    }

    public static RequestOperation patchUser(String userId, PatchBody patchBody) {
      return patchUser(generateBulkId(), userId, patchBody);
    }

    public static RequestOperation patchUser(String bulkId, String userId, PatchBody patchBody) {
      return patchResource(bulkId, userId, USERS_PATH, patchBody);
    }

    public static RequestOperation createGroup(Group group) {
      return createGroup(generateBulkId(), group);
    }

    public static RequestOperation createGroup(String bulkId, Group group) {
      return new RequestOperation.Builder()
          .setBulkId(bulkId)
          .setMethod(RequestMethod.POST)
          .setData(group)
          .setPath(GROUPS_PATH)
          .build();
    }

    public static RequestOperation updateGroup(Group group) {
      return updateGroup(generateBulkId(), group);
    }

    public static RequestOperation updateGroup(String bulkId, Group group) {
      return new RequestOperation.Builder()
          .setBulkId(bulkId)
          .setMethod(RequestMethod.PUT)
          .setData(group)
          .setPath(GROUPS_PATH + "/" + group.getId())
          .build();
    }

    public static RequestOperation deleteGroup(String groupId) {
      return deleteGroup(generateBulkId(), groupId);
    }

    public static RequestOperation deleteGroup(String bulkId, String groupId) {
      return new RequestOperation.Builder()
          .setBulkId(bulkId)
          .setMethod(RequestMethod.DELETE)
          .setPath(GROUPS_PATH + "/" + groupId)
          .build();
    }

    public static RequestOperation patchGroup(String groupId, PatchBody patchBody) {
      return patchGroup(generateBulkId(), groupId, patchBody);
    }

    public static RequestOperation patchGroup(String bulkId, String groupId, PatchBody patchBody) {
      return patchResource(bulkId, groupId, GROUPS_PATH, patchBody);
    }

    public static RequestOperation patchResource(String bulkId, String resourceId, String resourceEndpoint,  PatchBody patchBody) {
      return new RequestOperation.Builder()
          .setBulkId(bulkId)
          .setMethod(RequestMethod.PATCH)
          .setData(patchBody)
          .setPath(resourceEndpoint + "/" + resourceId)
          .build();
    }
    // @formatter:on

    private static String generateBulkId() {
      return UUID.randomUUID().toString();
    }
  }
  public static class Builder {
    private final Map<String, RequestOperation> operations = new LinkedHashMap<>();
    private final WebTarget targetSystem;
    private final SCIMRequest scimRequest;

    private Integer failsOnError;

    Builder(WebTarget targetSystem, SCIMRequest scimRequest) {
      this.targetSystem = targetSystem.path(BULK);
      this.scimRequest = scimRequest;
    }

    public Builder withFailsOnError(int failsOnError) {
      this.failsOnError = failsOnError;
      return this;
    }

    public Builder addBulkOperation(RequestOperation requestOperation) {
      operations.put(requestOperation.getBulkId(), requestOperation);
      return this;
    }

    public BulkRequest build() {
      if (failsOnError == null) {
        failsOnError = defaultFailsOnError();
      }

      BulkBody<RequestOperation> body = BulkBody.forRequest(failsOnError, new ArrayList<>(operations.values()));
      return new BulkRequest(targetSystem, scimRequest, body);
    }

    private int defaultFailsOnError() {
      return operations.size();
    }
  }
}
