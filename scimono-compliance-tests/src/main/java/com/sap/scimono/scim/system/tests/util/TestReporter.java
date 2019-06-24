package com.sap.scimono.scim.system.tests.util;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestReporter implements BeforeEachCallback {
  private static final Logger logger = LoggerFactory.getLogger(TestReporter.class);

  @Override
  public void beforeEach(ExtensionContext extensionContext) {
    logger.info("Starting test: {}", extensionContext.getDisplayName());
  }
}
