package com.sap.scimono.api;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.exception.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class GroupsTest {

  private Groups groups;

  @BeforeEach
  public void setup() {
    SCIMApplication scimApplication = new SCIMApplication() {

    };
    groups = new Groups(scimApplication, null);
  }

  @Test
  public void testUpdateGroupWithEmptyBody() {
    String userId = String.valueOf(UUID.randomUUID());
    assertThrows(InvalidInputException.class, () -> groups.updateGroup(userId, null));
  }

  @Test
  public void testPatchGroupWithEmptyBody() {
    String userId = String.valueOf(UUID.randomUUID());
    assertThrows(InvalidInputException.class, () -> groups.patchGroup(userId, null));
  }

}
