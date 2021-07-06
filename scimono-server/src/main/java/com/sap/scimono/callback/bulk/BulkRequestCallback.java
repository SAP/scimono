package com.sap.scimono.callback.bulk;

import com.sap.scimono.entity.bulk.BulkRequest;

public interface BulkRequestCallback {
  /**
   * Handles a collection of requests as one request.
   * 
   * @param bulkRequest
   * @return bulkResponse
   */
  public BulkRequest handleBulkRequest(BulkRequest bulkRequest);
}
