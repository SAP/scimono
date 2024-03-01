
package com.sap.scimono.callback.users;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.sap.scimono.api.request.RequestedResourceAttributes;
import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.entity.EnterpriseExtension;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.paging.PageInfo;
import com.sap.scimono.entity.paging.PagedResult;
import com.sap.scimono.entity.patch.PatchBody;

public interface UsersCallback {
  /**
   * @param userName, unique username
   * @return the user with the specified username or null if no such user exists
   */
  User getUserByUsername(final String userName);

  /**
   * @param userId, unique user id
   * @return the user with the specified userId or null if no such user exists
   */
  User getUser(final String userId);

  /**
   * @param additionalAttributes additional attributes to be returned of excluded from the response
   * @return the user with the specified userId or null if no such user exists
   */
  default User getUser(String userId, RequestedResourceAttributes additionalAttributes) {
    return getUser(userId);
  }

  /**
   * @param additionalAttributes additional attributes to be returned of excluded from the response
   * @param filter               value of the filter query parameter
   * @return the user with the specified userId or null if no such user exists
   */
  default User getUser(String userId, RequestedResourceAttributes additionalAttributes, final String filter) {
    return getUser(userId, additionalAttributes);
  }

  /**
   * Returns a page of users (limited by {@link SCIMConfigurationCallback#getMaxResourcesPerPage()}),
   * taking into account the specified filter and paging parameters.
   *
   * @param pageInfo parsed paging parameters from the API request
   * @param filter   unparsed filter string from the $filter query parameter
   * @return a page of users or empty page if no users match the filter/paging criteria
   */
  PagedResult<User> getUsers(final PageInfo pageInfo, final String filter);

  /**
   * Returns a page of users (more info in {@link UsersCallback#getUsers(PageInfo, String)} ()}
   * adding specifying additional attributes to be returned of excluded from the response
   * @param additionalAttributes additional attributes to be returned of excluded from the response
   * @return a page of users or empty page if no users match the filter/paging criteria
   */
  default PagedResult<User> getUsers(final PageInfo pageInfo, final String filter, RequestedResourceAttributes additionalAttributes) {
    return getUsers(pageInfo, filter);
  }

  /**
   * Creates a user with the provided attributes. The user object must have all mandatory attributes available,
   * including metadata (version, etc.). The returned user must have its id set.
   *
   * @param user
   */
  User createUser(final User user);

  /**
   * +
   * Updates a user with the provided attributes. The user object must have all mandatory attributes available,
   * including metadata (id, new version, etc.).
   *
   * @param user
   */
  User updateUser(final User user);

  /**
   * Updates a user with the provided attributes. The user object must have all mandatory attributes available,
   * including metadata (id, new version, etc.).
   *
   * @param userId
   * @param patchBody
   * @param userMeta
   */
  void patchUser(String userId, PatchBody patchBody, Meta userMeta);

  /**
   * Deletes the user with the specified userId.
   *
   * @param userId
   */
  void deleteUser(final String userId);

  /**
   * Generates a user id for a new user
   *
   * @return a unique user identifier
   */
  Optional<String> generateId();

  /**
   * @return set of schemas ids of the attributes that can be modified with PATCH request
   */
  default Set<String> getSchemaIdsAllowingPatch() {
    return new HashSet<>(Arrays.asList(User.SCHEMA, EnterpriseExtension.ENTERPRISE_URN));
  }
}
