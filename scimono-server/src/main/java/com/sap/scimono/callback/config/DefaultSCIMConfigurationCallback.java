
package com.sap.scimono.callback.config;

import com.sap.scimono.entity.config.AuthenticationSchemeSetting;
import com.sap.scimono.entity.config.BulkSetting;
import com.sap.scimono.entity.config.FilterSetting;
import com.sap.scimono.entity.config.PagingSetting;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;

public class DefaultSCIMConfigurationCallback implements SCIMConfigurationCallback {
  private static final int DEFAULT_RESOURCES_PAGE_LIMIT = 100;

  @Override
  public int getMaxResourcesPerPage() {
    return DEFAULT_RESOURCES_PAGE_LIMIT;
  }

  @Override
  public BulkSetting getBulkSetting() {
    return new BulkSetting(false, 0, 0);
  }

  @Override
  public FilterSetting getFilterSetting() {
    return new FilterSetting(true, getMaxResourcesPerPage());
  }

  @Override
  public PagingSetting getPagingSetting() {
    return new PagingSetting(true, Integer.parseInt(PagedByIndexSearchResult.DEFAULT_START_INDEX),
        Integer.parseInt(PagedByIndexSearchResult.DEFAULT_COUNT), PagedByIdentitySearchResult.PAGINATION_BY_ID_START_PARAM,
        PagedByIdentitySearchResult.PAGINATION_BY_ID_END_PARAM);
  }

  @Override
  public AuthenticationSchemeSetting getAuthSchemeSetting() {
    return AuthenticationSchemeSetting.OAUTH_BEARER_TOKEN;
  }

  @Override
  public boolean getPasswordChangeSupportedSetting() {
    return false;
  }

  @Override
  public boolean getPatchSupportedSetting() {
    return false;
  }

  @Override
  public boolean getSortSupportedSetting() {
    return false;
  }

  @Override
  public boolean getETagSupportedSetting() {
    return false;
  }

  @Override
  public String getServiceProviderConfigVersion() {
    return "1";
  }
}
