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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.scimono.entity.base.MultiValuedAttribute;
import com.sap.scimono.entity.base.MultiValuedAttributeType;
import com.sap.scimono.helper.Strings;

/**
 * This class represents a {@link User} or a {@link Group} which are members of an actual {@link Group}
 * <p>
 * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-8">SCIM core schema
 * 2.0, sections 8</a>
 * </p>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class MemberRef extends MultiValuedAttribute implements Serializable {

  private static final long serialVersionUID = 2965422671682767189L;

  private final Type type;

  /**
   * Constructor for deserialization, it is not intended for general use.
   */
  @JsonCreator
  private MemberRef(@JsonProperty(OPERATION_FIELD) final String operation, @JsonProperty(value = VALUE_FIELD, required = true) final String value,
      @JsonProperty(DISPLAY_FIELD) final String display, @JsonProperty(PRIMARY_FIELD) final Boolean primary, @JsonProperty(REF_FIELD) final String reference,
      @JsonProperty(TYPE_FIELD) final Type type) {
    super(operation, value, display, primary, reference);
    this.type = type;
  }

  private MemberRef(final Builder builder) {
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
    if (!(obj instanceof MemberRef)) {
      return false;
    }
    final MemberRef other = (MemberRef) obj;
    return Objects.equals(type, other.type);
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

  @Override
  @JsonIgnore
  public Boolean isPrimary() {
    return super.isPrimary();
  }

  /**
   * The Builder class is used to construct instances of the {@link MemberRef}
   */
  public static final class Builder extends MultiValuedAttribute.Builder {

    private Type type;

    public Builder() {
    }

    /**
     * builds an Builder based of the given Attribute
     *
     * @param member existing Attribute
     */
    public Builder(final MemberRef member) {
      super(member);
      type = member.type;
    }

    /**
     * builds an Builder based of the given {@link User} or {@link Group}
     *
     * @param resource existing {@link User} or {@link Group}
     */
    public Builder(final Resource<?> resource) {
      setValue(resource.getId());
    }

    @Override
    public Builder setReference(final String reference) {
      super.setReference(reference);
      return this;
    }

    @Override
    public Builder setValue(final String value) {
      super.setValue(value);
      return this;
    }

    @Override
    public Builder setDisplay(final String display) {
      super.setDisplay(display);
      return this;
    }

    /**
     * Sets the label indicating the attribute's function (See {@link MemberRef#getType()}).
     *
     * @param type the type of the attribute
     * @return the builder itself
     */
    public Builder setType(final Type type) {
      this.type = type;
      return this;
    }

    @Override
    public Builder setOperation(final String operation) {
      super.setOperation(operation);
      return this;
    }

    @Override
    public MemberRef build() {
      return new MemberRef(this);
    }
  }

  /**
   * Represents an Member reference type.
   */
  public static class Type extends MultiValuedAttributeType {
    private static final long serialVersionUID = -5924495425354434388L;
    /**
     * indicates that the member is a {@link User}
     */
    public static final Type USER = new Type(User.RESOURCE_TYPE_USER);
    /**
     * indicates that the member is a {@link Group}
     */
    public static final Type GROUP = new Type(Group.RESOURCE_TYPE_GROUP);

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
