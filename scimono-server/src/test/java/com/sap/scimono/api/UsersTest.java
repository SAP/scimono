package com.sap.scimono.api;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.exception.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class UsersTest {

  private Users users;

  @BeforeEach
  public void setup() {
    SCIMApplication scimApplication = new SCIMApplication() {

    };
    users = new Users(scimApplication, null);
  }

  @Test
  public void testUpdateUserWithEmptyBody() {
    String userId = String.valueOf(UUID.randomUUID());

    assertThrows(InvalidInputException.class, () -> users.updateUser(userId, null));
  }

  @Test
  public void testPatchUserWithEmptyBody() {
    String userId = String.valueOf(UUID.randomUUID());
    assertThrows(InvalidInputException.class, () -> users.patchUser(userId, null));
  }

}
