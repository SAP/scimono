package com.sap.scimono.scim.system.tests.conditions;

import com.sap.scimono.scim.system.tests.SCIMComplianceTest;
import com.sap.scimono.scim.system.tests.extensions.GroupClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.GroupFailSafeClient;
import com.sap.scimono.scim.system.tests.extensions.UserClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.UserFailSafeClient;

class BackendSystemStateCheck extends SCIMComplianceTest {
  private final UserFailSafeClient userFailSafeClient = UserClientScimResponseExtension.forClearingAfterEachExecutions(userRequest).getFailSafeClient();
  private final GroupFailSafeClient groupFailSafeClient = GroupClientScimResponseExtension.forClearingAfterEachExecutions(groupRequest).getFailSafeClient();

  boolean isUsersEndpointEmpty() {
    return userFailSafeClient.getAllWithoutPaging().getResources().isEmpty();
  }

  boolean isGroupsEndpointEmpty() {
    return groupFailSafeClient.getAllWithoutPaging().getResources().isEmpty();
  }
}
