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
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.scim.system.tests.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class E2ECustomAttributesIntegrationTest extends CompliantSCIMSystemIntegrationTest {

  private static boolean setupDone;

  private static final String FIRST_CUSTOM_SCHEMA_ID = Schema.EXTENSION_SCHEMA_URN + "FirstCustAttr";
  private static final String SECOND_CUSTOM_SCHEMA_ID = Schema.EXTENSION_SCHEMA_URN + "SecondCustAttr";
  private static final String THIRD_CUSTOM_SCHEMA_ID = Schema.EXTENSION_SCHEMA_URN + "ThirdCustAttr";
  private static final String FOURTH_CUSTOM_SCHEMA_ID = Schema.EXTENSION_SCHEMA_URN + "FourthCustAttr";
  private static final String CUSTOM_ATTR_DISPLAYNAME = "customDisplayName";
  private static final String CUSTOM_ATTR_INTERNALID = "internalId";

  private static List<Schema> testCustomSchemas = new ArrayList<>();

  @BeforeEach
  public void setUpBeforeTest() {
    if (!setupDone) {
      setupDone = true;

      testCustomSchemas.add(createCustomTestSchema(FIRST_CUSTOM_SCHEMA_ID, CUSTOM_ATTR_DISPLAYNAME, CUSTOM_ATTR_INTERNALID));
      testCustomSchemas.add(createCustomTestSchema(SECOND_CUSTOM_SCHEMA_ID, CUSTOM_ATTR_DISPLAYNAME, CUSTOM_ATTR_INTERNALID));
      testCustomSchemas.add(createCustomTestSchema(THIRD_CUSTOM_SCHEMA_ID, CUSTOM_ATTR_DISPLAYNAME, CUSTOM_ATTR_INTERNALID));
      testCustomSchemas.add(createCustomTestSchema(FOURTH_CUSTOM_SCHEMA_ID, CUSTOM_ATTR_DISPLAYNAME, CUSTOM_ATTR_INTERNALID));

      for (Schema customSchema : testCustomSchemas) {
        SCIMResponse<Schema> schemaResponse = schemaRequest.readSingleSchema(customSchema.getId());

        assertEquals(OK.getStatusCode(), schemaResponse.getStatusCode());
        assertEquals(customSchema.getId(), schemaResponse.get().getId());
      }
    }
  }



  @Test
  public void testCreateUserCustomAttributes() {
    createUserCustomAttributes("testCreateUserCustomAttributes", FIRST_CUSTOM_SCHEMA_ID);
  }

  @Test
  public void testCreateReadUserCustomAttributes() {
    User createdUser = createUserCustomAttributes("testCreateReadUserCustomAttributes", FIRST_CUSTOM_SCHEMA_ID);
    User returnedUser = getUser(createdUser.getId());

    Arrays.stream(new String[] {FIRST_CUSTOM_SCHEMA_ID}).forEach(customSchemaId -> {
      Extension createdCustomAttributes = returnedUser.getExtension(customSchemaId);
      assertNotNull(createdCustomAttributes);
      assertEquals(customSchemaId, createdCustomAttributes.getUrn());
      assertEquals("BigTopalka", createdCustomAttributes.getAttributeValueAsString(CUSTOM_ATTR_DISPLAYNAME));
      assertEquals("I071825", createdCustomAttributes.getAttributeValueAsString(CUSTOM_ATTR_INTERNALID));
    });
  }

  @Test
  public void testUpdateUserCustomAttributes() {
    User createdUser = createUserCustomAttributes("userWithCustomAttributes", FIRST_CUSTOM_SCHEMA_ID);

    User.Builder updatedUserBuilder = new User.Builder(createdUser);
    Map<String, Object> customAttributes = new HashMap<>();
    customAttributes.put(CUSTOM_ATTR_DISPLAYNAME, "UpdatedDisplayName");
    customAttributes.put(CUSTOM_ATTR_INTERNALID, "UpdatedInternalId");
    updatedUserBuilder.addExtension(new Extension.Builder(FIRST_CUSTOM_SCHEMA_ID).setAttributes(customAttributes).build());

    User returnedUser = updateUser(createdUser.getId(), updatedUserBuilder.build());
    assertNotNull(returnedUser);

    Arrays.stream(new String[] {FIRST_CUSTOM_SCHEMA_ID}).forEach(customSchemaId -> {
      Extension createdCustomAttributes = returnedUser.getExtension(customSchemaId);
      assertNotNull(createdCustomAttributes);
      assertEquals(customSchemaId, createdCustomAttributes.getUrn());
      assertEquals("UpdatedDisplayName", createdCustomAttributes.getAttributeValueAsString(CUSTOM_ATTR_DISPLAYNAME));
      assertEquals("UpdatedInternalId", createdCustomAttributes.getAttributeValueAsString(CUSTOM_ATTR_INTERNALID));
    });
  }

  @Test
  public void testUpdateReadUserCustomAttributes() {
    User createdUser = createUserCustomAttributes("testUpdateReadUserCustomAttributes", FIRST_CUSTOM_SCHEMA_ID);

    User.Builder updatedUserBuilder = new User.Builder(createdUser);
    Map<String, Object> customAttributes = new HashMap<>();
    customAttributes.put(CUSTOM_ATTR_DISPLAYNAME, "UpdatedDisplayName");
    customAttributes.put(CUSTOM_ATTR_INTERNALID, "UpdatedInternalId");
    updatedUserBuilder.addExtension(new Extension.Builder(FIRST_CUSTOM_SCHEMA_ID).setAttributes(customAttributes).build());

    updateUser(createdUser.getId(), updatedUserBuilder.build());

    User returnedUser = getUser(createdUser.getId());
    assertNotNull(returnedUser);

    Arrays.stream(new String[] {FIRST_CUSTOM_SCHEMA_ID}).forEach(customSchemaId -> {
      Extension createdCustomAttributes = returnedUser.getExtension(customSchemaId);
      assertNotNull(createdCustomAttributes);
      assertEquals(customSchemaId, createdCustomAttributes.getUrn());
      assertEquals("UpdatedDisplayName", createdCustomAttributes.getAttributeValueAsString(CUSTOM_ATTR_DISPLAYNAME));
      assertEquals("UpdatedInternalId", createdCustomAttributes.getAttributeValueAsString(CUSTOM_ATTR_INTERNALID));
    });
  }

  @Test
  public void testGetUsersFilteredCustomAttribute() {
    createUserCustomAttributes("testUserFilterCustomAttribute", FIRST_CUSTOM_SCHEMA_ID);

    String customAttrFilter = FIRST_CUSTOM_SCHEMA_ID + ":" + CUSTOM_ATTR_INTERNALID + " eq \"I071825\"";
    List<User> fetchedUsers = getUsersFiltered(customAttrFilter);
    assertNotNull(fetchedUsers);
    assertEquals(3, fetchedUsers.size());
  }

  @Test
  public void testGetUsersFilteredCustomAttributes() {
    User testUser = createUserCustomAttributes("testUserFilterCustomAttributes", SECOND_CUSTOM_SCHEMA_ID, THIRD_CUSTOM_SCHEMA_ID);

    String firstCustomAttrFilter = SECOND_CUSTOM_SCHEMA_ID + ":" + CUSTOM_ATTR_INTERNALID + " eq \"I071825\"";
    String secondCustomAttrFilter = THIRD_CUSTOM_SCHEMA_ID + ":" + CUSTOM_ATTR_DISPLAYNAME + " eq \"BigTopalka\"";
    List<User> fetchedUsers = getUsersFiltered(firstCustomAttrFilter + " and " + secondCustomAttrFilter);
    assertNotNull(fetchedUsers);
    assertEquals(1, fetchedUsers.size());
    assertEquals(testUser, fetchedUsers.get(0));
  }

  @Test
  public void testDeleteCustomSchemaValuesGone() {
    User testUser = createUserCustomAttributes("testDeleteCustomSchemaValuesGone", FOURTH_CUSTOM_SCHEMA_ID);

    deleteTestCustomSchema(FOURTH_CUSTOM_SCHEMA_ID);

    User user = getUser(testUser.getId());
    assertNotNull(user);
    assertTrue(user.getExtensions().isEmpty());
  }

  private User createUserCustomAttributes(final String userName, final String... customSchemaIds) {
    List<Extension> userCustomAttributes = TestData.createTestCustomAttribues(CUSTOM_ATTR_DISPLAYNAME, CUSTOM_ATTR_INTERNALID, customSchemaIds);
    User userToCreate = TestData.buildTestUserWithExtentions(userName, userCustomAttributes);

    User testUser = createUser(userToCreate);
    assertNotNull(testUser);

    Arrays.stream(customSchemaIds).forEach(customSchemaId -> {
      Extension createdCustomAttributes = testUser.getExtension(customSchemaId);
      assertNotNull(createdCustomAttributes);
      assertEquals(customSchemaId, createdCustomAttributes.getUrn());
      assertEquals("BigTopalka", createdCustomAttributes.getAttributeValueAsString(CUSTOM_ATTR_DISPLAYNAME));
      assertEquals("I071825", createdCustomAttributes.getAttributeValueAsString(CUSTOM_ATTR_INTERNALID));
    });

    return testUser;
  }

  private void deleteTestCustomSchema(final String customSchemaId) {
    deleteSchema(customSchemaId);
    testCustomSchemas.remove(testCustomSchemas.stream().filter(schema -> schema.getId().equals(customSchemaId)).findFirst().get());
  }

}
