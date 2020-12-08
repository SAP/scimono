
package com.sap.scimono.callback.groups;

import java.util.Optional;

import com.sap.scimono.api.request.RequestedResourceAttributes;
import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.paging.PageInfo;
import com.sap.scimono.entity.paging.PagedResult;
import com.sap.scimono.entity.patch.PatchBody;

public interface GroupsCallback {
  /**
   * @param groupId, unique group id
   * @return the group with the specified groupId or null if no such group exists
   */
  Group getGroup(final String groupId);

  default Group getGroup(String groupId, RequestedResourceAttributes additionalAttributes) {
    return getGroup(groupId);
  }

  /**
   * Returns a page of groups (limited by {@link SCIMConfigurationCallback#getMaxResourcesPerPage()}),
   * taking into account the specified filter and paging parameters.
   *
   * @param pageInfo parsed paging parameters from the API request
   * @param filter   unparsed filter string from the $filter query parameter
   * @return a page of groups or empty page if no groups match the filter/paging criteria
   */
  PagedResult<Group> getGroups(final PageInfo pageInfo, final String filter);

  /**
   * Returns a page of groups (more info in {@link GroupsCallback#getGroups(PageInfo, String)} ()}
   * adding specifying additional attributes to be returned of excluded from the response
   * @param additionalAttributes additional attributes to be returned of excluded from the response
   * @return a page of groups or empty page if no groups match the filter/paging criteria
   */
  default PagedResult<Group> getGroups(final PageInfo pageInfo, final String filter, RequestedResourceAttributes additionalAttributes) {
    return getGroups(pageInfo, filter);
  }


  /**
   * Creates a group with the provided attributes. The group object must have all mandatory attributes available,
   * including metadata (version, etc.). The returned group must have its id set.
   *
   * @param group
   */
  Group createGroup(final Group group);

  /**
   * Updates a group with the provided attributes. The group object must have all mandatory attributes available,
   * including metadata (id, new version, etc.).
   *
   * @param group
   */
  Group updateGroup(final Group group);

  /**
   * Updates a group with the provided attributes. The group object must have all mandatory attributes available,
   * including metadata (id, new version, etc.).
   *
   * @param groupId
   * @param patchBody
   * @param groupMeta
   */
  void patchGroup(String groupId, PatchBody patchBody, Meta groupMeta);

  /**
   * Deletes the group with the specified groupId.
   *
   * @param groupId
   */
  void deleteGroup(final String groupId);

  /**
   * Generates a group id for a new group
   *
   * @return a unique group idnetifier
   */
  Optional<String> generateId();
}
