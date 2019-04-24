package com.sap.scimono.client.query;

public interface IdentityPageQuery extends SCIMQuery {
  IdentityPageQuery withStartId(String startId);

  IdentityPageQuery withStartIdAndCount(String startId, int count);
}
