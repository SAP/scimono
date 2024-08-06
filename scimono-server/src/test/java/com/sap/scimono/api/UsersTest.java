package com.sap.scimono.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scimono.SCIMApplication;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.callback.users.UsersCallback;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.exception.InvalidInputException;
import com.sap.scimono.exception.ResourceNotFoundException;

public class UsersTest {

  private Users users;

  private ObjectMapper mapper;
  private SchemasCallback schemasCallbackMock = Mockito.mock(SchemasCallback.class, Mockito.CALLS_REAL_METHODS);
  private UsersCallback usersCallbackMock = Mockito.mock(UsersCallback.class, Mockito.CALLS_REAL_METHODS);

  ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
  ArgumentCaptor<PatchBody> patchBodyCaptor = ArgumentCaptor.forClass(PatchBody.class);
  private final String PATCH_OP_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:PatchOp";

  @Before
  public void setup() {
    mapper = new ObjectMapper();
    SCIMApplication scimApplication = new SCIMApplication() {

      @Override
      public SchemasCallback getSchemasCallback() {
        return schemasCallbackMock;
      }

      @Override
      public UsersCallback getUsersCallback() {
        return usersCallbackMock;
      }
    };
    users = new Users(scimApplication, null);
  }

  @Test(expected = InvalidInputException.class)
  public void testUpdateUserWithEmptyBody() {
    String userId = String.valueOf(UUID.randomUUID());
    users.updateUser(userId, null);
  }

  @Test(expected = InvalidInputException.class)
  public void testPatchUserWithEmptyBody() {
    String userId = String.valueOf(UUID.randomUUID());
    users.patchUser(userId, null);
  }

  @Test
  public void testPatchUserActivate() throws JsonProcessingException {
    Mockito.doNothing().when(usersCallbackMock).patchUser(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.doReturn(new ArrayList<>()).when(schemasCallbackMock).getCustomSchemas();
    Mockito.doReturn(new User.Builder().build()).when(usersCallbackMock).getUser(Mockito.any());
    String userId = String.valueOf(UUID.randomUUID());
    Set<String> schemas = new HashSet<>();
    schemas.add(PATCH_OP_SCHEMA);


    JsonNode valueTrue = getValueTrue();
    PatchOperation patchOperation1 = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.ADD)
        .setPath("active")
        .setValue(valueTrue)
        .build();
    PatchBody patchBody = new PatchBody.Builder()
            .addOperation(patchOperation1)
            .setSchemas(schemas)
            .build();
    users.patchUser(userId, patchBody);

    Mockito.verify(usersCallbackMock).patchUser(userIdCaptor.capture(), patchBodyCaptor.capture(), Mockito.any());
    Assert.assertEquals(userId, userIdCaptor.getValue());
    Assert.assertEquals(patchBody, patchBodyCaptor.getValue());
  }

  @Test(expected = ResourceNotFoundException.class)
  @DisplayName("Test patch user with non existing resource and remove operation on a not removable attribute. The existence of the resource given in the path should be validated first. Expected ResourceNotFoundException (404).")
  public void testPatchUserNonExistingResource() throws JsonProcessingException {
    Mockito.doNothing().when(usersCallbackMock).patchUser(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.doReturn(new Attribute.Builder().required(true).build()).when(schemasCallbackMock).getAttribute(Mockito.any());
    Mockito.doReturn(new ArrayList<>()).when(schemasCallbackMock).getCustomSchemas();
    String userId = String.valueOf(UUID.randomUUID());
    Set<String> schemas = new HashSet<>();
    schemas.add(PATCH_OP_SCHEMA);


    JsonNode valueDisplayName = getValueDisplayName();
    PatchOperation patchOperation1 = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REMOVE)
        .setPath("displayName")
        .setValue(valueDisplayName)
        .build();
    PatchBody patchBody = new PatchBody.Builder()
        .addOperation(patchOperation1)
        .setSchemas(schemas)
        .build();
    users.patchUser(userId, patchBody);
  }

  @Test(expected = InvalidInputException.class)
  @DisplayName("Test patch user with existing resource and remove operation on a not removable attribute. Expected InvalidInputException (400) since this is not allowed.")
  public void testPatchUserExistingResource() throws JsonProcessingException {
    Mockito.doNothing().when(usersCallbackMock).patchUser(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.doReturn(new Attribute.Builder().required(true).build()).when(schemasCallbackMock).getAttribute(Mockito.any());
    Mockito.doReturn(new User.Builder().build()).when(usersCallbackMock).getUser(Mockito.any());
    Mockito.doReturn(new ArrayList<>()).when(schemasCallbackMock).getCustomSchemas();
    String userId = String.valueOf(UUID.randomUUID());
    Set<String> schemas = new HashSet<>();
    schemas.add(PATCH_OP_SCHEMA);


    JsonNode valueDisplayName = getValueDisplayName();
    PatchOperation patchOperation1 = new PatchOperation.Builder()
        .setOp(PatchOperation.Type.REMOVE)
        .setPath("displayName")
        .setValue(valueDisplayName)
        .build();
    PatchBody patchBody = new PatchBody.Builder()
        .addOperation(patchOperation1)
        .setSchemas(schemas)
        .build();
    users.patchUser(userId, patchBody);
  }

  private JsonNode getValueTrue() throws JsonProcessingException {
    JsonNode boolValue = mapper.readTree("true");
    return boolValue;
  }

  private JsonNode getValueDisplayName() throws JsonProcessingException {
    JsonNode stringValue = mapper.readTree("\"displayName\"");
    return stringValue;
  }

}
