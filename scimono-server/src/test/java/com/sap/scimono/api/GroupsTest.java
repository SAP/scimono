package com.sap.scimono.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scimono.SCIMApplication;
import com.sap.scimono.callback.groups.GroupsCallback;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.exception.InvalidInputException;
import com.sap.scimono.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class GroupsTest {

  private Groups groups;

  private ObjectMapper mapper;
  private SchemasCallback schemasCallbackMock = Mockito.mock(SchemasCallback.class, Mockito.CALLS_REAL_METHODS);
  private GroupsCallback groupsCallback = Mockito.mock(GroupsCallback.class, Mockito.CALLS_REAL_METHODS);

  private final String PATCH_OP_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:PatchOp";

  @BeforeEach
  public void setup() {
    mapper = new ObjectMapper();
    SCIMApplication scimApplication = new SCIMApplication() {

      @Override
      public SchemasCallback getSchemasCallback() {
        return schemasCallbackMock;
      }

      @Override
      public GroupsCallback getGroupsCallback() {
        return groupsCallback;
      }

    };
    groups = new Groups(scimApplication, null);
  }

  @Test
  public void testUpdateGroupWithEmptyBody() {
    String groupId = String.valueOf(UUID.randomUUID());
    assertThrows(InvalidInputException.class, () -> groups.updateGroup(groupId, null));
  }

  @Test
  public void testPatchGroupWithEmptyBody() {
    String groupId = String.valueOf(UUID.randomUUID());
    assertThrows(InvalidInputException.class, () -> groups.patchGroup(groupId, null));
  }

  @Test
  @DisplayName("Test patch group with non existing resource and remove operation on a not removable attribute. The existence of the resource given in the path should be validated first. Expected ResourceNotFoundException (404).")
  public void testPatchGroupNonExistingResource() throws JsonProcessingException {
    Mockito.doNothing().when(groupsCallback).patchGroup(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.doReturn(new Attribute.Builder().required(true).build()).when(schemasCallbackMock).getAttribute(Mockito.any());
    Mockito.doReturn(new ArrayList<>()).when(schemasCallbackMock).getCustomSchemas();
    String groupId = String.valueOf(UUID.randomUUID());
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
    assertThrows(ResourceNotFoundException.class, () -> groups.patchGroup(groupId, patchBody));
  }

  @Test
  @DisplayName("Test patch group with existing resource and remove operation on a not removable attribute. Expected InvalidInputException (400) since this is not allowed.")
  public void testPatchGroupExistingResource() throws JsonProcessingException {
    Mockito.doNothing().when(groupsCallback).patchGroup(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.doReturn(new Attribute.Builder().required(true).build()).when(schemasCallbackMock).getAttribute(Mockito.any());
    Mockito.doReturn(new Group.Builder().build()).when(groupsCallback).getGroup(Mockito.any());
    Mockito.doReturn(new ArrayList<>()).when(schemasCallbackMock).getCustomSchemas();
    String groupId = String.valueOf(UUID.randomUUID());
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
    assertThrows(InvalidInputException.class, () -> groups.patchGroup(groupId, patchBody));
  }



  private JsonNode getValueDisplayName() throws JsonProcessingException {
    JsonNode stringValue = mapper.readTree("\"displayName\"");
    return stringValue;
  }

}
