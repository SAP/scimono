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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.sap.scimono.entity.base.MultiValuedAttribute;
import com.sap.scimono.entity.base.MultiValuedAttributeType;
import com.sap.scimono.entity.data.ImageDataURI;
import com.sap.scimono.entity.data.PhotoValueType;
import com.sap.scimono.exception.InvalidInputException;
import com.sap.scimono.helper.Strings;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a photo attribute.
 * <p>
 * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2">SCIM core schema
 * 2.0, section 3.2</a>
 * </p>
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE)
public final class Photo extends MultiValuedAttribute implements Serializable {

  private static final long serialVersionUID = -3801047382575408796L;

  private final Type type;

  /**
   * Constructor for deserialization, it is not intended for general use.
   *
   * @param operation
   * @param value
   * @param display
   * @param primary
   * @param reference
   * @param type
   */
  @JsonCreator
  public Photo(@JsonProperty(OPERATION_FIELD) final String operation, @JsonProperty(VALUE_FIELD) final String value,
      @JsonProperty(DISPLAY_FIELD) final String display, @JsonProperty(PRIMARY_FIELD) final boolean primary, @JsonProperty(REF_FIELD) final String reference,
      @JsonProperty(TYPE_FIELD) final Type type) {
    super(operation, value, display, primary, reference);
    this.type = type;
  }

  private Photo(final Builder builder) {
    super(builder);
    type = builder.type;
  }

  /**
   * the value of the photo as URI. Check first with {@link Photo#getValueType()} if the type is {@link PhotoValueType#URI}
   *
   * @return returns the value of the photo as URI
   */
  public URI getValueAsURI() {
    URI uri;
    try {
      uri = new URI(super.getValue());
    } catch (final URISyntaxException e) {
      throw new InvalidInputException(e.getMessage());
    }
    return uri;
  }

  /**
   * the value of the photo as {@link ImageDataURI}. Check first with {@link Photo#getValueType()} if the type is
   * {@link PhotoValueType#IMAGE_DATA_URI}
   *
   * @return the value of the photo as {@link ImageDataURI}
   */
  @JsonIgnore
  private ImageDataURI getValueAsImageDataURI() {
    return new ImageDataURI(super.getValue());
  }

  /**
   * @return the type of the saved photo value
   */
  @JsonIgnore
  public PhotoValueType getValueType() {
    PhotoValueType photoType;
    if (super.getValue().startsWith("data:image/") && super.getValue().contains(";base64,")) {
      getValueAsImageDataURI();
      photoType = PhotoValueType.IMAGE_DATA_URI;
    } else {
      getValueAsURI();
      photoType = PhotoValueType.URI;
    }

    return photoType;
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
    if (!(obj instanceof Photo)) {
      return false;
    }
    final Photo other = (Photo) obj;
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

  /**
   * Builder class that is used to build {@link Photo} instances
   */
  public static class Builder extends MultiValuedAttribute.Builder {

    private Type type;

    public Builder() {
      // default constructor used to construct new entities
    }

    /**
     * builds an Builder based of the given Attribute
     *
     * @param photo existing Attribute
     */
    public Builder(final Photo photo) {
      super(photo);
      type = photo.type;
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

    /**
     * an URI pointing to an image
     *
     * @param uri a image URI
     * @return the Builder itself
     */
    public Builder setValue(final URI uri) {
      super.setValue(uri.toString());
      return this;
    }

    public Builder removeValue() {
      super.setValue(null);
      return this;
    }


    /**
     * an imageDataURI which contains a small in data image. For performance issues it is recommend to to store big pictures as ImageDataURI
     *
     * @param image a image
     * @return the Builder itself
     */
    public Builder setValue(final ImageDataURI image) {
      super.setValue(image.toString());
      return this;
    }

    /**
     * Sets the label indicating the attribute's function (See {@link Photo#getType()}).
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
    public Photo build() {
      return new Photo(this);
    }
  }

  /**
   * Represents a photo type. Canonical values are available as static constants.
   */
  public static class Type extends MultiValuedAttributeType {
    private static final long serialVersionUID = 5676963413757504786L;
    public static final Type PHOTO = new Type("photo");
    public static final Type THUMBNAIL = new Type("thumbnail");

    /**
     * Public constructor for the Type class
     *
     * @param typeName
     */
    Type(final String typeName) {
      super(typeName);
    }

    /**
     * This method is used to implement sort of smal DSL to get proper Type, e.g. Photo.Type.of(photoType)
     *
     * @param typeName
     * @return new Type istance
     */
    public static Type of(final String typeName) {
      if (isNullOrEmpty(typeName)) {
        return null;
      }
      return new Type(typeName);
    }
  }

}
