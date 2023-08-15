
package com.sap.scimono.callback.groups;

import java.util.Optional;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.paging.PageInfo;
import com.sap.scimono.entity.paging.PagedResult;
import com.sap.scimono.entity.patch.PatchBody;

public class DefaultGroupsCallback implements GroupsCallback {


  @Override
  public Group getGroup(String groupId) {
    throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
  }

  @Override
  public PagedResult<Group> getGroups(PageInfo pageInfo, String filter) {
    throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
  }

  @Override
  public Group createGroup(Group group) {
    throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
  }

  @Override
  public Group updateGroup(Group group) {
    throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
  }

  @Override
  public void patchGroup(String groupId, PatchBody patchBody, Meta groupMeta) {
    throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
  }

  @Override
  public void deleteGroup(String groupId) {
    throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
  }

  @Override
  public Optional<String> generateId() {
    return Optional.empty();
  }
}
