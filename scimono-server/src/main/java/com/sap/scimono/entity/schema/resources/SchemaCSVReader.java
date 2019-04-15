
package com.sap.scimono.entity.schema.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.Schema;

public class SchemaCSVReader {
  private static final String SCHEMAS_CSV_LOCATION = "data/schemas.csv";
  private static final String SCHEMAS_ATTRIBUTES_CSV_LOCATION = "data/schemas_attributes.csv";

  private static final String CSV_DELIMITER_REGEX = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
  private static final int CSV_HEADER_LINE_INDEX = 0;

  private static Map<String, Schema> importedSchemas;

  public static Map<String, Schema> getImportedSchemasFromCSVs() {
    if (importedSchemas == null) {
      importedSchemas = Collections.unmodifiableMap(importSchemasFromCSVs());
    }

    return importedSchemas;
  }

  private static Map<String, Schema> importSchemasFromCSVs() {
    Map<String, Schema> importedSchemas = new HashMap<>();

    Map<String, String[]> allSchemasAllProperties = getSchemaProperties();
    Map<String, List<String[]>> allSchemasAllAttributes = getSchemaAttributes();

    allSchemasAllProperties.entrySet().forEach(schemaWithProperties -> {
      String schemaId = schemaWithProperties.getKey();

      Schema.Builder schemaBuilder = new Schema.Builder();
      buildSchemaProperties(schemaBuilder, schemaWithProperties.getValue());
      buildSchemaAttributes(schemaBuilder, allSchemasAllAttributes.get(schemaId));

      importedSchemas.put(schemaId, schemaBuilder.build());
    });

    return importedSchemas;
  }


  private static Map<String, String[]> getSchemaProperties() {
    List<String[]> schemaCSVLines = parseCSVLines(SCHEMAS_CSV_LOCATION);

    Map<String, String[]> result = new HashMap<>();

    for (int i = CSV_HEADER_LINE_INDEX + 1; i < schemaCSVLines.size(); i++) {
      String[] schemaProperties = schemaCSVLines.get(i);
      String schemaId = schemaProperties[SchemasCSVHeaderIndex.ID];
      result.put(schemaId, schemaProperties);
    }

    return result;
  }

  private static Map<String, List<String[]>> getSchemaAttributes() {
    List<String[]> schemaAttributesCsvLines = parseCSVLines(SCHEMAS_ATTRIBUTES_CSV_LOCATION);

    Map<String, List<String[]>> allSchemasAllAttributes = new HashMap<>();

    for (int i = CSV_HEADER_LINE_INDEX + 1; i < schemaAttributesCsvLines.size(); i++) {
      String[] attributeProperties = schemaAttributesCsvLines.get(i);
      String schemaId = attributeProperties[SchemasAttributesCSVHeaderIndex.SCHEMA_ID];

      allSchemasAllAttributes.computeIfAbsent(schemaId, emptyList -> new ArrayList<>());
      allSchemasAllAttributes.get(schemaId).add(attributeProperties);
    }

    return allSchemasAllAttributes;
  }

