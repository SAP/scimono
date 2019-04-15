
package com.sap.scimono.filter;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.scimono.exception.InvalidFilterException;

import java.util.BitSet;

public class QueryFilterErrorHandler implements ANTLRErrorListener {
  private static final Logger logger = LoggerFactory.getLogger(QueryFilterErrorHandler.class);

  @Override
  public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine,
      final String msg, final RecognitionException e) {
    logger.error("Syntax error while parsing filter. Offending symbol {}, line {}, charPositionInLine {}, message {}",
        offendingSymbol, line, charPositionInLine, msg, e);
    throw new InvalidFilterException(msg);
  }

  @Override
  public void reportAmbiguity(final Parser recognizer, final DFA dfa, final int startIndex, final int stopIndex, final boolean exact,
      final BitSet ambigAlts, final ATNConfigSet configs) {
    throw new InvalidFilterException();
  }

  @Override
  public void reportAttemptingFullContext(final Parser recognizer, final DFA dfa, final int startIndex, final int stopIndex,
      final BitSet conflictingAlts, final ATNConfigSet configs) {
    throw new InvalidFilterException();
  }

  @Override
  public void reportContextSensitivity(final Parser recognizer, final DFA dfa, final int startIndex, final int stopIndex, final int prediction,
      final ATNConfigSet configs) {
    throw new InvalidFilterException();
  }

}
