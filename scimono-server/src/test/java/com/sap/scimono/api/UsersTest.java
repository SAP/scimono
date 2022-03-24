package com.sap.scimono.api;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.callback.resourcetype.ResourceTypesCallback;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.callback.users.UsersCallback;
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
public class UsersTest {

  private Users users;

  @Mock
  UriInfo uriInfo;
  @Mock
  SchemasCallback schemasCallback;
  @Mock
  ResourceTypesCallback resourceTypesCallback;
  @Mock
  UsersCallback usersCallback;
  @Mock
  SCIMConfigurationCallback scimConfigurationCallback;
  @Mock
  SCIMApplication scimApplication;

  @Before
  public void setup() {
    Mockito.when(scimApplication.getConfigurationCallback()).thenReturn(scimConfigurationCallback);
    Mockito.when(scimApplication.getUsersCallback()).thenReturn(usersCallback);
    Mockito.when(scimApplication.getSchemasCallback()).thenReturn(schemasCallback);
    Mockito.when(scimApplication.getResourceTypesCallback()).thenReturn(resourceTypesCallback);

    users = new Users(scimApplication, uriInfo);
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

}
