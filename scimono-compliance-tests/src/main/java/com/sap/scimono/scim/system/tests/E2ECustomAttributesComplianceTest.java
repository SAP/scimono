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

import com.sap.scimono.entity.User;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.base.ExtensionFieldType;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.scim.system.tests.extensions.SchemaClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.SchemaFailSafeClient;
import com.sap.scimono.scim.system.tests.extensions.UserClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.UserFailSafeClient;
import com.sap.scimono.scim.system.tests.util.TestData;
import com.sap.scimono.scim.system.tests.util.TestProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class E2ECustomAttributesComplianceTest extends SCIMComplianceTest {
  private static final Logger logger = LoggerFactory.getLogger(E2ECustomAttributesComplianceTest.class);

  @RegisterExtension
  static SchemaClientScimResponseExtension resourceAwareSchemaRequest = SchemaClientScimResponseExtension
      .forClearingAfterAllExecutions(SCIMComplianceTest.configureScimClientService(TestProperties.SERVICE_URL).buildSchemaRequest());

  @RegisterExtension
  UserClientScimResponseExtension resourceAwareUserRequest = UserClientScimResponseExtension.forClearingAfterEachExecutions(userRequest);

  private static final String FIRST_CUSTOM_SCHEMA_ID = Schema.EXTENSION_SCHEMA_URN + "FirstCustAttr";
  private static final String SECOND_CUSTOM_SCHEMA_ID = Schema.EXTENSION_SCHEMA_URN + "SecondCustAttr";
  private static final String THIRD_CUSTOM_SCHEMA_ID = Schema.EXTENSION_SCHEMA_URN + "ThirdCustAttr";
  private static final String FOURTH_CUSTOM_SCHEMA_ID = Schema.EXTENSION_SCHEMA_URN + "FourthCustAttr";
  private static final String CUSTOM_ATTR_DISPLAYNAME = "customDisplayName";
  private static final String CUSTOM_ATTR_INTERNALID = "internalId";
  private static final SchemaFailSafeClient schemaFailSafeClient = resourceAwareSchemaRequest.getFailSafeClient();

  private final UserFailSafeClient userFailsSafeClient = resourceAwareUserRequest.getFailSafeClient();

  @BeforeAll
  public static void setup() {
    createCustomTestSchema(FIRST_CUSTOM_SCHEMA_ID, CUSTOM_ATTR_DISPLAYNAME, CUSTOM_ATTR_INTERNALID);
    createCustomTestSchema(SECOND_CUSTOM_SCHEMA_ID, CUSTOM_ATTR_DISPLAYNAME, CUSTOM_ATTR_INTERNALID);
    createCustomTestSchema(THIRD_CUSTOM_SCHEMA_ID, CUSTOM_ATTR_DISPLAYNAME, CUSTOM_ATTR_INTERNALID);
  }

  @Test
  @DisplayName("Test Create user with custom attributes")
  public void testCreateUserCustomAttributes() {
    createUserCustomAttributes("testCreateUserCustomAttributes", FIRST_CUSTOM_SCHEMA_ID);
  }

  @Test
  @DisplayName("Test Create user with custom attributes and verify Get response")
  public void testCreateReadUserCustomAttributes() {
    User createdUser = createUserCustomAttributes("testCreateReadUserCustomAttributes", FIRST_CUSTOM_SCHEMA_ID);
    User returnedUser = userFailsSafeClient.getSingle(createdUser.getId());

    // @formatter:off
    Arrays.stream(new String[] {FIRST_CUSTOM_SCHEMA_ID}).forEach(customSchemaId -> {
      Extension createdCustomAttributes = returnedUser.getExtension(customSchemaId);
      assertNotNull(createdCustomAttributes);
      assertEquals(customSchemaId, createdCustomAttributes.getUrn());
      assertEquals("BigTopalka", createdCustomAttributes.getAttributeValueAsString(CUSTOM_ATTR_DISPLAYNAME));
      assertEquals("I071825", createdCustomAttributes.getAttributeValueAsString(CUSTOM_ATTR_INTERNALID));
    });
    // @formatter:on
  }

  @Test
  @DisplayName("Test Update user with custom attributes with PUT")
  public void testUpdateUserCustomAttributes() {
    User createdUser = createUserCustomAttributes("userWithCustomAttributes", FIRST_CUSTOM_SCHEMA_ID);

    User.Builder updatedUserBuilder = new User.Builder(createdUser);
    Map<String, Object> customAttributes = new HashMap<>();
    customAttributes.put(CUSTOM_ATTR_DISPLAYNAME, "UpdatedDisplayName");
    customAttributes.put(CUSTOM_ATTR_INTERNALID, "UpdatedInternalId");
    updatedUserBuilder.addExtension(new Extension.Builder(FIRST_CUSTOM_SCHEMA_ID).setAttributes(customAttributes).build());

    logger.info("Updating User: {}, adding custom schema attributes", createdUser.getUserName());
    User returnedUser = userFailsSafeClient.update(createdUser.getId(), updatedUserBuilder.build());

    // @formatter:off
    Arrays.stream(new String[] {FIRST_CUSTOM_SCHEMA_ID}).forEach(customSchemaId -> {
      Extension createdCustomAttributes = returnedUser.getExtension(customSchemaId);
      assertNotNull(createdCustomAttributes);
      assertEquals(customSchemaId, createdCustomAttributes.getUrn());
      assertEquals("UpdatedDisplayName", createdCustomAttributes.getAttributeValueAsString(CUSTOM_ATTR_DISPLAYNAME));
      assertEquals("UpdatedInternalId", createdCustomAttributes.getAttributeValueAsString(CUSTOM_ATTR_INTERNALID));
    });
    // @formatter:on
  }

  @Test
  @DisplayName("Test Update user with custom attributes with PUT and verify Get response")
  public void testUpdateReadUserCustomAttributes() {
    User createdUser = createUserCustomAttributes("testUpdateReadUserCustomAttributes", FIRST_CUSTOM_SCHEMA_ID);

    User.Builder updatedUserBuilder = new User.Builder(createdUser);
    Map<String, Object> customAttributes = new HashMap<>();
    customAttributes.put(CUSTOM_ATTR_DISPLAYNAME, "UpdatedDisplayName");
    customAttributes.put(CUSTOM_ATTR_INTERNALID, "UpdatedInternalId");
    updatedUserBuilder.addExtension(new Extension.Builder(FIRST_CUSTOM_SCHEMA_ID).setAttributes(customAttributes).build());

    String createdUserName = createdUser.getUserName();
    logger.info("Updating User: {}, add custom schema attributes", createdUserName);
    userFailsSafeClient.update(createdUser.getId(), updatedUserBuilder.build());

    logger.info("Fetching User: {}", createdUserName);
    User returnedUser = userFailsSafeClient.getSingle(createdUser.getId());

    // @formatter:off
    Arrays.stream(new String[] {FIRST_CUSTOM_SCHEMA_ID}).forEach(customSchemaId -> {
      Extension createdCustomAttributes = returnedUser.getExtension(customSchemaId);
      assertNotNull(createdCustomAttributes);
      assertEquals(customSchemaId, createdCustomAttributes.getUrn());
      assertEquals("UpdatedDisplayName", createdCustomAttributes.getAttributeValueAsString(CUSTOM_ATTR_DISPLAYNAME));
      assertEquals("UpdatedInternalId", createdCustomAttributes.getAttributeValueAsString(CUSTOM_ATTR_INTERNALID));
    });
    // @formatter:on
  }

  @Test
  @DisplayName("Test Get users filtered by custom attribute")
  public void testGetUsersFilteredCustomAttribute() {
    logger.info("Creating User: testGetUsersFilteredCustomAttribute, without custom attributes");
    userFailsSafeClient.create(TestData.buildTestUser("testGetUsersFilteredCustomAttribute-NoCustomSchema"));

    createUserCustomAttributes("testUserFilterCustomAttribute-1", FIRST_CUSTOM_SCHEMA_ID);
    createUserCustomAttributes("testGetUsersFilteredCustomAttribute-2", FIRST_CUSTOM_SCHEMA_ID);
    createUserCustomAttributes("testGetUsersFilteredCustomAttribute-3", FIRST_CUSTOM_SCHEMA_ID);

    String customAttrFilter = FIRST_CUSTOM_SCHEMA_ID + ":" + CUSTOM_ATTR_INTERNALID + " eq \"I071825\"";

    logger.info("Fetching User with custom attribute filter: {}", customAttrFilter);
    List<User> fetchedUsers = userFailsSafeClient.getAllByFilter(customAttrFilter);

    assertEquals(3, fetchedUsers.size(), "Verify fetched Users count");
  }

  @Test
  @DisplayName("Test Get users filtered by multiple custom attributes")
  public void testGetUsersFilteredCustomAttributes() {
    User testUser = createUserCustomAttributes("testUserFilterCustomAttributes", SECOND_CUSTOM_SCHEMA_ID, THIRD_CUSTOM_SCHEMA_ID);

    String firstCustomAttrFilter = SECOND_CUSTOM_SCHEMA_ID + ":" + CUSTOM_ATTR_INTERNALID + " eq \"I071825\"";
    String secondCustomAttrFilter = THIRD_CUSTOM_SCHEMA_ID + ":" + CUSTOM_ATTR_DISPLAYNAME + " eq \"BigTopalka\"";

    logger.info("Fetching User with multiple custom attributes filters: {} | {}", firstCustomAttrFilter, secondCustomAttrFilter);
    List<User> fetchedUsers = userFailsSafeClient.getAllByFilter(firstCustomAttrFilter + " and " + secondCustomAttrFilter);

    assertEquals(1, fetchedUsers.size());
    assertEquals(testUser, fetchedUsers.get(0));
  }

  @Test
  @DisplayName("Test Delete custom schema and verify values from the resource are disappeared")
  public void testDeleteCustomSchemaValuesGone() {
    createCustomTestSchema(FOURTH_CUSTOM_SCHEMA_ID, CUSTOM_ATTR_DISPLAYNAME, CUSTOM_ATTR_INTERNALID);
    User testUser = createUserCustomAttributes("testDeleteCustomSchemaValuesGone", FOURTH_CUSTOM_SCHEMA_ID);

    logger.info("Deleting custom schema: {}", FOURTH_CUSTOM_SCHEMA_ID);
    schemaFailSafeClient.delete(FOURTH_CUSTOM_SCHEMA_ID);

    logger.info("Fetching User: {}", testUser.getUserName());
    User user = userFailsSafeClient.getSingle(testUser.getId());

    assertNotNull(user);
    assertTrue(user.getExtensions().isEmpty());
  }

  private User createUserCustomAttributes(final String userName, final String... customSchemaIds) {
    List<Extension> userCustomAttributes = TestData.createTestCustomAttribues(CUSTOM_ATTR_DISPLAYNAME, CUSTOM_ATTR_INTERNALID, customSchemaIds);
    User userToCreate = TestData.buildTestUserWithExtentions(userName, userCustomAttributes);

    logger.info("Creating User: {}, with custom attributes", userName);
    User testUser = userFailsSafeClient.create(userToCreate);

    // @formatter:off
    Arrays.stream(customSchemaIds).forEach(customSchemaId -> {
      Extension createdCustomAttributes = testUser.getExtension(customSchemaId);
      assertNotNull(createdCustomAttributes);
      assertEquals(customSchemaId, createdCustomAttributes.getUrn());
      assertEquals("BigTopalka", createdCustomAttributes.getAttributeValueAsString(CUSTOM_ATTR_DISPLAYNAME));
      assertEquals("I071825", createdCustomAttributes.getAttributeValueAsString(CUSTOM_ATTR_INTERNALID));
    });
    // @formatter:on

    return testUser;
  }

  private static Schema createCustomTestSchema(final String schemaId, final String custAttr1, final String custAttr2) {
    Map<String, ExtensionFieldType<?>> customAttrsNameToType = new HashMap<>();
    customAttrsNameToType.put(custAttr1, ExtensionFieldType.STRING);
    customAttrsNameToType.put(custAttr2, ExtensionFieldType.STRING);
    Schema testSchema = TestData.buildCustomSchemaWithAttrs(schemaId, customAttrsNameToType);

    logger.info("Creating custom Schema: {}", schemaId);
    return schemaFailSafeClient.create(testSchema);
  }

}
