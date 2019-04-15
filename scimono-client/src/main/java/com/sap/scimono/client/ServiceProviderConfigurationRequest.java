package com.sap.scimono.client;

import com.sap.scimono.entity.config.ServiceProviderConfig;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static com.sap.scimono.api.API.SERVICE_PROVIDER_CONFIG;
import static com.sap.scimono.client.ResourceAction.GET_SINGLE;

public class ServiceProviderConfigurationRequest {
  private WebTarget targetSystem;
  private SCIMRequest scimRequest;

  public ServiceProviderConfigurationRequest(WebTarget targetSystem, SCIMRequest scimRequest) {
    this.targetSystem = targetSystem.path(SERVICE_PROVIDER_CONFIG);
    this.scimRequest = scimRequest;
  }

  public SCIMResponse<ServiceProviderConfig> readServiceProviderConfiguration() {
    Response response = scimRequest.get(targetSystem);
    return SCIMResponse.newInstance(ServiceProviderConfig.class, response, scimRequest.getScimActionResponseStatusConfig(GET_SINGLE));
  }
}
