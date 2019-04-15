
package com.sap.scimono.entity.schema;

public enum AttributeDataType {
  STRING("string"),
  BOOLEAN("boolean"),
  DECIMAL("decimal"),
  INTEGER("integer"),
  DATE_TIME("dateTime"),
  BINARY("binary"),
  REFERENCE("reference"),
  COMPLEX("complex");

  private String dateType;

  AttributeDataType(String dataType) {
    this.dateType = dataType;
  }

  @Override
  public String toString() {
    return dateType;
  }

  public static AttributeDataType of(String type) {
    for (AttributeDataType dt : values()) {
      if (dt.toString().equals(type)) {
        return dt;
      }
    }
    throw new IllegalArgumentException("unrecognised data type");
  }
}
