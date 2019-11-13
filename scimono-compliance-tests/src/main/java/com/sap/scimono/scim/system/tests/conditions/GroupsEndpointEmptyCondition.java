/**
 * Copyright (c) 2019 by SAP Labs Bulgaria, url: http://www.sap.com All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP SE, Walldorf. You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered into with SAP.
 *
 * Created on Nov 5, 2019 by I309486
 *
 */

package com.sap.scimono.scim.system.tests.conditions;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class GroupsEndpointEmptyCondition implements ExecutionCondition {

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {
    BackendSystemStateChecker backendCheck = new BackendSystemStateChecker();
    if (backendCheck.isGroupsEndpointEmpty()) {
      return ConditionEvaluationResult.enabled("Backend system has no groups created.");
    }
    return ConditionEvaluationResult.disabled("Backend system has groups alredy created.");
  }
}
