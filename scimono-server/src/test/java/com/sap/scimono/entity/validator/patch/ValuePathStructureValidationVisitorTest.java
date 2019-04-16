package com.sap.scimono.entity.validator.patch;

import com.sap.scimono.entity.validation.patch.PatchValidationException;
import com.sap.scimono.filter.QueryFilterParser;
import com.sap.scimono.filter.patch.ValuePathStructureValidationVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

//The tests belongs to corresponding labels in createOperationPredicate grammar
public class ValuePathStructureValidationVisitorTest {

  // @formatter:off
  @DisplayName("Test for Invalid multivalued filter")
  @ParameterizedTest(name = "Test that --- {0} --- is NOT valid multivalued filter")
  @ValueSource(strings = {
      "userName eq \"something\"",
      "userName eq \"something\" and title eq \"something\"",
      "userName eq \"something\" or title eq \"something\"",
      "(userName eq \"something\")"
  })
  public void testInvalidMultivaluedAttributeFilterExpressionInput(String validExpressionInput) {
    assertThrows(PatchValidationException.class, () -> execute(validExpressionInput));
  }

  @DisplayName("Test for valid multivalued filter")
  @ParameterizedTest(name = "Test that --- {0} --- is valid multivalued filter")
  @ValueSource(strings = {
      "emails[type[some eq \"some\"]]",
      "emails[type eq \"something\" and value eq \"something\"]",
      "emails[type eq \"something\" or value eq \"something\"]",
      "emails[(type eq \"something\")]"
  })
  public void testValidMultivaluedAttributeFilterExpressionInput(String invalidExpressionInput) {
    assertDoesNotThrow(() -> execute(invalidExpressionInput));
  }
  // @formatter:on

  private void execute(String valuePath){
    QueryFilterParser.parse(valuePath, new ValuePathStructureValidationVisitor());
  }
}

