package com.sap.scimono.scim.system.tests.launcher;

import com.sap.scimono.scim.system.tests.E2EGroupComplianceTest;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.LogManager;

public class TestsLauncher {
  static {
    LogManager.getLogManager().reset();
    SLF4JBridgeHandler.install();
  }

  private final LauncherProperties launcherProperties;

  private TestsLauncher(final LauncherProperties launcherProperties) {
    this.launcherProperties = launcherProperties;
  }

  public void launch() {
    setTestParameters();

    TestMethodsSelectorsFactory testMethodsSelectorsFactory = launcherProperties.getTestsFilePath() == null ?
        TestMethodsSelectorsFactory.forAllTests() :
        TestMethodsSelectorsFactory.fromPropertiesFile(launcherProperties.getTestsFilePath().getValue());

    LauncherDiscoveryRequest launcherDiscoveryRequest = LauncherDiscoveryRequestBuilder.request()
        .selectors(DiscoverySelectors.selectClass(E2EGroupComplianceTest.class))
        .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "true")
        .build();

    Launcher launcher = LauncherFactory.create();
    launcher.discover(launcherDiscoveryRequest);

    launcher.registerTestExecutionListeners(new TestsExecutionReporter());
    launcher.execute(launcherDiscoveryRequest);
  }

  private void setTestParameters() {
    setTestParameter(launcherProperties.getServiceUrl());
    setTestParameter(launcherProperties.getBasicAuthEnabled());
    setTestParameter(launcherProperties.getBasicAuthUser());
    setTestParameter(launcherProperties.getBasicAuthPassword());
    setTestParameter(launcherProperties.getOauthServiceUrl());
    setTestParameter(launcherProperties.getOauthClientId());
    setTestParameter(launcherProperties.getOathSecret());
  }

  private void setTestParameter(LauncherProperties.LauncherProperty launcherProperty) {
    if(launcherProperty != null) {
      System.setProperty(launcherProperty.getName(), launcherProperty.getValue());
    }
  }

  public static void main(String[] args) {
     new TestsLauncher(LauncherProperties.fromCommandLineArguments(args)).launch();
  }
}
