package com.sap.scimono.callback.bulk;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.sap.scimono.entity.bulk.BulkRequest;


public class DefaultBulkRequestCallback implements BulkRequestCallback {

  @Override
  public BulkRequest handleBulkRequest(BulkRequest bulkRequest) {
    throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
  }

}
