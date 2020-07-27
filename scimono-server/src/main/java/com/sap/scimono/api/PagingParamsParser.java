
package com.sap.scimono.api;

import com.sap.scimono.exception.InvalidInputException;

public class PagingParamsParser {

  private PagingParamsParser() {
    super();
  }

  public static int getCountNum(String count, int countNum) {
    try {
      countNum = Integer.parseInt(count);
    } catch (NumberFormatException e) {
      throw new InvalidInputException("Count is not a numeric value or is out of range.");
    }
    return countNum;
  }

  public static int getStartIndexNum(String startIndex, int startIndexNum) {
    try {
      startIndexNum = Integer.parseInt(startIndex);
    } catch (NumberFormatException e) {
      throw new InvalidInputException("StartIndex is not a numeric value or is out of range.");
    }
    return startIndexNum;
  }
}
