
package com.sap.scimono.scim.system.tests.util;

import static com.sap.scimono.entity.definition.CoreGroupAttributes.MEMBERS_TYPE;
import static com.sap.scimono.entity.definition.CoreGroupAttributes.MEMBERS_VALUE;
import static com.sap.scimono.entity.definition.ResourceConstants.DISPLAY_NAME_FIELD;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.scimono.entity.Address;
import com.sap.scimono.entity.Email;
import com.sap.scimono.entity.EnterpriseExtension;
import com.sap.scimono.entity.Entitlement;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Im;
import com.sap.scimono.entity.Manager;
import com.sap.scimono.entity.MemberRef;
import com.sap.scimono.entity.Name;
import com.sap.scimono.entity.PhoneNumber;
import com.sap.scimono.entity.Photo;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.Role;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.X509Certificate;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.base.ExtensionFieldType;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.Schema;

public class TestData {
  public static final JsonNodeFactory JACKSON_NODE_FACTORY = JsonNodeFactory.instance;

  public static MemberRef buildGroupMemberResourceWithId(final String resourceId) {
    return new MemberRef.Builder().setValue(resourceId).build();
  }

  public static Group buildGroup(final String groupName, final String... memberIds) {
    Group.Builder groupBuilder = new Group.Builder(groupName);
    for (String nextMemberId : memberIds) {
      groupBuilder.addMember(buildGroupMemberResourceWithId(nextMemberId));
    }

    return groupBuilder.build();
  }

  public static User buildTestUser(final String name) {
    return setAttributesToATestUser(name).build();
  }

  public static User buildFullUserRepresentation(final String userName, final String managerId) {
    return setAllAttributesToTestUser(userName, managerId).build();
  }

  public static User.Builder buildUniqueUserFullRepresentation(final String userName) {
    // @formatter:off
    return new User.Builder(userName)
        .addEmail(
            new Email.Builder()
            .setPrimary(true)
            .setValue(UUID.randomUUID().toString())
            .build())
        .setActive(true)
        .addAddress(new Address.Builder()
            .setDisplay(UUID.randomUUID().toString())
            .setLocality(UUID.randomUUID().toString())
            .setRegion(UUID.randomUUID().toString())
            .setPostalCode(UUID.randomUUID().toString())
            .setCountry(UUID.randomUUID().toString())
            .build())
        .addRole(new Role.Builder()
            .setDisplay(UUID.randomUUID().toString())
            .setValue(UUID.randomUUID().toString())
            .setPrimary(true)
            .setType(Role.Type.of(UUID.randomUUID().toString()))
            .build())
        .addExtension(new EnterpriseExtension.Builder()
            .setEmployeeNumber(UUID.randomUUID().toString())
            .setCostCenter(UUID.randomUUID().toString())
            .setOrganization(UUID.randomUUID().toString())
            .setDivision(UUID.randomUUID().toString())
            .setDepartment(UUID.randomUUID().toString())
            .setManager(new Manager.Builder()
                .setValue(UUID.randomUUID().toString())
                .build())
            .build());
    // @formatter:on
  }

  public static User buildTestUserWithExtentions(final String name, final List<Extension> userExtCustomAttributes) {
    User.Builder testUser = setAttributesToATestUser(name);
    userExtCustomAttributes.forEach(testUser::addExtension);

    return testUser.build();
  }

  public static User.Builder setAttributesToATestUser(final String name) {
    // @formatter:off
    return new User.Builder(name)
        .setName(
            new Name.Builder()
            .setGivenName("Topalka")
            .setFamilyName("Angelova")
            .setHonorificSuffix("II")
            .build())
        .setDisplayName(name)
        .setActive(true);
    // @formatter:on
  }

  public static User.Builder setAllAttributesToTestUser(final String userName, final String managerId) {
    // @formatter:off
    User.Builder testUser = new User.Builder(userName)
        .setName(
            new Name.Builder()
            .setGivenName("Topalka")
            .setFamilyName("Angelova")
            .setHonorificSuffix("II")
            .setHonorificPrefix("Empiror")
            .setMiddleName("Arthur")
            .build())
        .setDisplayName(userName)
        .setNickName(userName)
        .setProfileUrl("http://prifleUrl.com")
        .setTitle("Empiror")
        .setUserType("king")
        .setPreferredLanguage("bg_BG")
        .setLocale("bg")
        .setTimezone("UTC")
        .setExternalId(userName)
        .setActive(true);
    // @formatter:on

    setAllMultivaluedAttributesToATestUser(testUser);
    addAllEnterpriseAttributesToTestUser(testUser, managerId);
    return testUser;
  }

