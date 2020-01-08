
package com.sap.scimono.helper;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.sap.scimono.api.API;
import com.sap.scimono.entity.*;
import com.sap.scimono.entity.Resource.Builder;
import com.sap.scimono.entity.paging.PagedResult;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Resources {

  private Resources() {
  }

  public static <T> T addLocation(final Resource<T> resource, final URI resourceLocation) {
    return addLocation(resource, resourceLocation.toString());
  }

  public static <T extends Resource<T>> void addLocation(final PagedResult<T> pagedResources, final UriInfo uriInfo) {
    List<T> resources = pagedResources.getResources();
    for (int i = 0; i < resources.size(); ++i) {
      T resource = resources.get(i);
      resource = addLocation(resource, uriInfo.getAbsolutePathBuilder().path(resource.getId()));
      resources.set(i, resource);
    }
  }

  public static <T> T addLocation(final Resource<T> resource, final UriBuilder resourceLocationBuilder) {
    return addLocation(resource, resourceLocationBuilder.build());
  }

  private static <T> T addLocation(final Resource<T> resource, final String resourceLocation) {
    Meta.Builder locationMeta = new Meta.Builder(resource.getMeta());
    locationMeta.setLocation(resourceLocation);

    Builder<T> resourceWithLocation = resource.builder();
    resourceWithLocation.setMeta(locationMeta.build());

    return resourceWithLocation.build();
  }

  public static Group addMembersLocation(Group group, UriInfo uriInfo) {
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
            .setReference(uriInfo.getBaseUriBuilder()
                .path(resourceEnpoint)
                .path(memberRef.getValue())
                .build()
                .toString()
            ).build();})
        .collect(Collectors.toSet());
    // @formatter:on

    return new Group.Builder(group).setMembers(memberRefsWithLocation).build();
  }

  public static User addRelationalEntitiesLocation(User user, UriInfo uriInfo) {
    User.Builder userBuilder = new User.Builder(user);

    addAssignedGroupsLocation(userBuilder, user.getGroups(), uriInfo);

    if (user.isExtensionPresent(EnterpriseExtension.ENTERPRISE_URN)) {
      EnterpriseExtension enterpriseExtension = (EnterpriseExtension) user.getExtension(EnterpriseExtension.ENTERPRISE_URN);
      addManagerLocation(userBuilder, enterpriseExtension, uriInfo);
    }

    return userBuilder.build();
  }

  private static void addAssignedGroupsLocation(User.Builder userBuilder, List<GroupRef> groupRefs, UriInfo uriInfo) {
    // @formatter:off
    List<GroupRef> groupRefsWithLocation = groupRefs.stream().map(groupRef -> new GroupRef.Builder(groupRef)
        .setReference(uriInfo.getBaseUriBuilder().path(API.GROUPS).path(groupRef.getValue()).build().toString()).build())
        .collect(Collectors.toList());
    // @formatter:on

    userBuilder.setGroups(groupRefsWithLocation);
  }

  private static void addManagerLocation(User.Builder userBuilder, EnterpriseExtension enterpriseExtension, UriInfo uriInfo) {
    Manager manager = enterpriseExtension.getManager();
    if (manager == null) {
      return;
    }

    // @formatter:off
    Manager managerWithLocation = new Manager.Builder(manager)
        .setReference(uriInfo.getBaseUriBuilder().path(API.USERS).path(manager.getValue()).toString()).build();
    // @formatter:on

    userBuilder.removeExtension(EnterpriseExtension.ENTERPRISE_URN);
    userBuilder.addExtension(new EnterpriseExtension.Builder(enterpriseExtension).setManager(managerWithLocation).build());
  }
}
