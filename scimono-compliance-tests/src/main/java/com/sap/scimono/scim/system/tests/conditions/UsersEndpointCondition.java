package com.sap.scimono.scim.system.tests.conditions;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.AnnotatedElement;

import static com.sap.scimono.entity.User.RESOURCE_TYPE_USER;

public class UsersEndpointCondition implements ExecutionCondition {
  private final ResourceEndpointConditionEvaluation evaluation =
      new ResourceEndpointConditionEvaluation(() -> new BackendSystemStateCheck().isUsersEndpointEmpty());

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    return evaluation.evaluate(getBackendState(context), RESOURCE_TYPE_USER);
  }

  private BackendState getBackendState(ExtensionContext extensionContext) {
    AnnotatedElement annotatedElement = extensionContext.getElement().orElseThrow(IllegalStateException::new);
    if (!annotatedElement.isAnnotationPresent(EnableOnUsersBackendState.class)){
      throw new IllegalStateException();
    }

    return annotatedElement.getAnnotation(EnableOnUsersBackendState.class).state();
  }
}
