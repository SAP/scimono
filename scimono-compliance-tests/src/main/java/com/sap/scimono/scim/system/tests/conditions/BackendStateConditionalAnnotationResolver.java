package com.sap.scimono.scim.system.tests.conditions;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.AnnotatedElement;

class BackendStateConditionalAnnotationResolver {
  private ExtensionContext extensionContext;

  BackendStateConditionalAnnotationResolver(ExtensionContext extensionContext) {
    this.extensionContext = extensionContext;
  }

  BackendState getBackendState() {
    AnnotatedElement annotatedElement = extensionContext.getElement().orElseThrow(IllegalStateException::new);
    if (!annotatedElement.isAnnotationPresent(EnableOnUsersBackendState.class)){
      throw new IllegalStateException();
    }

    return annotatedElement.getAnnotation(EnableOnUsersBackendState.class).state();
  }
}
