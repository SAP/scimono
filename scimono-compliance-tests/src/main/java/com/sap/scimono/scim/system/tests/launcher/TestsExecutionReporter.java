package com.sap.scimono.scim.system.tests.launcher;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.platform.engine.TestExecutionResult.Status.ABORTED;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL;

public class TestsExecutionReporter implements TestExecutionListener {
  private static final Logger logger = LoggerFactory.getLogger(TestsExecutionReporter.class);
  private final Map<TestExecutionResult.Status, Collection<String>> allTestsResult = new EnumMap<>(TestExecutionResult.Status.class);

  @Override
  public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
    if(!testIdentifier.isTest()) {
      return;
    }

    String testExecutionResultString = testExecutionResult.toString();
    logger.info("Test: {} finished | Status: {} | Result: {}", testIdentifier.getDisplayName(), testExecutionResult.getStatus(), testExecutionResultString);

    if(FAILED == testExecutionResult.getStatus() && testExecutionResult.getThrowable().isPresent()){
      logger.error("Test: {} failed | Cause: ", testIdentifier.getDisplayName(), testExecutionResult.getThrowable().get());
    }

    allTestsResult.computeIfAbsent(testExecutionResult.getStatus(), status -> new ArrayList<>());
    allTestsResult.get(testExecutionResult.getStatus()).add(testIdentifier.getDisplayName());
  }

  @Override
  public void testPlanExecutionFinished(TestPlan testPlan) {
    long allTestsCount = allTestsResult.values().stream().mapToLong(Collection::size).sum();
    logger.info("All tests: {}", allTestsCount);

    Stream.of(SUCCESSFUL, ABORTED, FAILED).forEach(status -> {
      Collection<String> tests = allTestsResult.getOrDefault(status, new ArrayList<>());
      logger.info("{} tests: {}", status, tests.size());

      tests.forEach(testName -> logger.info("Test: {} - {}", testName, status));
    });
  }
}
