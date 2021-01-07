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

import static com.sap.scimono.entity.definition.EnterpriseUserAttributes.MANAGER_DISPLAY_NAME;
import static com.sap.scimono.entity.definition.EnterpriseUserAttributes.MANAGER_REF;
import static com.sap.scimono.entity.definition.EnterpriseUserAttributes.MANAGER_VALUE;
import static com.sap.scimono.entity.definition.ResourceConstants.DISPLAY_NAME_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.MultivaluedAttributeConstants.REF_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.MultivaluedAttributeConstants.VALUE_FIELD;
import static java.util.Objects.hash;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sap.scimono.helper.Strings;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Java class for manager complex type.
 */
@JsonInclude(Include.NON_EMPTY)
public final class Manager implements Serializable {

  private static final long serialVersionUID = -1341197800095918850L;

  @JsonProperty(VALUE_FIELD)
  private final String value;
  @JsonProperty(REF_FIELD)
  private final String reference;
  @JsonProperty
  private final String displayName;

  @JsonCreator
  public Manager(@JsonProperty(VALUE_FIELD) final String value, @JsonProperty(REF_FIELD) final String reference,
      @JsonProperty(DISPLAY_NAME_FIELD) final String displayName) {
    this.value = value;
    this.reference = reference;
    this.displayName = displayName;
  }

  private Manager(final Builder builder) {
    value = builder.value;
    reference = builder.reference;
    displayName = builder.displayName;
  }

  public String getValue() {
    return value;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getReference() {
    return reference;
  }

  @JsonIgnore
  public boolean isEmpty() {
    return Strings.isNullOrEmpty(value);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + hash(displayName);
    result = prime * result + hash(value);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Manager)) {
      return false;
    }
    Manager other = (Manager) obj;
    if (!Objects.equals(displayName, other.displayName)) {
      return false;
    }
    return Objects.equals(value, other.value);
  }

  public static class Builder {
    private String value;
    private String reference;
    private String displayName;

    public Builder() {
      // Default constructor used to instantiate new Manager objects
    }

    public Builder(Manager manager) {
      // Default constructor used to instantiate new Manager objects
      this.value = manager.value;
      this.reference = manager.reference;
      this.displayName = manager.displayName;
    }

    public Builder(final Map<String, String> managerValues) {
      if (managerValues != null) {
        value = managerValues.get(MANAGER_VALUE.scimName());
        reference = managerValues.get(MANAGER_REF.scimName());
        displayName = managerValues.get(MANAGER_DISPLAY_NAME.scimName());
      }
    }

    public Builder setValue(final String value) {
      this.value = value;
      return this;
    }

    public Builder setReference(final String reference) {
      this.reference = reference;
      return this;
    }

    public Builder setDisplayName(final String displayName) {
      this.displayName = displayName;
      return this;
    }

    public Manager build() {
      return new Manager(this);
    }
  }
}
