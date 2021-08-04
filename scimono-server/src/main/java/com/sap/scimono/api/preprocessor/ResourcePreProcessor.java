package com.sap.scimono.api.preprocessor;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import com.sap.scimono.callback.groups.GroupsCallback;
import com.sap.scimono.callback.resourcetype.ResourceTypesCallback;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.callback.users.UsersCallback;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.validation.ResourceCustomAttributesValidator;
import com.sap.scimono.helper.ReadOnlyAttributesEraser;
import com.sap.scimono.helper.ResourceLocationService;
import com.sap.scimono.helper.UnnecessarySchemasEraser;

public class ResourcePreProcessor<T extends Resource<T>> {
  private final ResourceLocationService resourceLocationService;
  private final Supplier<Optional<String>> resourceIdSupplier;
  private final ResourceTypesCallback resourceTypesAPI;
  private final SchemasCallback schemaAPI;
  private final String resourceType;
  private final String resourceSchema;

  private ResourcePreProcessor(ResourceLocationService resourceLocationService,
                               Supplier<Optional<String>> resourceIdSupplier,
                               ResourceTypesCallback resourceTypesAPI,
                               SchemasCallback schemaAPI,
                               String resourceType,
                               String resourceSchema) {
    this.resourceLocationService = resourceLocationService;
    this.resourceIdSupplier = resourceIdSupplier;
    this.resourceTypesAPI = resourceTypesAPI;
    this.schemaAPI = schemaAPI;
    this.resourceType = resourceType;
    this.resourceSchema = resourceSchema;
  }

  public T prepareForUpdate(T resource, String resourceId) {
    ResourceCustomAttributesValidator<T> resourceCustomAttributesValidator = ResourceCustomAttributesValidator.forPut(schemaAPI, resourceTypesAPI);
    return prepare(resource, resourceId, resourceCustomAttributesValidator);
  }

  public T prepareForCreate(T resource) {
    ResourceCustomAttributesValidator<T> resourceCustomAttributesValidator = ResourceCustomAttributesValidator.forPost(schemaAPI, resourceTypesAPI);
    return prepare(resource ,resourceIdSupplier.get().orElse(null), resourceCustomAttributesValidator);
  }

  public static ResourcePreProcessor<User> forUsers(ResourceLocationService resourceLocationService,
                                                    UsersCallback usersAPI,
                                                    ResourceTypesCallback resourceTypesAPI,
                                                    SchemasCallback schemaAPI) {
    return new ResourcePreProcessor<>(resourceLocationService, usersAPI::generateId, resourceTypesAPI, schemaAPI, User.RESOURCE_TYPE_USER, User.SCHEMA);
  }

  public static ResourcePreProcessor<Group> forGroups(ResourceLocationService resourceLocationService,
                                                     GroupsCallback groupsAPI,
                                                     ResourceTypesCallback resourceTypesAPI,
                                                     SchemasCallback schemaAPI) {
    return new ResourcePreProcessor<>(resourceLocationService, groupsAPI::generateId, resourceTypesAPI, schemaAPI, Group.RESOURCE_TYPE_GROUP, Group.SCHEMA);
  }

  private T prepare(T newResource, String resourceId, ResourceCustomAttributesValidator<T> resourceCustomAttributesValidator) {
    ReadOnlyAttributesEraser<T> readOnlyAttributesEraser = new ReadOnlyAttributesEraser<>(schemaAPI);
    T resourceWithoutReadOnlyAttributes = readOnlyAttributesEraser.eraseAllFormCustomExtensions(newResource);

    UnnecessarySchemasEraser<T> unnecessarySchemasEraser = new UnnecessarySchemasEraser<>();
    T resource = unnecessarySchemasEraser.eraseAllUnnecessarySchemas(resourceWithoutReadOnlyAttributes, resourceSchema);

    String version = UUID.randomUUID().toString();
    Meta.Builder metaBuilder = new Meta.Builder().setVersion(version).setResourceType(resourceType);
    if (resourceId != null) {
      metaBuilder.setLocation(resourceLocationService.getLocation(resourceId).toString());
    }

    T resourceWIthMeta = resource.builder().setId(resourceId).setMeta(metaBuilder.build()).build();
    resourceCustomAttributesValidator.validate(resourceWIthMeta);

    return resourceWIthMeta;
  }
}
