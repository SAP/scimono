
package com.sap.scim.entity.patch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sap.scim.entity.Meta;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
public class PatchBody implements Serializable {

  private static final long serialVersionUID = 2532412326436910861L;

  public static final String PATCH_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:PatchOp";

  private final List<PatchOperation> operations;

  private final Set<String> schemas;
  private Meta meta;

  /**
   * Constructor for deserialization, it is not intended for general use.
   */
  @JsonCreator
  private PatchBody(@JsonProperty(value = "schemas", required = true) final Set<String> schemas,
      @JsonProperty(value = "Operations", required = true) final List<PatchOperation> operations) {
    this.operations = operations;
    this.schemas = schemas;
  }

  private PatchBody(final Builder builder) {
    operations = builder.operations;
    schemas = builder.schemas;
    meta = builder.meta;
  }

  @JsonProperty(value = "Operations")
  public List<PatchOperation> getOperations() {
    return operations;
  }

  public Set<String> getSchemas() {
    return schemas;
  }

  public Meta getMeta() {
    return meta;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (operations == null ? 0 : operations.hashCode());
    result = prime * result + (schemas == null ? 0 : schemas.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PatchBody other = (PatchBody) obj;
    if (operations == null) {
      if (other.operations != null) {
        return false;
      }
    } else if (!operations.equals(other.operations)) {
      return false;
    }
    if (schemas == null) {
      return other.schemas == null;
    } else return schemas.equals(other.schemas);
  }

  /**
   * Builder class that is used to build {@link PatchBody} instances
   */
  public static class Builder {

    private List<PatchOperation> operations = new ArrayList<>();
    private Set<String> schemas = new HashSet<>(Collections.singleton(PATCH_SCHEMA));
    private Meta meta;

    public Builder() {
    }

    /**
     * builds an Builder based of the given Attribute
     *
     * @param operations existing Attribute
     */
    public Builder(final List<PatchOperation> operations) {
      this.operations = operations;
    }

    public Builder(PatchBody patchBody) {
      this.operations = patchBody.operations;
      this.schemas = patchBody.schemas;
      this.meta = patchBody.meta;
    }

    /**
     * Set sequence of {@link PatchOperation} objects}
     *
     * @param {@link List} of {@link PatchOperation}
     * @return the builder itself
     */
    public Builder setOperations(final List<PatchOperation> operations) {
      this.operations = operations;
      return this;
    }

    public Builder addOperation(PatchOperation operation) {
      operations.add(operation);
      return this;
    }

    public Builder setSchemas(Set<String> schemas) {
      this.schemas = schemas;
      return this;
    }

    public PatchBody build() {
      return new PatchBody(this);
    }

    public Builder setMeta(Meta meta) {
      this.meta = meta;
      return this;
    }
  }
}
