
package com.sap.scimono.callback.groups;

import java.util.Optional;

import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.paging.PageInfo;
import com.sap.scimono.entity.paging.PagedResult;
import com.sap.scimono.entity.patch.PatchBody;

public interface GroupsCallback {
  /**
   * @param groupId, unique user id
   * @return the group with the specified groupId or null if no such group exists
   */
  Group getGroup(final String groupId);

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
  void updateGroup(final Group group);

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
   * Generates a group id for a new user
   *
   * @return a unique group idnetifier
   */
  Optional<String> generateId();
}
