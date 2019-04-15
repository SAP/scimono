package com.sap.scimono.filter.patch;

import com.sap.scimono.SCIMFilterParser;
import com.sap.scimono.entity.validation.patch.PatchValidationException;
import com.sap.scimono.exception.SCIMException;
import com.sap.scimono.filter.QueryFilterVisitor;

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
