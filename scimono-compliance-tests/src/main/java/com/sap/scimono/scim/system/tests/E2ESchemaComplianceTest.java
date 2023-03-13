/**
 * Copyright (c) 2016 by SAP Labs Bulgaria, url: http://www.sap.com All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP SE, Walldorf. You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered into with SAP.
 *
 * Created on Dec 16, 2016 by i061675
 *
 */

package com.sap.scimono.scim.system.tests;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.EnterpriseExtension;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.config.ServiceProviderConfig;
import com.sap.scimono.entity.schema.ResourceType;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.scim.system.tests.extensions.SchemaClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.SchemaFailSafeClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sap.scimono.scim.system.tests.util.TestData.buildTestSchema;
import static com.sap.scimono.scim.system.tests.util.TestUtil.constructResourceLocation;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class E2ESchemaComplianceTest extends SCIMComplianceTest {
  private static final String TEST_SCHEMA_ID = Schema.EXTENSION_SCHEMA_URN + "Test";
  private static final List<String> scimPredefinedCoreSchemaIds = Arrays.asList(User.SCHEMA, Group.SCHEMA, EnterpriseExtension.ENTERPRISE_URN,
      ServiceProviderConfig.SCHEMA, ResourceType.SCHEMA, Schema.SCHEMA);

  private static final Logger logger = LoggerFactory.getLogger(E2ESchemaComplianceTest.class);

  @RegisterExtension
  SchemaClientScimResponseExtension resourceAwareSchemaRequest = SchemaClientScimResponseExtension.forClearingAfterEachExecutions(schemaRequest);

  private final SchemaFailSafeClient schemaFailSafeClient = resourceAwareSchemaRequest.getFailSafeClient();

  @Test
  @DisplayName("Test Get core schemas")
  public void test1GetCoreSchemas() {
    logger.info("Fetching all Schemas");
    List<Schema> allSchemas = schemaFailSafeClient.getAllWithIndexPaging();

    Set<String> allSchemaIds = allSchemas.stream().map(Schema::getId).collect(Collectors.toSet());
    assertTrue(allSchemaIds.containsAll(scimPredefinedCoreSchemaIds), "Verify all core schemas are present in the response");
  }

  @Test
  @DisplayName("Test Create schema")
  public void test2CreateSchema() {
    logger.info("Creating custom Schema: {}", TEST_SCHEMA_ID);
    Schema customSchema = schemaFailSafeClient.create(buildTestSchema(TEST_SCHEMA_ID));

    assertAll("Verify Create Schema Response",
        () -> assertEquals(TEST_SCHEMA_ID, customSchema.getId(), "Verify schema id"),
        () -> assertEquals("Test", customSchema.getName(), "Verify schema name"),
        () -> assertEquals(2, customSchema.getAttributes().size(), "Verify attributes count"),
        () -> assertNotNull(customSchema.getMeta(), "Verify meta")
    );

    assertAll("Verify meta attributes",
        () -> assertEquals(Schema.RESOURCE_TYPE_SCHEMA, customSchema.getMeta().getResourceType(), "'Verify meta.resourceType'"),
        () -> assertTrue(customSchema.getMeta().getLocation().endsWith(constructResourceLocation(customSchema)), "Verify 'meta.location'")
    );
  }

  @Test
  @DisplayName("Test Get schema")
  public void test3GetSchema() {
    logger.info("Creating custom Schema: {}", TEST_SCHEMA_ID);
    schemaFailSafeClient.create(buildTestSchema(TEST_SCHEMA_ID));

    logger.info("Fetching custom Schema: {}", TEST_SCHEMA_ID);
    Schema customSchema = schemaFailSafeClient.getSingle(TEST_SCHEMA_ID);

    assertEquals(TEST_SCHEMA_ID, customSchema.getId(), "Verify schema id");
  }

  @Test
  @DisplayName("Test Get all schemas")
  public void test4GetAllSchemas() {
    logger.info("Creating custom Schema: {}", TEST_SCHEMA_ID);
    schemaFailSafeClient.create(buildTestSchema(TEST_SCHEMA_ID));

    logger.info("Fetching all schemas");
    List<Schema> allSchemas = schemaFailSafeClient.getAllWithIndexPaging();

    boolean testSchemaExists = allSchemas.stream().anyMatch(schema -> TEST_SCHEMA_ID.equals(schema.getId()));
    assertTrue(testSchemaExists, "Verify schema exist in the response");
  }

  @Test
  @DisplayName("Test Delete schema")
  public void test5DeleteSchema() throws InterruptedException {
    logger.info("Creating custom Schema: {}", TEST_SCHEMA_ID);
    schemaFailSafeClient.create(buildTestSchema(TEST_SCHEMA_ID));

    logger.info("Fetching custom Schema: {}", TEST_SCHEMA_ID);
    schemaFailSafeClient.getSingle(TEST_SCHEMA_ID);

    logger.info("Deleting custom Schema: {}", TEST_SCHEMA_ID);
    SCIMResponse<Void> scimResponse = resourceAwareSchemaRequest.deleteSchema(TEST_SCHEMA_ID);

    assertAll("Verify Delete Schema response", getResponseStatusAssertions(scimResponse, true, NO_CONTENT));

    Thread.sleep(6 * 1000);

    logger.info("Fetching custom Schema: {}", TEST_SCHEMA_ID);
    SCIMResponse<Schema> getSchemaResponse = resourceAwareSchemaRequest.readSingleSchema(TEST_SCHEMA_ID);

    assertAll("Verify Delete Schema response", getResponseStatusAssertions(getSchemaResponse, false, NOT_FOUND));
  }
}
