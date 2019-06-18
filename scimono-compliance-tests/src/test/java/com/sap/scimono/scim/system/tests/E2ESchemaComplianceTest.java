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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sap.scimono.scim.system.tests.util.TestData.buildTestSchema;
import static com.sap.scimono.scim.system.tests.util.TestUtil.constructResourceLocation;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class E2ESchemaComplianceTest extends SCIMComplianceTest {
  private static final String TEST_SCHEMA_ID = Schema.EXTENSION_SCHEMA_URN + "Test";
  private static final List<String> scimPredefinedCoreSchemaIds = Arrays.asList(User.SCHEMA, Group.SCHEMA, EnterpriseExtension.ENTERPRISE_URN,
      ServiceProviderConfig.SCHEMA, ResourceType.SCHEMA, Schema.SCHEMA);

  @Test
  public void test1GetCoreSchemas() {
    List<Schema> allSchemas = getAllSchemas();
    assertNotNull(allSchemas);

    Set<String> allSchemaIds = allSchemas.stream().map(Schema::getId).collect(Collectors.toSet());
    assertTrue(allSchemaIds.containsAll(scimPredefinedCoreSchemaIds));
  }

  @Test
  public void test2CreateSchema() {
    Schema customSchema = createSchema(buildTestSchema(TEST_SCHEMA_ID));
    assertNotNull(customSchema);

    assertEquals(TEST_SCHEMA_ID, customSchema.getId());
    assertEquals("Test", customSchema.getName());
    assertEquals(2, customSchema.getAttributes().size());

    assertNotNull(customSchema.getMeta());
    assertEquals(Schema.RESOURCE_TYPE_SCHEMA, customSchema.getMeta().getResourceType());

    assertTrue(customSchema.getMeta().getLocation().endsWith(constructResourceLocation(customSchema)));
  }

  @Test
  public void test3GetSchema() {
    Schema customSchema = getSchema(TEST_SCHEMA_ID);
    assertNotNull(customSchema);
    assertEquals(TEST_SCHEMA_ID, customSchema.getId());
  }

  @Test
  public void test4GetAllSchemas() {
    List<Schema> allSchemas = getAllSchemas();
    assertNotNull(allSchemas);

    boolean testSchemaExists = allSchemas.stream().anyMatch(schema -> TEST_SCHEMA_ID.equals(schema.getId()));
    assertTrue(testSchemaExists);
  }

  @Test
  public void test5DeleteSchema() throws InterruptedException {
    Schema testSchema = getSchema(TEST_SCHEMA_ID);
    assertNotNull(testSchema);

    SCIMResponse<Void> scimResponse = schemaRequest.deleteSchema(TEST_SCHEMA_ID);
    assertEquals(SC_NO_CONTENT, scimResponse.getStatusCode());

    Thread.sleep(6 * 1000);

    SCIMResponse<Schema> getSchemaResponse = schemaRequest.readSingleSchema(TEST_SCHEMA_ID);
    assertFalse(getSchemaResponse.isSuccess());
  }
}
