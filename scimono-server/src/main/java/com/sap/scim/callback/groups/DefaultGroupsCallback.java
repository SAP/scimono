
package com.sap.scim.callback.groups;

import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.sap.scim.entity.Group;
import com.sap.scim.entity.Meta;
import com.sap.scim.entity.paging.PageInfo;
import com.sap.scim.entity.paging.PagedResult;
import com.sap.scim.entity.patch.PatchBody;

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
  public void updateGroup(Group group) {
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
