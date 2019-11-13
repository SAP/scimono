/**
 * Copyright (c) 2019 by SAP Labs Bulgaria, url: http://www.sap.com All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP SE, Walldorf. You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered into with SAP.
 *
 * Created on Nov 6, 2019 by I309486
 *
 */

package com.sap.scimono.scim.system.tests.conditions;

import org.junit.jupiter.api.extension.RegisterExtension;

import com.sap.scimono.scim.system.tests.SCIMComplianceTest;
import com.sap.scimono.scim.system.tests.extensions.GroupClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.GroupFailSafeClient;
import com.sap.scimono.scim.system.tests.extensions.UserClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.UserFailSafeClient;

public class BackendSystemStateChecker extends SCIMComplianceTest {

  @RegisterExtension
  UserClientScimResponseExtension resourceAwareUserRequest = UserClientScimResponseExtension.forClearingAfterEachExecutions(userRequest);

  @RegisterExtension
  GroupClientScimResponseExtension resourceAwareGroupRequest = GroupClientScimResponseExtension.forClearingAfterEachExecutions(groupRequest);

  private final UserFailSafeClient userFailSafeClient = resourceAwareUserRequest.getFailSafeClient();
  private final GroupFailSafeClient groupFailSafeClient = resourceAwareGroupRequest.getFailSafeClient();

  public boolean isUsersEndpointEmpty() {
    int alreadyCreatedUsersCount = userFailSafeClient.getAllWithIdPaging().size();
    if (alreadyCreatedUsersCount == 0) {
      return true;
    }

    return false;
  }

  public boolean isGroupsEndpointEmpty() {
    int alreadyCreatedGroupsCount = groupFailSafeClient.getAllWithIdPaging().size();
    if (alreadyCreatedGroupsCount == 0) {
      return true;
    }

    return false;
  }

}
