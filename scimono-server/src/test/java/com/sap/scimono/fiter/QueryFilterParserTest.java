
package com.sap.scimono.fiter;

import com.sap.scimono.SCIMFilterBaseVisitor;
import com.sap.scimono.exception.InvalidFilterException;
import com.sap.scimono.filter.QueryFilterParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryFilterParserTest {
  private static SCIMFilterBaseVisitor<Void> dummyVisitor = new SCIMFilterBaseVisitor<>();

  // @formatter:off
  @DisplayName("Test parsing SCIM filter with valid syntax")
  @ParameterizedTest(name = "Test that --- {0} --- is valid SCIM filter")
  @ValueSource(strings = {
      "userName pr",
      " userName pr ",
      "urn:ietf:params:scim:schemas:core:2.0:User:userName pr",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.firstName pr",
      "userName eq \"bjensen\"",
      "name.firstName co \"bjensen\"",
      "urn:ietf:params:scim:schemas:core:2.0:User:userName eq \"bjensen\"",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.firstName eq \"bjensen\"",
      "userName pr AND familyName pr",
      "name.firstName pr OR name.familyName pr",
      "urn:ietf:params:scim:schemas:core:2.0:User:userName pr AND urn:ietf:params:scim:schemas:core:2.0:User:firstName pr",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.firstName pr OR urn:ietf:params:scim:schemas:core:2.0:User:name.familyName pr",
      "userName[type eq \"work\"]",
      "name.firstName[type eq \"work\"]",
      "urn:ietf:params:scim:schemas:core:2.0:User:userName[type eq \"work\"]",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.firstName[type eq \"work\"]",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.firstName[(type eq \"work\")]",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.firstName[not (type eq \"work\")]",
      "emails[type eq \"work\" and value co \"example.com\"]",
      "userType eq \"Employee\" and emails[type eq \"work\" and value co \"@example.com\"]",
      "emails[type eq \"work\" and value co \"@example.com\"] or ims[type eq \"xmpp\" and value co \"@foo.com\"]",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.firstName[type eq \"work\"]",
      "name.familyName co \"O'Malley\"",
      "userName sw \"J\"",
      "schemas eq \"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\"",
      "userType eq \"Employee\" and (emails co \"example.com\" or emails.value co \"example.org\")",
      "userType ne \"Employee\" and not (emails co \"example.com\" or emails.value co \"example.org\")",
      "userType eq \"Employee\" and (emails.type eq \"work\")",
      "userType eq \"Employee\" and emails [type eq \"work\" and value co \"@example.com\"]",
      "emails[type eq \"work\" and value co \"@example.com\"] or ims[type eq \"xmpp\" and value co \"@foo.com\"]"
  }) void testParsingValidFilterSyntax(String invalidExpressionInput) {
    assertDoesNotThrow(() -> execute(invalidExpressionInput));
  }

  @DisplayName("Test parsing SCIM filter with invalid syntax")
  @ParameterizedTest(name = "Test that --- {0} --- is NOT valid SCIM filter")
  @ValueSource(strings = {
      "userName prs",
      " userName prs ",
      "urn:ietf:params:scim:schemas:core:2.0:User:userName prs",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.firstName prs",
      "userName eqs \"bjensen\"",
      "name.firstName cos \"bjensen\"",
      "urn:ietf:params:scim:schemas:core:2.0:User:userName eqs \"bjensen\"",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.firstName eqs \"bjensen\"",
      "userName pr ANDs familyName pr",
      "name.firstName pr ORs name.familyName pr",
      "urn:ietf:params:scim:schemas:core:2.0:User:userName pr ANDs urn:ietf:params:scim:schemas:core:2.0:User:firstName pr",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.firstName pr ORs urn:ietf:params:scim:schemas:core:2.0:User:name.familyName pr",
      "userName[type eqs \"work\"]",
      "name.firstName[type eqs \"work\"]",
      "urn:ietf:params:scim:schemas:core:2.0:User:userName[type eqs \"work\"]",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.firstName[type eqs \"work\"]",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.firstName[(type eqs \"work\")]",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.firstName[not (type eqs \"work\")]",
      "emails[type eq \"work\" ands value co \"example.com\"]",
      "userType eq \"Employee\" ands emails[type eq \"work\" and value co \"@example.com\"]",
      "emails[type eq \"work\" and value co \"@example.com\"] ors ims[type eq \"xmpp\" ands value co \"@foo.com\"]",
      "urn:ietf:params:scim:schemas:core:2.0:User:name.firstName[type eqs \"work\"]",
      "name.familyName cos \"O'Malley\"",
      "userName sws \"J\"",
      "schemas eqs \"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\"",
      "userType eq \"Employee\" and (emails co \"example.com\" ors emails.value co \"example.org\")",
      "userType ne \"Employee\" and nots (emails co \"example.com\" or emails.value co \"example.org\")",
      "userType eq \"Employee\" ands (emails.type eq \"work\")",
      "userType eq \"Employee\" ands emails [type eq \"work\" and value co \"@example.com\"]",
      "emails[type eq \"work\" and value co \"@example.com\"] ors ims[type eq \"xmpp\" and value co \"@foo.com\"]"
  }) void testParsingInvalidFilterSyntax(String validExpressionInput) {
    assertThrows(InvalidFilterException.class, () -> execute(validExpressionInput));
  }

  private void execute(String valuePath){
    QueryFilterParser.parse(valuePath, dummyVisitor);
  }
}
