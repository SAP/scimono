
package com.sap.scimono.filter.patch;

import com.sap.scimono.SCIMFilterParser;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.validation.Validator;
import com.sap.scimono.entity.validation.patch.PatchValidationException;
import com.sap.scimono.entity.validation.patch.PathAttributeExistenceValidator;
import com.sap.scimono.entity.validation.patch.PathMutabilityValidator;
import com.sap.scimono.entity.validation.patch.PathSchemaExistenceValidator;
import com.sap.scimono.entity.validation.patch.ValuePathAttributesValidator;
import com.sap.scimono.exception.SCIMException;
import com.sap.scimono.filter.QueryFilterVisitor;

import java.util.ArrayList;
import java.util.List;

public class ValuePathAttributesValidationVisitor extends QueryFilterVisitor<Void, Void> {
  protected ValuePathAttributesValidator valuePathValidator;
  protected String currentAttributePath;
  protected PatchOperation operation;

  public ValuePathAttributesValidationVisitor(ValuePathAttributesValidator valuePathValidator, PatchOperation operation) {
    this(valuePathValidator, operation, valuePathValidator.getCoreSchemaId());
  }

  private ValuePathAttributesValidationVisitor(ValuePathAttributesValidator valuePathValidator, PatchOperation operation,
      String currentAttributePath) {
    this.valuePathValidator = valuePathValidator;
    this.operation = operation;
    this.currentAttributePath = currentAttributePath;
  }

  @Override
  public Void visitAttrPath(SCIMFilterParser.AttrPathContext ctx) {
    return ctx.accept(new AttrPathVisitor(valuePathValidator, operation, currentAttributePath));
  }

  @Override
  public Void visitValuePath(SCIMFilterParser.ValuePathContext ctx) {
    return ctx.accept(new ValuePathExpressionVisitor(valuePathValidator, operation, currentAttributePath));

  }

  protected void validateAttribute(String attributeName) {
    String fullAttrName;
    if (isParentAttribute()) {
      fullAttrName = SchemasCallback.addSchemaToPathIfNotExist(attributeName, valuePathValidator.getCoreSchemaId());
    } else if (isSubAttrBelongToParentSchema(attributeName, valuePathValidator.getCoreSchemaId())) {
      fullAttrName = createMultivaluedFullSubAttributeNotation(attributeName);
    } else {
      throw new PatchValidationException(SCIMException.Type.INVALID_PATH, String.format("Attribute with name '%s' does not exist", attributeName));
    }

    validateAttributeByFullAttributeNotation(fullAttrName);
  }

  protected void validateAttributeByFullAttributeNotation(String fullAttrNotation) {
    List<Validator<PatchOperation>> syntaxValidators = new ArrayList<>();
    SchemasCallback schemaAPI = valuePathValidator.getSchemaAPI();

    syntaxValidators.add(new PathSchemaExistenceValidator(valuePathValidator.getRequiredSchemas()));
    syntaxValidators.add(new PathAttributeExistenceValidator(schemaAPI));

    if (schemaAPI.getSchema(currentAttributePath) != null) {
      syntaxValidators.add(new PathMutabilityValidator(schemaAPI));
    }

    PatchOperation newPatchOperation = new PatchOperation.Builder(operation).setPath(fullAttrNotation).build();
    syntaxValidators.forEach(validator -> validator.validate(newPatchOperation));
  }

  private String createMultivaluedFullSubAttributeNotation(String subAttributeName) {
    SchemasCallback schemaAPI = valuePathValidator.getSchemaAPI();

    if (SchemasCallback.isAttributeNotationContainsSchema(subAttributeName)) {
      return subAttributeName;
    }

    return schemaAPI.appendSubAttributeToPath(currentAttributePath, subAttributeName);
  }

  private boolean isParentAttribute() {
    return valuePathValidator.getCoreSchemaId().equals(currentAttributePath);
  }

  private boolean isSubAttrBelongToParentSchema(String targetAttr, String parentAttr) {
    if (!SchemasCallback.isAttributeNotationContainsSchema(targetAttr)) {
      return true;
    }

    SchemasCallback schemaAPI = valuePathValidator.getSchemaAPI();
    String targetAttrSchema = schemaAPI.getSchemaIdFromAttributeNotation(targetAttr);
    return parentAttr.startsWith(targetAttrSchema);
  }

  @Override
  public Void getParsedFilter() {
    return null;
  }

  private static class AttrPathVisitor extends ValuePathAttributesValidationVisitor {

    private AttrPathVisitor(ValuePathAttributesValidator valuePathValidator, PatchOperation operation, String currentAttributePath) {
      super(valuePathValidator, operation, currentAttributePath);
    }

    @Override
    public Void visitAttrPath(SCIMFilterParser.AttrPathContext ctx) {
      String attributeName = ctx.getText();

      validateAttribute(attributeName);
      return null;
    }
  }

  private static class ValuePathExpressionVisitor extends ValuePathAttributesValidationVisitor {

    private ValuePathExpressionVisitor(ValuePathAttributesValidator valuePathValidator, PatchOperation operation, String currentAttributePath) {
      super(valuePathValidator, operation, currentAttributePath);
    }

    @Override
    public Void visitValuePath(SCIMFilterParser.ValuePathContext ctx) {
      SCIMFilterParser.AttrPathContext pathContext = ctx.attrPath();

      pathContext.accept(new AttrPathVisitor(valuePathValidator, operation, currentAttributePath));

      String attributeName = pathContext.getText();

      SchemasCallback schemaAPI = valuePathValidator.getSchemaAPI();
      if (SchemasCallback.isAttributeNotationContainsSchema(attributeName)) {
        attributeName = schemaAPI.removeSchemaFromAttributeNotation(attributeName, valuePathValidator.getCoreSchemaId());
      }

      String newCurrentAttributePath = schemaAPI.appendSubAttributeToPath(currentAttributePath, attributeName);
      return ctx.valFilter().accept(new ValuePathAttributesValidationVisitor(valuePathValidator, operation, newCurrentAttributePath));
    }
  }
}
