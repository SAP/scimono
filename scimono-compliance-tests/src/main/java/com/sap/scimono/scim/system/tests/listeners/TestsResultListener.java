package com.sap.scimono.scim.system.tests.listeners;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;

public class TestsResultListener implements TestExecutionListener {
	
	private boolean anyTestFailed = false;
	
	
	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (!testIdentifier.isTest()) {
			return;
		}
		
		if (FAILED == testExecutionResult.getStatus()) {
			anyTestFailed = true;
		}
	}
	
	
	public boolean isAnyTestFailed() {
		return anyTestFailed;
	}
	
}
