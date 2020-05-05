
package com.sap.scimono.callback.config;

import com.sap.scimono.entity.config.AuthenticationSchemeSetting;
import com.sap.scimono.entity.config.BulkSetting;
import com.sap.scimono.entity.config.FilterSetting;
import com.sap.scimono.entity.config.PagingSetting;

import java.net.URI;

public interface SCIMConfigurationCallback {

  /**
   * Determines the maximum amount of resources (e.g. users, groups) returned by page. The returned result is <b>not</b> cached.
   *
   * @return resource limit per page
   */
  int getMaxResourcesPerPage();

  BulkSetting getBulkSetting();

  FilterSetting getFilterSetting();

  PagingSetting getPagingSetting();

  AuthenticationSchemeSetting getAuthSchemeSetting();

  boolean getPasswordChangeSupportedSetting();

  boolean getPatchSupportedSetting();

  boolean getSortSupportedSetting();

  boolean getETagSupportedSetting();

  String getServiceProviderConfigVersion();

  default URI getRedirectApiRoot() {
    return null;
  }
}
