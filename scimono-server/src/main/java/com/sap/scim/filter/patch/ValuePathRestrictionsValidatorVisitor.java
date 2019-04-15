package com.sap.scim.filter.patch;

import com.sap.scim.SCIMFilterParser;
import com.sap.scim.entity.validation.patch.PatchValidationException;
import com.sap.scim.exception.SCIMException;
import com.sap.scim.filter.QueryFilterVisitor;

import java.util.stream.Stream;

public class ValuePathRestrictionsValidatorVisitor extends QueryFilterVisitor<Void, Void> {

  @Override
  public Void visitLogExpression(SCIMFilterParser.LogExpressionContext ctx) {
    String operator = ctx.OPERATOR().getText();

    if(!isOperatorSupported(operator)){
      throw new PatchValidationException(SCIMException.Type.INVALID_FILTER,
          String.format("Filter following value fitler operation is not supported: '%s'", operator));
    }

    return null;
  }

  @Override
  public Void visitValValuePathExpression(SCIMFilterParser.ValValuePathExpressionContext ctx) {
    throw new PatchValidationException(SCIMException.Type.INVALID_FILTER, "Multivalued sub attribute of multivalued attribute is not allowed");
  }

  @Override
  public Void visitBraceExpression(SCIMFilterParser.BraceExpressionContext ctx) {
    throw new PatchValidationException(SCIMException.Type.INVALID_FILTER, "Brace filter expression is not supported");
  }

  @Override
  public Void visitValBraceExpression(SCIMFilterParser.ValBraceExpressionContext ctx) {
    throw new PatchValidationException(SCIMException.Type.INVALID_FILTER, "Brace filter expression is not supported");

  }

  private boolean isOperatorSupported(String operator){
    return Stream.of("eq", "co").anyMatch(operator::equalsIgnoreCase);
  }

  @Override
  public Void getParsedFilter() {
    return null;
  }
}
