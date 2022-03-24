package com.sap.scimono.api;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.callback.groups.GroupsCallback;
import com.sap.scimono.callback.resourcetype.ResourceTypesCallback;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.exception.InvalidInputException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.UriInfo;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class GroupsTest {

  private Groups groups;

  @Mock
  UriInfo uriInfo;
  @Mock
  SchemasCallback schemasCallback;
  @Mock
  ResourceTypesCallback resourceTypesCallback;
  @Mock
  GroupsCallback groupsCallback;
  @Mock
  SCIMConfigurationCallback scimConfigurationCallback;
  @Mock
  SCIMApplication scimApplication;

  @Before
  public void setup() {
    Mockito.when(scimApplication.getConfigurationCallback()).thenReturn(scimConfigurationCallback);
    Mockito.when(scimApplication.getGroupsCallback()).thenReturn(groupsCallback);
    Mockito.when(scimApplication.getSchemasCallback()).thenReturn(schemasCallback);
    Mockito.when(scimApplication.getResourceTypesCallback()).thenReturn(resourceTypesCallback);

    groups = new Groups(scimApplication, uriInfo);
  }

  @Test(expected = InvalidInputException.class)
  public void testUpdateGroupWithEmptyBody() {
    String userId = String.valueOf(UUID.randomUUID());
    groups.updateGroup(userId, null);
  }

  @Test(expected = InvalidInputException.class)
  public void testPatchGroupWithEmptyBody() {
    String userId = String.valueOf(UUID.randomUUID());
    groups.patchGroup(userId, null);
  }

}
