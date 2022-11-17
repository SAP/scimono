
package com.sap.scimono.entity.validation.patch;

import static com.sap.scimono.callback.schemas.SchemasCallback.SCHEMA_URN_DELIMETER;
import static com.sap.scimono.callback.schemas.SchemasCallback.isAttributeNotationContainsSchema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sap.scimono.callback.groups.GroupsCallback;
import com.sap.scimono.callback.resourcetype.ResourceTypesCallback;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.callback.users.UsersCallback;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.entity.validation.Validator;
import com.sap.scimono.helper.Strings;


public class PatchValidationFramework {

  private final SchemasCallback schemaAPI;
  private final ResourceTypesCallback resourceTypesAPI;

  private final String coreSchemaId;
  private final String resourceType;

  private final Map<String, Schema> requiredSchemas;

  // @formatter:off
  private PatchValidationFramework(SchemasCallback schemaAPI,
                                   ResourceTypesCallback resourceTypesAPI,
                                   Map<String, Schema> requiredSchemas,
                                   String coreSchemaId,
                                   String resourceType) {
    this.schemaAPI = schemaAPI;
    this.resourceTypesAPI = resourceTypesAPI;
    this.requiredSchemas = requiredSchemas;
    this.coreSchemaId = coreSchemaId;
    this.resourceType = resourceType;
  }
  // @formatter:on

  public void validate(PatchBody body) {
    List<Validator<PatchBody>> validators = Arrays.asList(new PatchSchemaPresenceValidator(), new AnyOperationPresenceValidator());

    validators.forEach(v -> v.validate(body));
    
    // @formatter:off
    body.getOperations().forEach(operation -> {
      List<String> matchingPaths = schemaAPI.getSchema(coreSchemaId)
    		  .getAttributes()
    		  .stream()
    		  .map(Attribute::getName)
    		  .filter(attributeName -> attributeName.equalsIgnoreCase(operation.getPath()))
    		  .collect(Collectors.toList());
      
      String caseExactPath = matchingPaths.isEmpty() ? operation.getPath() : matchingPaths.get(0);
      String fullPath = addSchemaToPathIfNotExist(caseExactPath, coreSchemaId);
      validateOperation(new PatchOperation.Builder(operation).setPath(fullPath).build());
    });
    // @formatter:on
  }
  //TODO check utils that contain this method
  private static String addSchemaToPathIfNotExist(String path, String defaultSchema) {
    if (Strings.isNullOrEmpty(path) || isAttributeNotationContainsSchema(path)) {
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
        validators.add(new PatchOperationSchemaBasedAttributeValueValidator(schemaAPI, coreSchemaId, requiredSchemas));
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
      validators.add(new PathAttributeExistenceValidator(schemaAPI));
      validators.add(new PathMutabilityValidator(schemaAPI));
      validators.add(new PathRemoveRequiredAttributeValidator(schemaAPI, resourceTypesAPI, resourceType));
    }

    return validators;
  }

  private boolean isOperationPathContainsValueFilter(String path){
    return path.contains("[");
  }

  public static PatchValidationFramework groupsFramework(final SchemasCallback schemaAPI, final ResourceTypesCallback resourceTypesAPI, final
      GroupsCallback groupsAPI) {
    String coreSchemaId = Group.SCHEMA;
    String resourceType = Group.RESOURCE_TYPE_GROUP;

    Map<String, Schema> requiredSchemas = getRequiredSchemas(schemaAPI, groupsAPI.getSchemaIdsAllowingPatch());
    return new PatchValidationFramework(schemaAPI, resourceTypesAPI, requiredSchemas, coreSchemaId, resourceType);
  }

  public static PatchValidationFramework usersFramework(final SchemasCallback schemaAPI, final ResourceTypesCallback resourceTypesAPI, final UsersCallback usersAPI) {
    String coreSchemaId = User.SCHEMA;
    String resourceType = User.RESOURCE_TYPE_USER;

    Map<String, Schema> requiredSchemas = getRequiredSchemas(schemaAPI, usersAPI.getSchemaIdsAllowingPatch());
    return new PatchValidationFramework(schemaAPI, resourceTypesAPI, requiredSchemas, coreSchemaId, resourceType);
  }

  private static Map<String, Schema> getRequiredSchemas(final SchemasCallback schemaAPI, final Set<String> requiredSchemaIds) {
    // @formatter:off
    Map<String, Schema> requiredSchemas = schemaAPI.getCustomSchemas().stream().collect(Collectors.toMap(Schema::getId, schema -> schema));
    requiredSchemas.putAll(schemaAPI.getSchemas().stream()
        .filter(schema -> requiredSchemaIds.contains(schema.getId()))
        .collect(Collectors.toMap(Schema::getId, schema -> schema)));
    // @formatter:on
    return requiredSchemas;
  }

}
