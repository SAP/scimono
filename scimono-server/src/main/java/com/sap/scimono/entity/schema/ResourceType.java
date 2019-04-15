
package com.sap.scimono.entity.schema;

import static java.util.Objects.hash;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.schema.validation.ValidCoreSchema;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ResourceType extends Resource<ResourceType> {

  public static final String RESOURCE_TYPE_RESOURCE_TYPE = "ResourceType";
  public static final String SCHEMA = CORE_SCHEMA + RESOURCE_TYPE_RESOURCE_TYPE;
  private static final long serialVersionUID = 5854212230654248047L;

  @JsonProperty(required = true)
  private final String name;
  @JsonProperty
  private final String description;
  @JsonProperty(required = true)
  private final String endpoint;
  @JsonProperty(required = true)
  private final String schema;
  @JsonProperty
  private final List<SchemaExtension> schemaExtensions;

  @JsonCreator
  private ResourceType(@JsonProperty(value = "id", required = true) final String id,
      @JsonProperty(value = "endpoint", required = true) final String endpoint, @JsonProperty(value = "schema", required = true) final String schema,
      @JsonProperty("meta") final Meta meta, @JsonProperty("schemas") final Set<String> schemas, @JsonProperty("name") final String name,
      @JsonProperty("description") final String description, @JsonProperty("schemaExtensions") final List<SchemaExtension> schemaExtensions) {
    super(id, null, meta, schemas);
    this.name = name;
    this.description = description;
    this.endpoint = endpoint;
    this.schema = schema;
    this.schemaExtensions = schemaExtensions;
  }

  private ResourceType(final Builder builder) {
    super(builder);
    name = builder.name;
    description = builder.description;
    endpoint = builder.endpoint;
    schema = builder.schema;
    schemaExtensions = builder.schemaExtensions;
  }

  @Override
  @ValidCoreSchema(SCHEMA)
  public Set<String> getSchemas() {
    return super.getSchemas();
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public String getSchema() {
    return schema;
  }

  public List<SchemaExtension> getSchemaExtensions() {
    return schemaExtensions;
  }

  @Override
  public Builder builder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + hash(description);
    result = prime * result + hash(endpoint);
    result = prime * result + hash(name);
    result = prime * result + hash(schema);
    result = prime * result + hash(schemaExtensions);
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
    if (!(obj instanceof ResourceType)) {
      return false;
    }
    ResourceType other = (ResourceType) obj;
    if (!Objects.equals(description, other.description)) {
      return false;
    }
    if (!Objects.equals(endpoint, other.endpoint)) {
      return false;
    }
    if (!Objects.equals(name, other.name)) {
      return false;
    }
    if (!Objects.equals(schema, other.schema)) {
      return false;
    }
    return Objects.equals(schemaExtensions, other.schemaExtensions);
  }

  public static class Builder extends Resource.Builder<ResourceType> {

    private String name;
    private String description;
    private String endpoint;
    private String schema;
    private List<SchemaExtension> schemaExtensions = new ArrayList<>();

    Builder(final ResourceType resource) {
      super(resource);
      if (resource != null) {
        name = resource.name;
        description = resource.description;
        endpoint = resource.endpoint;
        schema = resource.schema;
        schemaExtensions = resource.schemaExtensions;
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

    public Builder endpoint(final String endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    public Builder schema(final String schema) {
      this.schema = schema;
      return this;
    }

    public Builder addSchemaExtension(final SchemaExtension schemaExt) {
      schemaExtensions.add(schemaExt);
      return this;
    }

    public Builder removeSchemaExtension(final SchemaExtension schemaExt) {
      schemaExtensions.remove(schemaExt);
      return this;
    }

    public Builder clearSchemaExtensions() {
      schemaExtensions.clear();
      return this;
    }

    @Override
    public ResourceType build() {
      return new ResourceType(this);
    }

  }

}
