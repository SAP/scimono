
package com.sap.scimono.scim.system.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.scim.system.tests.extensions.SchemaClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.UserClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.util.TestData;
import com.sap.scimono.scim.system.tests.util.TestProperties;

public class InputValidationAttributeTypeTest extends SCIMComplianceTest {
  private static final Logger logger = LoggerFactory.getLogger(InputValidationAttributeTypeTest.class);
  private static String TEST_SCHEMA_ID = Schema.EXTENSION_SCHEMA_URN + "TestSchemaAllIds";
  private static int testCounter = 0;

  @RegisterExtension
  static SchemaClientScimResponseExtension resourceAwareSchemaRequest = SchemaClientScimResponseExtension
      .forClearingAfterAllExecutions(SCIMComplianceTest.configureScimClientService(TestProperties.SERVICE_URL).buildSchemaRequest());

  @RegisterExtension
  UserClientScimResponseExtension resourceAwareUserRequest = UserClientScimResponseExtension.forClearingAfterEachExecutions(userRequest);

  @BeforeAll
  public static void setUpBeforeTest() {
    logger.info("Creating custom schema: {}", TEST_SCHEMA_ID);
    resourceAwareSchemaRequest.createSchema(TestData.buildTestSchemaWithAllAttributeTypes(TEST_SCHEMA_ID));
  }

  private User buildTestUser(final String attrName, final Object attrValue) {
    Map<String, Object> attrNameToValue = new HashMap<>();
    attrNameToValue.put(attrName, attrValue);

    Extension userExtension = new Extension.Builder(TEST_SCHEMA_ID).setAttributes(attrNameToValue).build();
    User.Builder testUser = TestData.setAttributesToATestUser("InputTestUser" + testCounter++);

    return testUser.addExtension(userExtension).build();
  }

  @ParameterizedTest(name = "Test adding value: {2} to attribute: {1} and verify Http status: {0}")
  @MethodSource("provideCustomAttributeTestParameters")
  public void test(final int expectedResponseCode, final String attrName, final Object attrValue) {
    User testUser = buildTestUser(attrName, attrValue);
    logger.info("Creating User: {} with custom schema attributes", testUser.getUserName());
    SCIMResponse<User> scimResponse = resourceAwareUserRequest.createUser(testUser);

    String errorMessage = String.format("Test for user \'%s\' with attribute \'%s\' and value \'%s\' fails!", testUser.getUserName(), attrName,
        attrValue);

    assertEquals(expectedResponseCode, scimResponse.getStatusCode(), errorMessage);
  }

  private static Stream<Arguments> provideCustomAttributeTestParameters() {
    // @formatter:off
    return Stream.of(
        Arguments.of(400, "booleanAttribute", ""),
        Arguments.of(400, "booleanAttribute", "true1"),
        Arguments.of(400, "booleanAttribute", -1),
        Arguments.of(400, "booleanAttribute", 12.12),
        Arguments.of(400, "booleanAttribute", "2016-03-01T12:22:33Z"),
        Arguments.of(201, "booleanAttribute", null),
        Arguments.of(400, "booleanAttribute", "null"),
        Arguments.of(201, "booleanAttribute", true),
        Arguments.of(201, "booleanAttribute", false),
        Arguments.of(400, "booleanAttribute", "true"),
        Arguments.of(400, "booleanAttribute", "false"),

        Arguments.of(400, "integerAttribute", ""),
        Arguments.of(400, "integerAttribute", "true1"),
        Arguments.of(400, "integerAttribute", true),
        Arguments.of(400, "integerAttribute", false),
        Arguments.of(201, "integerAttribute", -1),
        Arguments.of(400, "integerAttribute", 12.12),
        Arguments.of(400, "integerAttribute", "2016-03-01T12:22:33Z"),
        Arguments.of(400, "integerAttribute", "-1"),
        Arguments.of(201, "integerAttribute", null),

        Arguments.of(400, "decimalAttribute", ""),
        Arguments.of(400, "decimalAttribute", "true1"),
        Arguments.of(400, "decimalAttribute", true),
        Arguments.of(400, "decimalAttribute", false),
        Arguments.of(201, "decimalAttribute", -1),
        Arguments.of(201, "decimalAttribute", 12.12),
        Arguments.of(400, "decimalAttribute", "2016-03-01T12:22:33Z"),
        Arguments.of(400, "decimalAttribute", "-12.12"),
        Arguments.of(201, "decimalAttribute", null),

        Arguments.of(201, "stringAttribute", ""),
        Arguments.of(400, "stringAttribute", -12.12),
        Arguments.of(400, "stringAttribute", -3),
        Arguments.of(201, "stringAttribute", "validString1"),
        Arguments.of(201, "stringAttribute", "null"),
        Arguments.of(201, "stringAttribute", null),

        Arguments.of(400, "dateTimeAttribute", ""),
        Arguments.of(400, "dateTimeAttribute", -12.12),
        Arguments.of(400, "dateTimeAttribute", "https://www.facebook.com/joro.ivanov"),
        Arguments.of(400, "dateTimeAttribute", "12"),
        Arguments.of(201, "dateTimeAttribute", "2016-03-01T12:22:33Z"),
        Arguments.of(201, "dateTimeAttribute", "2016-03-01T12:22:33.123Z"),
        Arguments.of(400, "dateTimeAttribute", "2016-03-01T12:22:33.123+02"),
        Arguments.of(400, "dateTimeAttribute", "2016-03-01"),
        Arguments.of(400, "dateTimeAttribute", "2016-03-01T12:22"),
        Arguments.of(400, "dateTimeAttribute", "12:22:33"),
        Arguments.of(400, "dateTimeAttribute", "2016-03-0112:22:33"),
        Arguments.of(201, "dateTimeAttribute", null),

        Arguments.of(400, "binaryAttribute", ""),
        Arguments.of(400, "binaryAttribute", "-12.1"),
        Arguments.of(400, "binaryAttribute", -12.1),
        Arguments.of(400, "binaryAttribute", 1),
        Arguments.of(201, "binaryAttribute", null),
        Arguments.of(201, "binaryAttribute", "U3VtbWVyIHRpbWUgaXMgZ3JlYXQhCkkgbG92ZSB0aGUgc2VhIGFuZCB0aGUgYmVhY2hlcyE=")
    );
    // @formatter:on
  }
}