  public static User.Builder addAllEnterpriseAttributesToTestUser(final User.Builder userBuilder, final String managerId) {
    // @formatter:off
    return userBuilder.addExtension(
        new EnterpriseExtension.Builder()
        .setCostCenter("costCenter")
        .setOrganization("SAP")
        .setDivision("SAP")
        .setDepartment("IDM")
        .setManager(managerId == null ? null : new Manager.Builder().setValue(managerId).build())
        .setEmployeeNumber("i31233")
        .build());
    // @formatter:on
  }

  public static Email buildWorkEmailWithDefaultAttrs() {
    // @formatter:off
    return new Email.Builder()
        .setDisplay("myWorkMail")
        .setValue("work_joro@sap.com")
        .setType(Email.Type.WORK)
        .setPrimary(false)
        .build();
    // @formatter:on
  }

  public static Email buildPersonalEmailWithDefaultAttrs() {
    // @formatter:off
    return new Email.Builder()
        .setDisplay("myPersonalMail")
        .setValue("personal_joro@sap.com")
        .setType(Email.Type.HOME)
        .setPrimary(true)
        .build();
    // @formatter:on
  }

  public static Address buildHomeAddressDefaultAttrs() {
    // @formatter:off
    return new Address.Builder()
        .setType(Address.Type.HOME)
        .setCountry("Bulgaria")
        .setFormatted("Plovdiv, Bulgaria")
        .setLocality("Plovdiv")
        .setRegion("Plovdiv")
        .setStreetAddress("Ivan III")
        .setPostalCode("1234")
        .build();
    // @formatter:on
  }

  public static User.Builder setAllMultivaluedAttributesToATestUser(final User.Builder userBuilder) {

 // @formatter:off
    Email emailPrimary = new Email.Builder()
         .setDisplay("AchoMail")
         .setPrimary(true)
         .setType(Email.Type.WORK)
         .setValue("achkata@work.com")
         .build();

    Email emailNotPrimery = new Email.Builder()
        .setDisplay("AchoMail")
        .setPrimary(false)
        .setType(Email.Type.HOME)
        .setValue("achkata@home.com")
        .build();

    Photo photoPrimary = new Photo.Builder().
        setDisplay("AchoPhoto")
        .setPrimary(true)
        .setType(Photo.Type.PHOTO)
        .setValue(URI.create("http://photosample.com/acho"))
        .build();

    Im imsPrimary = new Im.Builder()
        .setDisplay("AchoSkypeIms")
        .setPrimary(true)
        .setType(Im.Type.SKYPE)
        .setValue("achkataTopalovSkype")
        .build();

    PhoneNumber phonePrimary = new PhoneNumber.Builder()
        .setDisplay("AchoPhoneNumberWork")
        .setPrimary(true)
        .setType(PhoneNumber.Type.WORK)
        .setValue("+123234")
        .build();

    Entitlement entitlementPrimary = new Entitlement.Builder()
        .setDisplay("AchoEntitlementPrimary")
        .setPrimary(true)
        .setType(Entitlement.Type.of("primary"))
        .setValue("primaryEntitlement")
        .build();

    Role rolePrimary = new Role.Builder()
        .setDisplay("AchoAdmin")
        .setPrimary(true)
        .setType(Role.Type.of("admin"))
        .setValue("adminRole")
        .build();

    X509Certificate certificatePrimary = new X509Certificate.Builder()
        .setDisplay("LoginCert").setPrimary(true)
        .setType(X509Certificate.Type.of("cer"))
        .setValue("MIIDQzCCAqygAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwTjELMAkGA1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFDASBgNVBAoMC2V4YW1wbGUuY29tMRQwEgYDVQQDDAtleGFtcGxlLmNvbTAeFw0xMTEwMjIwNjI0MzFaFw0xMjEwMDQwNjI0MzFaMH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRQwEgYDVQQKDAtleGFtcGxlLmNvbTEhMB8GA1UEAwwYTXMuIEJhcmJhcmEgSiBKZW5zZW4gSUlJMSIwIAYJKoZIhvcNAQkBFhNiamVuc2VuQGV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7Kr+Dcds/JQ5GwejJFcBIP682X3xpjis56AK02bc1FLgzdLI8auoR+cC9/Vrh5t66HkQIOdA4unHh0AaZ4xL5PhVbXIPMB5vAPKpzz5iPSi8xO8SL7I7SDhcBVJhqVqr3HgllEG6UClDdHO7nkLuwXq8HcISKkbT5WFTVfFZzidPl8HZ7DhXkZIRtJwBweq4bvm3hM1Os7UQH05ZS6cVDgweKNwdLLrT51ikSQG3DYrl+ft781UQRIqxgwqCfXEuDiinPh0kkvIi5jivVu1Z9QiwlYEdRbLJ4zJQBmDrSGTMYn4lRc2HgHO4DqB/bnMVorHB0CC6AV1QoFK4GPe1LwIDAQABo3sweTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU8pD0U0vsZIsaA16lL8En8bx0F/gwHwYDVR0jBBgwFoAUdGeKitcaF7gnzsNwDx708kqaVt0wDQYJKoZIhvcNAQEFBQADgYEAA81SsFnOdYJtNg5Tcq+/ByEDrBgnusx0jloUhByPMEVkoMZ3J7j1ZgI8rAbOkNngX8+pKfTiDz1RC4+dx8oU6Za+4NJXUjlL5CvV6BEYb1+QAEJwitTVvxB/A67g42/vzgAtoRUeDov1+GFiBZ+GNF/cAYKcMtGcrs2i97ZkJMo=")
        .build();

    Address address = new Address.Builder()
        .setType(Address.Type.WORK)
        .setCountry("Bulgaria")
        .setFormatted("Sofia, Bulgaria")
        .setLocality("Sofia")
        .setRegion("Sofia")
        .setStreetAddress("Boris III")
        .setPostalCode("1000")
        .build();

    userBuilder.addEmails(Arrays.asList(emailPrimary, emailNotPrimery))
        .addPhoto(photoPrimary)
        .addIm(imsPrimary)
        .addPhoneNumber(phonePrimary)
        .addEntitlement(entitlementPrimary)
        .addRole(rolePrimary)
        .addX509Certificate(certificatePrimary)
        .addAddress(address);
    // @formatter:on
    return userBuilder;
  }

