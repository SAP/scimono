
package com.sap.scim;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.scim.api.Groups;
import com.sap.scim.api.ResourceTypes;
import com.sap.scim.api.Schemas;
import com.sap.scim.api.ServiceProviderConfiguration;
import com.sap.scim.api.ServletRequestProviderFilter;
import com.sap.scim.api.Users;
import com.sap.scim.api.helper.InternalExceptionMapper;
import com.sap.scim.api.helper.InvalidInputExceptionMapper;
import com.sap.scim.api.helper.JacksonResolver;
import com.sap.scim.api.helper.JsonProcessingExceptionMapper;
import com.sap.scim.api.helper.ScimExceptionMapper;
import com.sap.scim.api.helper.ValidationExceptionMapper;
import com.sap.scim.callback.config.DefaultSCIMConfigurationCallback;
import com.sap.scim.callback.config.SCIMConfigurationCallback;
import com.sap.scim.callback.groups.DefaultGroupsCallback;
import com.sap.scim.callback.groups.GroupsCallback;
import com.sap.scim.callback.resourcetype.DefaultResourceTypesCallback;
import com.sap.scim.callback.resourcetype.ResourceTypesCallback;
import com.sap.scim.callback.schemas.DefaultSchemasCallback;
import com.sap.scim.callback.schemas.SchemasCallback;
import com.sap.scim.callback.users.DefaultUsersCallback;
import com.sap.scim.callback.users.UsersCallback;
import com.sap.scim.entity.schema.validation.CustomInputValidator;

public abstract class SCIMApplication extends Application {
  private static final Logger logger = LoggerFactory.getLogger(SCIMApplication.class);

  private static final String SCIM_IMPLEMENTATION_INSTANCE = "SCIM_IMPLEMENTATION_INSTANCE";

  private Map<String, Object> props = new HashMap<>();

  @Override
  public Map<String, Object> getProperties() {
    props.putAll(super.getProperties());
    props.put(SCIM_IMPLEMENTATION_INSTANCE, this);

    return props;
  }

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> scimResources = new HashSet<>();

    // public API
    scimResources.add(Users.class);
    scimResources.add(Groups.class);
    scimResources.add(Schemas.class);
    scimResources.add(ResourceTypes.class);
    scimResources.add(ServiceProviderConfiguration.class);

    scimResources.addAll(getAdditionalResourceProviders());

    // internal providers
    scimResources.add(InternalExceptionMapper.class);
    scimResources.add(InvalidInputExceptionMapper.class);
    scimResources.add(JacksonResolver.class);
    scimResources.add(JsonProcessingExceptionMapper.class);
    scimResources.add(ScimExceptionMapper.class);

    scimResources.add(ServletRequestProviderFilter.class);

    logger.info("Initializing SCIM resource providers {}", scimResources);
    return scimResources;
  }

  @Override
  public Set<Object> getSingletons() {
    return Collections.singleton(new ValidationExceptionMapper());
  }

  /**
   * Returns the specific application implementation instance.
   *
   * @param app
   * @return
   */
  public static SCIMApplication from(final Application app) {
    return (SCIMApplication) app.getProperties().get(SCIM_IMPLEMENTATION_INSTANCE);
  }

  /**
   * @return a request-scoped UsersCallback implementation.
   */
  public UsersCallback getUsersCallback() {
    return new DefaultUsersCallback();
  }

  /**
   * @return a request-scoped GroupsCallback implementation.
   */
  public GroupsCallback getGroupsCallback() {
    return new DefaultGroupsCallback();
  }

  /**
   * @return a request-scoped SchemasCallback implementation.
   */
  public SchemasCallback getSchemasCallback() {
    return new DefaultSchemasCallback();
  }

  /**
   * @return a request-scoped ResourceTypesCallback implementation.
   */
  public ResourceTypesCallback getResourceTypesCallback() {
    return new DefaultResourceTypesCallback();
  }

  /**
   * @return a request-scoped SCIMConfigurationCallback implementation.
   */
  public SCIMConfigurationCallback getConfigurationCallback() {
    return new DefaultSCIMConfigurationCallback();
  }

  /**
   * Return a set of classes that should be exposed as resource providers (e.g. /Users)
   *
   * @return
   */
  public Set<Class<?>> getAdditionalResourceProviders() {
    return Collections.emptySet();
  }

  public Map<CustomInputValidator.Type, CustomInputValidator> getCustomConstraintValidators() {
    return Collections.emptyMap();
  }
}
