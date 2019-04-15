
package com.sap.scim.callback.config;

import com.sap.scim.entity.config.AuthenticationSchemeSetting;
import com.sap.scim.entity.config.BulkSetting;
import com.sap.scim.entity.config.FilterSetting;
import com.sap.scim.entity.config.PagingSetting;

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
}
