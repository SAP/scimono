
package com.sap.scimono.api;

import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.api.API.SERVICE_PROVIDER_CONFIG;
import static com.sap.scimono.entity.config.ServiceProviderConfig.RESOURCE_TYPE_SP_CONFIG;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.config.ServiceProviderConfig;
import com.sap.scimono.entity.config.ServiceProviderConfig.Builder;
import com.sap.scimono.helper.ResourceLocationService;

@Path(SERVICE_PROVIDER_CONFIG)
@Produces(APPLICATION_JSON_SCIM)
@Consumes(APPLICATION_JSON_SCIM)
public class ServiceProviderConfiguration {

  private final SCIMConfigurationCallback scimConfig;
  private final ResourceLocationService resourceLocationService;

  public ServiceProviderConfiguration(@Context Application appContext, @Context UriInfo uriInfo) {
    SCIMApplication scimApplication = SCIMApplication.from(appContext);

    scimConfig = scimApplication.getConfigurationCallback();
    resourceLocationService = new ResourceLocationService(uriInfo, scimConfig, SERVICE_PROVIDER_CONFIG);
  }

  @GET
  public ServiceProviderConfig getServerConfig() {
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
        .setMeta(new Meta.Builder()
            .setResourceType(RESOURCE_TYPE_SP_CONFIG)
            .setVersion(scimConfig.getServiceProviderConfigVersion())
            .setLocation(resourceLocationService.getLocation().toString())
            .build()
        );
    //@formatter:on

    return configBuilder.build();
  }

}
