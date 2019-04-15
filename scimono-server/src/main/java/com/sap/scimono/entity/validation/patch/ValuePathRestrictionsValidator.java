
package com.sap.scimono.entity.validation.patch;

import com.sap.scimono.entity.patch.PatchOperation;
import com.sap.scimono.entity.validation.Validator;
import com.sap.scimono.filter.QueryFilterParser;
import com.sap.scimono.filter.patch.ValuePathRestrictionsValidatorVisitor;

public class ValuePathRestrictionsValidator implements Validator<PatchOperation> {

  @Override
  public void validate(PatchOperation operation) {
    QueryFilterParser.parse(operation.getPath(), new ValuePathRestrictionsValidatorVisitor());
  }
}
