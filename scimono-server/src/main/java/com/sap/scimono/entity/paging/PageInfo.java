
package com.sap.scimono.entity.paging;

import com.sap.scimono.helper.Strings;

public class PageInfo {

  private final int entityCount;
  private final String startId;
  private final int startIndex;

  private PageInfo(final int entityCount, final int startIndex) {
    this.entityCount = entityCount;
    this.startIndex = startIndex;
    this.startId = null;
  }

  private PageInfo(final int entityCount, final String startId) {
    this.entityCount = entityCount;
    this.startId = startId;
    this.startIndex = 0;
  }

  public int getEntityCount() {
    return entityCount;
  }

  public String getStartId() {
    return startId;
  }

  public int getStartIndex() {
    return startIndex;
  }

  public static PageInfo getInstance(int count, int startIndex, String startId) {
    if (!Strings.isNullOrEmpty(startId)) {
      return new PageInfo(count, startId);
    }

    return new PageInfo(count, startIndex);
  }

  public boolean isStandardPaging() {
    return Strings.isNullOrEmpty(startId);
  }

}