  private static List<String[]> parseCSVLines(String csvFileLocation) {
    List<String[]> csvLines = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(
        csvFileLocation)))) {

      String line;
      while ((line = reader.readLine()) != null) {
        String[] attributes = line.split(CSV_DELIMITER_REGEX);
        csvLines.add(attributes);
      }
    } catch (IOException e) {
      //TODO fixme
      e.printStackTrace();
    }

    return csvLines;
  }

  private static void buildSchemaProperties(Schema.Builder schemaBuilder, String[] schemaProperties) {
    schemaBuilder.setId(schemaProperties[SchemasCSVHeaderIndex.ID]);

    Meta meta = new Meta.Builder(Instant.parse(schemaProperties[SchemasCSVHeaderIndex.CREATED]),
        Instant.parse(schemaProperties[SchemasCSVHeaderIndex.LAST_MODIFIED])).setVersion(schemaProperties[SchemasCSVHeaderIndex.VERSION]).build();

    schemaBuilder.setMeta(meta);
    schemaBuilder.name(schemaProperties[SchemasCSVHeaderIndex.NAME]);
    schemaBuilder.description(schemaProperties[SchemasCSVHeaderIndex.DESCRIPTION]);
  }

  private static void buildSchemaAttributes(Schema.Builder schemaBuilder, List<String[]> attributeProperties) {
    Map<String, Attribute.Builder> attributeMap = new HashMap<>();

    // @formatter:off
    List<String[]> parentAttrs = attributeProperties.stream()
      .filter(attr -> attr[SchemasAttributesCSVHeaderIndex.SUB_ATTRIBUTE_OF].equals("NULL"))
      .collect(Collectors.toList());

    parentAttrs.forEach(parentAttributeProperties -> {
        String attrName = parentAttributeProperties[SchemasAttributesCSVHeaderIndex.NAME];
        attributeMap.put(attrName, buildAttribute(parentAttributeProperties));
      });

    List<String[]> childAttrs = attributeProperties.stream()
        .filter(attr-> !attr[SchemasAttributesCSVHeaderIndex.SUB_ATTRIBUTE_OF].equals("NULL"))
        .collect(Collectors.toList());

    childAttrs.forEach(childAttributeProperties -> {
      Attribute.Builder attrBuilder = buildAttribute(childAttributeProperties);

      Attribute.Builder parentAttribute = attributeMap.get(childAttributeProperties[SchemasAttributesCSVHeaderIndex.SUB_ATTRIBUTE_OF]);
      parentAttribute.addSubAttribute(attrBuilder.build());
    });

    schemaBuilder.addAttributes(
        attributeMap.values().stream()
        .map(builder -> builder.build())
        .collect(Collectors.toList())
    );
    // @formatter:on
  }

  private static Attribute.Builder buildAttribute(String[] attrProperties) {
    Attribute.Builder attrBuilder = new Attribute.Builder();

    // @formatter:off
    attrBuilder
      .name(attrProperties[SchemasAttributesCSVHeaderIndex.NAME])
      .type(attrProperties[SchemasAttributesCSVHeaderIndex.TYPE])
      .description(attrProperties[SchemasAttributesCSVHeaderIndex.DESCRIPTION])
      .multiValued(Integer.parseInt(attrProperties[SchemasAttributesCSVHeaderIndex.MULTIVALUED]) != 0)
      .required(Integer.parseInt(attrProperties[SchemasAttributesCSVHeaderIndex.REQUIRED]) != 0)
      .caseExact(Integer.parseInt(attrProperties[SchemasAttributesCSVHeaderIndex.CASE_EXACT]) != 0)
      .mutability(attrProperties[SchemasAttributesCSVHeaderIndex.MUTABILITY])
      .returned(attrProperties[SchemasAttributesCSVHeaderIndex.RETURNED])
      .uniqueness(attrProperties[SchemasAttributesCSVHeaderIndex.UNIQUENESS].equals("NULL") ? null : attrProperties[SchemasAttributesCSVHeaderIndex.UNIQUENESS])
      .setCanonicalValues(attrProperties[SchemasAttributesCSVHeaderIndex.CANONICAL_VALUES].equals("NULL") ? null : attrProperties[SchemasAttributesCSVHeaderIndex.CANONICAL_VALUES].replace("\"", ""))
      .setReferenceTypes(attrProperties[SchemasAttributesCSVHeaderIndex.REFERENCE_TYPES].equals("NULL") ? null : attrProperties[SchemasAttributesCSVHeaderIndex.REFERENCE_TYPES].replace("\"", ""))
      .subAttributeOf(attrProperties[SchemasAttributesCSVHeaderIndex.SUB_ATTRIBUTE_OF].equals("NULL") ? null : attrProperties[SchemasAttributesCSVHeaderIndex.SUB_ATTRIBUTE_OF]);
    // @formatter:on

    return attrBuilder;
  }
}
