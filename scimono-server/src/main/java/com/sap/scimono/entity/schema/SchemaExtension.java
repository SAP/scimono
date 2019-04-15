
package com.sap.scimono.entity.schema;

import static java.util.Objects.hash;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SchemaExtension implements Serializable {

  private static final long serialVersionUID = -4732322610440636607L;

  @JsonProperty(required = true)
  private final String schema;
  @JsonProperty(required = true)
  private final boolean required;

  @JsonCreator
  public SchemaExtension(@JsonProperty(value = "schema", required = true) final String schema, @JsonProperty("required") final boolean required) {
    this.schema = schema;
    this.required = required;
  }

  public String getSchema() {
    return schema;
  }

  public boolean isRequired() {
    return required;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Boolean.hashCode(required);
    result = prime * result + hash(schema);
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
    if (!(obj instanceof SchemaExtension)) {
      return false;
    }
    SchemaExtension other = (SchemaExtension) obj;
    if (required != other.required) {
      return false;
    }
    return Objects.equals(schema, other.schema);
  }

}
