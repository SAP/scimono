
package com.sap.scimono.scim.system.tests;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Address;
import com.sap.scimono.entity.Email;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.MemberRef;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.base.ExtensionFieldType;
import com.sap.scimono.entity.definition.CoreGroupAttributes;
import com.sap.scimono.entity.definition.CoreUserAttributes;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.scim.system.tests.extensions.GroupClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.GroupFailSafeClient;
import com.sap.scimono.scim.system.tests.extensions.SchemaClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.SchemaFailSafeClient;
import com.sap.scimono.scim.system.tests.extensions.UserClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.UserFailSafeClient;
import com.sap.scimono.scim.system.tests.util.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.sap.scimono.entity.definition.CoreGroupAttributes.MEMBERS;
import static com.sap.scimono.entity.definition.CoreUserAttributes.ADDRESSES;
import static com.sap.scimono.entity.definition.CoreUserAttributes.DISPLAY_NAME;
import static com.sap.scimono.entity.definition.CoreUserAttributes.EMAILS;
import static com.sap.scimono.entity.definition.CoreUserAttributes.TITLE;
import static com.sap.scimono.entity.definition.CoreUserAttributes.USER_TYPE;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class E2EPatchOperationComplianceTest extends SCIMComplianceTest {
  private static final Logger logger = LoggerFactory.getLogger(E2EPatchOperationComplianceTest.class);

  private static final String USER_TYPE_DEVELOPER = "developer";

  private static Email workMail = TestData.buildWorkEmailWithDefaultAttrs();
  private static Email personalMail = TestData.buildPersonalEmailWithDefaultAttrs();
  private static Address homeAddress = TestData.buildHomeAddressDefaultAttrs();

  @RegisterExtension
  UserClientScimResponseExtension resourceAwareUserRequest = UserClientScimResponseExtension.forClearingAfterEachExecutions(userRequest);

  @RegisterExtension
  GroupClientScimResponseExtension resourceAwareGroupRequest = GroupClientScimResponseExtension.forClearingAfterEachExecutions(groupRequest);

  @RegisterExtension
  SchemaClientScimResponseExtension resourceAwareSchemaRequest = SchemaClientScimResponseExtension.forClearingAfterEachExecutions(schemaRequest);

  private final UserFailSafeClient userFailSafeClient = resourceAwareUserRequest.getFailSafeClient();
  private final GroupFailSafeClient groupFailSafeClient = resourceAwareGroupRequest.getFailSafeClient();
  private final SchemaFailSafeClient schemaFailSafeClient = resourceAwareSchemaRequest.getFailSafeClient();

  @Test
  @DisplayName("Test Add value to attribute with no previous value and verify Http status code: 204")
  public void testAddValueToAttributeWithNoPrevValue204() {
    String testUserName = "testAddValueToAttributeWithNoPrevValue204";
    logger.info("Creating User: {}", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).build());

    logger.info("Fetching User: testAddValueToAttributeWithNoPrevValue204");
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, EMAILS.scimName(),TestData.buildMultivaluedJSONNode(personalMail));
    logger.info("Patching User: {}, adding new email", testUserName);
    SCIMResponse<?> scimResponse = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User Response", getResponseStatusAssertions(scimResponse, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertEquals(1, patchedUser.getEmails().size(), "Verify emails size");
  }

  @Test
  @DisplayName("Test Add address with type and verify Http status code: 204")
  public void testAddAddressWithType204() {
    String testUserName = "testAddAddressWithType204";

    logger.info("Creating User: {}", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, ADDRESSES.scimName(),TestData.buildMultivaluedJSONNode(homeAddress));
    logger.info("Patching User: {}, adding new address", testUserName);
    SCIMResponse<?> scimResponse = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify User Group Response", getResponseStatusAssertions(scimResponse, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertEquals(1, patchedUser.getAddresses().size(), "Verify addresses size");

  }

  @Test
  @DisplayName("Test Add add second value to multivalued attribute and verify Http status code: 204")
  public void testAddSecondValueToMultivalueAttribute204() {
    String testUserName = "testAddSecondValueToMultivalueAttribute204";
    logger.info("Creating User: {} with work email", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser("testAddSecondValueToMultivalueAttribute204").addEmail(workMail).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    assertAll("Verify GET Response assertions: ",
        () -> assertEquals(1, createdUser.getEmails().size(), "Verify emails count"),
        () -> assertEquals(workMail.getType(), createdUser.getEmails().iterator().next().getType(), "Verify email type")
    );

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, EMAILS.scimName(), TestData.buildMultivaluedJSONNode(personalMail));
    logger.info("Patching User: {}, adding new personal email", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response: ", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());
    assertEquals(2, patchedUser.getEmails().size(), "Verify emails count");
  }

  @Test
  @DisplayName("Test Add illegal value to custom attribute and verify Http status code: 400")
  public void testAddIllegalValueToCustomAttribute400() {
    String testUserName = "testAddIllegalValueToCustomAttribute400";
    logger.info("Creating User: {} with work email", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser("testAddIllegalValueToCustomAttribute400").addEmail(workMail).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, EMAILS.scimName(), "SomeIllegalValue");

    logger.info("Patching User: {}, adding illegal value in emails", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response: ", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test add value to custom attribute and verify Http status code: 204")
  public void testAddValueToCustomAttribute204() {
    String customSchemaName = Schema.EXTENSION_SCHEMA_URN + "PatchCustSchema1";
    String customAttr1Name = "stringAttr1";
    String customAttr2Name = "stringAttr2";

    createCustomTestSchema(customSchemaName, customAttr1Name, customAttr2Name);
    Extension customSchema = new Extension.Builder(customSchemaName).setAttribute(customAttr1Name, "meh").build();
    String testUserName = "testAddValueToCustomAttribute204";

    logger.info("Creating User: {} with custom schema attributes", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).addExtension(customSchema).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    String customAttr2Value = "TotalyLegalValue";
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, customSchemaName.concat(customAttr2Name), customAttr2Value);

    logger.info("Patching User: {}, adding new custom schema value", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response: ", getResponseStatusAssertions(response, true, NO_CONTENT));

    User patchedTestUser = userFailSafeClient.getSingle(createdUser.getId());
    assertEquals(patchedTestUser.getExtension(customSchemaName).getAttributes().get(customAttr2Name), customAttr2Value);
  }

  @Test
  @DisplayName("Test Add the same value twice and verify Http status code: 204")
  public void testAddSameValueTwiceToAnAttribute204() {
    String testUserName = "testAddSameValueTwiseToAnAttribute204";
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).addEmail(workMail).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    assertAll("Verify GET Response assertions: ",
        () -> assertEquals(1, createdUser.getEmails().size(), "Verify emails count"),
        () -> assertEquals(workMail.getType(), createdUser.getEmails().iterator().next().getType(), "Verify email type")
    );

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, EMAILS.scimName(), TestData.buildMultivaluedJSONNode(workMail));

    logger.info("Patching User: {}, adding same email again", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertAll("Verify GET Response: ",
        () -> assertEquals(1, patchedUser.getEmails().size(), "Verify emails count"),
        () -> assertEquals(createdUser.getMeta().getVersion(), patchedUser.getMeta().getVersion(), "Verify 'meta.version' is not changed"),
        () -> assertEquals(createdUser.getMeta().getLastModified(), patchedUser.getMeta().getLastModified(), "Verify 'meta.lastModified' is not changed")
    );
  }

  @Test
  @DisplayName("Test Add new value to single value attribute and verify Http status code: 204")
  public void testAddNewValueToSingleValueAttribute204() {
    String testUserName = "testAddNewValueToSingleValueAttribute204";
    logger.info("Creating User: {} with userType: employee", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).setUserType("employee").build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, USER_TYPE.scimName(), USER_TYPE_DEVELOPER);
    logger.info("Patching User: {}, adding new userType: developer - with ADD Patch Operation", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertEquals(USER_TYPE_DEVELOPER, patchedUser.getUserType(), "Verify 'userType' is changed");
  }

  @Test
  @DisplayName("Test Add same value to single value attribute and verify Http status code: 204")
  public void testAddSameValueToSingleValueAttribute204() {
    String testUserName = "testAddSameValueToSingleValueAttribute204";
    logger.info("Creating User: {} with userType: {}", testUserName, USER_TYPE_DEVELOPER);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).setUserType(USER_TYPE_DEVELOPER).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, USER_TYPE.scimName(), USER_TYPE_DEVELOPER);
    logger.info("Patching User: {}, adding same userType", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertAll("Verify GET Response: ",
        () -> assertEquals(USER_TYPE_DEVELOPER, patchedUser.getUserType(), "Verify 'userType' is not changed"),
        () -> assertEquals(createdUser.getMeta().getVersion(), patchedUser.getMeta().getVersion(), "Verify 'meta.version'"),
        () -> assertEquals(createdUser.getMeta().getLastModified(), patchedUser.getMeta().getLastModified(), "Verify 'meta.lastModified'")
    );
  }

  @Test
  @DisplayName("Test Remove value from non existing attribute and verify Http status code: 400")
  public void testRemoveValueFromNonExistingAttribute400() {
    String testUserName = "testRemoveValueFromNonExistingAttribute400";
    logger.info("Creating User: {}", testUserName);
    User createdUser = userFailSafeClient.create(TestData.buildTestUser(testUserName));

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, "NonExistingAttr", null);
    logger.info("Patching User: {}, removing non existing attribute", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Remove attribute value and verify Http status code: 204")
  public void testRemoveAttributeValue204() {
    String testUserName = "testRemoveAttributeValue200";

    logger.info("Creating User: {}", testUserName);
    User createdUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).setUserType(USER_TYPE_DEVELOPER).build());
    assertEquals(USER_TYPE_DEVELOPER, createdUser.getUserType(), "Verify 'userType' in Create User response");

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, USER_TYPE.scimName(), null);
    logger.info("Patching User: {}, removing 'userType'", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertNull(patchedUser.getUserType(), "Verify 'userType' is removed");
  }

  @Test
  @DisplayName("Test Remove multivalued attribute and verify Http status code: 204")
  public void testRemoveMultivaluedAttribute204() {
    Collection<Email> userEmails = new ArrayList<>();
    userEmails.add(workMail);
    userEmails.add(personalMail);

    String testUserName = "testRemoveMultivaluedAttribute204";
    logger.info("Creating User: {} with personal and work email", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).addEmails(userEmails).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());
    assertEquals(2, createdUser.getEmails().size(), "Verify emails count");

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, EMAILS.scimName(), null);
    logger.info("Patching User: {}, removing emails", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertEquals(0, patchedUser.getEmails().size(), "Verify emails count");
  }

  @Test
  @DisplayName("Test Remove readOnly attribute and verify Http status code: 400")
  public void testRemoveReadOnlyAttribute400() {
    String testUserName = "testRemoveReadOnlyAttribute400";
    logger.info("Creating User: {}", testUserName);
    User testUser = userFailSafeClient.create(TestData.buildTestUser(testUserName));
    String createdUserId = testUser.getId();

    String groupDisplayName = "testRemoveReadOnlyAttribute400-Group";
    logger.info("Creating Group: {} with member: {}", groupDisplayName, testUserName);
    Group testGroup = groupFailSafeClient.create(TestData.buildGroup(groupDisplayName, createdUserId));

    logger.info("Fetching Group: {}", groupDisplayName);
    Group createdGroup = groupFailSafeClient.getSingle(testGroup.getId());
    assertEquals(1, createdGroup.getMembers().size(), "Verify members count");

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(createdUserId);

    assertEquals(1, createdUser.getGroups().size(), "Verify groups count");

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, CoreUserAttributes.GROUPS.scimName(), "");
    logger.info("Patching User: {}, removing groups which is readonly attribute", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUserId);

    assertAll("Verify Patch User response", getResponseStatusAssertions(response, false, BAD_REQUEST));

    logger.info("Fetching User: {}", testUserName);
    createdUser = userFailSafeClient.getSingle(createdUserId);

    assertEquals(1, createdUser.getGroups().size(), "Verify groups count");
  }

  @Test
  @DisplayName("Test Remove group members from group and verify Http status code: 204")
  public void testRemoveGroupMembersFromGroup204() {
    String testUserName = "testRemoveGroupMembersFromGroup204";
    User testUser = userFailSafeClient.create(TestData.buildTestUser(testUserName));
    String createdUserId = testUser.getId();

    String firstGroupDisplayName = "testRemoveGroupMembersFromGroup200";
    logger.info("Creating Group: {} with User member: {}", firstGroupDisplayName, testUserName);
    Group testGroup1 = groupFailSafeClient.create(TestData.buildGroup(firstGroupDisplayName, createdUserId));

    String secondGroupDisplayName = "testPatchRemoveGroupMembers213";
    logger.info("Creating Group: {} with User member: {} and Group member: {}", secondGroupDisplayName, testUserName, firstGroupDisplayName);
    Group testGroup2 = groupFailSafeClient.create(TestData.buildGroup(secondGroupDisplayName, createdUserId, testGroup1.getId()));

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, MEMBERS.scimName(), "");
    logger.info("Patching Group: {}, removing members", secondGroupDisplayName);
    SCIMResponse<?> response = resourceAwareGroupRequest.patchGroup(patchBody, testGroup2.getId());
    assertAll("Verify Patch Group response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching Group: {}", secondGroupDisplayName);
    Group patchedGroup = groupFailSafeClient.getSingle(testGroup2.getId());
    assertEquals(0, patchedGroup.getMembers().size(), "Verify second group's members count");
  }

  @Test
  @DisplayName("Test Remove value from custom attribute and verify Http status code: 204")
  public void testRemoveValueToCustomAttribute204() {
    String customSchemaName = Schema.EXTENSION_SCHEMA_URN + "PatchCustSchema2";
    String customAttr1Name = "stringAttr1";
    String customAttr2Name = "stringAttr2";

    createCustomTestSchema(customSchemaName, customAttr1Name, customAttr2Name);

    Extension customSchema = new Extension.Builder(customSchemaName).setAttribute(customAttr1Name, "meh").build();

    String testUserName = "testRemoveValueToCustomAttribute204";
    logger.info("Creating User: {}", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).addExtension(customSchema).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, customSchemaName.concat(customAttr2Name), "");
    logger.info("Patching User: {}", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));
  }

  @Test
  @DisplayName("Test Replace attribute with illegal value and verify Http status code: 400")
  public void testReplaceValueWithIllegalValue400() {
    String testUserName = "testReplaceValueWithIllegalValue400";
    logger.info("Creating User: {} with userType: {}", testUserName, USER_TYPE_DEVELOPER);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).setUserType(USER_TYPE_DEVELOPER).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, USER_TYPE.scimName(), true);
    logger.info("Patching User: {}, replacing 'userType' with illegal value", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, false, BAD_REQUEST));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertEquals(USER_TYPE_DEVELOPER, patchedUser.getUserType(), "Verify 'userType' remains the same");
  }

  @Test
  @DisplayName("Test Replace attribute with legal value and verify Http status code: 204")
  public void testReplaceValueWithLegalValue204() {
    String testUserName = "testReplaceValueWithLegalValue204";

    logger.info("Creating User: {}", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).setUserType("employee").build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, USER_TYPE.scimName(), USER_TYPE_DEVELOPER);
    logger.info("Patching User: {}, replacing 'userType'", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertEquals(USER_TYPE_DEVELOPER, patchedUser.getUserType(), "Verify 'userType'");
  }

  @Test
  @DisplayName("Test Replace attribute with value from different attribute type and verify Http status code: 400")
  public void testReplaceValueWithDifferentAttributeType400() {
    String testUserName = "testReplaceValueWithDifferentAttributeType400";
    logger.info("Creating User: {}", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).setUserType(USER_TYPE_DEVELOPER).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    Email workMailDiplicate = new Email.Builder().setDisplay(workMail.getDisplay()).setPrimary(true).setType(Email.Type.WORK).build();
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, USER_TYPE.scimName(), workMailDiplicate);

    logger.info("Patching User: {}, replacing 'userType' with value from different attribute type", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, false, BAD_REQUEST));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());
    assertEquals(USER_TYPE_DEVELOPER, patchedUser.getUserType(), "Verify 'userType' remains the same");
  }

  @Test
  @DisplayName("Test Replace all mutivalued attribute and verify Http status code: 204")
  public void testReplaceMultiValueAttributes204() {
    Collection<Email> userEmails = new ArrayList<>();
    userEmails.add(workMail);
    userEmails.add(personalMail);

    String testUserName = "testReplaceMultiValueAttributes204";
    logger.info("Creating User: {} with personal and work email", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).addEmails(userEmails).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    Email newMail = new Email.Builder().setDisplay("newMail").setPrimary(true).setType(Email.Type.WORK).setValue("newValue@newValue.com").build();
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, EMAILS.scimName(), TestData.buildMultivaluedJSONNode(newMail));

    logger.info("Patching User: {} replacing all emails with new work email", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertEquals(1, patchedUser.getEmails().size(), "Verify emails count");
    assertEquals(newMail.getValue(), patchedUser.getEmails().iterator().next().getValue(), "Verify email.value");
  }

  @Test
  @DisplayName("Test Replace attribute with illegal attribute type and verify Http status code: 400")
  public void testReplaceValueWithIllegalAttributeType400() {
    String testUserName = "testReplaceValueWithIllegalAttributeType400";
    logger.info("Creating User: {}", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, USER_TYPE.scimName(), workMail);
    logger.info("Patching User: {}, replacing with illegal attribute type", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User response", getResponseStatusAssertions(response, false, BAD_REQUEST));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertNull(patchedUser.getUserType(), "Verify 'userType' remains the same");
  }

  @Test
  @DisplayName("Test Replace multivalued attribute that has no previous value and verify Http status code: 204")
  public void testReplaceMultiValuedAttrThatHasNoPrevValue204() {
    String testUserName = "testReplaceAttrValueThatHasNoPrevValue204";
    logger.info("Creating User: {}", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, EMAILS.scimName(), TestData.buildMultivaluedJSONNode(workMail));
    logger.info("Patching User: {}, replacing emails", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertEquals(1, patchedUser.getEmails().size(), "Verify emails count");
  }

  @Test
  @DisplayName("Test Replace single-valued attribute that has no previous value  and verify Http status code: 204")
  public void testReplaceSingleValuedAttrThatHasNoPrevValue204() {
    String testUserName = "testReplaceAttrValueThatHasNoPrevValue200";
    logger.info("Creating User: {}", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, USER_TYPE.scimName(), USER_TYPE_DEVELOPER);

    logger.info("Patching User: {}, replacing 'userType'", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);

    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertEquals(USER_TYPE_DEVELOPER, patchedUser.getUserType(), "Verify 'userType'");
  }

  @Test
  @DisplayName("Test Replace attribute with empty value and verify Http status code: 204")
  public void testReplaceAttrValueWithEmptyValue204() {
    String testUserName = "testReplaceAttrValueWithEmptyValue204";
    logger.info("Creating User: {}", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).setUserType(USER_TYPE_DEVELOPER).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, USER_TYPE.scimName(), "");
    logger.info("Patching User: {}, replacing 'userType' with empty value", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertEquals(USER_TYPE_DEVELOPER, patchedUser.getUserType(), "Verify 'userType' remains the same");
  }

  @Test
  @DisplayName("Test Replace custom attribute and verify Http status code: 204")
  public void testReplaceValueToCustomAttribute204() {
    String customSchemaName = Schema.EXTENSION_SCHEMA_URN + "PatchCustSchema3";
    String customAttr1Name = "stringAttr1";
    String customAttr2Name = "stringAttr2";

    createCustomTestSchema(customSchemaName, customAttr1Name, customAttr2Name);

    Extension customSchema = new Extension.Builder(customSchemaName).setAttribute(customAttr1Name, "meh").build();

    String testUserName = "testReplaceValueToCustomAttribute204";
    logger.info("Creating User: {}", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).addExtension(customSchema).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    String customAttr1NewValue = "TotalyLegalValue";
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, customSchemaName.concat(customAttr1Name), customAttr1NewValue);

    logger.info("Patching User: {} replace value to custom attribute", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedTestUser = userFailSafeClient.getSingle(createdUser.getId());
    assertEquals(patchedTestUser.getExtension(customSchemaName).getAttributes().get(customAttr1Name), customAttr1NewValue, "Verify custom attribute: " + customAttr1Name);
  }

  @Test
  @DisplayName("Test Patch operation with invalid value path filter and verify Http status code: 400")
  public void testPatchOperationWithInvalidValuePathFilter400() {
    String testUserName = "testReplaceValuesOfMultivaluedAttributesByIncorrectFilter400";
    logger.info("Creating User: {} with work email", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).addEmail(workMail).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    Email newMail = new Email.Builder().setDisplay("newMail").setPrimary(true).setType(Email.Type.WORK).setValue("newValue@newValue.com").build();
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, "emailssssssssss[type eq \"work\"]",
        TestData.buildMultivaluedJSONNode(newMail));

    logger.info("Patching User: {} with invalid value filter", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, false, BAD_REQUEST));
  }

  @Test
  @DisplayName("Test Replace values of multivalued attribute by filter and verify Http status code: 204")
  public void testReplaceValuesOfMultivaluedAttributesByFilter204() {
    Collection<Email> userEmails = new ArrayList<>();
    userEmails.add(workMail);
    userEmails.add(personalMail);

    String testUserName = "testReplaceValuesOfMultivaluedAttributesByFilter204";
    logger.info("Creating User: {} with work email", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).addEmails(userEmails).build());

    logger.info("Fetching User: {} with personal and work email", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    Email newMail = new Email.Builder().setDisplay("newMail").setPrimary(true).setType(Email.Type.HOME).setValue("newValue@newValue.com").build();
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, "emails[type eq \"home\"]",
        TestData.buildMultivaluedJSONNode(newMail));

    logger.info("Patching User: {}, replacing home email", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());
    assertEquals(2, patchedUser.getEmails().size(), "Verify emails count");

    // @formatter:off
    Email newEmailFetched = patchedUser.getEmails().stream()
        .filter(email -> Email.Type.HOME.equals(email.getType()))
        .findAny()
        .orElseThrow(AssertionError::new);
    // @formatter:on

    assertEquals(newMail.getValue(), newEmailFetched.getValue(), "Verify emails.value");
  }

  @Test
  @DisplayName("Test Replace sub-attribute value of multivalued attribute by filter and verify Http status code: 204")
  public void testReplaceSubAttributeValueOfMultivaluedAttributeByFilter204() {
    String testUserName = "testReplaceSubAttributeValueOfMultivaluedAttributeByFilter204";
    logger.info("Creating User: {} with personal email", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).addEmail(personalMail).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    String newEmailValue = "mail@newdomain.com";
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, "emails[type eq \"home\"].value", newEmailValue);
    logger.info("Patching User: {}, replacing emails.value", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertEquals(1, patchedUser.getEmails().size(), "Verify emails count");
    assertEquals(newEmailValue, patchedUser.getEmails().iterator().next().getValue(), "Verify 'emails.value'");
  }

  @Test
  @DisplayName("Test Remove values of multivalued attribute by filter and verify Http status code: 204")
  public void testRemoveValuesOfMultivaluedAttributesByFilter204() {
    Collection<Email> userEmails = new ArrayList<>();
    userEmails.add(workMail);
    userEmails.add(personalMail);

    String testUserName = "testRemoveValuesOfMultivaluedAttributesByFilter204";
    logger.info("Creating User: {} with personal and work emails", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).addEmails(userEmails).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, "emails[type eq \"home\"]", null);
    logger.info("Patching User: {}, removing home emails", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());

    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertEquals(1, patchedUser.getEmails().size(), "Verify emails count");

    assertTrue(patchedUser.getEmails().stream().map(Email::getType).noneMatch(Email.Type.HOME::equals),
        "Removed by value filter emails are not present");
  }

  @Test
  @DisplayName("Test Remove sub-attribute value of multivalued attribute by filter and verify Http status code: 204")
  public void testRemoveSubAttributeValueOfMultivaluedAttributeByFilter204() {
    String testUserName = "testRemoveSubAttributeValueOfMultivaluedAttributeByFilter204";
    logger.info("Creating User: {} with personal email", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).addEmail(personalMail).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, "emails[type eq \"home\"].type", null);
    logger.info("Patching User: {} removing home email type ", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertEquals(1, patchedUser.getEmails().size(), "Verify emails count");
    assertNull(patchedUser.getEmails().iterator().next().getType(), "Verify 'emails.type' is removed");
  }

  @Test
  @DisplayName("Test Add sub-attribute value of multivalued attribute by filter and verify Http status code: 204")
  public void testAddSubAttributeValueOfMultivaluedAttributeByFilter204() {
    String initialEmailValue = "initialEmail@mail.com";
    Email initialEmail = new Email.Builder().setValue(initialEmailValue).build();

    String testUserName = "testAddSubAttributeValueOfMultivaluedAttributeByFilter204";
    logger.info("Creating User: {} with email", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).addEmail(initialEmail).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    Email.Type patchedEmailType = Email.Type.WORK;

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, String.format("emails[value eq \"%s\"].type", initialEmailValue),
        patchedEmailType.toString());

    logger.info("Patching User: {}, adding type for email: {}", testUserName, initialEmailValue);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertEquals(1, patchedUser.getEmails().size(), "Verify emails count");
    assertEquals(patchedEmailType, patchedUser.getEmails().iterator().next().getType(), "Verify 'emails.type'");
  }

  @Test
  @DisplayName("Test Executing PUT Operation after several Patch Operations and verify Http status code: 200")
  public void testExecutingPUTOperationAfterSeveralPatchOperations200(){
    String testUserName = "testExecutingPUTOperationAfterSeveralPatchOperations200";
    logger.info("Creating User: {} with email", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    PatchBody firstPatchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, DISPLAY_NAME.scimName(), "meh1");
    logger.info("Fetching User: {}, adding new displayName", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(firstPatchBody, createdUser.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    PatchBody secondPatchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, USER_TYPE.scimName(), "meh2");
    logger.info("Fetching User: {}, adding new displayName", testUserName);
    response = resourceAwareUserRequest.patchUser(secondPatchBody, createdUser.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    String displayNamePUTUpdate = "meh3UpdatedWithPUT";
    User userForUpdateWithPUT = new User.Builder(createdUser).setDisplayName("meh3UpdatedWithPUT").build();

    logger.info("Updating User: {} with PUT operation", testUserName);
    User updatedUser = userFailSafeClient.update(createdUser.getId(), userForUpdateWithPUT);

    assertEquals(displayNamePUTUpdate, updatedUser.getDisplayName(), "Verify 'displayName'");
    assertNull(updatedUser.getUserType(), "Provided userType by past patch operation must be null");
  }
  @Test
  @DisplayName("Test Change user by multiple Patch operation in one request and verify Http status code: 204")
  public void testChangeUserByMultiplePatchOperationsInOneRequest() {
    String testUserName = "testChangeUserByMultiplePatchOperationsInOneRequest";
    logger.info("Creating User: {}", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).build());

    logger.info("Fetching User: {}", testUserName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    String displayName = "dName";
    String title = "tItle";

    PatchOperation op1 = TestData.buildPatchOperation(PatchOperation.Type.ADD, DISPLAY_NAME.scimName(), displayName);
    PatchOperation op2 = TestData.buildPatchOperation(PatchOperation.Type.ADD, TITLE.scimName(), title);
    PatchOperation op3 = TestData.buildPatchOperation(PatchOperation.Type.ADD, EMAILS.scimName(), TestData.buildMultivaluedJSONNode(personalMail));

    PatchBody patchBody = TestData.buildPatchBody(op1, op2, op3);

    logger.info("Patching User: {} with multiple Patch operations", testUserName);
    SCIMResponse<?> response = resourceAwareUserRequest.patchUser(patchBody, createdUser.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching User: {}", testUserName);
    User patchedUser = userFailSafeClient.getSingle(createdUser.getId());

    assertAll("Verify Patch response",
        () -> assertEquals(displayName, patchedUser.getDisplayName(), "Verify 'displayName'"),
        () -> assertEquals(title, patchedUser.getTitle(), "Verify 'title'"),
        () -> assertEquals(1, patchedUser.getEmails().size(), "Verify emails count")
    );
  }

  @Test
  @DisplayName("Test Change group by multiple Patch operation in one request and verify Http status code: 204")
  public void testChangeGroupByMultiplePatchOperationsInOneRequest() {
    String testUserName = "testChangeGroupByMultiplePatchOperationsInOneRequest-UserMember";
    String groupDisplayName = "testChangeGroupByMultiplePatchOperationsInOneRequest-Group";

    logger.info("Creating Group: {}", groupDisplayName);
    Group testGroup = groupFailSafeClient.create(new Group.Builder().setDisplayName(groupDisplayName).build());
    logger.info("Creating User: {}", testUserName);
    User testUser = userFailSafeClient.create(TestData.setAttributesToATestUser(testUserName).build());

    logger.info("Fetching User: {}", testUserName);
    Group createdGroup = groupFailSafeClient.getSingle(testGroup.getId());
    logger.info("Fetching Group: {}", groupDisplayName);
    User createdUser = userFailSafeClient.getSingle(testUser.getId());

    String firstDisplayName = "dName";
    String secondDisplayName = "dName2";
    MemberRef userRef = new MemberRef.Builder().setValue(createdUser.getId()).setType(MemberRef.Type.USER).build();


    PatchOperation op1 = TestData.buildPatchOperation(PatchOperation.Type.ADD, CoreGroupAttributes.DISPLAY_NAME.scimName(), firstDisplayName);
    PatchOperation op2 = TestData.buildPatchOperation(PatchOperation.Type.REPLACE, CoreGroupAttributes.DISPLAY_NAME.scimName(), secondDisplayName);
    PatchOperation op3 = TestData.buildPatchOperation(PatchOperation.Type.ADD, MEMBERS.scimName(), TestData.buildMemberRefMultivaluedJSONNode(userRef));

    PatchBody patchBody = TestData.buildPatchBody(op1, op2, op3);

    logger.info("Patching Group: {} with multiple Patch operations", groupDisplayName);
    SCIMResponse<?> response = resourceAwareGroupRequest.patchGroup(patchBody, createdGroup.getId());
    assertAll("Verify Patch User response", getResponseStatusAssertions(response, true, NO_CONTENT));

    logger.info("Fetching Group: {}", groupDisplayName);
    Group patchedGroup = groupFailSafeClient.getSingle(createdGroup.getId());

    assertAll("Verify Get Group response",
        () -> assertEquals(patchedGroup.getDisplayName(), secondDisplayName, "Verify 'displayName'"),
        () -> assertEquals(1, patchedGroup.getMembers().size(), "Verify members count")
    );
  }

  private Schema createCustomTestSchema(final String schemaId, final String custAttr1, final String custAttr2) {
    Map<String, ExtensionFieldType<?>> customAttrsNameToType = new HashMap<>();
    customAttrsNameToType.put(custAttr1, ExtensionFieldType.STRING);
    customAttrsNameToType.put(custAttr2, ExtensionFieldType.STRING);
    Schema testSchema = TestData.buildCustomSchemaWithAttrs(schemaId, customAttrsNameToType);

    logger.info("Creating Custom Schema: {}", schemaId);
    return schemaFailSafeClient.create(testSchema);
  }
}
