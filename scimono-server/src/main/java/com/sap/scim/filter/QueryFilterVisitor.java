
package com.sap.scim.filter;

import com.sap.scim.SCIMFilterBaseVisitor;
import com.sap.scim.exception.InvalidFilterException;

public abstract class QueryFilterVisitor<F, T> extends SCIMFilterBaseVisitor<T> {

  /**
   * Returns the constructed custom filter after visiting the full expression tree.
   *
   * @throws InvalidFilterException if the filter does not conform to semantic rules defined by implementation
   * @return
   */
  public abstract F getParsedFilter() throws InvalidFilterException;

}
