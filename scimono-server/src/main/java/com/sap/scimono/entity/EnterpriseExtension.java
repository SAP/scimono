/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2013-2016 tarent solutions GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.sap.scimono.entity;

import static com.sap.scimono.entity.definition.EnterpriseUserAttributes.COST_CENTER;
import static com.sap.scimono.entity.definition.EnterpriseUserAttributes.DEPARTMENT;
import static com.sap.scimono.entity.definition.EnterpriseUserAttributes.DIVISION;
import static com.sap.scimono.entity.definition.EnterpriseUserAttributes.EMPLOYEE_NUMBER;
import static com.sap.scimono.entity.definition.EnterpriseUserAttributes.MANAGER;
import static com.sap.scimono.entity.definition.EnterpriseUserAttributes.ORGANIZATION;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.sap.scimono.entity.base.Extension;

/**
 * Java class for Enterprise User Schema extension. See <a href="https://tools.ietf.org/html/rfc7643#section-4.3">SCIM specification, section 4.3</a>.
 */
@JsonAutoDetect(getterVisibility = Visibility.NONE)
public final class EnterpriseExtension extends Extension {
  public static final String ENTERPRISE_URN = "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User";

  private static final long serialVersionUID = 1625838139038034633L;

  private EnterpriseExtension(final Builder builder) {
    super(builder);
  }

  public String getEmployeeNumber() {
    return getAttributeValueAsString(EMPLOYEE_NUMBER.scimName());
  }

  public String getCostCenter() {
    return getAttributeValueAsString(COST_CENTER.scimName());
  }

  public String getOrganization() {
    return getAttributeValueAsString(ORGANIZATION.scimName());
  }

  public String getDivision() {
    return getAttributeValueAsString(DIVISION.scimName());
  }

  public String getDepartment() {
    return getAttributeValueAsString(DEPARTMENT.scimName());
  }

  public Manager getManager() {
    return (Manager) getAttribute(MANAGER.scimName());
  }

  public static final class Builder extends Extension.Builder {

    public Builder() {
      super(ENTERPRISE_URN);
    }

    public Builder(EnterpriseExtension enterpriseExtension) {
      super(ENTERPRISE_URN);
      setEmployeeNumber(enterpriseExtension.getEmployeeNumber());
      setCostCenter(enterpriseExtension.getCostCenter());
      setOrganization(enterpriseExtension.getOrganization());
      setDivision(enterpriseExtension.getDivision());
      setDepartment(enterpriseExtension.getDepartment());

      Manager enterpriseExtensionManager = enterpriseExtension.getManager();
      if (enterpriseExtensionManager != null) {
        setManager(new Manager.Builder(enterpriseExtensionManager).build());
      }
    }

    @SuppressWarnings("unchecked")
    public Builder(final Map<String, Object> values) {
      this();
      setEmployeeNumber((String) values.get(EMPLOYEE_NUMBER.scimName()));
      setCostCenter((String) values.get(COST_CENTER.scimName()));
      setOrganization((String) values.get(ORGANIZATION.scimName()));
      setDivision((String) values.get(DIVISION.scimName()));
      setDepartment((String) values.get(DEPARTMENT.scimName()));
      setManager(new Manager.Builder((Map<String, String>) values.get(MANAGER.scimName())).build());
    }

    public Builder setEmployeeNumber(final String employeeNumber) {
      setAttribute(EMPLOYEE_NUMBER.scimName(), employeeNumber);
      return this;
    }

    public Builder setCostCenter(final String costCenter) {
      setAttribute(COST_CENTER.scimName(), costCenter);
      return this;
    }

    public Builder setOrganization(final String organization) {
      setAttribute(ORGANIZATION.scimName(), organization);
      return this;
    }

    public Builder setDivision(final String division) {
      setAttribute(DIVISION.scimName(), division);
      return this;
    }

    public Builder setDepartment(final String department) {
      setAttribute(DEPARTMENT.scimName(), department);
      return this;
    }

    public Builder setManager(final Manager manager) {
      if (manager != null && !manager.isEmpty()) {
        setAttribute(MANAGER.scimName(), manager);
      } else {
        setAttribute(MANAGER.scimName(), null);
      }

      return this;
    }

    @Override
    public EnterpriseExtension build() {
      return new EnterpriseExtension(this);
    }
  }

}
