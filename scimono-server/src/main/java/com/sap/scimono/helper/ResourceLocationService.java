
package com.sap.scimono.helper;

import com.sap.scimono.api.API;
import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.entity.EnterpriseExtension;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.GroupRef;
import com.sap.scimono.entity.Manager;
import com.sap.scimono.entity.MemberRef;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.Resource.Builder;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.paging.PagedResult;
import com.sap.scimono.exception.InternalScimonoException;

import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourceLocationService {
  private final UriInfo uriInfo;
  private final URI redirectApiRoot;
  private final String endpoint;


  public ResourceLocationService(UriInfo uriInfo, SCIMConfigurationCallback scimConfigurationAPI, String endpoint) {
    this.uriInfo = uriInfo;
    this.redirectApiRoot = scimConfigurationAPI.getRedirectApiRoot();
    this.endpoint = endpoint;
  }

  public URI getLocation(String path) {
    return rootUriBuilder().path(endpoint).path(encodePath(path)).build();
  }

  public URI getLocation() {
    return rootUriBuilder().path(endpoint).build();
  }

  public <T> T addLocation(final Resource<T> resource, final URI resourceLocation) {
    return addMetaLocation(resource, resourceLocation.toString());
  }

  public <T> T addLocation(final Resource<T> resource) {
    return addLocation(resource, rootUriBuilder().path(endpoint).build());
  }

  public <T extends Resource<T>> void addLocation(final PagedResult<T> pagedResources) {
    List<T> resources = pagedResources.getResources();
    for (int i = 0; i < resources.size(); ++i) {
      T resource = resources.get(i);
      resource = addLocation(resource, resource.getId());
      resources.set(i, resource);
    }
  }

  public <T> T addLocation(final Resource<T> resource, final String path) {
    return addLocation(resource, rootUriBuilder().path(endpoint).path(encodePath(path)).build());
  }


  private static <T> T addMetaLocation(final Resource<T> resource, final String resourceLocation) {
    Meta.Builder locationMeta = new Meta.Builder(resource.getMeta());
    locationMeta.setLocation(resourceLocation);

    Builder<T> resourceWithLocation = resource.builder();
    resourceWithLocation.setMeta(locationMeta.build());

    return resourceWithLocation.build();
  }

  public Group addMembersLocation(Group group) {
    // @formatter:off
    Set<MemberRef> memberRefsWithLocation = group.getMembers().stream()
        .map(memberRef -> {
          MemberRef.Type memberType = memberRef.getType();

          if (memberType == null) {
            return memberRef;
          }

          String resourceEnpoint = "";
          if(MemberRef.Type.USER.equals(memberType)){
            resourceEnpoint = API.USERS;
          } else if(MemberRef.Type.GROUP.equals(memberType)){
            resourceEnpoint = API.GROUPS;
          }

          return new MemberRef.Builder(memberRef)
            .setReference(rootUriBuilder()
                .path(resourceEnpoint)
                .path(encodePath(memberRef.getValue()))
                .build()
                .toString()
            ).build();})
        .collect(Collectors.toSet());
    // @formatter:on

    return new Group.Builder(group).setMembers(memberRefsWithLocation).build();
  }

  public User addRelationalEntitiesLocation(User user) {
    User.Builder userBuilder = new User.Builder(user);
    addAssignedGroupsLocation(userBuilder, user.getGroups());

    if (user.isExtensionPresent(EnterpriseExtension.ENTERPRISE_URN)) {
      EnterpriseExtension enterpriseExtension = (EnterpriseExtension) user.getExtension(EnterpriseExtension.ENTERPRISE_URN);
      addManagerLocation(userBuilder, enterpriseExtension);
    }

    return userBuilder.build();
  }

  private UriBuilder rootUriBuilder() {
    return redirectApiRoot == null ? uriInfo.getBaseUriBuilder() : UriBuilder.fromUri(redirectApiRoot);
  }

  private void addAssignedGroupsLocation(User.Builder userBuilder, List<GroupRef> groupRefs) {
    // @formatter:off
    List<GroupRef> groupRefsWithLocation = groupRefs.stream().map(groupRef -> new GroupRef.Builder(groupRef)
        .setReference(rootUriBuilder().path(API.GROUPS).path(encodePath(groupRef.getValue())).build().toString()).build())
        .collect(Collectors.toList());
    // @formatter:on

    userBuilder.setGroups(groupRefsWithLocation);
  }

  private void addManagerLocation(User.Builder userBuilder, EnterpriseExtension enterpriseExtension) {
    Manager manager = enterpriseExtension.getManager();
    if (manager == null) {
      return;
    }

    // @formatter:off
    Manager managerWithLocation = new Manager.Builder(manager)
        .setReference(rootUriBuilder().path(API.USERS).path(encodePath(manager.getValue())).toString()).build();
    // @formatter:on

    userBuilder.removeExtension(EnterpriseExtension.ENTERPRISE_URN);
    userBuilder.addExtension(new EnterpriseExtension.Builder(enterpriseExtension).setManager(managerWithLocation).build());
  }

  private String encodePath(String path) {
    try {
      return URLEncoder.encode(path, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      throw new InternalScimonoException("Unrecognized encoding", e);
    }
  }
}
