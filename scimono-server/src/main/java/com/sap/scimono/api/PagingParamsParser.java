
package com.sap.scimono.api;

import com.sap.scimono.exception.InvalidInputException;

public class PagingParamsParser {

  private PagingParamsParser() {
    super();
  }

  public static int parseCount(String countParam, int count) {
    try {
      count = Integer.parseInt(countParam);

      if (count < 0) {
        count = 0;
      }
    } catch (NumberFormatException e) {
      throw new InvalidInputException("Count is not a numeric value or is out of range.");
    }
    return count;
  }

  public static int parseStartIndex(String startIndexParam, int startIndex) {
    try {
      startIndex = Integer.parseInt(startIndexParam);

      if (startIndex < 1) {
        startIndex = 1;
      }
    } catch (NumberFormatException e) {
      throw new InvalidInputException("StartIndex is not a numeric value or is out of range.");
    }
    return startIndex;
  }
}
