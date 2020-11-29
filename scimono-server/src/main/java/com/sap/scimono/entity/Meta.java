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

import static com.sap.scimono.entity.definition.ResourceConstants.META_ATTRIBUTES_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.META_CREATED_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.META_LAST_MODIFIED_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.META_LOCATION_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.META_RESOURCE_TYPE_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.META_VERSION_FIELD;
import static java.util.Objects.hash;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents the meta data of a resource.
 * <p>
 * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02">SCIM core schema 2.0</a>
 * </p>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class Meta implements Serializable {

  private static final long serialVersionUID = -4536271487921469946L;

  private final Instant created;
  private final Instant lastModified;
  private final String location;
  private final String version;
  private final Set<String> attributes;
  private final String resourceType;
  private final Map<String, Object> additionalAttributes;

  @JsonCreator
  public Meta(@JsonProperty(META_CREATED_FIELD) final Instant created, @JsonProperty(META_LAST_MODIFIED_FIELD) final Instant lastModified,
      @JsonProperty(META_LOCATION_FIELD) final String location, @JsonProperty(META_VERSION_FIELD) final String version,
      @JsonProperty(META_ATTRIBUTES_FIELD) final Set<String> attributes, @JsonProperty(META_RESOURCE_TYPE_FIELD) final String resourceType) {
    this.created = created;
    this.lastModified = lastModified;
    this.location = location;
    this.version = version;
    this.attributes = attributes;
    this.resourceType = resourceType;
    this.additionalAttributes = new HashMap<>();
  }

  private Meta(final Builder builder) {
    created = builder.created;
    lastModified = builder.lastModified;
    attributes = builder.attributes;
    location = builder.location;
    version = builder.version;
    resourceType = builder.resourceType;
    additionalAttributes = builder.additionalAttributes;
  }

  /**
   * Gets the URI of the Resource being returned.
   * <p>
   * For more detailed information please look at the <a href= "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-5">SCIM core schema
   * 2.0, section 5</a>
   * </p>
   *
   * @return the location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Gets the version of the Resource being returned.
   * <p>
   * For more detailed information please look at the <a href= "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-5">SCIM core schema
   * 2.0, section 5</a>
   * </p>
   *
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Gets the attributes to be deleted from the Resource
   * <p>
   * For more detailed information please look at the <a href= "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-5">SCIM core schema
   * 2.0, section 5</a>
   * </p>
   *
   * @return a set of attributes to be deleted
   */
  public Set<String> getAttributes() {
    return attributes;
  }

  /**
   * Gets the date when the {@link Resource} was created
   *
   * @return the creation date
   */
  public Instant getCreated() {
    if (created != null) {
      return created;
    }
    return null;
  }

  /**
   * Gets the date when the {@link Resource} was last modified
   *
   * @return the last modified date
   */
  public Instant getLastModified() {
    if (lastModified != null) {
      return lastModified;
    }
    return null;
  }

  /**
   * Gets the type of the Resource (User or Group)
   *
   * @return the type of the actual resource
   */
  public String getResourceType() {
    return resourceType;
  }

  /**
   * Gets the additional meta attributes provided by service provider
   * @return attributes
   */
  @JsonAnyGetter
  public Map<String, Object> getAdditionalAttributes() {
    return additionalAttributes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + hash(attributes);
    result = prime * result + hash(created);
    result = prime * result + hash(lastModified);
    result = prime * result + hash(location);
    result = prime * result + hash(resourceType);
    result = prime * result + hash(version);
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
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Meta other = (Meta) obj;
    if (!Objects.equals(attributes, other.attributes)) {
      return false;
    }
    if (!Objects.equals(created, other.created)) {
      return false;
    }
    if (!Objects.equals(lastModified, other.lastModified)) {
      return false;
    }
    if (!Objects.equals(location, other.location)) {
      return false;
    }
    if (!Objects.equals(resourceType, other.resourceType)) {
      return false;
    }
    return Objects.equals(version, other.version);
  }

  /**
   * Builder class that is used to build {@link Meta} instances
   */
  public static class Builder {
    private final Instant created;
    private Instant lastModified;
    private String location;
    private String version;
    private Set<String> attributes = new HashSet<>();
    private String resourceType;
    private Map<String, Object> additionalAttributes = new HashMap<>();

    /**
     * Constructs a new builder with the created and last modified time set to the current time
     */
    public Builder() {
      created = Instant.now();
      lastModified = created;
    }

    /**
     * Will set created to given value and lastModified to System.currentTime Only be used by the server. Will be ignored by PUT and PATCH operations
     */
    public Builder(final Instant created, final Instant lastModified) {
      this.created = created;
      this.lastModified = lastModified;
    }

    /**
     * Constructs a new builder with the created and last modified time set to the given values
     *
     * @param meta the meta object to copy from
     */
    public Builder(final Meta meta) {
      if (meta == null) {
        created = Instant.now();
        lastModified = created;
      } else {
        created = meta.created;
        lastModified = meta.lastModified;
        location = meta.location;
        version = meta.version;
        attributes = meta.attributes;
        resourceType = meta.resourceType;
        additionalAttributes = meta.additionalAttributes;
      }
    }

    /**
     * Set the location (See {@link Meta#getLocation()}).
     *
     * @param location the resource uri
     * @return the builder itself
     */
    public Builder setLocation(final String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the version of the Resource (See {@link Meta#getVersion()}).
     *
     * @param version the version of the resource
     * @return the builder itself
     */
    public Builder setVersion(final String version) {
      this.version = version;
      return this;
    }

    /**
     * Sets the type of the Resource (See {@link Meta#getResourceType()}).
     *
     * @param resourceType the type
     * @return the builder itself
     */
    public Builder setResourceType(final String resourceType) {
      this.resourceType = resourceType;
      return this;
    }

    /**
     * Sets the names of the attributes to be removed from the Resource.
     *
     * @param attributes name of attributes to be deleted
     * @return the builder itself
     */
    public Builder setAttributes(final Set<String> attributes) {
      this.attributes = attributes;
      return this;
    }

    /**
     * Sets the lastModified attribute of the Resource.
     *
     * @param lastModified timestamp
     * @return the builder itself
     */
    public Builder setLastModified(final Instant lastModified) {
      this.lastModified = lastModified;
      return this;
    }

    /**
     * Add additional meta attribute provided by service provider.
     *
     * @param attrName attribute name
     * @param attribute value
     * @return the builder itself
     */
    public Builder addAdditionalAttribute(final String attrName, final Object attribute) {
      if (attrName == null || attribute == null) {
        return this;
      }

      additionalAttributes.put(attrName, attribute);
      return this;
    }

    public Builder addAdditionalAttributes(Map<String, Object> attributes) {
      if (attributes == null) {
        return this;
      }

      attributes.forEach(this::addAdditionalAttribute);
      return this;
    }

    /**
     * Builds a Meta Object with the given parameters
     *
     * @return a new Meta Object
     */
    public Meta build() {
      return new Meta(this);
    }
  }
}
