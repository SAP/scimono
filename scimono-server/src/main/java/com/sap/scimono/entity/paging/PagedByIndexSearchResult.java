/**
 * The MIT License (MIT)
 * <p>
 * Copyright (C) 2013-2016 tarent solutions GmbH
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.sap.scimono.entity.paging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.User;

import static com.sap.scimono.helper.Objects.sameOrEmpty;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A class that holds all information from a search request
 * <p>
 * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02">SCIM core schema 2.0</a>
 * </p>
 *
 * @param <T> {@link User} or {@link Group}
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PagedByIndexSearchResult<T extends Resource<T>> {

  private static final String SCHEMA = "urn:ietf:params:scim:api:messages:2.0:ListResponse";
  public static final String DEFAULT_START_INDEX = "1";
  public static final String DEFAULT_COUNT = "100";
  private final long totalResults;
  private final long itemsPerPage;
  private final Long startIndex;
  private final Set<String> schemas = Collections.singleton(SCHEMA);

  private final List<T> resources;

  @JsonCreator
  public PagedByIndexSearchResult(@JsonProperty("Resources") final List<T> resources, @JsonProperty("totalResults") final long totalResults,
                                  @JsonProperty("itemsPerPage") final long itemsPerPage, @JsonProperty("startIndex") final Long startIndex) {
    this.resources = sameOrEmpty(resources);
    this.totalResults = totalResults;
    this.itemsPerPage = itemsPerPage;
    this.startIndex = startIndex;
  }

  public PagedByIndexSearchResult(PagedResult<T> resources, final Long startIndex) {
    this(resources.getResources(), resources.getTotalResourceCount(), resources.getResourcesCount(), startIndex);
  }

  /**
   * gets a list of found {@link User}s or {@link Group}s
   *
   * @return a list of found resources
   */
  @JsonProperty("Resources")
  public List<T> getResources() {
    return resources;
  }

  /**
   * The total number of results returned by the list or query operation. This may not be equal to the number of elements in the Resources attribute
   * of the list response if pagination is requested.
   *
   * @return the total result
   */
  public long getTotalResults() {
    return totalResults;
  }

  /**
   * Gets the schemas of the search result
   *
   * @return the search result schemas
   */
  public Set<String> getSchemas() {
    return schemas;
  }

  /**
   * The number of Resources returned in a list response page.
   *
   * @return items per page
   */
  public long getItemsPerPage() {
    return itemsPerPage;
  }

  /**
   * The 1-based index of the first result in the current set of list results.
   *
   * @return the start index of the actual page
   */
  public Long getStartIndex() {
    return startIndex;
  }
}
