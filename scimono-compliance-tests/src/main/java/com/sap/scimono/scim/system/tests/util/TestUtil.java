
package com.sap.scimono.scim.system.tests.util;

import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.schema.Schema;

import jakarta.ws.rs.core.UriBuilder;

import static com.sap.scimono.api.API.GROUPS;
import static com.sap.scimono.api.API.SCHEMAS;
import static com.sap.scimono.api.API.USERS;
import static com.sap.scimono.scim.system.tests.util.TestProperties.SERVICE_URL;

public class TestUtil {

  public static String constructResourceLocation(final Resource<?> resource) {
    UriBuilder resourcePath = UriBuilder.fromUri(SERVICE_URL);

    if (resource instanceof Group) {
      resourcePath.path(GROUPS);
    } else if (resource instanceof User) {
      resourcePath.path(USERS);
    } else if (resource instanceof Schema) {
      resourcePath.path(SCHEMAS);
    }

    resourcePath.path(resource.getId());
    return resourcePath.toString();
  }

  protected static String getSchemaName(final String schemaIdOrName) {
    if (SchemasCallback.isCustomSchema(schemaIdOrName)) {
      return schemaIdOrName.substring(Schema.EXTENSION_SCHEMA_URN.length());
    }

    return schemaIdOrName;
  }

}
