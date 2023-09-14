
package com.sap.scimono.entity.schema;

import static com.sap.scimono.helper.Objects.sameOrEmpty;
import static com.sap.scimono.helper.Strings.isNullOrEmpty;
import static java.util.Objects.hash;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
public class Attribute implements Serializable {

  private static final long serialVersionUID = -7368794137485194307L;

  private final String name;
  private final String type;
  @Valid
  private final List<Attribute> subAttributes;
  private final boolean multiValued;
  private final String description;
  private final boolean required;
  private final List<String> canonicalValues;
  private final boolean caseExact;
  private final String mutability;
  private final String returned;
  private final String uniqueness;
  private final List<String> referenceTypes;

  private String subAttributeOf;
  private String ownerSchemaId;

  @JsonCreator
  private Attribute(@JsonProperty("name") final String name, @JsonProperty("type") final String type,
      @JsonProperty("subAttributes") final List<Attribute> subAttributes, @JsonProperty("multiValued") final boolean multiValued,
      @JsonProperty("description") final String description, @JsonProperty("required") final boolean required,
      @JsonProperty("canonicalValues") final List<String> canonicalValues, @JsonProperty("caseExact") final boolean caseExact,
      @JsonProperty("mutability") final String mutability, @JsonProperty("returned") final String returned,
      @JsonProperty("uniqueness") final String uniqueness, @JsonProperty("referenceTypes") final List<String> referenceTypes) {
    this.name = name;
    this.type = type;
    this.subAttributes = sameOrEmpty(subAttributes);
    this.multiValued = multiValued;
    this.description = description;
    this.required = required;
    this.canonicalValues = sameOrEmpty(canonicalValues);
    this.caseExact = caseExact;
    this.mutability = mutability;
    this.returned = returned;
    this.uniqueness = uniqueness;
    this.referenceTypes = sameOrEmpty(referenceTypes);
  }

  private Attribute(final Builder builder) {
    name = builder.name;
    type = builder.type;
    subAttributes = builder.subAttributes;
    multiValued = builder.multiValued;
    description = builder.description;
    required = builder.required;
    canonicalValues = builder.canonicalValues;
    caseExact = builder.caseExact;
    mutability = builder.mutability;
    returned = builder.returned;
    uniqueness = builder.uniqueness;
    referenceTypes = builder.referenceTypes;
    subAttributeOf = builder.subAttributeOf;
    ownerSchemaId = builder.ownerSchemaId;
  }


  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public List<Attribute> getSubAttributes() {
    return subAttributes;
  }

  public boolean isMultiValued() {
    return multiValued;
  }

  public String getDescription() {
    return description;
  }

  public boolean isRequired() {
    return required;
  }

  public List<String> getCanonicalValues() {
    return canonicalValues;
  }

  public boolean isCaseExact() {
    return caseExact;
  }

  public String getMutability() {
    return mutability;
  }

  public String getReturned() {
    return returned;
  }

  public String getUniqueness() {
    return uniqueness;
  }

  public List<String> getReferenceTypes() {
    return referenceTypes;
  }

  @JsonIgnore
  public String getSubAttributeOf() {
    return subAttributeOf;
  }

  @JsonIgnore
  public String getOwnerSchemaId() {
    return ownerSchemaId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + hash(canonicalValues);
    result = prime * result + Boolean.hashCode(caseExact);
    result = prime * result + hash(description);
    result = prime * result + Boolean.hashCode(multiValued);
    result = prime * result + hash(mutability);
    result = prime * result + hash(name);
    result = prime * result + hash(referenceTypes);
    result = prime * result + Boolean.hashCode(required);
    result = prime * result + hash(returned);
    result = prime * result + hash(subAttributes);
    result = prime * result + hash(type);
    result = prime * result + hash(uniqueness);
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
    if (!(obj instanceof Attribute)) {
      return false;
    }
    Attribute other = (Attribute) obj;
    if (!Objects.equals(canonicalValues, other.canonicalValues)) {
      return false;
    }
    if (caseExact != other.caseExact) {
      return false;
    }
    if (!Objects.equals(description, other.description)) {
      return false;
    }
    if (multiValued != other.multiValued) {
      return false;
    }
    if (!Objects.equals(mutability, other.mutability)) {
      return false;
    }
    if (!Objects.equals(name, other.name)) {
      return false;
    }
    if (!Objects.equals(referenceTypes, other.referenceTypes)) {
      return false;
    }
    if (required != other.required) {
      return false;
    }
    if (!Objects.equals(returned, other.returned)) {
      return false;
    }
    if (!Objects.equals(subAttributes, other.subAttributes)) {
      return false;
    }
    if (!Objects.equals(type, other.type)) {
      return false;
    }
    return Objects.equals(uniqueness, other.uniqueness);
  }

