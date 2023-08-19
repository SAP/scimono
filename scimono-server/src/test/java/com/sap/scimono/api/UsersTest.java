package com.sap.scimono.api;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.exception.InvalidInputException;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class UsersTest {

  private Users users;

  @Before
  public void setup() {
    SCIMApplication scimApplication = new SCIMApplication() {

    };
    users = new Users(scimApplication, null);
  }

  @Test(expected = InvalidInputException.class)
  public void testUpdateUserWithEmptyBody() {
    String userId = String.valueOf(UUID.randomUUID());
    users.updateUser(userId, null, null);
  }

  @Test(expected = InvalidInputException.class)
  public void testPatchUserWithEmptyBody() {
    String userId = String.valueOf(UUID.randomUUID());
    users.patchUser(userId, null, null);
  }

}
