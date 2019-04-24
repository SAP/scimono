package com.sap.scimono.client.query;

import javax.ws.rs.client.WebTarget;

public interface SCIMQuery {
  WebTarget apply(WebTarget target);

  class SCIMQueryBuilder {
    private WebTarget target;

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
