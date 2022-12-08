
package com.sap.scimono.scim.system.tests;

import com.sap.scimono.client.SCIMRequest;
import com.sap.scimono.entity.Address;
import com.sap.scimono.entity.EnterpriseExtension;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.scim.system.tests.extensions.GroupClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.GroupFailSafeClient;
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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilterEntitiesComplianceTest extends SCIMComplianceTest {
  private static final Logger logger = LoggerFactory.getLogger(FilterEntitiesComplianceTest.class);

  private static final String CUSTOM_BOOLEAN_ATTRIBUTE_NAME = "booleanAttribute";
  private static final String CUSTOM_INTEGER_ATTRIBUTE_NAME = "integerAttribute";
  private static final String CUSTOM_STRING_ATTRIBUTE_NAME = "stringAttribute";

  private static final String testUser1UserName = "FilterEntitiesIntegrationTestUser1";
  private static final String testUser2UserName = "FilterEntitiesIntegrationTestUser2";
  private static final String testUser3UserName = "FilterEntitiesIntegrationTestUser3";
  private static final String testUser4UserName = "FilterEntitiesIntegrationTestUser4";

  private static final String testGroup1DisplayName = "FilterEntitiesIntegrationTestGroup1";
  private static final String testGroup2DisplayName = "FilterEntitiesIntegrationTestGroup2";
  private static final String testGroup3Key = "FilterEntitiesIntegrationTestGroup3";

  private static final String testCustomSchemaName = Schema.EXTENSION_SCHEMA_URN.concat("FilterEntitiesIT");

  private static final Map<String, User> testUsers = new HashMap<>();
  private static final Map<String, Group> testGroups = new HashMap<>();

  @RegisterExtension
  static UserClientScimResponseExtension clearAfterAllUserRequest = UserClientScimResponseExtension
      .forClearingAfterAllExecutions(configureScimClientService(TestProperties.SERVICE_URL)
      .buildUserRequest(requestWithCustomHeaders(SCIMRequest.newBuilder())));

  @RegisterExtension
  static GroupClientScimResponseExtension clearAfterAllGroupRequest = GroupClientScimResponseExtension
      .forClearingAfterAllExecutions(configureScimClientService(TestProperties.SERVICE_URL)
      .buildGroupRequest(requestWithCustomHeaders(SCIMRequest.newBuilder())));

  @RegisterExtension
  static SchemaClientScimResponseExtension clearAfterAllSchemaRequest = SchemaClientScimResponseExtension
      .forClearingAfterAllExecutions(configureScimClientService(TestProperties.SERVICE_URL)
      .buildSchemaRequest(requestWithCustomHeaders(SCIMRequest.newBuilder())));


  @RegisterExtension
  UserClientScimResponseExtension resourceAwareUserRequest = UserClientScimResponseExtension.forClearingAfterEachExecutions(userRequest);

  @RegisterExtension
  GroupClientScimResponseExtension resourceAwareGroupRequest = GroupClientScimResponseExtension.forClearingAfterEachExecutions(groupRequest);

  private static final UserFailSafeClient clearAfterAllUserFailSafeClient = clearAfterAllUserRequest.getFailSafeClient();
  private static final GroupFailSafeClient clearAfterAllGroupFailSafeClient = clearAfterAllGroupRequest.getFailSafeClient();
  private static final SchemaFailSafeClient clearAfterAllSchemaFailSafeClient = clearAfterAllSchemaRequest.getFailSafeClient();

  private final UserFailSafeClient userFailSafeClient = resourceAwareUserRequest.getFailSafeClient();
  private final GroupFailSafeClient groupFailSafeClient = resourceAwareGroupRequest.getFailSafeClient();

  @BeforeAll
  public static void setup() {
    clearAfterAllSchemaFailSafeClient.create(TestData.buildTestSchemaWithAllAttributeTypes(testCustomSchemaName));
    createTestUsers();
    createTestGroups();
  }

  @Test
  @DisplayName("Test Get user filtered by userName")
  public void testGetUsersFilteredByUsername() {
    logger.info("Fetching all Users by userName filter: ");
    List<User> filteredUsers = userFailSafeClient.getAllByFilter(generateFilterQuery(USER_NAME.scimName(), testUser1UserName));

    logger.info("Fetching all Users by full attribute path userName filter");
    List<User> filteredUsersByFullAttrPath = userFailSafeClient.getAllByFilter(generateFilterQuery(USER_NAME.fullAttributePath(), testUser1UserName));

    assertEquals(1, filteredUsers.size(), "Verify filtered Users count");
    assertEquals(filteredUsers, filteredUsersByFullAttrPath, "Verify both responses are same");

    assertEquals(testUsers.get(testUser1UserName).getId(), filteredUsers.get(0).getId(), "Verify resource id");
  }

  @Test
  @DisplayName("Test Get users filtered by email")
  public void testGetUsersFilteredByEmail() {
    String emailValue = testUsers.get(testUser1UserName).getEmails().get(0).getValue();
    testFilteredUsers(EMAILS_VALUE.relativePath(), emailValue);
    testFilteredUsers(EMAILS_VALUE.fullAttributePath(), emailValue);
  }

  @Test
  @DisplayName("Test Users filtered by address")
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
  @DisplayName("Test Get users filtered by roles")
  public void testGetUsersFilteredByRoles() {
    String roleValue = testUsers.get(testUser1UserName).getRoles().get(0).getValue();
    testFilteredUsers(ROLES_VALUE.relativePath(), roleValue);
    testFilteredUsers(ROLES_VALUE.fullAttributePath(), roleValue);
  }

  @Test
  @DisplayName("Test Get users filtered by groups")
  public void testGetUsersFilteredByGroups() {
    logger.info("Fetching User: {}", testUser1UserName);
    String groupDisplayName = userFailSafeClient.getSingle(testUsers.get(testUser1UserName).getId()).getGroups().get(0).getDisplay();
    testFilteredUsers(GROUPS_DISPLAY.relativePath(), groupDisplayName);
    testFilteredUsers(GROUPS_DISPLAY.fullAttributePath(), groupDisplayName);
  }

  @Test
  @DisplayName("Test Get users filtered by enterprise attribute")
  public void testGetUsersFilteredByEnterpriseAttributes() {
    EnterpriseExtension userEnterpriseExtension = (EnterpriseExtension) testUsers.get(testUser1UserName).getExtension(ENTERPRISE_URN);

    testFilteredUsers(EMPLOYEE_NUMBER.fullAttributePath(), userEnterpriseExtension.getEmployeeNumber());
    testFilteredUsers(COST_CENTER.fullAttributePath(), userEnterpriseExtension.getCostCenter());
    testFilteredUsers(DIVISION.fullAttributePath(), userEnterpriseExtension.getDivision());
    testFilteredUsers(DEPARTMENT.fullAttributePath(), userEnterpriseExtension.getDepartment());
    testFilteredUsers(MANAGER_VALUE.fullAttributePath(), userEnterpriseExtension.getManager().getValue());
  }

  @Test
  @DisplayName("Test Get users filtered by custom schema attribute")
  public void testGetUsersFilteredByCustomSchemaAttributes() {
    Extension userCustomExtension = testUsers.get(testUser1UserName).getExtension(testCustomSchemaName);

    for (Map.Entry<String, Object> nextCustomAttribute : userCustomExtension.getAttributes().entrySet()) {
      testFilterByCustomSchemaAttribute(nextCustomAttribute.getKey(), nextCustomAttribute.getValue().toString());
    }
  }

  @Test
  @DisplayName("Test Get users filtered by userName or email")
  public void testGetUsersFilteredByUserNameOREmail() {
    String user1EmailValue = testUsers.get(testUser1UserName).getEmails().get(0).getValue();
    String filterQuery = generateFilterQueryWithOROperator(generateFilterQuery(EMAILS_VALUE.relativePath(), user1EmailValue),
        generateFilterQuery(USER_NAME.scimName(), testUser3UserName));

    String fullRepresentationFilterQuery = generateFilterQueryWithOROperator(generateFilterQuery(EMAILS_VALUE.fullAttributePath(), user1EmailValue),
        generateFilterQuery(USER_NAME.fullAttributePath(), testUser3UserName));

    List<User> filteredUsers = userFailSafeClient.getAllByFilter(filterQuery);

    assertEquals(filteredUsers, userFailSafeClient.getAllByFilter(fullRepresentationFilterQuery));
    assertEquals(3, filteredUsers.size());
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers));
    assertTrue(isUserFetchedInResponse(testUsers.get(testUser3UserName).getId(), filteredUsers));
  }

  @Test
  @DisplayName("Test Get users filtered by country or costCenter")
  public void testGetUsersFilteredByCountryORCostCenter() {
    String costCenter = ((EnterpriseExtension) testUsers.get(testUser1UserName).getExtension(ENTERPRISE_URN)).getCostCenter();
    String addressCountry = testUsers.get(testUser3UserName).getAddresses().get(0).getCountry();

    String filterQuery = generateFilterQueryWithOROperator(generateFilterQuery(ADDRESSES_COUNTRY.relativePath(), addressCountry),
        generateFilterQuery(COST_CENTER.fullAttributePath(), costCenter));

    logger.info("Fetching Users by filter with OR operator: {}", filterQuery);
    List<User> filteredUsers = userFailSafeClient.getAllByFilter(filterQuery);

    assertAll("Verify Fetching filtered Users response",
        () -> assertEquals(3, filteredUsers.size(), "Verify filtered users count"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers), "Verify User: " + testUser1UserName + " exist in the response"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers), "Verify User: " + testUser2UserName + " exist in the response"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser3UserName).getId(), filteredUsers), "Verify User: " + testUser3UserName + " exist in the response")
    );
  }

  @Test
  @DisplayName("Test Get users filtered by locality or Custom attribute")
  public void testGetUsersFilteredByLocalityORCustomStringAttribute() {
    String customStringAttribute = testUsers.get(testUser1UserName).getExtension(testCustomSchemaName)
        .getAttributeValueAsString(CUSTOM_STRING_ATTRIBUTE_NAME);
    String addressLocality = testUsers.get(testUser3UserName).getAddresses().get(0).getLocality();

    String filterQuery = generateFilterQueryWithOROperator(generateFilterQuery(ADDRESSES_LOCALITY.fullAttributePath(), addressLocality),
        generateFilterQuery(String.format("%s:%s", testCustomSchemaName, CUSTOM_STRING_ATTRIBUTE_NAME), customStringAttribute));

    logger.info("Fetching Users by OR operation with custom attribute: {}", filterQuery);
    List<User> filteredUsers = userFailSafeClient.getAllByFilter(filterQuery);

    assertAll("Verify Fetching filtered Users response",
        () -> assertEquals(3, filteredUsers.size(), "Verify filtered users count"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers), "Verify User: " + testUser1UserName + " exist in the response"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers), "Verify User: " + testUser2UserName + " exist in the response"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser3UserName).getId(), filteredUsers), "Verify User: " + testUser3UserName + " exist in the response")
    );
  }

  @Test
  @DisplayName("Test Get users filtered by region or Custom integer attribute")
  public void testGetUsersFilteredByRegionORCustomIntegerAttribute() {
    String customStringAttribute = testUsers.get(testUser1UserName).getExtension(testCustomSchemaName)
        .getAttributeValueAsString(CUSTOM_INTEGER_ATTRIBUTE_NAME);
    String addressRegion = testUsers.get(testUser3UserName).getAddresses().get(0).getRegion();

    String filterQuery = generateFilterQueryWithOROperator(generateFilterQuery(ADDRESSES_REGION.relativePath(), addressRegion),
        generateFilterQuery(String.format("%s:%s", testCustomSchemaName, CUSTOM_INTEGER_ATTRIBUTE_NAME), customStringAttribute));

    logger.info("Fetching Users by OR operation with custom Integer attribute: {}", filterQuery);
    List<User> filteredUsers = userFailSafeClient.getAllByFilter(filterQuery);

    assertAll("Verify Fetching filtered Users response",
        () -> assertEquals(2, filteredUsers.size(), "Verify filtered users count"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers), "Verify User: " + testUser1UserName + " exist in the response"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser3UserName).getId(), filteredUsers), "Verify User: " + testUser3UserName + " exist in the response")
    );
  }

  @Test
  @DisplayName("Test Get users filtered by postalCode or Custom attribute")
  public void testGetUsersFilteredByPostalCodeORCustomBooleanAttribute() {
    String customStringAttribute = testUsers.get(testUser1UserName).getExtension(testCustomSchemaName)
        .getAttributeValueAsString(CUSTOM_BOOLEAN_ATTRIBUTE_NAME);
    String addressPostalCode = testUsers.get(testUser3UserName).getAddresses().get(0).getPostalCode();

    String filterQuery = generateFilterQueryWithOROperator(generateFilterQuery(ADDRESSES_POSTAL_CODE.relativePath(), addressPostalCode),
        generateFilterQuery(String.format("%s:%s", testCustomSchemaName, CUSTOM_BOOLEAN_ATTRIBUTE_NAME), customStringAttribute));

    logger.info("Fetching Users by OR operation with custom Boolean attribute: {}", filterQuery);
    List<User> filteredUsers = userFailSafeClient.getAllByFilter(filterQuery);

    assertAll("Verify Fetching filtered Users response",
        () -> assertEquals(2, filteredUsers.size(), "Verify filtered users count"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers), "Verify User: " + testUser1UserName + " exist in the response"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser3UserName).getId(), filteredUsers), "Verify User: " + testUser3UserName + " exist in the response")
    );
  }

  @Test
  @DisplayName("Test Get users filtered by userName and email")
  public void testGetUsersFilteredByUserNameANDEmail() {
    String emailValue = testUsers.get(testUser1UserName).getEmails().get(0).getValue();
    String filterQuery = generateFilterQueryWithANDOperator(generateFilterQuery(EMAILS_VALUE.relativePath(), emailValue),
        generateFilterQuery(USER_NAME.scimName(), testUser1UserName));

    logger.info("Fetching Users by userName and email: {}", filterQuery);
    List<User> filteredUsers = userFailSafeClient.getAllByFilter(filterQuery);

    assertAll("Verify Fetching filtered Users response",
        () -> assertEquals(1, filteredUsers.size(), "Verify filtered Users count"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers), "Verify User: " + testUser2UserName + " exist in the response")
    );
  }

  @Test
  @DisplayName("Test Get users filtered by userName and groups")
  public void testGetUsersFilteredByUserNameANDGroups() {
    String filterQuery = generateFilterQueryWithANDOperator(generateFilterQuery(GROUPS_DISPLAY.relativePath(), ""),
        generateFilterQuery(USER_NAME.scimName(), testUser1UserName));

    logger.info("Fetching Users by userName and groups: {}", filterQuery);
    List<User> filteredUsers = userFailSafeClient.getAllByFilter(filterQuery);

    assertEquals(0, filteredUsers.size(), "Verify filtered Users count");
  }

  @Test
  @DisplayName("Test Get users filtered by groups and locale")
  public void testGetUsersFilteredByGroupsANDLocale() {
    logger.info("Fetching User: {}", testUser1UserName);
    String groupsDisplayValue = userFailSafeClient.getSingle(testUsers.get(testUser1UserName).getId()).getGroups().get(0).getDisplay();

    String localeValue = testUsers.get(testUser1UserName).getAddresses().get(0).getLocality();
    String filterQuery = generateFilterQueryWithANDOperator(generateFilterQuery(GROUPS_DISPLAY.relativePath(), groupsDisplayValue),
        generateFilterQuery(ADDRESSES_LOCALITY.relativePath(), localeValue));

    logger.info("Fetching Users by groups and locale: {}", filterQuery);
    List<User> filteredUsers = userFailSafeClient.getAllByFilter(filterQuery);

    assertAll("Verify Fetching filtered Users response",
        () -> assertEquals(2, filteredUsers.size(), "Verify filtered users count"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers), "Verify User: " + testUser1UserName + " exist in the response"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers), "Verify User: " + testUser2UserName + " exist in the response")
    );
  }

  @Test
  @DisplayName("Test Get users filtered by costCenter and custom attribute")
  public void testGetUsersFilteredByCostCenterANDCustomAttribute() {
    String costCenterValue = ((EnterpriseExtension) testUsers.get(testUser1UserName).getExtension(ENTERPRISE_URN)).getCostCenter();
    String customAttributeValue = testUsers.get(testUser1UserName).getExtension(testCustomSchemaName)
        .getAttributeValueAsString(CUSTOM_STRING_ATTRIBUTE_NAME);
    String filterQuery = generateFilterQueryWithANDOperator(generateFilterQuery(COST_CENTER.fullAttributePath(), costCenterValue),
        generateFilterQuery(String.format("%s:%s", testCustomSchemaName, CUSTOM_STRING_ATTRIBUTE_NAME), customAttributeValue));

    logger.info("Fetching Users by costCenter and custom attribute: {}", filterQuery);
    List<User> filteredUsers = userFailSafeClient.getAllByFilter(filterQuery);

    assertAll("Verify Fetching filtered Users response",
        () -> assertEquals(2, filteredUsers.size(), "Verify filtered users count"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers), "Verify User: " + testUser1UserName + " exist in the response"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers), "Verify User: " + testUser2UserName + " exist in the response")
    );
  }

  @Test
  @DisplayName("Test Get users filtered by complex filter 1")
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

    logger.info("Fetching Users by complex filter: {}", filterQuery);
    List<User> filteredUsers = userFailSafeClient.getAllByFilter(filterQuery);

    String filterQueryReverseOrder = generateFilterQueryWithOROperator(filterSecondPart, filterFirstPart);
    logger.info("Fetching Users by complex filter in reverse order: {}", filterQueryReverseOrder);
    List<User> filteredUsersByReversedOrderFilter = userFailSafeClient.getAllByFilter(filterQueryReverseOrder);

    assertAll("Verify Fetching filtered Users response",
        () -> assertEquals(filteredUsers, filteredUsersByReversedOrderFilter, "Verify users filtered by normal and reverse filter order are same"),
        () -> assertEquals(3, filteredUsers.size(), "Verify filtered users count"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers), "Verify User: " + testUser1UserName + " exist in the response"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers), "Verify User: " + testUser2UserName + " exist in the response"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser3UserName).getId(), filteredUsers), "Verify User: " + testUser3UserName + " exist in the response")
    );
  }

  @Test
  @DisplayName("Test Get users filtered by complex filter 2")
  public void testGetUsersFilteredByComplexFilter2() {
    String user3AddressesLocality = testUsers.get(testUser3UserName).getAddresses().get(0).getLocality();
    String user1CustomAttribute = testUsers.get(testUser1UserName).getExtension(testCustomSchemaName)
        .getAttributeValueAsString(CUSTOM_STRING_ATTRIBUTE_NAME);

    logger.info("Fetching User: {}", testUser1UserName);
    String user1GroupsDisplayValue = userFailSafeClient.getSingle(testUsers.get(testUser1UserName).getId()).getGroups().get(0).getDisplay();

    String filterFirstPart = generateFilterQueryWithOROperator(generateFilterQuery(COST_CENTER.fullAttributePath(), user3AddressesLocality),
        generateFilterQuery(String.format("%s:%s", testCustomSchemaName, CUSTOM_STRING_ATTRIBUTE_NAME), user1CustomAttribute));
    String filterSecondPart = generateFilterQuery(GROUPS_DISPLAY.fullAttributePath(), user1GroupsDisplayValue);

    String filterQuery = generateFilterQueryWithANDOperator(filterFirstPart, filterSecondPart);

    logger.info("Fetching Users by complex filter: {}", filterQuery);
    List<User> filteredUsers = userFailSafeClient.getAllByFilter(filterQuery);

    String filterQueryReverseOrder = generateFilterQueryWithOROperator(filterSecondPart, filterFirstPart);
    logger.info("Fetching Users by complex filter in reverse order: {}", filterQueryReverseOrder);
    List<User> filteredUsersByReversedOrderFilter = userFailSafeClient.getAllByFilter(filterQueryReverseOrder);

    assertAll("Verify Fetching filtered Users response",
        () -> assertEquals(filteredUsers, filteredUsersByReversedOrderFilter, "Verify users filtered by normal and reverse filter order are same"),
        () -> assertEquals(2, filteredUsers.size(), "Verify filtered users count"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers), "Verify User: " + testUser1UserName + " exist in the response"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers), "Verify User: " + testUser2UserName + " exist in the response")
    );
  }

  @Test
  @DisplayName("Test Get groups filtered by displayName twice with AND operator")
  public void testGetGroupsFilteredByDisplayNameAttributeWithAND() {
    String filterQuery = generateFilterQueryWithANDOperator(generateFilterQuery(DISPLAY_NAME.scimName(), testGroup1DisplayName),
        generateFilterQuery(DISPLAY_NAME.scimName(), testGroup2DisplayName));

    logger.info("Fetching Users filtered by displayName twice with AND operator: {}", filterQuery);
    List<Group> filteredGroups = groupFailSafeClient.getAllByFilter(filterQuery);

    assertEquals(0, filteredGroups.size(), "Verify Fetched Users count");
  }

  @Test
  @DisplayName("Test Get groups filtered by displayName and non existing value")
  public void testGetGroupsFilteredByDisplayNameAttributeWithANDNonExistingGroup1() {
    String filterQuery = generateFilterQueryWithANDOperator(
        generateFilterQuery(DISPLAY_NAME.scimName(), testGroup1DisplayName),
        generateFilterQuery(DISPLAY_NAME.scimName(), "non-existing-gn")
    );

    logger.info("Fetching Groups filtered by displayName andd non existing attribute: {}", filterQuery);
    List<Group> filteredGroups = groupFailSafeClient.getAllByFilter(filterQuery);

    assertEquals(0, filteredGroups.size(), "Verify Fetched Groups count");
  }

  @Test
  @DisplayName("Test Get groups filtered by displayName and non existing value 2")
  public void testGetGroupsFilteredByDisplayNameAttributeWithANDNonExistingGroup2() {
    String filterQuery = generateFilterQueryWithANDOperator(
        generateFilterQuery(DISPLAY_NAME.scimName(), ""),
        generateFilterQuery(DISPLAY_NAME.scimName(), testGroup2DisplayName)
    );

    logger.info("Fetching Groups filtered by displayName twice with AND operator: {}", filterQuery);
    List<Group> filteredGroups = groupFailSafeClient.getAllByFilter(filterQuery);

    assertEquals(0, filteredGroups.size(), "Verify Fetched Groups count");
  }

  @Test
  @DisplayName("Test Get groups filtered by displayName and non existing value 3")
  public void testGetGroupsFilteredByDisplayNameAttributeWithANDNonExistingGroup3() {
    String filterQuery = generateFilterQueryWithANDOperator(generateFilterQuery(DISPLAY_NAME.scimName(), ""),
        generateFilterQuery(DISPLAY_NAME.scimName(), ""));

    logger.info("Fetching Groups filtered by displayName twice with AND operator: {}", filterQuery);
    List<Group> filteredGroups = groupFailSafeClient.getAllByFilter(filterQuery);

    assertEquals(0, filteredGroups.size(), "Verify Fetched Groups count");
  }

  @Test
  @DisplayName("Test Get groups filtered by displayName")
  public void testGetGroupsFilteredByDisplayNameAttribute() {
    logger.info("Fetching Groups filtered by displayName");
    List<Group> filteredGroups = groupFailSafeClient.getAllByFilter(generateFilterQuery(DISPLAY_NAME.scimName(), testGroup1DisplayName));

    logger.info("Fetching Groups filtered by displayName with full attribute path");
    List<Group> filteredGroupsByFullAttrPath = groupFailSafeClient.getAllByFilter(generateFilterQuery(DISPLAY_NAME.fullAttributePath(), testGroup1DisplayName));

    assertAll("Verify Fetching filtered Users response",
        () -> assertEquals(filteredGroups, filteredGroupsByFullAttrPath, "Verify Groups filtered by relative and full attribute path filter are same"),
        () -> assertEquals(2, filteredGroups.size(), "Verify filtered Groups count"),
        () -> assertTrue(isGroupFetchedInResponse(testGroups.get(testGroup1DisplayName).getId(), filteredGroups), "Verify Group: " + testGroup1DisplayName + " exist in the response"),
        () -> assertTrue(isGroupFetchedInResponse(testGroups.get(testGroup3Key).getId(), filteredGroups), "Verify Group: " + testGroup3Key + " exist in the response")
    );
  }

  @Test
  @DisplayName("Test Get groups filtered by displayName twice with OR")
  public void testGetGroupsFilteredByDisplayNameAttributeWithOR() {
    String filterQuery = generateFilterQueryWithOROperator(
        generateFilterQuery(DISPLAY_NAME.scimName(), testGroup1DisplayName),
        generateFilterQuery(DISPLAY_NAME.scimName(), testGroup2DisplayName)
    );

    logger.info("Fetching Groups filtered by displayName twice with OR operator: {}", filterQuery);
    List<Group> filteredGroups = groupFailSafeClient.getAllByFilter(filterQuery);

    String filterQueryFullAttrPath = generateFilterQueryWithOROperator(
        generateFilterQuery(DISPLAY_NAME.fullAttributePath(), testGroup1DisplayName),
        generateFilterQuery(DISPLAY_NAME.fullAttributePath(), testGroup2DisplayName)
    );

    logger.info("Fetching Groups filtered by full attribyte path displayName twice with OR operator: {}", filterQueryFullAttrPath);
    List<Group> filteredGroupsByFullAttrPath = groupFailSafeClient.getAllByFilter(filterQueryFullAttrPath);

    assertAll("Verify Fetching filtered Users response",
        () -> assertEquals(filteredGroups, filteredGroupsByFullAttrPath, "Verify Groups filtered by relative and full attribute path filter are same"),
        () -> assertEquals(3, filteredGroups.size(), "Verify filtered Groups count"),
        () -> assertTrue(isGroupFetchedInResponse(testGroups.get(testGroup1DisplayName).getId(), filteredGroups), "Verify Group: " + testGroup1DisplayName + " exist in the response"),
        () -> assertTrue(isGroupFetchedInResponse(testGroups.get(testGroup2DisplayName).getId(), filteredGroups), "Verify Group: " + testGroup2DisplayName + " exist in the response"),
        () -> assertTrue(isGroupFetchedInResponse(testGroups.get(testGroup3Key).getId(), filteredGroups), "Verify Group: " + testGroup3Key + " exist in the response")
    );
  }

  @Test
  @DisplayName("Test Get groups filtered by displayName Or non existing value")
  public void testGetGroupsFilteredByDisplayNameAttributeWithORNonExistingGroup1() {
    String filterQuery = generateFilterQueryWithOROperator(
        generateFilterQuery(DISPLAY_NAME.scimName(), testGroup1DisplayName),
        generateFilterQuery(DISPLAY_NAME.scimName(), "non-existing-gn")
    );

    logger.info("Fetching Groups filtered by displayName OR non existing Group: {}", filterQuery);
    List<Group> filteredGroups = groupFailSafeClient.getAllByFilter(filterQuery);

    String filterQueryFullAttributePath = generateFilterQueryWithOROperator(
        generateFilterQuery(DISPLAY_NAME.fullAttributePath(), testGroup1DisplayName),
        generateFilterQuery(DISPLAY_NAME.fullAttributePath(), "non-existing-gn")
    );

    logger.info("Fetching Groups filtered by full attribute path displayName OR non existing Group: {}", filterQueryFullAttributePath);
    List<Group> filteredGroupsByFullAttrPath = groupFailSafeClient.getAllByFilter(filterQueryFullAttributePath);

    assertAll("Verify Fetching filtered Users response",
        () -> assertEquals(filteredGroups, filteredGroupsByFullAttrPath, "Verify Groups filtered by relative and full attribute path filter are same"),
        () -> assertEquals(2, filteredGroups.size(), "Verify filtered Groups count"),
        () -> assertTrue(isGroupFetchedInResponse(testGroups.get(testGroup1DisplayName).getId(), filteredGroups), "Verify Group: " + testGroup1DisplayName + " exist in the response"),
        () -> assertTrue(isGroupFetchedInResponse(testGroups.get(testGroup3Key).getId(), filteredGroups), "Verify Group: " + testGroup3Key + " exist in the response")
    );
  }

  @Test
  @DisplayName("Test Get groups filtered by displayName Or non existing value 2")
  public void testGetGroupsFilteredByDisplayNameAttributeWithAndNonExistingGroup2() {
    String filterQuery = generateFilterQueryWithOROperator(
        generateFilterQuery(DISPLAY_NAME.scimName(), ""),
        generateFilterQuery(DISPLAY_NAME.scimName(), testGroup2DisplayName)
    );

    logger.info("Fetching Groups filtered by displayName OR non existing Group: {}", filterQuery);
    List<Group> filteredGroups = groupFailSafeClient.getAllByFilter(filterQuery);

    String filterQueryFullAttributePath = generateFilterQueryWithOROperator(
        generateFilterQuery(DISPLAY_NAME.fullAttributePath(), ""),
        generateFilterQuery(DISPLAY_NAME.fullAttributePath(), testGroup2DisplayName)
    );

    logger.info("Fetching Groups filtered by full attribute path displayName OR non existing Group: {}", filterQueryFullAttributePath);
    List<Group> filteredGroupsByFullAttrPath = groupFailSafeClient.getAllByFilter(filterQueryFullAttributePath);

    assertAll("Verify Fetching filtered Users response",
        () -> assertEquals(filteredGroups, filteredGroupsByFullAttrPath, "Verify Groups filtered by relative and full attribute path filter are same"),
        () -> assertEquals(1, filteredGroups.size(), "Verify filtered Groups count"),
        () -> assertTrue(isGroupFetchedInResponse(testGroups.get(testGroup2DisplayName).getId(), filteredGroups), "Verify Group: " + testGroup2DisplayName + " exist in the response")
    );
  }

  @Test
  @DisplayName("Test Get groups filtered by displayName Or non existing value 3")
  public void testGetGroupsFilteredByDisplayNameAttributeWithAndNonExistingGroup3() {
    String filterQuery = generateFilterQueryWithOROperator(
        generateFilterQuery(DISPLAY_NAME.scimName(), ""),
        generateFilterQuery(DISPLAY_NAME.scimName(), "")
    );

    logger.info("Fetching Groups filtered by displayName OR non existing Group: {}", filterQuery);
    List<Group> filteredGroups = groupFailSafeClient.getAllByFilter(filterQuery);

    String filterQueryFullAttributePath = generateFilterQueryWithOROperator(
        generateFilterQuery(DISPLAY_NAME.fullAttributePath(), ""),
        generateFilterQuery(DISPLAY_NAME.fullAttributePath(), "")
    );

    logger.info("Fetching Groups filtered by full attribute path displayName OR non existing Group: {}", filterQueryFullAttributePath);
    List<Group> filteredGroupsByFullAttrPath = groupFailSafeClient.getAllByFilter(filterQueryFullAttributePath);

    assertAll("Verify Fetching filtered Users response",
        () -> assertEquals(filteredGroups, filteredGroupsByFullAttrPath, "Verify Groups filtered by relative and full attribute path filter are same"),
        () -> assertEquals(0, filteredGroups.size(), "Verify filtered Groups count")
    );
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
    logger.info("Fetching all Users by {} filter", filterAttribute);
    List<User> filteredUsers = userFailSafeClient.getAllByFilter(generateFilterQuery(filterAttribute, filterValue));

    assertAll("Verify Fetching filtered Users response",
        () -> assertEquals(2, filteredUsers.size(), "Verify filtered users count"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers), "Verify User: " + testUser1UserName + " exist in the response"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers), "Verify User: " + testUser2UserName + " exist in the response")
    );
  }

  private void testFilterByCustomSchemaAttribute(final String filterAttribute, final String filterValue) {
    logger.info("Fetching all Users by custom schema attribute: {} ", filterAttribute);
    List<User> filteredUsers = userFailSafeClient.getAllByFilter(generateFilterQuery(String.format("%s:%s", testCustomSchemaName, filterAttribute), filterValue));

    assertAll("Verify Fetching filtered Users response",
        () -> assertEquals(2, filteredUsers.size(), "Verify filtered users count"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser1UserName).getId(), filteredUsers), "Verify User: " + testUser1UserName + " exist in the response"),
        () -> assertTrue(isUserFetchedInResponse(testUsers.get(testUser2UserName).getId(), filteredUsers), "Verify User: " + testUser2UserName + " exist in the response")
    );
  }

  private static void createTestUsers() {
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

    testUsers.put(testUser1UserName, clearAfterAllUserFailSafeClient.create(testUser1));
    testUsers.put(testUser2UserName, clearAfterAllUserFailSafeClient.create(testUser2));
    testUsers.put(testUser3UserName, clearAfterAllUserFailSafeClient.create(testUser3));
    testUsers.put(testUser4UserName, clearAfterAllUserFailSafeClient.create(testUser4));
  }

  private static void createTestGroups() {
    Group testGroup1 = buildGroup(testGroup1DisplayName, testUsers.get(testUser1UserName).getId(), testUsers.get(testUser2UserName).getId());
    Group testGroup2 = buildGroup(testGroup2DisplayName, testUsers.get(testUser2UserName).getId());
    Group testGroup3 = new Group.Builder(testGroup1DisplayName).build();

    logger.info("Creating multiple Groups: {} | {} | {}", testGroup1DisplayName, testGroup2DisplayName, testGroup1DisplayName);
    testGroups.put(testGroup1DisplayName, clearAfterAllGroupFailSafeClient.create(testGroup1));
    testGroups.put(testGroup2DisplayName, clearAfterAllGroupFailSafeClient.create(testGroup2));
    testGroups.put(testGroup3Key, clearAfterAllGroupFailSafeClient.create(testGroup3));
  }
}
