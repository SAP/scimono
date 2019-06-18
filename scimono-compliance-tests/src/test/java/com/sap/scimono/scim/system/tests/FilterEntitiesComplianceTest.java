
package com.sap.scimono.scim.system.tests;

import com.sap.scimono.entity.Address;
import com.sap.scimono.entity.EnterpriseExtension;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.scim.system.tests.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.sap.scimono.entity.EnterpriseExtension.ENTERPRISE_URN;
import static com.sap.scimono.entity.definition.CoreGroupAttributes.DISPLAY_NAME;
import static com.sap.scimono.entity.definition.CoreUserAttributes.ADDRESSES_COUNTRY;
import static com.sap.scimono.entity.definition.CoreUserAttributes.ADDRESSES_LOCALITY;
import static com.sap.scimono.entity.definition.CoreUserAttributes.ADDRESSES_POSTAL_CODE;
import static com.sap.scimono.entity.definition.CoreUserAttributes.ADDRESSES_REGION;
import static com.sap.scimono.entity.definition.CoreUserAttributes.EMAILS_VALUE;
import static com.sap.scimono.entity.definition.CoreUserAttributes.GROUPS_DISPLAY;
import static com.sap.scimono.entity.definition.CoreUserAttributes.ROLES_VALUE;
import static com.sap.scimono.entity.definition.CoreUserAttributes.USER_NAME;
import static com.sap.scimono.entity.definition.EnterpriseUserAttributes.COST_CENTER;
import static com.sap.scimono.entity.definition.EnterpriseUserAttributes.DEPARTMENT;
import static com.sap.scimono.entity.definition.EnterpriseUserAttributes.DIVISION;
import static com.sap.scimono.entity.definition.EnterpriseUserAttributes.EMPLOYEE_NUMBER;
import static com.sap.scimono.entity.definition.EnterpriseUserAttributes.MANAGER_VALUE;
import static com.sap.scimono.scim.system.tests.util.TestData.buildGroup;
import static com.sap.scimono.scim.system.tests.util.TestData.buildTestUser;
import static com.sap.scimono.scim.system.tests.util.TestData.buildUniqueUserFullRepresentation;
import static com.sap.scimono.scim.system.tests.util.TestData.setAttributesToATestUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilterEntitiesComplianceTest extends SCIMComplianceTest {
  private static final String CUSTOM_BOOLEAN_ATTRIBUTE_NAME = "booleanAttribute";
  private static final String CUSTOM_INTEGER_ATTRIBUTE_NAME = "integerAttribute";
  private static final String CUSTOM_STRING_ATTRIBUTE_NAME = "stringAttribute";

  private static boolean isTestDataCreated = false;

  private final String testUser1UserName = "FilterEntitiesIntegrationTestUser1";
  private final String testUser2UserName = "FilterEntitiesIntegrationTestUser2";
  private final String testUser3UserName = "FilterEntitiesIntegrationTestUser3";
  private final String testUser4UserName = "FilterEntitiesIntegrationTestUser4";

  private final String testGroup1DisplayName = "FilterEntitiesIntegrationTestGroup1";
  private final String testGroup2DisplayName = "FilterEntitiesIntegrationTestGroup2";
  private final String testGroup3Key = "FilterEntitiesIntegrationTestGroup3";

  private final String testCustomSchemaName = Schema.EXTENSION_SCHEMA_URN.concat("FilterEntitiesIT");

  private static final Map<String, User> testUsers = new HashMap<>();
  private static final Map<String, Group> testGroups = new HashMap<>();

  @BeforeEach
  public void setUpBeforeTest() {
    if (!isTestDataCreated) {
      isTestDataCreated = true;

      createSchema(TestData.buildTestSchemaWithAllAttributeTypes(testCustomSchemaName));
      createTestUsers();
      createTestGroups();
    }
  }

  @Test
  public void testGetUsersFilteredByUsername() {
    List<User> filteredUsers = getUsersFiltered(generateFilterQuery(USER_NAME.scimName(), testUser1UserName));
    assertEquals(filteredUsers, getUsersFiltered(generateFilterQuery(USER_NAME.fullAttributePath(), testUser1UserName)));
    assertNotNull(filteredUsers);
    assertEquals(1, filteredUsers.size());
    assertEquals(testUsers.get(testUser1UserName).getId(), filteredUsers.get(0).getId());
  }

  @Test
  public void testGetUsersFilteredByEmail() {
    String emailValue = testUsers.get(testUser1UserName).getEmails().get(0).getValue();
    testFilteredUsers(EMAILS_VALUE.relativePath(), emailValue);
    testFilteredUsers(EMAILS_VALUE.fullAttributePath(), emailValue);
  }

  @Test
  public void testGetUsersFilteredByAddress() {
    Address testUser1Address = testUsers.get(testUser1UserName).getAddresses().get(0);

    testFilteredUsers(ADDRESSES_LOCALITY.relativePath(), testUser1Address.getLocality());
    testFilteredUsers(ADDRESSES_LOCALITY.fullAttributePath(), testUser1Address.getLocality());
    testFilteredUsers(ADDRESSES_REGION.relativePath(), testUser1Address.getRegion());
    testFilteredUsers(ADDRESSES_REGION.fullAttributePath(), testUser1Address.getRegion());
    testFilteredUsers(ADDRESSES_POSTAL_CODE.relativePath(), testUser1Address.getPostalCode());
    testFilteredUsers(ADDRESSES_POSTAL_CODE.fullAttributePath(), testUser1Address.getPostalCode());
    testFilteredUsers(ADDRESSES_COUNTRY.relativePath(), testUser1Address.getCountry());
    testFilteredUsers(ADDRESSES_COUNTRY.fullAttributePath(), testUser1Address.getCountry());
  }

  @Test
  public void testGetUsersFilteredByRoles() {
    String roleValue = testUsers.get(testUser1UserName).getRoles().get(0).getValue();
    testFilteredUsers(ROLES_VALUE.relativePath(), roleValue);
    testFilteredUsers(ROLES_VALUE.fullAttributePath(), roleValue);
  }

  @Test
  public void testGetUsersFilteredByGroups() {
    String groupDisplayName = getUser(testUsers.get(testUser1UserName).getId()).getGroups().get(0).getDisplay();
    testFilteredUsers(GROUPS_DISPLAY.relativePath(), groupDisplayName);
    testFilteredUsers(GROUPS_DISPLAY.fullAttributePath(), groupDisplayName);
  }

  @Test
  public void testGetUsersFilteredByEnterpriseAttributes() {
    EnterpriseExtension userEnterpriseExtension = (EnterpriseExtension) testUsers.get(testUser1UserName).getExtension(ENTERPRISE_URN);

    testFilteredUsers(EMPLOYEE_NUMBER.fullAttributePath(), userEnterpriseExtension.getEmployeeNumber());
    testFilteredUsers(COST_CENTER.fullAttributePath(), userEnterpriseExtension.getCostCenter());
    testFilteredUsers(DIVISION.fullAttributePath(), userEnterpriseExtension.getDivision());
    testFilteredUsers(DEPARTMENT.fullAttributePath(), userEnterpriseExtension.getDepartment());
    testFilteredUsers(MANAGER_VALUE.fullAttributePath(), userEnterpriseExtension.getManager().getValue());
  }

  @Test
  public void testGetUsersFilteredByCustomSchemaAttributes() {
    Extension userCustomExtension = testUsers.get(testUser1UserName).getExtension(testCustomSchemaName);

    for (Map.Entry<String, Object> nextCustomAttribute : userCustomExtension.getAttributes().entrySet()) {
      testFilterByCustomSchemaAttribute(nextCustomAttribute.getKey(), nextCustomAttribute.getValue().toString());
    }
  }

  @Test
  public void testGetUsersFilteredByUserNameOREmail() {
    String user1EmailValue = testUsers.get(testUser1UserName).getEmails().get(0).getValue();
    String filterQuery = generateFilterQueryWithOROperator(generateFilterQuery(EMAILS_VALUE.relativePath(), user1EmailValue),
        generateFilterQuery(USER_NAME.scimName(), testUser3UserName));

    String fullRepresentationFilterQuery = generateFilterQueryWithOROperator(generateFilterQuery(EMAILS_VALUE.fullAttributePath(), user1EmailValue),
        generateFilterQuery(USER_NAME.fullAttributePath(), testUser3UserName));

    List<User> filteredUsers = getUsersFiltered(filterQuery);

    assertEquals(filteredUsers, getUsersFiltered(fullRepresentationFilterQuery));
    assertEquals(3, filteredUsers.size());
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser3UserName).getId(), filteredUsers));
  }

  @Test
  public void testGetUsersFilteredByCountryORCostCenter() {
    String costCenter = ((EnterpriseExtension) testUsers.get(testUser1UserName).getExtension(ENTERPRISE_URN)).getCostCenter();
    String addressCountry = testUsers.get(testUser3UserName).getAddresses().get(0).getCountry();

    String filterQuery = generateFilterQueryWithOROperator(generateFilterQuery(ADDRESSES_COUNTRY.relativePath(), addressCountry),
        generateFilterQuery(COST_CENTER.fullAttributePath(), costCenter));

    List<User> filteredUsers = getUsersFiltered(filterQuery);

    assertEquals(3, filteredUsers.size());
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser3UserName).getId(), filteredUsers));
  }

  @Test
  public void testGetUsersFilteredByLocalityORCustomStringAttribute() {
    String customStringAttribute = testUsers.get(testUser1UserName).getExtension(testCustomSchemaName)
        .getAttributeValueAsString(CUSTOM_STRING_ATTRIBUTE_NAME);
    String addressLocality = testUsers.get(testUser3UserName).getAddresses().get(0).getLocality();

    String filterQuery = generateFilterQueryWithOROperator(generateFilterQuery(ADDRESSES_LOCALITY.fullAttributePath(), addressLocality),
        generateFilterQuery(String.format("%s:%s", testCustomSchemaName, CUSTOM_STRING_ATTRIBUTE_NAME), customStringAttribute));

    List<User> filteredUsers = getUsersFiltered(filterQuery);

    assertEquals(3, filteredUsers.size());
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser3UserName).getId(), filteredUsers));
  }

  @Test
  public void testGetUsersFilteredByRegionORCustomIntegerAttribute() {
    String customStringAttribute = testUsers.get(testUser1UserName).getExtension(testCustomSchemaName)
        .getAttributeValueAsString(CUSTOM_INTEGER_ATTRIBUTE_NAME);
    String addressRegion = testUsers.get(testUser3UserName).getAddresses().get(0).getRegion();

    String filterQuery = generateFilterQueryWithOROperator(generateFilterQuery(ADDRESSES_REGION.relativePath(), addressRegion),
        generateFilterQuery(String.format("%s:%s", testCustomSchemaName, CUSTOM_INTEGER_ATTRIBUTE_NAME), customStringAttribute));

    List<User> filteredUsers = getUsersFiltered(filterQuery);

    assertEquals(2, filteredUsers.size());
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser3UserName).getId(), filteredUsers));
  }

  @Test
  public void testGetUsersFilteredByPostalCodeORCustomBooleanAttribute() {
    String customStringAttribute = testUsers.get(testUser1UserName).getExtension(testCustomSchemaName)
        .getAttributeValueAsString(CUSTOM_BOOLEAN_ATTRIBUTE_NAME);
    String addressPostalCode = testUsers.get(testUser3UserName).getAddresses().get(0).getPostalCode();

    String filterQuery = generateFilterQueryWithOROperator(generateFilterQuery(ADDRESSES_POSTAL_CODE.relativePath(), addressPostalCode),
        generateFilterQuery(String.format("%s:%s", testCustomSchemaName, CUSTOM_BOOLEAN_ATTRIBUTE_NAME), customStringAttribute));

    List<User> filteredUsers = getUsersFiltered(filterQuery);

    assertEquals(2, filteredUsers.size());
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser3UserName).getId(), filteredUsers));
  }

  @Test
  public void testGetUsersFilteredByUserNameANDEmail() {
    String emailValue = testUsers.get(testUser1UserName).getEmails().get(0).getValue();
    String filterQuery = generateFilterQueryWithANDOperator(generateFilterQuery(EMAILS_VALUE.relativePath(), emailValue),
        generateFilterQuery(USER_NAME.scimName(), testUser1UserName));
    List<User> filteredUsers = getUsersFiltered(filterQuery);

    assertEquals(1, filteredUsers.size());
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers));
  }

  @Test
  public void testGetUsersFilteredByUserNameANDGroups() {
    String filterQuery = generateFilterQueryWithANDOperator(generateFilterQuery(GROUPS_DISPLAY.relativePath(), ""),
        generateFilterQuery(USER_NAME.scimName(), testUser1UserName));
    List<User> filteredUsers = getUsersFiltered(filterQuery);

    assertEquals(0, filteredUsers.size());
  }

  @Test
  public void testGetUsersFilteredByGroupsANDLocale() {
    String groupsDisplayValue = getUser(testUsers.get(testUser1UserName).getId()).getGroups().get(0).getDisplay();
    String localeValue = testUsers.get(testUser1UserName).getAddresses().get(0).getLocality();
    String filterQuery = generateFilterQueryWithANDOperator(generateFilterQuery(GROUPS_DISPLAY.relativePath(), groupsDisplayValue),
        generateFilterQuery(ADDRESSES_LOCALITY.relativePath(), localeValue));
    List<User> filteredUsers = getUsersFiltered(filterQuery);

    assertEquals(2, filteredUsers.size());
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers));
  }

  @Test
  public void testGetUsersFilteredByCostCenterANDCustomAttribute() {
    String costCenterValue = ((EnterpriseExtension) testUsers.get(testUser1UserName).getExtension(ENTERPRISE_URN)).getCostCenter();
    String customAttributeValue = testUsers.get(testUser1UserName).getExtension(testCustomSchemaName)
        .getAttributeValueAsString(CUSTOM_STRING_ATTRIBUTE_NAME);
    String filterQuery = generateFilterQueryWithANDOperator(generateFilterQuery(COST_CENTER.fullAttributePath(), costCenterValue),
        generateFilterQuery(String.format("%s:%s", testCustomSchemaName, CUSTOM_STRING_ATTRIBUTE_NAME), customAttributeValue));

    List<User> filteredUsers = getUsersFiltered(filterQuery);

    assertEquals(2, filteredUsers.size());
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers));
  }

  @Test
  public void testGetUsersFilteredByComplexFilter1() {
    String user1CostCenter = ((EnterpriseExtension) testUsers.get(testUser1UserName).getExtension(ENTERPRISE_URN)).getCostCenter();
    String user1CustomAttribute = testUsers.get(testUser1UserName).getExtension(testCustomSchemaName)
        .getAttributeValueAsString(CUSTOM_STRING_ATTRIBUTE_NAME);

    String user3AddressesCountry = testUsers.get(testUser3UserName).getAddresses().get(0).getCountry();
    String user3AddressesRegion = testUsers.get(testUser3UserName).getAddresses().get(0).getRegion();

    String filterFirstPart = generateFilterQueryWithANDOperator(generateFilterQuery(COST_CENTER.fullAttributePath(), user1CostCenter),
        generateFilterQuery(String.format("%s:%s", testCustomSchemaName, CUSTOM_STRING_ATTRIBUTE_NAME), user1CustomAttribute));
    String filterSecondPart = generateFilterQueryWithANDOperator(generateFilterQuery(ADDRESSES_COUNTRY.fullAttributePath(), user3AddressesCountry),
        generateFilterQuery(ADDRESSES_REGION.relativePath(), user3AddressesRegion));

    String filterQuery = generateFilterQueryWithOROperator(filterFirstPart, filterSecondPart);

    List<User> filteredUsers = getUsersFiltered(filterQuery);

    assertEquals(filteredUsers, getUsersFiltered(generateFilterQueryWithOROperator(filterSecondPart, filterFirstPart)));
    assertEquals(3, filteredUsers.size());
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser3UserName).getId(), filteredUsers));
  }

  @Test
  public void testGetUsersFilteredByComplexFilter2() {
    String user3AddressesLocality = testUsers.get(testUser3UserName).getAddresses().get(0).getLocality();
    String user1CustomAttribute = testUsers.get(testUser1UserName).getExtension(testCustomSchemaName)
        .getAttributeValueAsString(CUSTOM_STRING_ATTRIBUTE_NAME);
    String user1GroupsDisplayValue = getUser(testUsers.get(testUser1UserName).getId()).getGroups().get(0).getDisplay();

    String filterFirstPart = generateFilterQueryWithOROperator(generateFilterQuery(COST_CENTER.fullAttributePath(), user3AddressesLocality),
        generateFilterQuery(String.format("%s:%s", testCustomSchemaName, CUSTOM_STRING_ATTRIBUTE_NAME), user1CustomAttribute));
    String filterSecondPart = generateFilterQuery(GROUPS_DISPLAY.fullAttributePath(), user1GroupsDisplayValue);

    String filterQuery = generateFilterQueryWithANDOperator(filterFirstPart, filterSecondPart);
    List<User> filteredUsers = getUsersFiltered(filterQuery);

    assertEquals(filteredUsers, getUsersFiltered(generateFilterQueryWithANDOperator(filterFirstPart, filterSecondPart)));
    assertEquals(2, filteredUsers.size());
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers));
  }

  @Test
  public void testGetGroupsFilteredByDisplayNameAttributeWithAND() {
    String filterQuery = generateFilterQueryWithANDOperator(generateFilterQuery(DISPLAY_NAME.scimName(), testGroup1DisplayName),
        generateFilterQuery(DISPLAY_NAME.scimName(), testGroup2DisplayName));
    List<Group> filteredGroups = getGroupsFiltered(filterQuery);

    assertEquals(0, filteredGroups.size());
  }

  @Test
  public void testGetGroupsFilteredByDisplayNameAttributeWithANDNonExistingGroup1() {
    String filterQuery = generateFilterQueryWithANDOperator(generateFilterQuery(DISPLAY_NAME.scimName(), testGroup1DisplayName),
        generateFilterQuery(DISPLAY_NAME.scimName(), "non-existing-gn"));
    List<Group> filteredGroups = getGroupsFiltered(filterQuery);

    assertEquals(0, filteredGroups.size());
  }

  @Test
  public void testGetGroupsFilteredByDisplayNameAttributeWithANDNonExistingGroup2() {
    String filterQuery = generateFilterQueryWithANDOperator(generateFilterQuery(DISPLAY_NAME.scimName(), ""),
        generateFilterQuery(DISPLAY_NAME.scimName(), testGroup2DisplayName));
    List<Group> filteredGroups = getGroupsFiltered(filterQuery);

    assertEquals(0, filteredGroups.size());
  }

  @Test
  public void testGetGroupsFilteredByDisplayNameAttributeWithANDNonExistingGroup3() {
    String filterQuery = generateFilterQueryWithANDOperator(generateFilterQuery(DISPLAY_NAME.scimName(), ""),
        generateFilterQuery(DISPLAY_NAME.scimName(), ""));
    List<Group> filteredGroups = getGroupsFiltered(filterQuery);

    assertEquals(0, filteredGroups.size());
  }

  @Test
  public void testGetGroupsFilteredByDisplayNameAttribute() {
    List<Group> filteredGroups = getGroupsFiltered(generateFilterQuery(DISPLAY_NAME.scimName(), testGroup1DisplayName));

    assertEquals(filteredGroups, getGroupsFiltered(generateFilterQuery(DISPLAY_NAME.fullAttributePath(), testGroup1DisplayName)));
    assertEquals(2, filteredGroups.size());
    assertTrue(isGroupFetchedInResponse(testGroups.get(testGroup1DisplayName).getId(), filteredGroups));
    assertTrue(isGroupFetchedInResponse(testGroups.get(testGroup3Key).getId(), filteredGroups));
  }

  @Test
  public void testGetGroupsFilteredByDisplayNameAttributeWithOR() {
    String filterQuery = generateFilterQueryWithOROperator(generateFilterQuery(DISPLAY_NAME.scimName(), testGroup1DisplayName),
        generateFilterQuery(DISPLAY_NAME.scimName(), testGroup2DisplayName));
    List<Group> filteredGroups = getGroupsFiltered(filterQuery);

    assertEquals(filteredGroups,
        getGroupsFiltered(generateFilterQueryWithOROperator(generateFilterQuery(DISPLAY_NAME.fullAttributePath(), testGroup1DisplayName),
            generateFilterQuery(DISPLAY_NAME.fullAttributePath(), testGroup2DisplayName))));
    assertEquals(3, filteredGroups.size());
    assertTrue(isGroupFetchedInResponse(testGroups.get(testGroup1DisplayName).getId(), filteredGroups));
    assertTrue(isGroupFetchedInResponse(testGroups.get(testGroup2DisplayName).getId(), filteredGroups));
    assertTrue(isGroupFetchedInResponse(testGroups.get(testGroup3Key).getId(), filteredGroups));
  }

  public void testGetGroupsFilteredByDisplayNameAttributeWithORNonExistingGroup1() {
    String filterQuery = generateFilterQueryWithOROperator(generateFilterQuery(DISPLAY_NAME.scimName(), testGroup1DisplayName),
        generateFilterQuery(DISPLAY_NAME.scimName(), "non-existing-gn"));
    List<Group> filteredGroups = getGroupsFiltered(filterQuery);

    assertEquals(filteredGroups,
        getGroupsFiltered(generateFilterQueryWithOROperator(generateFilterQuery(DISPLAY_NAME.fullAttributePath(), testGroup1DisplayName),
            generateFilterQuery(DISPLAY_NAME.fullAttributePath(), "non-existing-gn"))));
    assertEquals(2, filteredGroups.size());
    assertTrue(isGroupFetchedInResponse(testGroups.get(testGroup1DisplayName).getId(), filteredGroups));
    assertTrue(isGroupFetchedInResponse(testGroups.get(testGroup3Key).getId(), filteredGroups));
  }

  @Test
  public void testGetGroupsFilteredByDisplayNameAttributeWithAndNonExistingGroup2() {
    String filterQuery = generateFilterQueryWithOROperator(generateFilterQuery(DISPLAY_NAME.scimName(), ""),
        generateFilterQuery(DISPLAY_NAME.scimName(), testGroup2DisplayName));
    List<Group> filteredGroups = getGroupsFiltered(filterQuery);

    assertEquals(filteredGroups, getGroupsFiltered(generateFilterQueryWithOROperator(generateFilterQuery(DISPLAY_NAME.fullAttributePath(), ""),
        generateFilterQuery(DISPLAY_NAME.fullAttributePath(), testGroup2DisplayName))));
    assertEquals(1, filteredGroups.size());
    assertTrue(isGroupFetchedInResponse(testGroups.get(testGroup2DisplayName).getId(), filteredGroups));
  }

  @Test
  public void testGetGroupsFilteredByDisplayNameAttributeWithAndNonExistingGroup3() {
    String filterQuery = generateFilterQueryWithOROperator(generateFilterQuery(DISPLAY_NAME.scimName(), ""),
        generateFilterQuery(DISPLAY_NAME.scimName(), ""));
    List<Group> filteredGroups = getGroupsFiltered(filterQuery);

    assertEquals(filteredGroups, getGroupsFiltered(generateFilterQueryWithOROperator(generateFilterQuery(DISPLAY_NAME.fullAttributePath(), ""),
        generateFilterQuery(DISPLAY_NAME.fullAttributePath(), ""))));
    assertEquals(0, filteredGroups.size());
  }

  private String generateFilterQuery(final String attributeName, final String attributeValue) {
    return String.format("%s eq \"%s\"", attributeName, attributeValue);
  }

  private String generateFilterQueryWithOROperator(final String filter1, final String filter2) {
    return String.format("%s or %s", filter1, filter2);
  }

  private String generateFilterQueryWithANDOperator(final String filter1, final String filter2) {
    return String.format("%s and %s", filter1, filter2);
  }

  private void testFilteredUsers(final String filterAttribute, final String filterValue) {
    List<User> filteredUsers = getUsersFiltered(generateFilterQuery(filterAttribute, filterValue));

    assertEquals(2, filteredUsers.size());
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers));
  }

  private void testFilterByCustomSchemaAttribute(final String filterAttribute, final String filterValue) {
    List<User> filteredUsers = getUsersFiltered(generateFilterQuery(String.format("%s:%s", testCustomSchemaName, filterAttribute), filterValue));

    assertEquals(2, filteredUsers.size());
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers));
  }

  private void createTestUsers() {
    // @formatter:off
    User testUser1 = buildUniqueUserFullRepresentation(testUser1UserName)
        .addExtension(new Extension.Builder(testCustomSchemaName)
            .setAttribute(CUSTOM_STRING_ATTRIBUTE_NAME, "customValue")
            .setAttribute(CUSTOM_INTEGER_ATTRIBUTE_NAME, 10)
            .setAttribute(CUSTOM_BOOLEAN_ATTRIBUTE_NAME, true)
            .build())
        .build();

    User testUser2 = new User.Builder(testUser1).setUserName(testUser2UserName).build();
    User testUser3 = setAttributesToATestUser(testUser3UserName)
        .addAddress(new Address.Builder()
            .setCountry(UUID.randomUUID().toString())
            .setRegion(UUID.randomUUID().toString())
            .setLocality(UUID.randomUUID().toString())
            .setPostalCode(UUID.randomUUID().toString())
            .build())
        .build();

    User testUser4 = buildTestUser(testUser4UserName);

    // @formatter:on

    testUsers.put(testUser1UserName, createUser(testUser1));
    testUsers.put(testUser2UserName, createUser(testUser2));
    testUsers.put(testUser3UserName, createUser(testUser3));
    testUsers.put(testUser4UserName, createUser(testUser4));
  }

  private void createTestGroups() {
    Group testGroup1 = buildGroup(testGroup1DisplayName, testUsers.get(testUser1UserName).getId(), testUsers.get(testUser2UserName).getId());
    Group testGroup2 = buildGroup(testGroup2DisplayName, testUsers.get(testUser2UserName).getId());
    Group testGroup3 = new Group.Builder(testGroup1DisplayName).build();

    testGroups.put(testGroup1DisplayName, createGroup(testGroup1));
    testGroups.put(testGroup2DisplayName, createGroup(testGroup2));
    testGroups.put(testGroup3Key, createGroup(testGroup3));
  }

}
