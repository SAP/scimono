package com.sap.scimono.scim.system.tests.launcher;

import com.sap.scimono.scim.system.tests.SCIMComplianceTest;
import org.junit.platform.engine.DiscoverySelector;

import java.util.List;

public interface TestMethodsSelectorsCreator {
  String TESTS_BASE_PACKAGE = SCIMComplianceTest.class.getPackage().getName();

  List<DiscoverySelector> createDiscoverySelectors();
}
