package com.sap.scimono.client.query;

import com.sap.scimono.SCIMFilterBaseVisitor;
import com.sap.scimono.filter.QueryFilterParser;

import jakarta.ws.rs.client.WebTarget;

public class FilterQuery implements SCIMQuery {
  private static final String FILTER_QUERY_PARAM = "filter";
  private final String filter;

  private FilterQuery(String filter) {
    this.filter = filter;
  }

  @Override
  public WebTarget apply(WebTarget target) {
    return target.queryParam(FILTER_QUERY_PARAM, filter);
  }

  public static FilterQuery fromString(String filter) {
    validateFilterSyntax(filter);
    return new FilterQuery(filter);
  }

  private static void validateFilterSyntax(String filter) {
    QueryFilterParser.parse(filter, new SCIMFilterBaseVisitor<>());
  }
}
