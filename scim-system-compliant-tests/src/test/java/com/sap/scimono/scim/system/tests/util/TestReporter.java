package com.sap.scimono.scim.system.tests.util;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestReporter implements BeforeTestExecutionCallback {

  @Override
  public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
    System.out.println("Starting test: " + extensionContext.getDisplayName());
  }
}
