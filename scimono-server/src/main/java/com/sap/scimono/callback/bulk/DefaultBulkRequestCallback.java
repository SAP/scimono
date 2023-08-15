package com.sap.scimono.callback.bulk;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import com.sap.scimono.entity.bulk.BulkBody;
import com.sap.scimono.entity.bulk.RequestOperation;
import com.sap.scimono.entity.bulk.ResponseOperation;


public class DefaultBulkRequestCallback implements BulkRequestCallback {

  @Override
  public BulkBody<ResponseOperation> handleBulkRequest(BulkBody<RequestOperation> bulkRequest) {
    throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
  }
}
