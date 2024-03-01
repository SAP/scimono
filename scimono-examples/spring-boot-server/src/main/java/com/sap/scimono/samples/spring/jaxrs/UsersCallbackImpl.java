package com.sap.scimono.samples.spring.jaxrs;

import com.sap.scimono.callback.users.UsersCallback;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.paging.PageInfo;
import com.sap.scimono.entity.paging.PagedResult;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.exception.ResourceNotFoundException;
import com.sap.scimono.exception.SCIMException;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UsersCallbackImpl implements UsersCallback {
  private static final Map<String, User> USERS_CONTAINER = new HashMap<>();

  @Override
  public User getUserByUsername(String s) {
    return null;
  }

  @Override
  public User getUser(String id) {
    User user = USERS_CONTAINER.get(id);
    if (user == null) {
      throw new ResourceNotFoundException(User.RESOURCE_TYPE_USER, id);
    }

    return user;
  }

  @Override
  public PagedResult<User> getUsers(PageInfo pageInfo, String filter) {
    Collection<User> allUsers = USERS_CONTAINER.values();
    return new PagedResult<>(allUsers.size(), new ArrayList<>(allUsers));
  }

  @Override
  public User createUser(User user) {
    if (USERS_CONTAINER.containsKey(user.getId())) {
      throw new SCIMException(SCIMException.Type.UNIQUENESS, "User with id: " + user.getId() + " already exists!", Response.Status.CONFLICT);
    }

    USERS_CONTAINER.put(user.getId(), user);
    return user;
  }

  @Override
  public User updateUser(User user) {
    if (USERS_CONTAINER.get(user.getId()) == null) {
      throw new ResourceNotFoundException(User.RESOURCE_TYPE_USER, user.getId());
    }

    USERS_CONTAINER.put(user.getId(), user);
    return user;
  }

  @Override
  public void patchUser(String s, PatchBody patchBody, Meta meta) {
    // Not implemented
  }

  @Override
  public void deleteUser(String id) {
    USERS_CONTAINER.remove(id);
  }

  @Override
  public Optional<String> generateId() {
    return Optional.of(UUID.randomUUID().toString());
  }
}
