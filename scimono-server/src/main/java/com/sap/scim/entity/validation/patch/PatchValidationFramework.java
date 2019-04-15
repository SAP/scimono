
package com.sap.scim.entity.validation.patch;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sap.scim.callback.schemas.SchemasCallback;
import com.sap.scim.entity.Group;
import com.sap.scim.entity.User;
import com.sap.scim.entity.patch.PatchBody;
import com.sap.scim.entity.patch.PatchOperation;
import com.sap.scim.entity.schema.Schema;
import com.sap.scim.entity.validation.Validator;
import com.sap.scim.helper.Strings;

public class PatchValidationFramework {
  private static final Pattern SCHEMA_PATTERN = Pattern.compile("^urn:[a-z0-9][a-z0-9-]{0,31}:([A-Za-z0-9()+,\\-.:=@;$_!*']|%[0-9a-f]{2})+$");
  private static final String SCHEMA_URN_DELIMETER = ":";

  private SchemasCallback schemaAPI;
  private String coreSchemaId;
  private Map<String, Schema> requiredSchemas;

  private PatchValidationFramework(final SchemasCallback schemaAPI, final Map<String, Schema> requiredSchemas, final String coreSchemaId) {
    this.schemaAPI = schemaAPI;
    this.requiredSchemas = requiredSchemas;
    this.coreSchemaId = coreSchemaId;
  }

  public void validate(PatchBody body) {
    List<Validator<PatchBody>> validators = Arrays.asList(new PatchSchemaPresenceValidator(), new AnyOperationPresenceValidator());

    validators.forEach(v -> v.validate(body));
    body.getOperations().forEach(operation -> {
      String fullPath = addSchemaToPathIfNotExist(operation.getPath(), coreSchemaId);
      validateOperation(new PatchOperation.Builder(operation).setPath(fullPath).build());
    });
  }
  //TODO check utils that contain this method
  private static String addSchemaToPathIfNotExist(String path, String defaultSchema) {
    if (Strings.isNullOrEmpty(path) || path.matches(SCHEMA_PATTERN.toString())) {
      return path;
    }
    return String.join(SCHEMA_URN_DELIMETER, defaultSchema, path);
  }

  private void validateOperation(PatchOperation operation) {
    List<Validator<PatchOperation>> validators = new LinkedList<>();
    validators.add(new OperationTypeValidator());
    validators.add(new MandatoryPathValidator());
    validators.add(new AddReplaceOperationValueValidator());
    validators.addAll(getPathValidators(operation.getPath()));

    if (!PatchOperation.Type.REMOVE.equals(operation.getOp())) {
        validators.add(new AttributeAndValueValidator(schemaAPI, coreSchemaId, requiredSchemas));
    }

    validators.forEach(v -> v.validate(operation));
  }

  private List<Validator<PatchOperation>> getPathValidators(String path){
    if(Strings.isNullOrEmpty(path)){
      return new ArrayList<>();
    }

    List<Validator<PatchOperation>> validators = new ArrayList<>();

    if(isOperationPathContainsValueFilter(path)){
      validators.add(new ValuePathAttributesValidator(requiredSchemas, schemaAPI, coreSchemaId));
      validators.add(new ValuePathStructureValidator());
      validators.add(new ValuePathRestrictionsValidator());
    } else {
      validators.add(new PathSchemaExistenceValidator(requiredSchemas));
      validators.add(new PathAttributeExistanceValidator(schemaAPI));
      validators.add(new PathMutabilityValidator(schemaAPI));
    }

    return validators;
  }

  private boolean isOperationPathContainsValueFilter(String path){
    return path.contains("[");
  }

  public static PatchValidationFramework groupsFramework(final SchemasCallback schemaAPI) {
    String coreSchemaId = Group.SCHEMA;
    Map<String, Schema> requiredSchemas = getRequredSchemas(schemaAPI, Collections.singleton(coreSchemaId));
    return new PatchValidationFramework(schemaAPI, requiredSchemas, coreSchemaId);
  }

  public static PatchValidationFramework usersFramework(final SchemasCallback schemaAPI) {
    String coreSchemaId = User.SCHEMA;
    Map<String, Schema> requiredSchemas = getRequredSchemas(schemaAPI, Collections.singleton(coreSchemaId));
    return new PatchValidationFramework(schemaAPI, requiredSchemas, coreSchemaId);
  }

  private static Map<String, Schema> getRequredSchemas(final SchemasCallback schemaAPI, final Set<String> requiredSchemaIds) {
    // @formatter:off
    Map<String, Schema> requiredSchemas = schemaAPI.getCustomSchemas().stream()
        .filter(schema -> schema.getId().startsWith(Schema.EXTENSION_SCHEMA_URN) || requiredSchemaIds.contains(schema.getId()))
        .collect(Collectors.toMap(schema -> schema.getId(), schema -> schema));

    return requiredSchemas;
    // @formatter:on
  }

}
