package com.sap.scimono.entity.bulk;

import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.helper.ResourceLocationService;

import java.util.List;
import java.util.stream.Collectors;

public class BulkResponseOperationLocationService {

  private final ResourceLocationService usersLocationService;
  private final ResourceLocationService groupsLocationService;

  public BulkResponseOperationLocationService(ResourceLocationService usersLocationService, ResourceLocationService groupsLocationService) {
    this.usersLocationService = usersLocationService;
    this.groupsLocationService = groupsLocationService;
  }

  public BulkBody<ResponseOperation> rebuildWithLocations(BulkBody<ResponseOperation> bulkResponse) {

    List<ResponseOperation> responseOperations = bulkResponse.getOperations().stream().map(respOperation -> {
      ResponseOperation.Builder builder = respOperation.builder();

      builder.withLocation(getValidResponseLocation(respOperation, usersLocationService, groupsLocationService));
      return builder.build();
    }).collect(Collectors.toList());

    return BulkBody.forResponse(responseOperations);
  }

  private String getValidResponseLocation(ResponseOperation respOperation,
      ResourceLocationService usersLocationService, ResourceLocationService groupsLocationService) {
    if (respOperation.getMethod() == RequestMethod.POST && !respOperation.isSuccessful()) {
      return null;
    }

    String location = respOperation.getLocation();
    String resourceType = respOperation.getResourceType();

    if (location == null && User.RESOURCE_TYPE_USER.equalsIgnoreCase(resourceType)) {
      location = usersLocationService.getLocation(respOperation.getResourceId()).toString();
    }

    if (location == null && Group.RESOURCE_TYPE_GROUP.equalsIgnoreCase(resourceType)) {
      location = groupsLocationService.getLocation(respOperation.getResourceId()).toString();
    }

    return location;
  }
}
