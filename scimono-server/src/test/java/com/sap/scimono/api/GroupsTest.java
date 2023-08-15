package com.sap.scimono.api;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.exception.InvalidInputException;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class GroupsTest {

  private Groups groups;

  @Before
  public void setup() {
    SCIMApplication scimApplication = new SCIMApplication() {

    };
    groups = new Groups(scimApplication, null);
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
