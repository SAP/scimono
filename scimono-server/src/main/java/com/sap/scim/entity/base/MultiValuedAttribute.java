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

package com.sap.scim.entity.base;

import static com.sap.scim.entity.definition.ResourceConstants.MultivaluedAttributeConstants.DISPLAY_FIELD;
import static com.sap.scim.entity.definition.ResourceConstants.MultivaluedAttributeConstants.OPERATION_FIELD;
import static com.sap.scim.entity.definition.ResourceConstants.MultivaluedAttributeConstants.PRIMARY_FIELD;
import static com.sap.scim.entity.definition.ResourceConstants.MultivaluedAttributeConstants.REF_FIELD;
import static com.sap.scim.entity.definition.ResourceConstants.MultivaluedAttributeConstants.VALUE_FIELD;
import static java.util.Objects.hash;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sap.scim.entity.MemberRef;
import com.sap.scim.helper.Strings;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a multi-valued attribute.
 * <p>
 * For more detailed information please look at the <a href= "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2">SCIM core schema
 * 2.0, section 3.2</a>
 * </p>
 */
@JsonInclude(Include.NON_EMPTY)
public abstract class MultiValuedAttribute implements Serializable {

  private static final long serialVersionUID = 5910207539638462247L;

  private final String operation;
  private final String value;
  private final String display;
  private final Boolean primary;
  @JsonProperty(REF_FIELD)
  private final String reference;

  /**
   * This constructor is present for serialization purposes not for general use. Please don't use it.
   */
  @JsonCreator
  protected MultiValuedAttribute(@JsonProperty(OPERATION_FIELD) final String operation,
      @JsonProperty(value = VALUE_FIELD, required = true) final String value, @JsonProperty(DISPLAY_FIELD) final String display,
      @JsonProperty(PRIMARY_FIELD) final Boolean primary, @JsonProperty(REF_FIELD) final String reference) {
    this.operation = operation;
    this.value = value;
    this.display = display;
    this.primary = primary;
    this.reference = reference;
  }

  protected MultiValuedAttribute(final Builder builder) {
    value = builder.value;
    display = builder.display;
    primary = builder.primary;
    operation = builder.operation;
    reference = builder.reference;
  }

  /**
   * Gets the attribute's significant value; e.g., the e-mail address, phone number etc.
   * <p>
   * For more detailed information please look at the <a href= "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2" >SCIM core
   * schema 2.0, section 3.2</a>
   * </p>
   *
   * @return the value of the actual multi value attribute
   */
  public String getValue() {
    return value;
  }

  /**
   * Gets the human readable name, primarily used for display purposes.
   * <p>
   * For more detailed information please look at the <a href= "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2" >SCIM core
   * schema 2.0, section 3.2</a>
   * </p>
   *
   * @return the display attribute
   */
  public String getDisplay() {
    return display;
  }

  /**
   * Gets a Boolean value indicating the 'primary' or preferred attribute value for this attribute.
   * <p>
   * For more detailed information please look at the <a href= "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2" >SCIM core
   * schema 2.0, section 3.2</a>
   * </p>
   *
   * @return the primary attribute
   */
  public Boolean isPrimary() {
    return primary;
  }

  /**
   * Gets the operation applied during a PATCH request.
   * <p>
   * For more detailed information please look at the <a href= "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2" >SCIM core
   * schema 2.0, section 3.2</a>
   * </p>
   *
   * @return the operation
   */
  public String getOperation() {
    return operation;
  }

  /**
   * Gets the reference to the actual SCIM Resource.
   * <p/>
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-8">SCIM core schema
   * 2.0, sections 8</a>
   * </p>
   *
   * @return the reference of the actual resource
   */
  public String getReference() {
    return reference;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + hash(display);
    result = prime * result + hash(operation);
    result = prime * result + hash(primary);
    result = prime * result + hash(reference);
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
    if (!(obj instanceof MultiValuedAttribute)) {
      return false;
    }
    final MultiValuedAttribute other = (MultiValuedAttribute) obj;
    if (!Objects.equals(display, other.display)) {
      return false;
    }
    if (!Objects.equals(operation, other.operation)) {
      return false;
    }
    if (!Objects.equals(primary, other.primary)) {
      return false;
    }
    if (!Objects.equals(reference, other.reference)) {
      return false;
    }
    return Objects.equals(value, other.value);
  }

  @Override
  public String toString() {

    Map<String, Object> valuesToDisplay = new LinkedHashMap<>();

    valuesToDisplay.put(OPERATION_FIELD, operation);
    valuesToDisplay.put(VALUE_FIELD, value);
    valuesToDisplay.put(DISPLAY_FIELD, display);
    valuesToDisplay.put(PRIMARY_FIELD, primary);
    valuesToDisplay.put(REF_FIELD, reference);

    return Strings.createPrettyEntityString(valuesToDisplay, this.getClass());
  }

  public static boolean isCollectionContainsPrimaryAttributes(final Collection<? extends MultiValuedAttribute> collection) {
    if (collection.isEmpty()) {
      return false;
    }

    return collection.stream().anyMatch(MultiValuedAttribute::isPrimary);
  }

  /**
   * Builder class that is used to build {@link MultiValuedAttribute} instances
   */
  public abstract static class Builder {

    private String operation;
    private String value;
    private String display;
    private Boolean primary;
    private String reference;

    protected Builder() {
      primary = false;
    }

    protected Builder(final MultiValuedAttribute multiValuedAttribute) {
      if (multiValuedAttribute == null) {
        throw new IllegalArgumentException("The given attribute can't be null.");
      }
      operation = multiValuedAttribute.getOperation();
      value = multiValuedAttribute.value;
      display = multiValuedAttribute.display;
      primary = multiValuedAttribute.primary;
    }

    /**
     * Sets the attribute's significant value (See {@link MultiValuedAttribute#getValue()}).
     *
     * @param value the value attribute
     * @return the builder itself
     */
    protected Builder setValue(final String value) {
      this.value = value;
      return this;
    }

    /**
     * Sets the human readable name (See {@link MultiValuedAttribute#getDisplay()}).
     * <p>
     * client info: the Display value is set by the OSIAM server. If a MultiValuedAttribute which is send to the OSIAM server has this value filled,
     * the value will be ignored or the action will be rejected.
     * </p>
     *
     * @param display a human readable name
     * @return the builder itself
     */
    protected Builder setDisplay(final String display) {
      this.display = display;
      return this;
    }

    /**
     * Sets the primary attribute (See {@link MultiValuedAttribute#isPrimary()}).
     *
     * @param primary indicates if this is the primary attribute
     * @return the builder itself
     */
    protected Builder setPrimary(final Boolean primary) {
      this.primary = primary;
      return this;
    }

    /**
     * Sets the operation (See {@link MultiValuedAttribute#getOperation()}).
     * <p>
     *
     * @param operation only "delete" is supported at the moment
     * @return the builder itself
     */
    protected Builder setOperation(final String operation) {
      this.operation = operation;
      return this;
    }

    /**
     * Sets the reference (See {@link MemberRef#getReference()}).
     * <p>
     * client info: the Reference value is set by the OSIAM server. If a MultiValuedAttribute which is send to the OSIAM server has this value filled,
     * the value will be ignored or the action will be rejected.
     * </p>
     *
     * @param reference the scim conform reference to the member
     * @return the builder itself
     */
    protected Builder setReference(final String reference) {
      this.reference = reference;
      return this;
    }

    /**
     * Builds a new Attribute with the given parameters
     *
     * @return a new MultiValuedAttribute Object
     */
    protected abstract MultiValuedAttribute build();
  }
}
