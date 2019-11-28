package com.sap.scimono.scim.system.tests.conditions;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.sap.scimono.entity.User.RESOURCE_TYPE_USER;

public class UsersEndpointCondition implements ExecutionCondition {
  private final ResourceEndpointConditionEvaluation evaluation =
      new ResourceEndpointConditionEvaluation(() -> new BackendSystemStateCheck().isUsersEndpointEmpty());

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    return evaluation.evaluate(new BackendStateConditionalAnnotationResolver(context).getBackendState(), RESOURCE_TYPE_USER);
  }
}
