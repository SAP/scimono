
package com.sap.scimono.filter;

import com.sap.scimono.SCIMFilterBaseVisitor;
import com.sap.scimono.SCIMFilterLexer;
import com.sap.scimono.SCIMFilterParser;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;

public class QueryFilterParser {
  private static final boolean TWO_STAGE_PARSING = false;

  private QueryFilterParser() {
  }

  /**
   * Parse the provided filter and traverse the resulting expression tree using the provided visitor implementation.
   * @param filter the incoming filter query (needs to be decoded)
   * @param visitor
   */


  public static <T extends SCIMFilterBaseVisitor<U>, U> U parse(final String filter, final T visitor) {
    if (filter == null || visitor == null) {
      return null;
    }

    SCIMFilterLexer lexer = new SCIMFilterLexer(CharStreams.fromString(filter.trim()));
    SCIMFilterParser parser = new SCIMFilterParser(new CommonTokenStream(lexer));

    if (TWO_STAGE_PARSING) {
      parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
      parser.setErrorHandler(new BailErrorStrategy());
    } else {
      parser.removeErrorListeners();
      parser.addErrorListener(new QueryFilterErrorHandler());
    }

    SCIMFilterParser.ParseContext parseTree = parser.parse();
    return visitor.visit(parseTree);
  }
}
