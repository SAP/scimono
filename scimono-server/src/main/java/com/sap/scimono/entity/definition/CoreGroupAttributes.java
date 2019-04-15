
package com.sap.scimono.entity.definition;

import static com.sap.scimono.entity.definition.CoreGroupAttributes.Constants.MEMBERS_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.*;
import static com.sap.scimono.entity.definition.ResourceConstants.MultivaluedAttributeConstants.*;

import com.sap.scimono.entity.Group;

public enum CoreGroupAttributes implements ScimAttribute<CoreGroupAttributes> {
  // @formatter:off
  ID(ID_FIELD, null),
  SCHEMAS(SCHEMAS_FIELD, null),
  EXTERNAL_ID(EXTERNAL_ID_FIELD, null),
  META(META_FIELD, null),
  META_CREATED(META_CREATED_FIELD, META),
  META_LAST_MODIFIED(META_LAST_MODIFIED_FIELD, META),
  META_ATTRIBUTES(META_ATTRIBUTES_FIELD, META),
  META_RESOURCE_TYPE(META_RESOURCE_TYPE_FIELD, META),
  META_LOCATION(META_LOCATION_FIELD, META),
  META_VERSION(META_VERSION_FIELD, META),

  DISPLAY_NAME(DISPLAY_NAME_FIELD, null),

  MEMBERS(MEMBERS_FIELD, null),
  MEMBERS_TYPE(TYPE_FIELD, MEMBERS),
  MEMBERS_VALUE(VALUE_FIELD, MEMBERS),
  MEMBERS_OPERATION(OPERATION_FIELD, MEMBERS),
  MEMBERS_DISPLAY(DISPLAY_FIELD, MEMBERS),
  MEMBERS_PRIMARY(PRIMARY_FIELD, MEMBERS),
  MEMBERS_REF(REF_FIELD, MEMBERS);
  // @formatter:on

  private static final ScimAttributesFactory<CoreGroupAttributes> CORE_GROUP_ATTRIBUTES_FACTORY = new ScimAttributesFactory<>(CoreGroupAttributes::values);

  private ScimAttribute<CoreGroupAttributes> scimAttribute;

  CoreGroupAttributes(String attributeName, CoreGroupAttributes parent) {
    scimAttribute = new ScimAttributeImpl<>(attributeName, Group.SCHEMA, parent, CoreGroupAttributes::values);
  }

  @Override
  public boolean isTopLevelAttribute() {
    return scimAttribute.isTopLevelAttribute();
  }

  @Override
  public String scimName() {
    return scimAttribute.scimName();
  }

  @Override
  public String fullAttributePath() {
    return scimAttribute.fullAttributePath();
  }

  @Override
  public String relativePath() {
    return scimAttribute.relativePath();
  }

  @Override
  public CoreGroupAttributes subAttributeFrom(String childAttributeName) {
    return scimAttribute.subAttributeFrom(childAttributeName);
  }

  @Override
  public CoreGroupAttributes getParent() {
    return scimAttribute.getParent();
  }

  @Override
  public String getSchemaId() {
    return scimAttribute.getSchemaId();
  }

  public static CoreGroupAttributes from(String attributeName) {
    return CORE_GROUP_ATTRIBUTES_FACTORY.from(attributeName);
  }

  public interface Constants {
    String MEMBERS_FIELD = "members";
  }
}
