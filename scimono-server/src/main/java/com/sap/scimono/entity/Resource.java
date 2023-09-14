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

import static com.sap.scimono.entity.definition.ResourceConstants.EXTERNAL_ID_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.ID_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.META_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.SCHEMAS_FIELD;
import static com.sap.scimono.helper.Objects.firstNonNull;
import static java.util.Objects.hash;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.base.MultiValuedAttribute;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.exception.InvalidInputException;
import com.sap.scimono.exception.SCIMException;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.*;

/**
 * This class represents a SCIM Resource and is the base class for {@link User}s and {@link Group}s. All sub-resources (User/Group/custom) need to be
 * in the same Java package or JSON deserialization will break. It is possible to include extra metadata in the JSON to make deserialization work but
 * it won't be compliant with the SCIM specification.
 */
public abstract class Resource<T> implements Serializable {

  public static final String CORE_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:";
  private static final long serialVersionUID = 1726103518645055449L;
  private static final Predicate<Map.Entry<String, Extension>> CUSTOM_EXTENSION_FILTER = extension -> extension.getKey()
      .startsWith(Schema.EXTENSION_SCHEMA_URN);

  private final String id;
  private final String externalId;
  private final Meta meta;
  @JsonProperty(required = true)
  private final Set<String> schemas;
  private final Map<String, Extension> extensions;

  protected Resource(@JsonProperty(ID_FIELD) final String id, @JsonProperty(EXTERNAL_ID_FIELD) final String externalId, @JsonProperty(META_FIELD) final Meta meta,
      @JsonProperty(value = SCHEMAS_FIELD, required = true) final Set<String> schemas) {
    this.id = id;
    this.externalId = externalId;
    this.meta = meta;
    if (schemas == null || schemas.isEmpty()) {
      throw new InvalidInputException("Schemas cannot be null or empty!");
    }
    this.schemas = schemas;
    extensions = new HashMap<>();
  }

  protected Resource(final Builder<T> builder) {
    id = builder.id;
    externalId = builder.externalId;
    meta = builder.meta;
    schemas = builder.schemas;
    extensions = builder.extensions;
  }

  /**
   * Gets the Id of the resource.
   *
   * @return the id of the resource
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the external Id of the resource.
   * <p>
   * For more information please look at <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-5.1">SCIM core schema 2.0, section
   * 5.1</a>
   * </p>
   *
   * @return the externalId
   */
  public String getExternalId() {
    return externalId;
  }

  /**
   * Gets the meta attribute
   *
   * @return the meta
   */
  public Meta getMeta() {
    return meta;
  }

  /**
   * Gets the list of defined schemas
   *
   * @return a the list of schemas as a {@link Set}
   */
  @NotNull
  @Size(min = 1)
  public Set<String> getSchemas() {
    return new HashSet<>(schemas);
  }

  /**
   * Provides an unmodifiable view of all additional {@link Extension} fields of the user
   *
   * @return an unmodifiable view of the extensions
   */
  @JsonAnyGetter
  public Map<String, Extension> getExtensions() {
    return Collections.unmodifiableMap(extensions);
  }

  /**
   * Provides the {@link Extension} with the given URN
   *
   * @param urn The URN of the extension
   * @return The extension for the given URN
   * @throws IllegalArgumentException If urn is null or empty
   * @throws NoSuchElementException If extension with given urn is not available
   */
  public Extension getExtension(final String urn) {
    if (urn == null || urn.isEmpty()) {
      throw new IllegalArgumentException("urn must be neither null nor empty");
    }

    if (!extensions.containsKey(urn)) {
      throw new NoSuchElementException("extension " + urn + " is not available");
    }

    return extensions.get(urn);
  }

  /**
   * Checks if an extension with the given urn is present because an extension is not returned if no field is set
   *
   * @param urn urn of the extension
   * @return true if the given extension is present, else false
   */
  public boolean isExtensionPresent(final String urn) {
    return extensions.containsKey(urn);
  }

  public boolean hasExtensions() {
    return !extensions.isEmpty();
  }

  public boolean hasCustomExtensions() {
    return extensions.entrySet().stream().anyMatch(CUSTOM_EXTENSION_FILTER);
  }

  @JsonIgnore
  public Map<String, Extension> getCustomExtensions() {
    // @formatter:off
    return extensions.entrySet().stream().filter(CUSTOM_EXTENSION_FILTER)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    // @formatter:ofn
  }