  public static List<Extension> createTestCustomAttribues(final String custAttr1, final String custAttr2,
      final String... schemasForCustomAttributes) {
    return Arrays.stream(schemasForCustomAttributes).map(customSchemaId -> {
      Map<String, Object> customAttributes = new HashMap<>();
      customAttributes.put(custAttr1, "BigTopalka");
      customAttributes.put(custAttr2, "I071825");

      return new Extension.Builder(customSchemaId).setAttributes(customAttributes).build();
    }).collect(Collectors.toList());
  }

  public static Schema buildTestSchema(final String schemaId) {
    Attribute attr1 = setDefaultPropertiesToTestAttribute("attrName1", ExtensionFieldType.STRING).build();
    Attribute attr2 = setAllDefaultPropertiesToTestAttribute("attrName2", ExtensionFieldType.STRING).build();

    return buildCustomSchemaWithAttrs(schemaId, Arrays.asList(attr1, attr2));
  }

  public static Schema buildTestSchemaWithAllAttributeTypes(final String schemaId) {
    Map<String, ExtensionFieldType<?>> attrNameToType = new HashMap<>();

    attrNameToType.put("booleanAttribute", ExtensionFieldType.BOOLEAN);
    attrNameToType.put("integerAttribute", ExtensionFieldType.INTEGER);
    attrNameToType.put("decimalAttribute", ExtensionFieldType.DECIMAL);
    attrNameToType.put("stringAttribute", ExtensionFieldType.STRING);
    attrNameToType.put("dateTimeAttribute", ExtensionFieldType.DATETIME);
    attrNameToType.put("binaryAttribute", ExtensionFieldType.BINARY);
    attrNameToType.put("referenceAttribute", ExtensionFieldType.REFERENCE);

    return buildCustomSchemaWithAttrs(schemaId, attrNameToType);
  }

  public static Schema buildCustomSchemaWithAttrs(final String schemaId, final Map<String, ExtensionFieldType<?>> attrNameToType) {
    List<Attribute> testAttributes = new ArrayList<>();
    for (Map.Entry<String, ExtensionFieldType<?>> nextEntry : attrNameToType.entrySet()) {
      Attribute testAttribute = setDefaultPropertiesToTestAttribute(nextEntry.getKey(), nextEntry.getValue()).build();
      testAttributes.add(testAttribute);
    }

    return buildCustomSchemaWithAttrs(schemaId, testAttributes);
  }

  public static Schema buildCustomSchemaWithAttrs(final String schemaId, final List<Attribute> attrs) {
    //@formatter:off
    Schema.Builder testSchemaBuilder = new Schema.Builder()
        .setId(schemaId)
        .name(TestUtil.getSchemaName(schemaId))
        .description("Test description");
    //@formatter:on

    for (Attribute nextAttr : attrs) {
      testSchemaBuilder.addAttribute(nextAttr);
    }

    return testSchemaBuilder.build();
  }

