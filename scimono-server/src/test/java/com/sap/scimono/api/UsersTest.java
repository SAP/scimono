package com.sap.scimono.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scimono.SCIMApplication;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.callback.users.UsersCallback;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.exception.InvalidInputException;

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
  }

  @Test(expected = InvalidInputException.class)
  public void testPatchUserWithEmptyBody() {
    String userId = String.valueOf(UUID.randomUUID());
  }

  @Test
  public void testPatchUserActivate() throws JsonProcessingException {
    Mockito.doNothing().when(usersCallbackMock).patchUser(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.doReturn(new ArrayList<>()).when(schemasCallbackMock).getCustomSchemas();
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

  private JsonNode getValueTrue() throws JsonProcessingException {
    JsonNode boolValue = mapper.readTree("true");
    return boolValue;
  }

}
