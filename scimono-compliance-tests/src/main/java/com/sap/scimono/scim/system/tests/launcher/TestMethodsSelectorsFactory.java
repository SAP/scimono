package com.sap.scimono.scim.system.tests.launcher;

import com.sap.scimono.scim.system.tests.util.TestReporter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sap.scimono.scim.system.tests.launcher.TestMethodsSelectorsCreator.TESTS_BASE_PACKAGE;

public class TestMethodsSelectorsFactory {
  private TestMethodsSelectorsCreator selectorsCreator;

  private TestMethodsSelectorsFactory(TestMethodsSelectorsCreator selectorsCreator) {
    this.selectorsCreator = selectorsCreator;
  }

  public List<DiscoverySelector> getDiscoverySelectors() {
    List<DiscoverySelector> discoverySelectors = new ArrayList<>();

    discoverySelectors.add(DiscoverySelectors.selectPackage(TestReporter.class.getPackage().getName()));
//    discoverySelectors.add(DiscoverySelectors.selectClass(E2EGroupComplianceTest.class));
    discoverySelectors.addAll(selectorsCreator.createDiscoverySelectors());

    return discoverySelectors;
  }

  public static TestMethodsSelectorsFactory fromPropertiesFile(String propertiesFileName) {
    return new TestMethodsSelectorsFactory(PropertyFileDiscoverySelector.load(propertiesFileName));
  }

  public static TestMethodsSelectorsFactory forAllTests() {
    return new TestMethodsSelectorsFactory(() -> Collections.singletonList(DiscoverySelectors.selectPackage(TESTS_BASE_PACKAGE)));
  }
}
