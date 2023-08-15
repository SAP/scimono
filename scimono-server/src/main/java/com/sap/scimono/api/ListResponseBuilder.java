package com.sap.scimono.api;

import static com.sap.scimono.entity.paging.PagedByIdentitySearchResult.PAGINATION_BY_ID_END_PARAM;
import static com.sap.scimono.helper.Strings.isNullOrEmpty;

import java.util.List;
import java.util.function.Supplier;

import jakarta.ws.rs.core.Response;

import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;

public class ListResponseBuilder<T extends Resource<T>> implements PagingStartParametersSetter, RequestedCountSetter, TotalResultsCountSetter, ResponseBuilder {
  private final List<T> resources;

  private Supplier<Response> responseSupplier;
  private int requestedCount;
  private int totalResultsCount;

  private ListResponseBuilder(List<T> resources) {
    this.resources = resources;
  }

  public static PagingStartParametersSetter forUsers(List<User> users) {
    return new ListResponseBuilder<>(users);
  }

  public static PagingStartParametersSetter forGroups(List<Group> groups) {
    return new ListResponseBuilder<>(groups);
  }

  @Override
  public RequestedCountSetter withPagingStartParameters(String startId, int startIndex) {
    if(isNullOrEmpty(startId)) {
      setStartIndexResponseSupplier(startIndex);
    } else {
      setStartIdResponseSupplier(startId);
    }

    return this;
  }

  @Override
  public TotalResultsCountSetter withRequestedCount(int count) {
    this.requestedCount = count;
    return this;
  }

  @Override
  public ResponseBuilder withTotalResultsCount(int count) {
    this.totalResultsCount = count;
    return this;
  }

  @Override
  public Response build() {
    return responseSupplier.get();
  }

  private void setStartIdResponseSupplier(String startId) {
    this.responseSupplier = () -> {
      if (resources.size() <= requestedCount) {
        return Response.ok(new PagedByIdentitySearchResult<>(resources, totalResultsCount, requestedCount, startId, PAGINATION_BY_ID_END_PARAM))
            .build();
      }

      int indexOfLastResource = resources.size() - 1;
      Resource<?> nextResource = resources.remove(indexOfLastResource);

      return Response.ok(new PagedByIdentitySearchResult<>(resources, totalResultsCount, requestedCount, startId, nextResource.getId())).build();

    };
  }

  private void setStartIndexResponseSupplier(int startIndex) {
    this.responseSupplier = () ->
        Response.ok(new PagedByIndexSearchResult<>(resources, totalResultsCount, requestedCount, startIndex)).build();
  }
}

interface PagingStartParametersSetter {
  RequestedCountSetter withPagingStartParameters(String startId, int startIndex);
}

interface RequestedCountSetter {
  TotalResultsCountSetter withRequestedCount(int count);
}

interface TotalResultsCountSetter {
  ResponseBuilder withTotalResultsCount(int count);
}

interface ResponseBuilder {
  Response build();
}
