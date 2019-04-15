
package com.sap.scimono.entity.patch;

import static com.sap.scimono.helper.Strings.isNullOrEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.sap.scimono.entity.base.MultiValuedAttributeType;

public class PatchOperation {

  private final Type op;
  private final String path;
  private final JsonNode value;

  /**
   * Constructor for deserialization, it is not intended for general use.
   */
  @JsonCreator
  private PatchOperation(@JsonProperty(value = "op", required = true) final Type op, @JsonProperty("path") final String path,
      @JsonProperty("value") final JsonNode value) {
    this.op = op;
    this.path = path;
    this.value = value;
  }

  private PatchOperation(final Builder builder) {
    op = builder.op;
    path = builder.path;
    value = builder.value;
  }

  public String getPath() {
    return path;
  }

  public Type getOp() {
    return op;
  }

  public JsonNode getValue() {
    return value;
  }

  /**
   * Builder class that is used to build {@link PatchOperation} instances
   */
  public static class Builder {

    private Type op;
    private String path;
    private JsonNode value;

    public Builder() {
    }

    /**
     * builds an Builder based of the given Attribute
     *
     * @param operation existing Attribute
     */
    public Builder(final PatchOperation operation) {
      op = operation.op;
      path = operation.path;
      value = operation.value;
    }

    /**
     * Sets the patch operation's type.
     *
     * @param op
     * @return the builder itself
     */
    public Builder setOp(final Type op) {
      this.op = op;
      return this;
    }

    /**
     * Set patch operation's path
     *
     * @param path
     * @return the builder itself
     */
    public Builder setPath(final String path) {
      this.path = path;
      return this;
    }

    /**
     * Sets the operation's value.
     *
     * @param value of the value attribute
     * @return the builder itself
     */
    public Builder setValue(final JsonNode value) {
      this.value = value;
      return this;
    }

    /**
     * build {@link PatchOperation} object based on attributes set on builder
     *
     * @return new {@link PatchOperation} object
     */
    public PatchOperation build() {
      return new PatchOperation(this);
    }
  }

  /**
   * Represents an patch operation type
   */
  public static class Type extends MultiValuedAttributeType {
    private static final long serialVersionUID = 3745169684287369226L;
    public static final Type ADD = new Type("add");
    public static final Type REMOVE = new Type("remove");
    public static final Type REPLACE = new Type("replace");

    public Type(final String typeName) {
      super(typeName);
    }

    public static Type of(final String typeName) {
      if (isNullOrEmpty(typeName) || !isPermitted(typeName)) {
        return null;
      }
      return new Type(typeName);
    }

    private static boolean isPermitted(String typeName) {
      return typeName.equalsIgnoreCase(ADD.getValue()) || typeName.equalsIgnoreCase(REMOVE.getValue())
          || typeName.equalsIgnoreCase(REPLACE.getValue());
    }
  }

}