  public static Attribute.Builder setDefaultPropertiesToTestAttribute(final String attrName, final ExtensionFieldType<?> attrType) {
  //@formatter:off
    return new Attribute.Builder()
        .name(attrName)
        .type(attrType.getName())
        .multiValued(false)
        .description("A human-readable name for the Test. REQUIRED.")
        .required(false)
        .caseExact(false)
        .mutability("readWrite")
        .returned("default")
        .uniqueness("none");
    //@formatter:on
  }

  public static Attribute.Builder setAllDefaultPropertiesToTestAttribute(final String attrName, final ExtensionFieldType<?> attrType) {
    //@formatter:off
    return setDefaultPropertiesToTestAttribute(attrName, attrType)
    .addCanonicalValue("a")
    .addCanonicalValue("b")
    .addCanonicalValue("c")
    .addReferenceType("external")
    .addReferenceType("uri");
    //@formatter:on

  }

  public static PatchBody preparePatchBodyWithCustomOperationAndSchema(final PatchOperation.Type op, final String customSchemaName) {
    // @formatter:off
    PatchOperation operation = setDefaultPatchOperationAttributes(op);

    return new PatchBody.Builder()
        .setSchemas(new HashSet<>(Collections.singletonList(customSchemaName)))
        .addOperation(operation)
        .build();
    // @formatter:on
  }

  public static PatchBody buildPatchBody(final PatchOperation... operations) {
    PatchBody.Builder patchBodyBuilder = TestData.setDefaultPatchBodyAttributes();
    Arrays.stream(operations).forEach(patchBodyBuilder::addOperation);

    return patchBodyBuilder.build();
  }

  public static PatchBody buildPatchBody(final PatchOperation.Type op, final String path, final Object value) {
    // @formatter:off
    return TestData.setDefaultPatchBodyAttributes()
        .addOperation(buildPatchOperation(op, path, value))
        .build();
    // @formatter:on

  }

  public static PatchOperation buildPatchOperation(final PatchOperation.Type op, final String path, final Object value) {
    // @formatter:off
    return new PatchOperation.Builder()
        .setOp(op)
        .setPath(path)
        .setValue(JACKSON_NODE_FACTORY.pojoNode(value))
        .build();
    // @formatter:on
  }

  public static PatchOperation setDefaultPatchOperationAttributes(final PatchOperation.Type op) {
    // @formatter:off
    return new PatchOperation.Builder()
        .setOp(op)
        .setPath(DISPLAY_NAME_FIELD)
        .setValue(JACKSON_NODE_FACTORY.textNode("AdoDisplayName"))
        .build();
    // @formatter:on
  }

  public static JsonNode buildMultivaluedJSONNode(final Object... values) {
    ArrayNode arrayNode = JACKSON_NODE_FACTORY.arrayNode();
    for (Object value : values) {
      arrayNode.add(JACKSON_NODE_FACTORY.pojoNode(value));
    }
    return arrayNode;
  }

  public static JsonNode buildMemberRefMultivaluedJSONNode(final MemberRef... memberRefs) {
    ArrayNode arrayNode = JACKSON_NODE_FACTORY.arrayNode();
    Arrays.stream(memberRefs).forEach(memberRef -> {
      ObjectNode objectNode = JACKSON_NODE_FACTORY.objectNode();

      objectNode.put(MEMBERS_TYPE.scimName(), memberRef.getType().toString());
      objectNode.put(MEMBERS_VALUE.scimName(), memberRef.getValue());

      arrayNode.add(objectNode);
    });

    return arrayNode;
  }

  public static PatchBody.Builder setDefaultPatchBodyAttributes() {
    return new PatchBody.Builder().setSchemas(new HashSet<>(Collections.singletonList(PatchBody.PATCH_SCHEMA)));
  }

  public static List<String> extractUserIds(final List<User> users) {
    return extractResourceIds(users);
  }

  public static List<String> extractGroupIds(final List<Group> groups) {
    return extractResourceIds(groups);
  }

  @SuppressWarnings("rawtypes")
  private static List<String> extractResourceIds(final List<? extends Resource> users) {
    List<String> resourceIds = new ArrayList<>();
    for (Resource<?> nextUser : users) {
      resourceIds.add(nextUser.getId());
    }

    return resourceIds;
  }

  protected Schema buildCustomTestSchema(final String schemaId, final String custAttr1, final String custAttr2) {
    Map<String, ExtensionFieldType<?>> customAttrsNameToType = new HashMap<>();

    customAttrsNameToType.put(custAttr1, ExtensionFieldType.STRING);
    customAttrsNameToType.put(custAttr2, ExtensionFieldType.STRING);

    return buildCustomSchemaWithAttrs(schemaId, customAttrsNameToType);
  }
}