  @Override
  public String toString() {
    //@formatter:off
    return new StringBuilder()
        .append("Attribute [name=").append(name)
        .append(", type=").append(type)
        .append(", subAttributes=").append(subAttributes)
        .append(", multiValued=").append(multiValued)
        .append(", description=").append(description)
        .append(", required=").append(required)
        .append(", canonicalValues=").append(canonicalValues)
        .append(", caseExact=").append(caseExact)
        .append(", mutability=").append(mutability)
        .append(", returned=").append(returned)
        .append(", uniqueness=").append(uniqueness)
        .append(", referenceTypes=").append(referenceTypes)
        .append("]").toString();
    //@formatter:on
  }

  public static class Builder {
    private static final String MULTI_VALUE_DELIMITER = ",";

    private String name;
    private String type;
    private final List<Attribute> subAttributes = new ArrayList<>();
    private boolean multiValued;
    private String description;
    private boolean required;
    private final List<String> canonicalValues = new ArrayList<>();
    private boolean caseExact;
    private String mutability;
    private String returned;
    private String uniqueness;
    private final List<String> referenceTypes = new ArrayList<>();
    private String subAttributeOf;
    private String ownerSchemaId;

    public Builder name(final String name) {
      this.name = name;
      return this;
    }

    public Builder type(final String type) {
      this.type = type;
      return this;
    }

    public Builder description(final String description) {
      this.description = description;
      return this;
    }

    public Builder mutability(final String mutability) {
      this.mutability = mutability;
      return this;
    }

    public Builder returned(final String returned) {
      this.returned = returned;
      return this;
    }

    public Builder uniqueness(final String uniqueness) {
      this.uniqueness = uniqueness;
      return this;
    }

    public Builder multiValued(final boolean multiValued) {
      this.multiValued = multiValued;
      return this;
    }

    public Builder required(final boolean required) {
      this.required = required;
      return this;
    }

    public Builder caseExact(final boolean caseExact) {
      this.caseExact = caseExact;
      return this;
    }

    public Builder clearSubAttributes() {
      subAttributes.clear();
      return this;
    }

    public Builder addSubAttribute(final Attribute attribute) {
      subAttributes.add(attribute);
      return this;
    }

    public Builder addSubAttributes(final Collection<Attribute> attributes) {
      subAttributes.addAll(attributes);
      return this;
    }

    public Builder removeSubAttribute(final Attribute attr) {
      subAttributes.remove(attr);
      return this;
    }

    public Builder clearCanonicalValues() {
      canonicalValues.clear();
      return this;
    }

    public Builder addCanonicalValue(final String value) {
      canonicalValues.add(value);
      return this;
    }

    public Builder removeCanonicalValue(final String value) {
      canonicalValues.remove(value);
      return this;
    }

    /**
     * Accepts a comma-separated list of canonical values. The strings "a,b,c" and "a, b, c" are treated as equivalent and will result in three
     * canonical values being added - "a", "b" and "c".
     *
     * @param csvCanonicalValues
     * @return
     */

    public Builder setCanonicalValues(final String csvCanonicalValues) {
      if (isNullOrEmpty(csvCanonicalValues)) {
        return this;
      }

      String[] values = csvCanonicalValues.split(MULTI_VALUE_DELIMITER);
      Arrays.stream(values).forEach(canonicalValue -> addCanonicalValue(canonicalValue.trim()));

      return this;
    }

    public Builder clearReferenceTypes() {
      referenceTypes.clear();
      return this;
    }

    public Builder addReferenceType(final String refType) {
      referenceTypes.add(refType);
      return this;
    }

    public Builder removeReferenceType(final String refType) {
      referenceTypes.remove(refType);
      return this;
    }

    /**
     * Accepts a comma-separated list of reference types. The strings "a,b,c" and "a, b, c" are treated as equivalent and will result in three
     * reference types being added - "a", "b" and "c".
     *
     * @param csvRefTypes
     * @return
     */

    public Builder setReferenceTypes(final String csvRefTypes) {
      if (isNullOrEmpty(csvRefTypes)) {
        return this;
      }

      String[] values = csvRefTypes.split(MULTI_VALUE_DELIMITER);
      Arrays.stream(values).forEach(refType -> addReferenceType(refType.trim()));

      return this;
    }

    public Builder subAttributeOf(final String attrName) {
      subAttributeOf = attrName;
      return this;
    }

    public Builder ownerSchemaId(String schemaId) {
      this.ownerSchemaId = schemaId;
      return this;
    }

    public String getSubAttributeOf() {
      return subAttributeOf;
    }

    public Attribute build() {
      return new Attribute(this);
    }
  }
}
