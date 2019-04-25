package com.sap.scimono.client.query;

public interface IndexPageQuery extends SCIMQuery {
  IndexPageQuery withStartIndex(long startIndex);

  IndexPageQuery withStartIndexAndCount(long startIndex, int count);
}
