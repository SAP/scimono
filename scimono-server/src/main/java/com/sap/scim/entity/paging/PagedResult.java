
package com.sap.scim.entity.paging;

import java.util.List;

public class PagedResult<T> {

  private int totalResourcesCount;
  private List<T> pageContents;

  public PagedResult(final int entityCountInDB, final List<T> entities) {
    this.totalResourcesCount = entityCountInDB;
    this.pageContents = entities;
  }

  public int getTotalResourceCount() {
    return totalResourcesCount;
  }

  public List<T> getResources() {
    return pageContents;
  }

  public int getResourcesCount() {
    return pageContents.size();
  }
}
