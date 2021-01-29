
package com.sap.scimono.helper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.schema.Schema;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class UnnecessarySchemasEraserTest {

  private static final String TEST_USER = "pesho";
  private static final Function<String, String> SCHEMA_SHOULD_BE_PRESENT_MESSAGE = schema -> String.format("%s should be present", schema);
  private static final Function<String, String> SCHEMA_SHOULD_BE_REMOVED_MESSAGE = schema -> String.format("%s should be removed", schema);

  @Test
  void testUnnecessaryCustomchemaShouldBeIgnored() {
    final String customSchemaWithProvidedAttributes = Schema.EXTENSION_SCHEMA_URN + "Custom1";
    final String customSchemaWithoutProvidedAttributes = Schema.EXTENSION_SCHEMA_URN + "Custom2";

    Set<String> providedSchemas = new HashSet<>();
    providedSchemas.add(customSchemaWithProvidedAttributes);
    providedSchemas.add(customSchemaWithoutProvidedAttributes);
    providedSchemas.add(User.SCHEMA);

    // @formatter:off
    User user = new User.Builder(TEST_USER)
        .addExtension(new Extension.Builder(customSchemaWithProvidedAttributes).build())
        .setSchemas(providedSchemas).build();
    // @formatter:on

    UnnecessarySchemasEraser<User> unnecessarySchemasEraser = new UnnecessarySchemasEraser<>();
    User userWithNecessarySchemas = unnecessarySchemasEraser.eraseAllUnnecessarySchemas(user, User.SCHEMA);

    Set<String> userSchemas = userWithNecessarySchemas.getSchemas();
    assertFalse(SCHEMA_SHOULD_BE_REMOVED_MESSAGE.apply(customSchemaWithoutProvidedAttributes),
        userSchemas.contains(customSchemaWithoutProvidedAttributes));
    assertTrue(SCHEMA_SHOULD_BE_PRESENT_MESSAGE.apply(customSchemaWithProvidedAttributes), userSchemas.contains(customSchemaWithProvidedAttributes));
  }

  @Test
  void testUnnecessaryCoreSchemaShouldBeIgnored() {
    Set<String> providedSchemas = new HashSet<>();
    providedSchemas.add(Group.SCHEMA);
    providedSchemas.add(User.SCHEMA);

    User user = new User.Builder(TEST_USER).setSchemas(providedSchemas).build();

    UnnecessarySchemasEraser<User> unnecessarySchemasEraser = new UnnecessarySchemasEraser<>();
    User userWithNecessarySchemas = unnecessarySchemasEraser.eraseAllUnnecessarySchemas(user, User.SCHEMA);

    Set<String> userSchemas = userWithNecessarySchemas.getSchemas();
    assertFalse(SCHEMA_SHOULD_BE_REMOVED_MESSAGE.apply(Group.SCHEMA), userSchemas.contains(Group.SCHEMA));
    assertTrue(SCHEMA_SHOULD_BE_PRESENT_MESSAGE.apply(User.SCHEMA), userSchemas.contains(User.SCHEMA));
  }

  @Test
  void testCoreExtensionFromAnotherResourceTypeShouldBeIgnored() {
    User user = new User.Builder(TEST_USER).addExtension(new Extension.Builder(Group.SCHEMA).build()).build();

    UnnecessarySchemasEraser<User> unnecessarySchemasEraser = new UnnecessarySchemasEraser<>();
    User userWithNecessarySchemas = unnecessarySchemasEraser.eraseAllUnnecessarySchemas(user, User.SCHEMA);

    assertFalse(SCHEMA_SHOULD_BE_REMOVED_MESSAGE.apply(Group.SCHEMA), userWithNecessarySchemas.isExtensionPresent(Group.SCHEMA));
    assertFalse(SCHEMA_SHOULD_BE_REMOVED_MESSAGE.apply(Group.SCHEMA), userWithNecessarySchemas.getSchemas().contains(Group.SCHEMA));
  }

}
