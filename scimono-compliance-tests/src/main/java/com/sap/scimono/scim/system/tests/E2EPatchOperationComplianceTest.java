
package com.sap.scimono.scim.system.tests;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Address;
import com.sap.scimono.entity.Email;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.MemberRef;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.definition.CoreGroupAttributes;
import com.sap.scimono.entity.definition.CoreUserAttributes;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.scim.system.tests.util.TestData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static com.sap.scimono.entity.definition.CoreGroupAttributes.MEMBERS;
import static com.sap.scimono.entity.definition.CoreUserAttributes.ADDRESSES;
import static com.sap.scimono.entity.definition.CoreUserAttributes.DISPLAY_NAME;
import static com.sap.scimono.entity.definition.CoreUserAttributes.EMAILS;
import static com.sap.scimono.entity.definition.CoreUserAttributes.TITLE;
import static com.sap.scimono.entity.definition.CoreUserAttributes.USER_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class E2EPatchOperationComplianceTest extends SCIMComplianceTest {
  private static final String USER_TYPE_DEVELOPER = "developer";

  private static Email workMail = TestData.buildWorkEmailWithDefaultAttrs();
  private static Email personalMail = TestData.buildPersonalEmailWithDefaultAttrs();
  private static Address homeAddress = TestData.buildHomeAddressDefaultAttrs();

  // ignore until the patch implementation is ready
  @Test
  public void testAddValueToAttributeWithNoPrevValue204() {
    User testUser = createUser(TestData.setAttributesToATestUser("testAddValueToAttributeWithNoPrevValue204").build());
    User createdUser = getUser(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, EMAILS.scimName(),TestData.buildMultivaluedJSONNode(personalMail));
    SCIMResponse<?> scimResponse = userRequest.patchUser(patchBody, createdUser.getId());

    assertTrue(scimResponse.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), scimResponse.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertEquals(1, patchedUser.getEmails().size());
  }

  @Test
  public void testAddAddressWithType204() {
    User testUser = createUser(TestData.setAttributesToATestUser("testAddAddressWithType204").build());
    User createdUser = getUser(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, ADDRESSES.scimName(),TestData.buildMultivaluedJSONNode(homeAddress));

    SCIMResponse<?> scimResponse = userRequest.patchUser(patchBody, createdUser.getId());

    assertTrue(scimResponse.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), scimResponse.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertEquals(1, patchedUser.getAddresses().size());
    assertEquals(homeAddress, patchedUser.getAddresses().get(0));
  }

  // ignore until the patch implementation is ready
  @Test
  public void testAddSecondValueToMultivalueAttribute204() {
    User testUser = createUser(TestData.setAttributesToATestUser("testAddSecondValueToMultivalueAttribute204").addEmail(workMail).build());
    User createdUser = getUser(testUser.getId());

    assertEquals(1, createdUser.getEmails().size());
    assertEquals(workMail.getDisplay(), createdUser.getEmails().iterator().next().getDisplay());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, EMAILS.scimName(), TestData.buildMultivaluedJSONNode(personalMail));
    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());

    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertEquals(2, patchedUser.getEmails().size());
  }

  @Test
  public void testAddIllegalValueToCustomAttribute400() {
    User testUser = createUser(TestData.setAttributesToATestUser("testAddIllegalValueToCustomAttribute400").addEmail(workMail).build());
    User createdUser = getUser(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, EMAILS.scimName(), "SomeIllegalValue");

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testAddValueToCustomAttribute204() {
    String customSchemaName = Schema.EXTENSION_SCHEMA_URN + "PatchCustSchema1";
    String customAttr1Name = "stringAttr1";
    String customAttr2Name = "stringAttr2";

    createCustomTestSchema(customSchemaName, customAttr1Name, customAttr2Name);

    Extension customSchema = new Extension.Builder(customSchemaName).setAttribute(customAttr1Name, "meh").build();
    User testUser = createUser(TestData.setAttributesToATestUser("testAddValueToCustomAttribute204").addExtension(customSchema).build());
    User createdUser = getUser(testUser.getId());

    String customAttr2Value = "TotalyLegalValue";
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, customSchemaName.concat(customAttr2Name), customAttr2Value);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());

    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    // User patchedTestUser = getUser(createdUser.getId());
    // assertEquals(patchedTestUser.getExtension(customSchemaName).getAttributes().get(customAttr2Name), customAttr2Value);
  }

  @Test
  public void testAddSameValueTwiseToAnAttribute204() {
    User testUser = createUser(TestData.setAttributesToATestUser("testAddSameValueTwiseToAnAttribute204").addEmail(workMail).build());

    User createdUser = getUser(testUser.getId());
    assertEquals(1, createdUser.getEmails().size());
    assertEquals(workMail.getDisplay(), createdUser.getEmails().iterator().next().getDisplay());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, EMAILS.scimName(), TestData.buildMultivaluedJSONNode(workMail));

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());

    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertEquals(1, patchedUser.getEmails().size());
    assertEquals(createdUser.getMeta().getVersion(), patchedUser.getMeta().getVersion());
    assertEquals(createdUser.getMeta().getLastModified(), patchedUser.getMeta().getLastModified());
  }

  @Test
  public void testAddNewValueToSingleValueAttribute204() {
    User testUser = createUser(TestData.setAttributesToATestUser("testAddNewValueToSingleValueAttribute204").setUserType("bachkator").build());
    User createdUser = getUser(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, USER_TYPE.scimName(), USER_TYPE_DEVELOPER);
    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());

    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertEquals(USER_TYPE_DEVELOPER, patchedUser.getUserType());
  }

  @Test
  public void testAddSameValueToSingleValueAttribute204() {
    User testUser = createUser(TestData.setAttributesToATestUser("testAddSameValueToSingleValueAttribute204").setUserType(USER_TYPE_DEVELOPER).build());
    User createdUser = getUser(testUser.getId());
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, USER_TYPE.scimName(), USER_TYPE_DEVELOPER);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());
    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertEquals(USER_TYPE_DEVELOPER, patchedUser.getUserType());
    assertEquals(createdUser.getMeta().getLastModified(), patchedUser.getMeta().getLastModified());
    assertEquals(createdUser.getMeta().getVersion(), patchedUser.getMeta().getVersion());
  }

  @Test
  public void testRemoveValueFromNonExistingAttribute400() {
    User createdUser = createUser(TestData.buildTestUser("testRemoveValueFromNonExistingAttribute400"));
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, "NonExistingAttr", null);
    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testRemoveAttributeValue204() {
    User createdUser = createUser(TestData.setAttributesToATestUser("testRemoveAttributeValue200").setUserType(USER_TYPE_DEVELOPER).build());
    assertEquals(USER_TYPE_DEVELOPER, createdUser.getUserType());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, USER_TYPE.scimName(), null);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());

    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertNull(patchedUser.getUserType());
  }

  @Test
  public void testRemoveMultivaluedAttribute204() {
    Collection<Email> userEmails = new ArrayList<>();
    userEmails.add(workMail);
    userEmails.add(personalMail);

    User testUser = createUser(TestData.setAttributesToATestUser("testRemoveMultivaluedAttribute204").addEmails(userEmails).build());

    User createdUser = getUser(testUser.getId());
    assertEquals(2, createdUser.getEmails().size());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, EMAILS.scimName(), null);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());
    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertEquals(0, patchedUser.getEmails().size());
  }

  @Test
  public void testRemoveReadOnlyAttribute400() {
    User testUser = createUser(TestData.buildTestUser("testRemoveReadOnlyAttribute400"));
    String createdUserId = testUser.getId();

    Group testGroup = createGroup(TestData.buildGroup("testRemoveReadOnlyAttribute400", createdUserId));
    Group createdGroup = getGroup(testGroup.getId());
    assertEquals(1, createdGroup.getMembers().size());

    User createdUser = getUser(createdUserId);
    assertEquals(1, createdUser.getGroups().size());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, CoreUserAttributes.GROUPS.scimName(), "");

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUserId);

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());

    createdUser = getUser(createdUserId);
    assertEquals(1, createdUser.getGroups().size());
  }

  @Test
  public void testRemoveGroupMembersFromGroup204() {
    User testUser = createUser(TestData.buildTestUser("testRemoveGroupMembersFromGroup204"));
    String createdUserId = testUser.getId();

    Group testGroup1 = createGroup(TestData.buildGroup("testRemoveGroupMembersFromGroup200", createdUserId));
    Group testGroup2 = createGroup(TestData.buildGroup("testPatchRemoveGroupMembers213", createdUserId, testGroup1.getId()));

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, MEMBERS.scimName(), "");
    SCIMResponse<?> response = groupRequest.patchGroup(patchBody, testGroup2.getId());

    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    Group patchedGroup = getGroup(testGroup2.getId());
    assertEquals(0, patchedGroup.getMembers().size());
  }

  @Test
  public void testRemoveValueToCustomAttribute204() {
    String customSchemaName = Schema.EXTENSION_SCHEMA_URN + "PatchCustSchema2";
    String customAttr1Name = "stringAttr1";
    String customAttr2Name = "stringAttr2";

    createCustomTestSchema(customSchemaName, customAttr1Name, customAttr2Name);

    Extension customSchema = new Extension.Builder(customSchemaName).setAttribute(customAttr1Name, "meh").build();
    User testUser = createUser(TestData.setAttributesToATestUser("testRemoveValueToCustomAttribute204").addExtension(customSchema).build());
    User createdUser = getUser(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, customSchemaName.concat(customAttr2Name), "");

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());

    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    // User patchedTestUser = getUser(createdUser.getId());
    // assertEquals(patchedTestUser.getExtension(customSchemaName).getAttributes().get(customAttr1Name), "");
  }

  @Test
  public void testReplaceValueWithIllegalValue400() {
    User testUser = createUser(TestData.setAttributesToATestUser("testReplaceValueWithIllegalValue400").setUserType(USER_TYPE_DEVELOPER).build());
    User createdUser = getUser(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, USER_TYPE.scimName(), true);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());
    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertEquals(USER_TYPE_DEVELOPER, patchedUser.getUserType());
  }

  @Test
  public void testReplaceValueWithLegalValue204() {
    User testUser = createUser(TestData.setAttributesToATestUser("testReplaceValueWithLegalValue204").setUserType("bachkator").build());
    User createdUser = getUser(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, USER_TYPE.scimName(), USER_TYPE_DEVELOPER);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());

    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertEquals(USER_TYPE_DEVELOPER, patchedUser.getUserType());
  }

  @Test
  public void testReplaceValueWithDifferentAttributeType400() {
    User testUser = createUser(TestData.setAttributesToATestUser("testReplaceValueWithDifferentAttributeType400").setUserType(USER_TYPE_DEVELOPER).build());
    User createdUser = getUser(testUser.getId());

    Email workMailDiplicate = new Email.Builder().setDisplay(workMail.getDisplay()).setPrimary(true).setType(Email.Type.WORK).build();
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, USER_TYPE.scimName(), workMailDiplicate);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());
    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertEquals(USER_TYPE_DEVELOPER, patchedUser.getUserType());
  }

  @Test
  public void testReplaceMultiValueAttributes204() {
    Collection<Email> userEmails = new ArrayList<>();
    userEmails.add(workMail);
    userEmails.add(personalMail);

    User testUser = createUser(TestData.setAttributesToATestUser("testReplaceMultiValueAttributes204").addEmails(userEmails).build());
    User createdUser = getUser(testUser.getId());

    Email newMail = new Email.Builder().setDisplay("newMail").setPrimary(true).setType(Email.Type.WORK).setValue("newValue@newValue.com").build();
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, EMAILS.scimName(), TestData.buildMultivaluedJSONNode(newMail));

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());
    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertEquals(1, patchedUser.getEmails().size());
    assertEquals(newMail, patchedUser.getEmails().iterator().next());
  }

  @Test
  public void testReplaceValueWithIllegalAttributeType400() {
    User testUser = createUser(TestData.setAttributesToATestUser("testReplaceValueWithIllegalAttributeType400").build());
    User createdUser = getUser(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, USER_TYPE.scimName(), workMail);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertNull(patchedUser.getUserType());
  }

  @Test
  public void testReplaceMultiValuedAttrThatHasNoPrevValue204() {
    User testUser = createUser(TestData.setAttributesToATestUser("testReplaceAttrValueThatHasNoPrevValue204").build());
    User createdUser = getUser(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, EMAILS.scimName(), TestData.buildMultivaluedJSONNode(workMail));

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());
    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertEquals(1, patchedUser.getEmails().size());
  }

  @Test
  public void testReplaceSingleValuedAttrThatHasNoPrevValue204() {
    User testUser = createUser(TestData.setAttributesToATestUser("testReplaceAttrValueThatHasNoPrevValue200").build());
    User createdUser = getUser(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, USER_TYPE.scimName(), USER_TYPE_DEVELOPER);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());
    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertEquals(USER_TYPE_DEVELOPER, patchedUser.getUserType());
  }

  @Test
  public void testReplaceAttrValueWithEmptyValue204() {
    User testUser = createUser(TestData.setAttributesToATestUser("testReplaceAttrValueWithEmptyValue204").setUserType(USER_TYPE_DEVELOPER).build());
    User createdUser = getUser(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, USER_TYPE.scimName(), "");

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());
    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertEquals(USER_TYPE_DEVELOPER, patchedUser.getUserType());
  }

  @Test
  public void testReplaceValueToCustomAttribute204() {
    String customSchemaName = Schema.EXTENSION_SCHEMA_URN + "PatchCustSchema3";
    String customAttr1Name = "stringAttr1";
    String customAttr2Name = "stringAttr2";

    createCustomTestSchema(customSchemaName, customAttr1Name, customAttr2Name);

    Extension customSchema = new Extension.Builder(customSchemaName).setAttribute(customAttr1Name, "meh").build();
    User testUser = createUser(TestData.setAttributesToATestUser("testReplaceValueToCustomAttribute204").addExtension(customSchema).build());
    User createdUser = getUser(testUser.getId());

    String customAttr1NewValue = "TotalyLegalValue";
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, customSchemaName.concat(customAttr2Name), customAttr1NewValue);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());
    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    // User patchedTestUser = getUser(createdUser.getId());
    // assertEquals(patchedTestUser.getExtension(customSchemaName).getAttributes().get(customAttr1Name), customAttr1NewValue);
  }

  @Test
  public void testPatchOperationWithInvalidValuePathFilter400() {
    User testUser = createUser(TestData.setAttributesToATestUser("testReplaceValuesOfMultivaluedAttributesByIncorrectFilter400").addEmail(workMail).build());
    User createdUser = getUser(testUser.getId());

    Email newMail = new Email.Builder().setDisplay("newMail").setPrimary(true).setType(Email.Type.WORK).setValue("newValue@newValue.com").build();
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, "emailssssssssss[type eq \"work\"]",
        TestData.buildMultivaluedJSONNode(newMail));

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());

    assertFalse(response.isSuccess());
    assertEquals(BAD_REQUEST.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testReplaceValuesOfMultivaluedAttributesByFilter204() {
    Collection<Email> userEmails = new ArrayList<>();
    userEmails.add(workMail);
    userEmails.add(personalMail);

    User testUser = createUser(TestData.setAttributesToATestUser("testReplaceValuesOfMultivaluedAttributesByFilter204").addEmails(userEmails).build());
    User createdUser = getUser(testUser.getId());

    Email newMail = new Email.Builder().setDisplay("newMail").setPrimary(true).setType(Email.Type.HOME).setValue("newValue@newValue.com").build();
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, "emails[type eq \"home\"]",
        TestData.buildMultivaluedJSONNode(newMail));

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());
    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertEquals(2, patchedUser.getEmails().size());

    // @formatter:off
    Email newEmailFetched = patchedUser.getEmails().stream().filter(email -> Email.Type.HOME.equals(email.getType())).findAny()
        .orElseThrow(AssertionError::new);
    // @formatter:on

    assertEquals(newMail, newEmailFetched);
  }

  @Test
  public void testReplaceSubAttributeValueOfMultivaluedAttributeByFilter204() {
    User testUser = createUser(TestData.setAttributesToATestUser("testReplaceSubAttributeValueOfMultivaluedAttributeByFilter204").addEmail(personalMail).build());
    User createdUser = getUser(testUser.getId());

    String newEmailValue = "mail@newdomain.com";
    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REPLACE, "emails[type eq \"home\"].value", newEmailValue);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());
    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());

    assertEquals(1, patchedUser.getEmails().size());
    assertEquals(newEmailValue, patchedUser.getEmails().iterator().next().getValue());
  }

  @Test
  public void testRemoveValuesOfMultivaluedAttributesByFilter204() {
    Collection<Email> userEmails = new ArrayList<>();
    userEmails.add(workMail);
    userEmails.add(personalMail);

    User testUser = createUser(TestData.setAttributesToATestUser("testRemoveValuesOfMultivaluedAttributesByFilter204").addEmails(userEmails).build());
    User createdUser = getUser(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, "emails[type eq \"home\"]", null);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());
    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());
    assertEquals(1, patchedUser.getEmails().size());

    assertTrue(patchedUser.getEmails().stream().map(Email::getType).noneMatch(Email.Type.HOME::equals),
        "Removed by value filter emails are not present");
  }

  @Test
  public void testRemoveSubAttributeValueOfMultivaluedAttributeByFilter204() {
    User testUser = createUser(TestData.setAttributesToATestUser("testRemoveSubAttributeValueOfMultivaluedAttributeByFilter204").addEmail(personalMail).build());
    User createdUser = getUser(testUser.getId());

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.REMOVE, "emails[type eq \"home\"].type", null);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());
    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());

    assertEquals(1, patchedUser.getEmails().size());
    assertNull(patchedUser.getEmails().iterator().next().getType());
  }

  @Test
  public void testAddSubAttributeValueOfMultivaluedAttributeByFilter204() {
    String initialEmailValue = "initialEmail@mail.com";
    Email initialEmail = new Email.Builder().setValue("initialEmail@mail.com").build();

    User testUser = createUser(TestData.setAttributesToATestUser("testAddSubAttributeValueOfMultivaluedAttributeByFilter204").addEmail(initialEmail).build());
    User createdUser = getUser(testUser.getId());

    Email.Type patchedEmailType = Email.Type.WORK;

    PatchBody patchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, String.format("emails[value eq \"%s\"].type", initialEmailValue),
        patchedEmailType.toString());

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());
    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());

    assertEquals(1, patchedUser.getEmails().size());
    assertEquals(patchedEmailType, patchedUser.getEmails().iterator().next().getType());
  }

  @Test
  public void testExecutingPUTOperationAfterSeveralPatchOperations200(){
    User testUser = createUser(TestData.setAttributesToATestUser("testExecutingPUTOperationAfterSeveralPatchOperations200").build());
    User createdUser = getUser(testUser.getId());

    PatchBody firstPatchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, DISPLAY_NAME.scimName(), "meh1");
    SCIMResponse<?> response = userRequest.patchUser(firstPatchBody, createdUser.getId());

    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    PatchBody secondPatchBody = TestData.buildPatchBody(PatchOperation.Type.ADD, USER_TYPE.scimName(), "meh2");
    response = userRequest.patchUser(secondPatchBody, createdUser.getId());

    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    String displayNamePUTUpdate = "meh3UpdatedWithPUT";
    User userForUpdateWithPUT = new User.Builder(createdUser).setDisplayName("meh3UpdatedWithPUT").build();
    User updatedUser = updateUser(createdUser.getId(), userForUpdateWithPUT);

    assertEquals(displayNamePUTUpdate, updatedUser.getDisplayName());
    assertNull(updatedUser.getUserType(), "Provided userType by past patch operation must be null");
  }
  @Test
  public void testChangeUserByMultiplePatchOperationsInOneRequest() {
    User testUser = createUser(TestData.setAttributesToATestUser("testChangeUserByMultiplePatchOperationsInOneRequest").build());
    User createdUser = getUser(testUser.getId());

    String displayName = "dName";
    String title = "tItle";

    PatchOperation op1 = TestData.buildPatchOperation(PatchOperation.Type.ADD, DISPLAY_NAME.scimName(), displayName);
    PatchOperation op2 = TestData.buildPatchOperation(PatchOperation.Type.ADD, TITLE.scimName(), title);
    PatchOperation op3 = TestData.buildPatchOperation(PatchOperation.Type.ADD, EMAILS.scimName(), TestData.buildMultivaluedJSONNode(personalMail));

    PatchBody patchBody = TestData.buildPatchBody(op1, op2, op3);

    SCIMResponse<?> response = userRequest.patchUser(patchBody, createdUser.getId());
    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    User patchedUser = getUser(createdUser.getId());

    assertEquals(displayName, patchedUser.getDisplayName());
    assertEquals(title, patchedUser.getTitle());

    assertEquals(1, patchedUser.getEmails().size());
  }

  @Test
  public void testChangeGroupByMultiplePatchOperationsInOneRequest() {
    Group testGroup = createGroup(new Group.Builder().setDisplayName("testChangeGroupByMultiplePatchOperationsInOneRequest-Group").build());
    User testUser = createUser(TestData.setAttributesToATestUser("testChangeGroupByMultiplePatchOperationsInOneRequest-UserMember").build());

    Group createdGroup = getGroup(testGroup.getId());
    User createdUser = getUser(testUser.getId());

    String firstDisplayName = "dName";
    String secondDisplayName = "dName2";
    MemberRef userRef = new MemberRef.Builder().setValue(createdUser.getId()).setType(MemberRef.Type.USER).build();


    PatchOperation op1 = TestData.buildPatchOperation(PatchOperation.Type.ADD, CoreGroupAttributes.DISPLAY_NAME.scimName(), firstDisplayName);
    PatchOperation op2 = TestData.buildPatchOperation(PatchOperation.Type.REPLACE, CoreGroupAttributes.DISPLAY_NAME.scimName(), secondDisplayName);
    PatchOperation op3 = TestData.buildPatchOperation(PatchOperation.Type.ADD, MEMBERS.scimName(), TestData.buildMemberRefMultivaluedJSONNode(userRef));

    PatchBody patchBody = TestData.buildPatchBody(op1, op2, op3);

    SCIMResponse<?> response = groupRequest.patchGroup(patchBody, createdGroup.getId());
    assertTrue(response.isSuccess());
    assertEquals(NO_CONTENT.getStatusCode(), response.getStatusCode());

    Group patchedGroup = getGroup(createdGroup.getId());

    assertEquals(patchedGroup.getDisplayName(), secondDisplayName);
    assertEquals(1, patchedGroup.getMembers().size());
  }
}
