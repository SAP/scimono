
package com.sap.scim.entity.definition;

import static com.sap.scim.entity.definition.EnterpriseUserAttributes.Constants.*;
import static com.sap.scim.entity.definition.ResourceConstants.DISPLAY_NAME_FIELD;
import static com.sap.scim.entity.definition.ResourceConstants.MultivaluedAttributeConstants.REF_FIELD;
import static com.sap.scim.entity.definition.ResourceConstants.MultivaluedAttributeConstants.VALUE_FIELD;

import com.sap.scim.entity.EnterpriseExtension;

public enum EnterpriseUserAttributes implements ScimAttribute<EnterpriseUserAttributes> {
  // @formatter:off
  EMPLOYEE_NUMBER(EMPLOYEE_NUMBER_FIELD, null),
  COST_CENTER(COST_CENTER_FIELD, null),
  ORGANIZATION(ORGANIZATION_FIELD, null),
  DIVISION(DIVISION_FIELD, null),
  DEPARTMENT(DEPARTMENT_FIELD, null),
  MANAGER(MANAGER_FIELD, null),
  MANAGER_VALUE(VALUE_FIELD, MANAGER),
  MANAGER_DISPLAY_NAME(DISPLAY_NAME_FIELD, MANAGER),
  MANAGER_REF(REF_FIELD, MANAGER);
  // @formatter:on

  private static final ScimAttributesFactory<EnterpriseUserAttributes> ENTERPRISE_ATTRIBUTES_FACTORY = new ScimAttributesFactory<>(EnterpriseUserAttributes::values);

  private ScimAttribute<EnterpriseUserAttributes> scimAttribute;

  EnterpriseUserAttributes(String attributeName, EnterpriseUserAttributes parent) {
    scimAttribute = new ScimAttributeImpl<>(attributeName, EnterpriseExtension.ENTERPRISE_URN, parent, EnterpriseUserAttributes::values);
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
  public EnterpriseUserAttributes subAttributeFrom(String childAttributeName) {
    return scimAttribute.subAttributeFrom(childAttributeName);
  }

  @Override
  public EnterpriseUserAttributes getParent() {
    return scimAttribute.getParent();
  }

  @Override
  public String getSchemaId() {
    return scimAttribute.getSchemaId();
  }

  public static EnterpriseUserAttributes from(String attributeName) {
    return ENTERPRISE_ATTRIBUTES_FACTORY.from(attributeName);
  }

  public interface Constants {
    String EMPLOYEE_NUMBER_FIELD = "employeeNumber";
    String COST_CENTER_FIELD = "costCenter";
    String ORGANIZATION_FIELD = "organization";
    String DIVISION_FIELD = "division";
    String DEPARTMENT_FIELD = "department";
    String MANAGER_FIELD = "manager";
  }
}
