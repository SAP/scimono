package com.sap.scimono.scim.system.tests.conditions;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;

import java.util.function.BooleanSupplier;

import static com.sap.scimono.scim.system.tests.conditions.BackendState.EMPTY;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

class ResourceEndpointConditionEvaluation {
  private BooleanSupplier isEmptyConditionResult;

  ResourceEndpointConditionEvaluation(BooleanSupplier isEmptyConditionResult) {
    this.isEmptyConditionResult = isEmptyConditionResult;
  }

  ConditionEvaluationResult evaluate(BackendState desiredBackendState, String resourceType) {
    return desiredBackendState == EMPTY ?
        endpointShouldBeEmptyConditionResult(resourceType) :
        endpointShouldContainUsersConditionResult(resourceType);
  }

  private ConditionEvaluationResult endpointShouldBeEmptyConditionResult(String resourceType) {
    return isEmptyConditionResult.getAsBoolean() ?
        enabled(String.format("Backend system has no %s created.", resourceType)) :
        disabled(String.format("Backend system has %s already created.", resourceType));
  }

  private ConditionEvaluationResult endpointShouldContainUsersConditionResult(String resourceType) {
    return isEmptyConditionResult.getAsBoolean()  ?
        disabled(String.format("Backend system has %s already created.", resourceType)) :
        enabled(String.format("Backend system has no %s created.", resourceType));
  }
}
