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

import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.ADDRESSES_COUNTRY_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.ADDRESSES_FORMATTED_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.ADDRESSES_LOCALITY_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.ADDRESSES_POSTAL_CODE_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.ADDRESSES_REGION_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.ADDRESSES_STREET_ADDRESS_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.MultivaluedAttributeConstants.DISPLAY_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.MultivaluedAttributeConstants.OPERATION_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.MultivaluedAttributeConstants.PRIMARY_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.MultivaluedAttributeConstants.REF_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.MultivaluedAttributeConstants.TYPE_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.MultivaluedAttributeConstants.VALUE_FIELD;
import static com.sap.scimono.helper.Strings.createPrettyEntityString;
import static com.sap.scimono.helper.Strings.isNullOrEmpty;
import static java.util.Objects.hash;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.scimono.entity.base.MultiValuedAttribute;
import com.sap.scimono.entity.base.MultiValuedAttributeType;

/**
 * A physical mailing address for a User
 * <p>
 * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema
 * 2.0</a>
 * </p>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class Address extends MultiValuedAttribute implements Serializable {

  private static final long serialVersionUID = 2731087785568277294L;

  private final String formatted;
  private final String streetAddress;
  private final String locality;
  private final String region;
  private final String postalCode;
  private final String country;
  private final Type type;

  /**
   * Constructor for deserialization, it is not intended for general use.
   */
  @JsonCreator
  private Address(@JsonProperty(OPERATION_FIELD) final String operation, @JsonProperty(VALUE_FIELD) final String value,
      @JsonProperty(DISPLAY_FIELD) final String display, @JsonProperty(PRIMARY_FIELD) final boolean primary, @JsonProperty(REF_FIELD) final String reference,
      @JsonProperty(ADDRESSES_FORMATTED_FIELD) final String formatted, @JsonProperty(ADDRESSES_STREET_ADDRESS_FIELD) final String streetAddress,
      @JsonProperty(ADDRESSES_LOCALITY_FIELD) final String locality, @JsonProperty(ADDRESSES_REGION_FIELD) final String region,
      @JsonProperty(ADDRESSES_POSTAL_CODE_FIELD) final String postalCode, @JsonProperty(ADDRESSES_COUNTRY_FIELD) final String country,
      @JsonProperty(TYPE_FIELD) final Type type) {
    super(operation, value, display, primary, reference);
    this.formatted = formatted;
    this.streetAddress = streetAddress;
    this.locality = locality;
    this.region = region;
    this.postalCode = postalCode;
    this.country = country;
    this.type = type;
  }

  /**
   * Constructor for serialization. It is not intended for public use.
   */
  private Address(final Builder builder) {
    super(builder);
    formatted = builder.formatted;
    streetAddress = builder.streetAddress;
    locality = builder.locality;
    region = builder.region;
    postalCode = builder.postalCode;
    country = builder.country;
    type = builder.type;
  }

  /**
   * Gets the full mailing address, formatted for display or use with a mailing label.
   *
   * @return the formatted address
   */
  public String getFormatted() {
    return formatted;
  }

  /**
   * Gets the full street address, which may include house number, street name, etc.
   *
   * @return the street address
   */
  public String getStreetAddress() {
    return streetAddress;
  }

  /**
   * Gets the city or locality
   *
   * @return the city or locality
   */
  public String getLocality() {
    return locality;
  }

  /**
   * Gets the state or region
   *
   * @return region the region
   */
  public String getRegion() {
    return region;
  }

  /**
   * Gets the postal code
   *
   * @return postalCode the postal code
   */
  public String getPostalCode() {
    return postalCode;
  }

  /**
   * Gets the country name in ISO 3166-1 alpha 2 format, e.g. "DE" or "US".
   *
   * @return the country
   */
  public String getCountry() {
    return country;
  }

  /**
   * Gets the type of the attribute.
   * <p/>
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
    result = prime * result + hash(country);
    result = prime * result + hash(formatted);
    result = prime * result + hash(locality);
    result = prime * result + hash(postalCode);
    result = prime * result + hash(region);
    result = prime * result + hash(streetAddress);
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
    if (!(obj instanceof Address)) {
      return false;
    }
    final Address other = (Address) obj;
    if (!Objects.equals(country, other.country)) {
      return false;
    }
    if (!Objects.equals(formatted, other.formatted)) {
      return false;
    }
    if (!Objects.equals(locality, other.locality)) {
      return false;
    }
    if (!Objects.equals(postalCode, other.postalCode)) {
      return false;
    }
    if (!Objects.equals(region, other.region)) {
      return false;
    }
    if (!Objects.equals(streetAddress, other.streetAddress)) {
      return false;
    }
    if (!Objects.equals(type, other.type)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    Map<String, Object> valuesToDisplay = new LinkedHashMap<>();
    valuesToDisplay.put(ADDRESSES_FORMATTED_FIELD, formatted);
    valuesToDisplay.put(ADDRESSES_STREET_ADDRESS_FIELD, streetAddress);
    valuesToDisplay.put(ADDRESSES_LOCALITY_FIELD, locality);
    valuesToDisplay.put(ADDRESSES_REGION_FIELD, region);
    valuesToDisplay.put(ADDRESSES_POSTAL_CODE_FIELD, postalCode);
    valuesToDisplay.put(ADDRESSES_COUNTRY_FIELD, country);
    valuesToDisplay.put(TYPE_FIELD, type);
    valuesToDisplay.put(OPERATION_FIELD, getOperation());
    valuesToDisplay.put(PRIMARY_FIELD, isPrimary());

    return createPrettyEntityString(valuesToDisplay, this.getClass());
  }

  /**
   * Builder class that is used to build {@link Address} instances
   */
  public static class Builder extends MultiValuedAttribute.Builder {

    private String formatted;
    private String streetAddress;
    private String locality;
    private String region;
    private String postalCode;
    private String country;
    private Type type;

    public Builder() {
      // Default constructor used to instantiate new Address objects
    }

    /**
     * builds an Builder based of the given Attribute
     *
     * @param address existing Attribute
     */
    public Builder(final Address address) {
      super(address);
      formatted = address.formatted;
      streetAddress = address.streetAddress;
      locality = address.locality;
      region = address.region;
      postalCode = address.postalCode;
      country = address.country;
      type = address.type;
    }

    /**
     * Sets the full mailing address (See {@link Address#getFormatted()}).
     *
     * @param formatted the formatted address
     * @return the builder itself
     */
    public Builder setFormatted(final String formatted) {
      this.formatted = formatted;
      return this;
    }

    /**
     * Sets the full street address component, (See {@link Address#getStreetAddress()}).
     *
     * @param streetAddress the street address
     * @return the builder itself
     */
    public Builder setStreetAddress(final String streetAddress) {
      this.streetAddress = streetAddress;
      return this;
    }

    /**
     * Sets the city or locality.
     *
     * @param locality the locality
     * @return the builder itself
     */
    public Builder setLocality(final String locality) {
      this.locality = locality;
      return this;
    }

    /**
     * Sets the state or region.
     *
     * @param region the region
     * @return the builder itself
     */
    public Builder setRegion(final String region) {
      this.region = region;
      return this;
    }

    /**
     * Sets the postal code
     *
     * @param postalCode the postal code
     * @return the builder itself
     */
    public Builder setPostalCode(final String postalCode) {
      this.postalCode = postalCode;
      return this;
    }

    /**
     * Sets the label indicating the attribute's function (See {@link Address#getType()}).
     *
     * @param type the type of the attribute
     * @return the builder itself
     */
    public Builder setType(final Type type) {
      this.type = type;
      return this;
    }

    /**
     * Sets the country name (See {@link Address#getCountry()}).
     *
     * @param country the country
     * @return the builder itself
     */
    public Builder setCountry(final String country) {
      this.country = country;
      return this;
    }

    @Override
    public Builder setPrimary(final Boolean primary) {
      super.setPrimary(primary);
      return this;
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
    public Address build() {
      return new Address(this);
    }

  }

  /**
   * Represents an address type. Canonical values are available as static constants.
   */
  public static class Type extends MultiValuedAttributeType {

    private static final long serialVersionUID = 7765783711577265725L;
    public static final Type WORK = new Type("work");
    public static final Type HOME = new Type("home");
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