  @JsonAnySetter
  public void handleExtensionSchema(final String schemaId, final Object value) {
    if (!isExtensionAllowed(schemaId) || !(value instanceof Map)) {
      throw new SCIMException(null, "Invalid user attribute: " + schemaId);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> extensionAttributes = (Map<String, Object>) value;
    Extension ext;
    if (EnterpriseExtension.ENTERPRISE_URN.equals(schemaId)) {
      ext = new EnterpriseExtension.Builder(extensionAttributes).build();
    } else {
      ext = new Extension.Builder(schemaId).setAttributes(extensionAttributes).build();
    }

    extensions.put(schemaId, ext);
  }

  private boolean isExtensionAllowed(String schemaId) {
    return (EnterpriseExtension.ENTERPRISE_URN.equals(schemaId) || SchemasCallback.isCustomSchema(schemaId)) && schemas.contains(schemaId);
  }

  public abstract Builder<T> builder();

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = hash(extensions);
    result = prime * result + hash(id);
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
    if (!(obj instanceof Resource)) {
      return false;
    }
    final Resource<?> other = (Resource<?>) obj;
    if (id != null && other.id != null && !Objects.equals(id, other.id)) {
      return false;
    }
    return Objects.equals(extensions, other.extensions);
  }

  /**
   * The Builder class is used to construct instances of the {@link Resource}
   */
  public abstract static class Builder<T> {
    String externalId;
    private String id;
    private Meta meta;
    private Set<String> schemas = new HashSet<>();
    private Map<String, Extension> extensions = new HashMap<>();

    public Builder(final Resource<T> resource) {
      if (resource != null) {
        id = resource.id;
        externalId = resource.externalId;
        meta = resource.meta;
        schemas = resource.schemas;
        extensions = new HashMap<>(firstNonNull(resource.extensions, extensions));
      }
    }

    protected void addSchema(final String schema) {
      schemas.add(schema);
    }

    /**
     * Sets the id of the resource.
     * <p>
     * client info: The id of a User will be created and set by the OSIAM server. If a {@link User} or {@link Group} which is send to the OSIAM server
     * has this value filled, the value will be ignored or the action will be rejected.
     * </p>
     *
     * @param id if of the resource
     * @return the builder itself
     */
    public Builder<T> setId(final String id) {
      this.id = id;
      return this;
    }

    /**
     * Sets the external id (See {@link Resource#getExternalId()}).
     *
     * @param externalId the external id
     * @return the builder itself
     */
    public Builder<T> setExternalId(final String externalId) {
      this.externalId = externalId;
      return this;
    }

    /**
     * @param meta the meta object
     * @return the builder itself
     */
    public Builder<T> setMeta(final Meta meta) {
      this.meta = meta;
      return this;
    }

    /**
     * Adds a collection of Extension to the User (See {@link User#getExtensions()}).
     *
     * @param extensions a collection of extensions
     * @return the builder itself
     */
    public Builder<T> addExtensions(final Collection<Extension> extensions) {
      if (extensions != null) {
        extensions.forEach(this::addExtension);
      }
      return this;
    }

    /**
     * Sets a Extension to the User (See {@link User#getExtension(String)}).
     *
     * @param extension a single Extension
     * @return the builder itself
     */
    public Builder<T> addExtension(final Extension extension) {
      if (extension != null) {
        extensions.put(extension.getUrn(), extension);
        addSchema(extension.getUrn());
      }
      return this;
    }

    /**
     * removes all Extensions from the actual User
     *
     * @return the builder itself
     */
    public Builder<T> removeExtensions() {
      extensions.clear();
      return this;
    }

    /**
     * removes one single Extension from the User
     *
     * @param urn the urn from the Extension to be removed
     * @return the builder itself
     */
    public Builder<T> removeExtension(final String urn) {
      extensions.remove(urn);
      return this;
    }
    
    /**
     * set Schemas for the User
     *
     * @param schemas to set
     * @return the builder itself
     */
    public Builder<T> setSchemas(final Set<String> schemas) {
      this.schemas = schemas;
      return this;
    }

    /**
     * Builds the Object of the Builder
     *
     * @return a new main Object of the Builder
     */
    public abstract T build();

    protected <V extends MultiValuedAttribute> boolean isMultivaluedAttributeExistInCollection(V targetAttribute, Collection<V> collection) {
      for (MultiValuedAttribute currentAttribute : collection) {
        if (currentAttribute.getValue() == null) {
          continue;
        }

        if (currentAttribute.equals(targetAttribute)) {
          return true;
        }
      }

      return false;
    }
  }

}
