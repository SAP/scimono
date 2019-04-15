package com.sap.scim.filter.patch;

import com.sap.scim.SCIMFilterParser;
import com.sap.scim.entity.validation.patch.PatchValidationException;
import com.sap.scim.exception.SCIMException;
import com.sap.scim.filter.QueryFilterVisitor;

public class ValuePathStructureValidationVisitor extends QueryFilterVisitor<Void, Void> {

  @Override
  public Void visitParse(SCIMFilterParser.ParseContext ctx) {
    SCIMFilterParser.FilterContext filterContext = ctx.filter();

    if(!(filterContext instanceof SCIMFilterParser.ValuePathExpressionContext)){
      throw new PatchValidationException(SCIMException.Type.INVALID_PATH, "The specified 'valuePath' is not valid");
    }

    return visit(filterContext);
  }

  @Override
  public Void getParsedFilter() {
    return null;
  }
}
