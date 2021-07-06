package com.sap.scimono.api;

import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.api.API.BULK;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.callback.bulk.BulkRequestCallback;
import com.sap.scimono.entity.bulk.BulkRequest;
import com.sap.scimono.entity.bulk.validation.ValidBulkRequest;

@Path(BULK)
@Produces(APPLICATION_JSON_SCIM)
@Consumes(APPLICATION_JSON_SCIM)
@ServletRequestProvider
public class Bulk {
  
  private final BulkRequestCallback bulkAPI;
  
  public Bulk(@Context final Application appContext, @Context final UriInfo uriInfo) {
    SCIMApplication scimApplication = SCIMApplication.from(appContext);
    bulkAPI = scimApplication.getBulkRequestCallback();
  }
  
  @POST
  public Response handleBulkRequest(@ValidBulkRequest final BulkRequest bulkRequest) {
    BulkRequest bulkResponse = bulkAPI.handleBulkRequest(bulkRequest);
    return Response.ok().entity(bulkResponse).build();
  }
}
