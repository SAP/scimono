
package com.sap.scimono.scim.system.tests;

import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.scim.system.tests.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InputValidationAttributeTypeTest extends CompliantSCIMSystemIntegrationTest {

  private static boolean isSetUpExecuted = false;
  private static String TEST_SCHEMA_ID = Schema.EXTENSION_SCHEMA_URN + "TestSchemaAllIds";
  private static int testCounter = 0;

  @BeforeEach
  public void setUpBeforeTest() {
    if (!isSetUpExecuted) {
      isSetUpExecuted = true;
      Schema createdSchema = createSchema(TestData.buildTestSchemaWithAllAttributeTypes(TEST_SCHEMA_ID));
      assertNotNull(createdSchema);
    }
  }

  private User buildTestUser(final String attrName, final Object attrValue) {
    Map<String, Object> attrNameToValue = new HashMap<>();
    attrNameToValue.put(attrName, attrValue);

    Extension userExtension = new Extension.Builder(TEST_SCHEMA_ID).setAttributes(attrNameToValue).build();
    User.Builder testUser = TestData.setAttributesToATestUser("InputTestUser" + testCounter++);

    return testUser.addExtension(userExtension).build();
  }

  @ParameterizedTest
  @MethodSource("provideCustomAttributeTestParameters")
  public void test(int expectedResponseCode, String attrName, Object attrValue) {
    User testUser = buildTestUser(attrName, attrValue);
    SCIMResponse<User> scimResponse = userRequest.createUser(testUser);

    String errorMessage = String.format("Test for user \'%s\' with attribute \'%s\' and value \'%s\' fails!", testUser.getUserName(), attrName,
        attrValue);

    assertEquals(expectedResponseCode, scimResponse.getStatusCode(), errorMessage);
  }

  private static Stream<Arguments> provideCustomAttributeTestParameters() {
    // @formatter:off
    return Stream.of(
        Arguments.of(201, "booleanAttribute", ""),
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

        Arguments.of(201, "integerAttribute", ""),
        Arguments.of(400, "integerAttribute", "true1"),
        Arguments.of(400, "integerAttribute", true),
        Arguments.of(400, "integerAttribute", false),
        Arguments.of(201, "integerAttribute", -1),
        Arguments.of(400, "integerAttribute", 12.12),
        Arguments.of(400, "integerAttribute", "2016-03-01T12:22:33Z"),
        Arguments.of(400, "integerAttribute", "-1"),
        Arguments.of(201, "integerAttribute", null),

        Arguments.of(201, "decimalAttribute", ""),
        Arguments.of(400, "decimalAttribute", "true1"),
        Arguments.of(400, "decimalAttribute", true),
        Arguments.of(400, "decimalAttribute", false),
        Arguments.of(201, "decimalAttribute", -1),
        Arguments.of(201, "decimalAttribute", 12.12),
        Arguments.of(400, "decimalAttribute", "2016-03-01T12:22:33Z"),
        Arguments.of(400, "decimalAttribute", "-12.12"),
        Arguments.of(201, "decimalAttribute", null),

        Arguments.of(201, "stringAttribute", ""),
        Arguments.of(201, "stringAttribute", -12.12),
        Arguments.of(201, "stringAttribute", -3),
        Arguments.of(201, "stringAttribute", "validString1"),
        Arguments.of(201, "stringAttribute", "null"),
        Arguments.of(201, "stringAttribute", null),

        Arguments.of(201, "datetimeAttribute", ""),
        Arguments.of(400, "datetimeAttribute", -12.12),
        Arguments.of(400, "datetimeAttribute", "https://www.facebook.com/joro.ivanov"),
        Arguments.of(400, "datetimeAttribute", "12"),
        Arguments.of(201, "datetimeAttribute", "2016-03-01T12:22:33Z"),
        Arguments.of(201, "datetimeAttribute", "2016-03-01T12:22:33.123Z"),
        Arguments.of(400, "datetimeAttribute", "2016-03-01T12:22:33.123+02"),
        Arguments.of(400, "datetimeAttribute", "2016-03-01"),
        Arguments.of(400, "datetimeAttribute", "2016-03-01T12:22"),
        Arguments.of(400, "datetimeAttribute", "12:22:33"),
        Arguments.of(400, "datetimeAttribute", "2016-03-0112:22:33"),
        Arguments.of(201, "datetimeAttribute", null),

        Arguments.of(201, "binaryAttribute", ""),
        Arguments.of(400, "binaryAttribute", "-12.1"),
        Arguments.of(400, "binaryAttribute", -12.1),
        Arguments.of(400, "binaryAttribute", 1),
        Arguments.of(201, "binaryAttribute", null),
        Arguments.of(201, "binaryAttribute", "U3VtbWVyIHRpbWUgaXMgZ3JlYXQhCkkgbG92ZSB0aGUgc2VhIGFuZCB0aGUgYmVhY2hlcyE=")
    );
    // @formatter:on
  }
}
