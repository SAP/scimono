package com.sap.scimono.client.query;

import jakarta.ws.rs.client.WebTarget;

public interface SCIMQuery {
  WebTarget apply(WebTarget target);

  class SCIMQueryBuilder {
    private final WebTarget target;

    public SCIMQueryBuilder(WebTarget target) {
      this.target = target;
    }

    public WebTarget get() {
      return target;
    }

    public SCIMQueryBuilder apply(SCIMQuery scimQuery) {
      return new SCIMQueryBuilder(scimQuery.apply(target));
    }

  }
}
