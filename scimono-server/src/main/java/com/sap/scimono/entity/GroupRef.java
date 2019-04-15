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

import static com.sap.scimono.entity.definition.ResourceConstants.MultivaluedAttributeConstants.DISPLAY_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.MultivaluedAttributeConstants.OPERATION_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.MultivaluedAttributeConstants.PRIMARY_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.MultivaluedAttributeConstants.REF_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.MultivaluedAttributeConstants.TYPE_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.MultivaluedAttributeConstants.VALUE_FIELD;
import static com.sap.scimono.helper.Strings.isNullOrEmpty;
import static java.util.Objects.hash;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.scimono.entity.base.MultiValuedAttribute;
import com.sap.scimono.entity.base.MultiValuedAttributeType;
import com.sap.scimono.helper.Strings;

/**
 * This class represents a Reference of a Group
 * <p>
 * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2">SCIM core schema
 * 2.0, section 3.2</a>
 * </p>
 */
public final class GroupRef extends MultiValuedAttribute implements Serializable {

  private static final long serialVersionUID = -3765452569557089013L;

  private final Type type;

  @JsonCreator
  private GroupRef(@JsonProperty(OPERATION_FIELD) final String operation, @JsonProperty(value = VALUE_FIELD, required = true) final String value,
      @JsonProperty(DISPLAY_FIELD) final String display, @JsonProperty(PRIMARY_FIELD) final boolean primary, @JsonProperty(REF_FIELD) final String reference,
      @JsonProperty(TYPE_FIELD) final Type type) {
    super(operation, value, display, primary, reference);
    this.type = type;
  }

  private GroupRef(final Builder builder) {
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
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final GroupRef other = (GroupRef) obj;
    return Objects.equals(type, other.type);
  }

  @Override
  public String toString() {
    Map<String, Object> valuesToDisplay = new LinkedHashMap<>();

    valuesToDisplay.put(DISPLAY_FIELD, getDisplay());
    valuesToDisplay.put(VALUE_FIELD, getValue());
    valuesToDisplay.put(TYPE_FIELD, type);
    valuesToDisplay.put(PRIMARY_FIELD, isPrimary());
    valuesToDisplay.put(OPERATION_FIELD, getOperation());
    valuesToDisplay.put(REF_FIELD, getReference());

    return Strings.createPrettyEntityString(valuesToDisplay, this.getClass());
  }

  /**
   * Builder class that is used to build {@link GroupRef} instances
   */
  public static class Builder extends MultiValuedAttribute.Builder {

    private Type type;

    public Builder(){

    }

    public Builder(GroupRef groupRef){
      super(groupRef);
      this.type = groupRef.type;
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
     * Sets the label indicating the attribute's function (See {@link GroupRef#getType()}).
     *
     * @param type the type of the attribute
     * @return the builder itself
     */
    public Builder setType(final Type type) {
      this.type = type;
      return this;
    }

    @Override
    public Builder setReference(final String reference) {
      super.setReference(reference);
      return this;
    }

    @Override
    public GroupRef build() {
      return new GroupRef(this);
    }

  }

  /**
   * Represents an Group reference type.
   */
  public static class Type extends MultiValuedAttributeType {
    private static final long serialVersionUID = 3801043985023173628L;
    /**
     * The User is in the Group directly.
     */
    public static final Type DIRECT = new Type("direct");
    /**
     * The User is not directly in the Group but in another Group that is in the Group
     */
    public static final Type INDIRECT = new Type("indirect");

    private Type(final String typeName) {
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
