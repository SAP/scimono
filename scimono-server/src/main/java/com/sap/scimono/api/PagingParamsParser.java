
package com.sap.scimono.api;

import com.sap.scimono.exception.InvalidInputException;

public class PagingParamsParser {

  private PagingParamsParser() {
    super();
  }

  public static int parseCount(String count, int countNum) {
    try {
      countNum = Integer.parseInt(count);

      if (countNum < 0) {
        countNum = 0;
      }
    } catch (NumberFormatException e) {
      throw new InvalidInputException("Count is not a numeric value or is out of range.");
    }
    return countNum;
  }

  public static int parseStartIndex(String startIndex, int startIndexNum) {
    try {
      startIndexNum = Integer.parseInt(startIndex);

      if (startIndexNum < 1) {
        startIndexNum = 1;
      }
    } catch (NumberFormatException e) {
      throw new InvalidInputException("StartIndex is not a numeric value or is out of range.");
    }
    return startIndexNum;
  }
}
