
package com.sap.scim.api;

import static com.sap.scim.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scim.entity.config.ServiceProviderConfig.RESOURCE_TYPE_SP_CONFIG;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.sap.scim.SCIMApplication;
import com.sap.scim.callback.config.SCIMConfigurationCallback;
import com.sap.scim.entity.Meta;
import com.sap.scim.entity.config.ServiceProviderConfig;
import com.sap.scim.entity.config.ServiceProviderConfig.Builder;

@Path(API.SERVICE_PROVIDER_CONFIG)
@Produces(APPLICATION_JSON_SCIM)
@Consumes(APPLICATION_JSON_SCIM)
public class ServiceProviderConfiguration {

  private final SCIMConfigurationCallback scimConfig;

  public ServiceProviderConfiguration(@Context Application appContext) {
    SCIMApplication scimApplication = SCIMApplication.from(appContext);

    scimConfig = scimApplication.getConfigurationCallback();
  }

  @GET
  public ServiceProviderConfig getServerConfig(@Context final UriInfo uriInfo) {
    //@formatter:off
    ServiceProviderConfig.Builder configBuilder = new Builder()
        .changePasswordSupported(scimConfig.getPasswordChangeSupportedSetting())
        .patchSupported(scimConfig.getPatchSupportedSetting())
        .sortSupported(scimConfig.getSortSupportedSetting())
        .etagSupported(scimConfig.getETagSupportedSetting())
        .bulk(scimConfig.getBulkSetting())
        .filter(scimConfig.getFilterSetting())
        .paging(scimConfig.getPagingSetting())
        .addAuthenticationScheme(scimConfig.getAuthSchemeSetting())
        .setMeta(new Meta.Builder().setResourceType(RESOURCE_TYPE_SP_CONFIG).setVersion(scimConfig.getServiceProviderConfigVersion()).setLocation(uriInfo.getAbsolutePath().toString()).build());
    //@formatter:on

    return configBuilder.build();
  }

}
