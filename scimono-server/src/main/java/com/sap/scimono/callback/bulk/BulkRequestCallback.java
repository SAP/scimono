package com.sap.scimono.callback.bulk;

import com.sap.scimono.entity.bulk.BulkBody;
import com.sap.scimono.entity.bulk.RequestOperation;
import com.sap.scimono.entity.bulk.ResponseOperation;

public interface BulkRequestCallback {
  /**
   * Handles a collection of user and group operations as described as described in SCIM 2.0 Bulk Operations.
   * 
   * @param bulkRequest
   * @return bulkResponse
   */
  BulkBody<ResponseOperation> handleBulkRequest(BulkBody<RequestOperation> bulkRequest);
}
