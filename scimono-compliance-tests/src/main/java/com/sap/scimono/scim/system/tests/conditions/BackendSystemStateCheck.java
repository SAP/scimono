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
