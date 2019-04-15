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

package com.sap.scim.entity;

import static com.sap.scim.entity.definition.ResourceConstants.MultivaluedAttributeConstants.DISPLAY_FIELD;
import static com.sap.scim.entity.definition.ResourceConstants.MultivaluedAttributeConstants.OPERATION_FIELD;
import static com.sap.scim.entity.definition.ResourceConstants.MultivaluedAttributeConstants.PRIMARY_FIELD;
import static com.sap.scim.entity.definition.ResourceConstants.MultivaluedAttributeConstants.REF_FIELD;
import static com.sap.scim.entity.definition.ResourceConstants.MultivaluedAttributeConstants.TYPE_FIELD;
import static com.sap.scim.entity.definition.ResourceConstants.MultivaluedAttributeConstants.VALUE_FIELD;
import static com.sap.scim.helper.Strings.isNullOrEmpty;
import static java.util.Objects.hash;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.scim.entity.base.MultiValuedAttribute;
import com.sap.scim.entity.base.MultiValuedAttributeType;
import com.sap.scim.helper.Strings;

/**
 * This class represents a phoneNumber attribute.
 * <p>
 * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2">SCIM core schema
 * 2.0, section 3.2</a>
 * </p>
 */
public final class PhoneNumber extends MultiValuedAttribute implements Serializable {

  private static final long serialVersionUID = -511746669960696470L;

  private final Type type;

  /**
   * Constructor for deserialization, it is not intended for general use.
   */
  @JsonCreator
  private PhoneNumber(@JsonProperty(OPERATION_FIELD) final String operation, @JsonProperty(value = VALUE_FIELD, required = true) final String value,
      @JsonProperty(DISPLAY_FIELD) final String display, @JsonProperty(PRIMARY_FIELD) final boolean primary, @JsonProperty(REF_FIELD) final String reference,
      @JsonProperty(TYPE_FIELD) final Type type) {
    super(operation, value, display, primary, reference);
    this.type = type;
  }

  private PhoneNumber(final Builder builder) {
    super(builder);
    type = builder.type;
  }

  /**
   * Gets the type of the attribute.
   * <p>
   * For more detailed information please look at the <a href= "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2" >SCIM core
   * schema 2.0, section 3.2</a>
   * </p>
   *
   * @return the actual type
   */
  public Type getType() {
    return type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + hash(type);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PhoneNumber)) {
      return false;
    }

    final PhoneNumber other = (PhoneNumber) obj;
    return super.equals(obj) && Objects.equals(type, other.type);
  }

  @Override
  public String toString() {
    Map<String, Object> valuesToDisplay = new LinkedHashMap<>();

    valuesToDisplay.put(VALUE_FIELD, getValue());
    valuesToDisplay.put(TYPE_FIELD, type);
    valuesToDisplay.put(PRIMARY_FIELD, isPrimary());
    valuesToDisplay.put(OPERATION_FIELD, getOperation());

    return Strings.createPrettyEntityString(valuesToDisplay, this.getClass());
  }

  /**
   * Builder class that is used to build {@link PhoneNumber} instances
   */
  public static class Builder extends MultiValuedAttribute.Builder {

    private Type type;

    public Builder() {
    }

    /**
     * builds an Builder based of the given Attribute
     *
     * @param phoneNumber existing Attribute
     */
    public Builder(final PhoneNumber phoneNumber) {
      super(phoneNumber);
      type = phoneNumber.type;
    }

    @Override
    public Builder setOperation(final String operation) {
      super.setOperation(operation);
      return this;
    }

    @Override
    public Builder setDisplay(final String display) {
      super.setDisplay(display);
      return this;

    }

    @Override
    public Builder setValue(final String value) {
      super.setValue(value);
      return this;
    }

    /**
     * Sets the label indicating the attribute's function (See {@link PhoneNumber#getType()}).
     *
     * @param type the type of the attribute
     * @return the builder itself
     */
    public Builder setType(final Type type) {
      this.type = type;
      return this;
    }

    @Override
    public Builder setPrimary(final Boolean primary) {
      super.setPrimary(primary);
      return this;
    }

    @Override
    public PhoneNumber build() {
      return new PhoneNumber(this);
    }
  }

  /**
   * Represents a phone number type. Canonical values are available as static constants.
   */
  public static class Type extends MultiValuedAttributeType {
    private static final long serialVersionUID = 1645839866241553362L;
    public static final Type WORK = new Type("work");
    public static final Type HOME = new Type("home");
    public static final Type MOBILE = new Type("mobile");
    public static final Type FAX = new Type("fax");
    public static final Type PAGER = new Type("pager");
    public static final Type OTHER = new Type("other");

    Type(final String typeName) {
      super(typeName);
    }

    public static Type of(final String typeName) {
      if (isNullOrEmpty(typeName)) {
        return null;
      }
      return new Type(typeName);
    }
  }
}
