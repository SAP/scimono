
package com.sap.scimono.entity.schema;

import static com.sap.scimono.entity.schema.AttributeDataType.COMPLEX;
import static java.util.Objects.hash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.schema.validation.ValidCoreSchema;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Schema extends Resource<Schema> {

  public static final String EXTENSION_SCHEMA_URN = "urn:sap:cloud:scim:schemas:extension:custom:2.0:";
  public static final String RESOURCE_TYPE_SCHEMA = "Schema";
  public static final String SCHEMA = CORE_SCHEMA + RESOURCE_TYPE_SCHEMA;
  private static final long serialVersionUID = 7291316930756788028L;

  private final String name;
  private final String description;
  @Valid
  private final List<Attribute> attributes;

  @JsonCreator
  public Schema(@JsonProperty(value = "id", required = true) final String id, @JsonProperty("externalId") final String externalId,
      @JsonProperty("meta") final Meta meta, @JsonProperty("schemas") final Set<String> schemas, @JsonProperty("name") final String name,
      @JsonProperty("description") final String description, @JsonProperty(value = "attributes", required = true) final List<Attribute> attributes) {
    super(id, externalId, meta, schemas);
    this.name = name;
    this.description = description;
    this.attributes = attributes;
  }

  protected Schema(final Builder builder) {
    super(builder);
    name = builder.name;
    description = builder.description;
    attributes = builder.attributes;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public List<Attribute> getAttributes() {
    return new ArrayList<>(attributes);
  }

  @Override
  @ValidCoreSchema(SCHEMA)
  public Set<String> getSchemas() {
    return super.getSchemas();
  }

  @Override
  public Builder builder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + hash(attributes);
    result = prime * result + hash(description);
    result = prime * result + hash(name);
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
    if (!(obj instanceof Schema)) {
      return false;
    }
    Schema other = (Schema) obj;
    if (!Objects.equals(attributes, other.attributes)) {
      return false;
    }
    if (!Objects.equals(description, other.description)) {
      return false;
    }
    return Objects.equals(name, other.name);
  }

  public static class Builder extends Resource.Builder<Schema> {

    private String name;
    private String description;
    private List<Attribute> attributes = new ArrayList<>();

    Builder(final Schema resource) {
      super(resource);
      if (resource != null) {
        name = resource.name;
        description = resource.description;
        attributes = resource.attributes;
      }
    }

    public Builder() {
      this(null);
      addSchema(SCHEMA);
    }

    @Override
    public Builder setId(final String id) {
      super.setId(id);
      return this;
    }

    @Override
    public Builder setMeta(final Meta meta) {
      super.setMeta(meta);
      return this;
    }

    public Builder name(final String name) {
      this.name = name;
      return this;
    }

    public Builder description(final String description) {
      this.description = description;
      return this;
    }

    public Builder clearAttributes() {
      attributes.clear();
      return this;
    }

    public Builder addAttributes(final Collection<Attribute> attrs) {
      attrs.forEach(this::addAttribute);
      return this;
    }

    public Builder addAttribute(final Attribute attr) {
      attributes.add(attr);
      return this;
    }

    public Builder removeAttributes(final Attribute attr) {
      attributes.remove(attr);
      return this;
    }

    @Override
    public Schema build() {
      return new Schema(this);
    }

  }

  @Override
  public String toString() {
    //@formatter:off
    return new StringBuilder()
        .append("Schema [id=").append(getId())
        .append(", name=").append(name)
        .append(", description=").append(description)
        .append(", attributes=").append(attributes)
        .append("]").toString();
    //@formatter:on
  }

  public Attribute toAttribute() {
    // @formatter:off
    return new Attribute.Builder()
        .name(this.getId())
        .multiValued(false)
        .type(COMPLEX.toString())
        .mutability("readWrite")
        .addSubAttributes(attributes)
        .build();
    // @formatter:on
  }

}
