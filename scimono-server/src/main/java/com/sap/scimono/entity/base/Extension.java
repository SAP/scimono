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

package com.sap.scimono.entity.base;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.hash;


/**
 * This class represents a schema extension.
 * <p>
 * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-4">SCIM core schema
 * 2.0, section 4</a>
 * </p>
 */
@JsonInclude(Include.NON_EMPTY)
public class Extension implements Serializable {

  private static final long serialVersionUID = -121658804932369438L;

  private final String urn;
  private final Map<String, Object> attributes;

  private Extension(final String urn, final Map<String, Object> attributes) {
    this.urn = urn;
    this.attributes = new HashMap<>(attributes);
  }

  protected Extension(final Builder builder) {
    this(builder.urn, builder.attributes);
  }

  @JsonIgnore
  public String getUrn() {
    return urn;
  }

  @JsonIgnore
  public Object getAttribute(final String attrName) {
    return attributes.get(attrName);
  }

  public String getAttributeValueAsString(final String attrName) {
    Object attributeValue = attributes.get(attrName);

    if (attributeValue == null) {
      return null;
    }

    return String.valueOf(attributeValue);
  }

  public Boolean getAttributeValueAsBoolean(String attrName) {
    Object attributeValue = attributes.get(attrName);

    if (!(attributeValue instanceof Boolean)) {
      return null;
    }

    return (Boolean) attributeValue;
  }

  public Integer getAttributeValueAsInteger(String attrName) {
    Object attributeValue = attributes.get(attrName);

    if (!(attributeValue instanceof Integer)) {
      return null;
    }

    return (Integer) attributeValue;
  }


  @JsonAnyGetter
  public Map<String, Object> getAttributes() {
    return new HashMap<>(attributes);
  }

  /**
   * Checks if the given field is present in this extension because not every field is mandatory (according to scim 2.0 spec).
   *
   * @param attrName Name of the field to check
   * @return true if the given field is present, else false
   */
  public boolean isAttributePresent(final String attrName) {
    return attributes.containsKey(attrName);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + hash(attributes);
    result = prime * result + hash(urn);
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
    if (!(obj instanceof Extension)) {
      return false;
    }
    final Extension other = (Extension) obj;
    if (!Objects.equals(attributes, other.attributes)) {
      return false;
    }
    return Objects.equals(urn, other.urn);
  }

  @Override
  public String toString() {
    return new StringBuilder("Extension [urn=").append(urn).append(", attributes=").append(attributes).append("]").toString();
  }

  /**
   * Builder class that is used to build {@link Extension} instances
   */
  public static class Builder {

    private final String urn;
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * Constructs an extension with the given urn.
     *
     * @param urn the urn of the extension
     */
    public Builder(final String urn) {
      this.urn = urn;
    }

    /**
     * Constructs an extension based on the given extension.
     *
     * @param extension existing extension
     */
    public Builder(final Extension extension) {
      urn = extension.urn;
      attributes = extension.attributes;
    }

    public Builder setAttribute(final String fieldName, final Object value) {
      if (value == null) {
        return removeAttribute(fieldName);
      }

      attributes.put(fieldName, value);
      return this;
    }

    public Builder setAttributes(final Map<String, ?> attributes) {
      if (attributes != null) {
        attributes.forEach(this::setAttribute);
      }
      return this;
    }

    public Builder removeAttribute(final String fieldName) {
      attributes.remove(fieldName);
      return this;
    }

    public Extension build() {
      return new Extension(this);
    }
  }

}
