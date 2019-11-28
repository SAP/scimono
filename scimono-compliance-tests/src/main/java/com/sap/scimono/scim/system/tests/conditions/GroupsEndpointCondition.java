package com.sap.scimono.scim.system.tests.conditions;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.sap.scimono.entity.Group.RESOURCE_TYPE_GROUP;

public class GroupsEndpointCondition implements ExecutionCondition {
  private final ResourceEndpointConditionEvaluation evaluation =
      new ResourceEndpointConditionEvaluation(() -> new BackendSystemStateCheck().isGroupsEndpointEmpty());

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    return evaluation.evaluate(new BackendStateConditionalAnnotationResolver(context).getBackendState(), RESOURCE_TYPE_GROUP);
  }
}
