package com.sap.scimono.scim.system.tests.conditions;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(GroupsEndpointCondition.class)
public @interface EnableOnGroupsBackendState {
  BackendState state();
}
