package com.sap.scimono.client;

import jakarta.ws.rs.client.WebTarget;

import com.sap.scimono.client.query.FilterQuery;
import com.sap.scimono.client.query.SCIMQuery;

public class RequestDetails {
  static final RequestDetails DEFAULT = RequestDetails.builder().build();

  private final SCIMQuery pageQuery;
  private final SCIMQuery filter;
  private final RequestAttributes attributes;

  private RequestDetails(Builder builder) {
    this.pageQuery = builder.pageQuery;
    this.filter = builder.filter;
    this.attributes = builder.attributes;
  }

  SCIMQuery getPageQuery() {
    return pageQuery;
  }

  SCIMQuery getFilter() {
    return filter;
  }

  RequestAttributes getAttributes() {
    return attributes;
  }

  WebTarget apply(WebTarget webTarget) {
    return new SCIMQuery.SCIMQueryBuilder(webTarget)
        .apply(pageQuery)
        .apply(filter)
        .apply(attributes)
        .get();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final RequestAttributes attributes;

    private SCIMQuery pageQuery;
    private SCIMQuery filter;

    private Builder() {
      pageQuery = target -> target;
      filter = target -> target;
      attributes = new RequestAttributes();
    }

    public Builder withPageQuery(SCIMQuery pageQuery) {
      this.pageQuery = pageQuery;
      return this;
    }

    public Builder withFilter(String filter) {
      this.filter = FilterQuery.fromString(filter);
      return this;
    }

    public Builder requestAttribute(String attribute) {
      this.attributes.requestAdditional(attribute);
      return this;
    }

    public Builder excludeAttribute(String attribute) {
      this.attributes.exclude(attribute);
      return this;
    }

    public RequestDetails build() {
      return new RequestDetails(this);
    }
  }
}
