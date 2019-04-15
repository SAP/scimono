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

import static com.sap.scim.entity.definition.CoreUserAttributes.Constants.NAME_FAMILY_NAME_FIELD;
import static com.sap.scim.entity.definition.CoreUserAttributes.Constants.NAME_FORMATTED_FIELD;
import static com.sap.scim.entity.definition.CoreUserAttributes.Constants.NAME_GIVEN_NAME_FIELD;
import static com.sap.scim.entity.definition.CoreUserAttributes.Constants.NAME_HONORIFIC_PREFIX_FIELD;
import static com.sap.scim.entity.definition.CoreUserAttributes.Constants.NAME_HONORIFIC_SUFFIX_FIELD;
import static com.sap.scim.entity.definition.CoreUserAttributes.Constants.NAME_MIDDLE_NAME_FIELD;
import static java.util.Objects.hash;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.scim.helper.Strings;

/**
 * This class represents the User's real name.
 * <p>
 * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
 * 2.0, section 6</a>
 * </p>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class Name implements Serializable {

  private static final long serialVersionUID = -2090787512643160922L;

  private final String formatted;
  private final String familyName;
  private final String givenName;
  private final String middleName;
  private final String honorificPrefix;
  private final String honorificSuffix;

  @JsonCreator
  private Name(@JsonProperty(NAME_FORMATTED_FIELD) final String formatted, @JsonProperty(NAME_FAMILY_NAME_FIELD) final String familyName,
      @JsonProperty(NAME_GIVEN_NAME_FIELD) final String givenName, @JsonProperty(NAME_MIDDLE_NAME_FIELD) final String middleName,
      @JsonProperty(NAME_HONORIFIC_PREFIX_FIELD) final String honorificPrefix, @JsonProperty(NAME_HONORIFIC_SUFFIX_FIELD) final String honorificSuffix) {
    this.formatted = formatted;
    this.familyName = familyName;
    this.givenName = givenName;
    this.middleName = middleName;
    this.honorificPrefix = honorificPrefix;
    this.honorificSuffix = honorificSuffix;
  }

  private Name(final Builder builder) {
    formatted = builder.formatted;
    familyName = builder.familyName;
    givenName = builder.givenName;
    middleName = builder.middleName;
    honorificPrefix = builder.honorificPrefix;
    honorificSuffix = builder.honorificSuffix;
  }

  /**
   * Gets the full name, including all middle names, titles, and suffixes as appropriate, formatted for display.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the formatted name
   */
  public String getFormatted() {
    return formatted;
  }

  /**
   * Gets the family name of the User.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the family name
   */
  public String getFamilyName() {
    return familyName;
  }

  /**
   * Gets the given (first) name of the User.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the given name
   */
  public String getGivenName() {
    return givenName;
  }

  /**
   * Gets the middle name(s) of the User.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the middle name
   */
  public String getMiddleName() {
    return middleName;
  }

  /**
   * Gets the honorific prefix(es) of the User.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the honorific prefix
   */
  public String getHonorificPrefix() {
    return honorificPrefix;
  }

  /**
   * Gets the honorific suffix(es) of the User.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the honorific sufix
   */
  public String getHonorificSuffix() {
    return honorificSuffix;
  }

  /**
   * <p>
   * Checks if this {@link Name} is empty, i.e. all properties are empty or null.
   * </p>
   *
   * @return true if all properties are null or empty, else false
   */
  @JsonIgnore
  public boolean isEmpty() {
    if (!Strings.isNullOrEmpty(formatted)) {
      return false;
    }

    if (!Strings.isNullOrEmpty(familyName)) {
      return false;
    }

    if (!Strings.isNullOrEmpty(givenName)) {
      return false;
    }

    if (!Strings.isNullOrEmpty(middleName)) {
      return false;
    }

    if (!Strings.isNullOrEmpty(honorificPrefix)) {
      return false;
    }

    if (!Strings.isNullOrEmpty(honorificSuffix)) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    Map<String, Object> valuesToDisplay = new LinkedHashMap<>();

    valuesToDisplay.put(NAME_FORMATTED_FIELD, formatted);
    valuesToDisplay.put(NAME_FAMILY_NAME_FIELD, familyName);
    valuesToDisplay.put(NAME_GIVEN_NAME_FIELD, givenName);
    valuesToDisplay.put(NAME_MIDDLE_NAME_FIELD, middleName);
    valuesToDisplay.put(NAME_HONORIFIC_PREFIX_FIELD, honorificPrefix);
    valuesToDisplay.put(NAME_HONORIFIC_SUFFIX_FIELD, honorificSuffix);

    return Strings.createPrettyEntityString(valuesToDisplay, this.getClass());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Name other = (Name) o;

    if (!Objects.equals(familyName, other.familyName)) {
      return false;
    }
    if (!Objects.equals(formatted, other.formatted)) {
      return false;
    }
    if (!Objects.equals(givenName, other.givenName)) {
      return false;
    }
    if (!Objects.equals(honorificPrefix, other.honorificPrefix)) {
      return false;
    }
    if (!Objects.equals(honorificSuffix, other.honorificSuffix)) {
      return false;
    }
    return Objects.equals(middleName, other.middleName);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + hash(familyName);
    result = prime * result + hash(formatted);
    result = prime * result + hash(givenName);
    result = prime * result + hash(honorificPrefix);
    result = prime * result + hash(honorificSuffix);
    result = prime * result + hash(middleName);
    return result;
  }

  /**
   * Builder class that is used to build {@link Name} instances
   */
  public static class Builder {
    private String formatted;
    private String familyName;
    private String givenName;
    private String middleName;
    private String honorificPrefix;
    private String honorificSuffix;

    public Builder() {

    }

    public Builder(Name name) {
      this.formatted = name.formatted;
      this.familyName = name.familyName;
      this.givenName = name.givenName;
      this.middleName = name.middleName;
      this.honorificPrefix = name.honorificPrefix;
      this.honorificSuffix = name.honorificSuffix;
    }

    /**
     * Sets the full name (See {@link Name#getFormatted()}).
     *
     * @param formatted the formatted name
     * @return the builder itself
     */
    public Builder setFormatted(final String formatted) {
      this.formatted = formatted;
      return this;
    }

    /**
     * Sets the family name of the User (See {@link Name#getFamilyName()}).
     *
     * @param familyName the family name
     * @return the builder itself
     */
    public Builder setFamilyName(final String familyName) {
      this.familyName = familyName;
      return this;
    }

    /**
     * Sets the given name of the User (See {@link Name#getGivenName()}).
     *
     * @param givenName the given name
     * @return the builder itself
     */
    public Builder setGivenName(final String givenName) {
      this.givenName = givenName;
      return this;
    }

    /**
     * Sets the middle name(s) of the User (See {@link Name#getMiddleName()}).
     *
     * @param middleName the middle name
     * @return the builder itself
     */
    public Builder setMiddleName(final String middleName) {
      this.middleName = middleName;
      return this;
    }

    /**
     * Sets the honorific prefix(es) of the User (See {@link Name#getHonorificPrefix()}).
     *
     * @param honorificPrefix the honorific prefix
     * @return the builder itself
     */
    public Builder setHonorificPrefix(final String honorificPrefix) {
      this.honorificPrefix = honorificPrefix;
      return this;
    }

    /**
     * Sets the honorific suffix(es) of the User (See {@link Name#getHonorificSuffix()}).
     *
     * @param honorificSuffix the honorific suffix
     * @return the builder itself
     */
    public Builder setHonorificSuffix(final String honorificSuffix) {
      this.honorificSuffix = honorificSuffix;
      return this;
    }

    /**
     * Builds a Name Object with the given parameters
     *
     * @return The complete Name Object
     */
    public Name build() {
      return new Name(this);
    }
  }
}
