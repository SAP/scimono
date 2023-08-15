package com.sap.scimono.client;

import jakarta.ws.rs.core.Response;
import java.util.Collection;

public class ActionResponseStatusConfig {
  private Collection<Response.Status> successResponseStatuses;

  public ActionResponseStatusConfig(Collection<Response.Status> successResponseStatuses) {
    this.successResponseStatuses = successResponseStatuses;
  }

  public Collection<Response.Status> getSuccessResponseStatuses() {
    return successResponseStatuses;
  }

  public boolean isSuccess(int responseCode) {
    return isSuccess(Response.Status.fromStatusCode(responseCode));
  }

  public boolean isSuccess(Response.Status responseStatus) {
    return successResponseStatuses.contains(responseStatus);
  }
}
