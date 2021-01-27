
package com.sap.scimono.helper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.schema.Schema;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UnnecessarySchemasEraserTest {

  @Test
  void testUnnecessaryCustomchemaShouldBeIgnored() {
    final String customSchemaWithProvidedAttributes = Schema.EXTENSION_SCHEMA_URN + "Custom1";
    final String customSchemaWithoutProvidedAttributes = Schema.EXTENSION_SCHEMA_URN + "Custom2";

    Set<String> providedSchemas = new HashSet<>();
    providedSchemas.add(customSchemaWithProvidedAttributes);
    providedSchemas.add(customSchemaWithoutProvidedAttributes);
    providedSchemas.add(User.SCHEMA);

    // @formatter:off
    User user = new User.Builder("pesho")
        .addExtension(new Extension.Builder(customSchemaWithProvidedAttributes).build())
        .setSchemas(providedSchemas).build();
    // @formatter:on

    UnnecessarySchemasEraser<User> unnecessarySchemasEraser = new UnnecessarySchemasEraser<>();
    User userWithNecessarySchemas = unnecessarySchemasEraser.eraseAllUnnecessarySchemas(user, User.SCHEMA);

    assertFalse(customSchemaWithoutProvidedAttributes + " should be removed",
        userWithNecessarySchemas.getSchemas().contains(customSchemaWithoutProvidedAttributes));
    assertTrue(customSchemaWithProvidedAttributes + " should be present",
        userWithNecessarySchemas.getSchemas().contains(customSchemaWithProvidedAttributes));
  }

  @Test
  void testUnnecessaryCoreSchemaShouldBeIgnored() {
    Set<String> providedSchemas = new HashSet<>();
    providedSchemas.add(Group.SCHEMA);
    providedSchemas.add(User.SCHEMA);

    User user = new User.Builder("pesho").setSchemas(providedSchemas).build();

    UnnecessarySchemasEraser<User> unnecessarySchemasEraser = new UnnecessarySchemasEraser<>();
    User userWithNecessarySchemas = unnecessarySchemasEraser.eraseAllUnnecessarySchemas(user, User.SCHEMA);

    assertFalse(Group.SCHEMA + " should be removed", userWithNecessarySchemas.getSchemas().contains(Group.SCHEMA));
    assertTrue(User.SCHEMA + " should be present", userWithNecessarySchemas.getSchemas().contains(User.SCHEMA));
  }

}
