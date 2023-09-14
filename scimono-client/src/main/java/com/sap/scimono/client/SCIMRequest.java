package com.sap.scimono.client;

import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.client.ResourceAction.CREATE_BULK;
import static com.sap.scimono.client.ResourceAction.CREATE_SINGLE;
import static com.sap.scimono.client.ResourceAction.DELETE;
import static com.sap.scimono.client.ResourceAction.GET_ALL;
import static com.sap.scimono.client.ResourceAction.GET_SINGLE;
import static com.sap.scimono.client.ResourceAction.PATCH_UPDATE;
import static com.sap.scimono.client.ResourceAction.PUT_UPDATE;
import static java.util.Collections.singleton;
import static jakarta.ws.rs.HttpMethod.PATCH;
import static jakarta.ws.rs.client.Entity.entity;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static jakarta.ws.rs.core.Response.Status.OK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

public class SCIMRequest {
  private final MultivaluedMap<String, Object> httpHeaders;
  private final Map<ResourceAction, ActionResponseStatusConfig> scimActionsResponseStatusConfig;

  private SCIMRequest(Builder builder) {
    this.httpHeaders = builder.httpHeaders;
    this.scimActionsResponseStatusConfig = builder.scimActionsResponseStatusConfig;
  }

  Response get(WebTarget webTarget) {
    return configureRequest(webTarget).get();
  }

  Response put(WebTarget webTarget, Object body) {
    return configureRequest(webTarget).put(entity(body, getDefaultContentType()));
  }

  Response post(WebTarget webTarget, Object body) {
    return configureRequest(webTarget).post(entity(body, getDefaultContentType()));
  }

  Response patch(WebTarget webTarget, Object body) {
    return configureRequest(webTarget).method(PATCH, entity(body, getDefaultContentType()));
  }

  Response delete(WebTarget webTarget) {
    return configureRequest(webTarget).delete();
  }

  ActionResponseStatusConfig getScimActionResponseStatusConfig(ResourceAction resourceAction) {
    return scimActionsResponseStatusConfig.get(resourceAction);
  }

  private Invocation.Builder configureRequest(WebTarget webTarget) {
    return webTarget.request().headers(copyHttpHeaders());
  }

  private String getDefaultContentType() {
    MultivaluedMap<String, Object> copyOfHttpHeaders = copyHttpHeaders();
    List<Object> contentTypes = copyOfHttpHeaders.get(CONTENT_TYPE);

    return contentTypes == null || contentTypes.isEmpty() ? APPLICATION_JSON_SCIM : contentTypes.get(contentTypes.size() - 1).toString();
  }

  private MultivaluedMap<String, Object> copyHttpHeaders() {
    MultivaluedMap<String, Object> result = new MultivaluedHashMap<>();
    httpHeaders.forEach((s, objects) -> result.put(s, new LinkedList<>(objects)));
    return result;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private final MultivaluedMap<String, Object> httpHeaders = defaultHeaders();
    private final Map<ResourceAction, ActionResponseStatusConfig> scimActionsResponseStatusConfig = defaultSCIMActionsResponseStatusConfig();

    private Builder() {
    }

    public Builder addHeader(String name, Object...values) {
      httpHeaders.addAll(name, values);
      return this;
    }

    public Builder setExpectedResponseStatuses(ResourceAction resourceAction, Response.Status...responseStatuses) {
      scimActionsResponseStatusConfig.put(resourceAction, new ActionResponseStatusConfig(new HashSet<>(Arrays.asList(responseStatuses))));
      return this;
    }


    public SCIMRequest build() {
      return new SCIMRequest(this);
    }

    private Map<ResourceAction, ActionResponseStatusConfig> defaultSCIMActionsResponseStatusConfig() {
      Map<ResourceAction, ActionResponseStatusConfig> actionsResponseStatusConfig = new EnumMap<>(ResourceAction.class);

      actionsResponseStatusConfig.put(GET_SINGLE, new ActionResponseStatusConfig(new ArrayList<>(singleton(OK))));
      actionsResponseStatusConfig.put(GET_ALL, new ActionResponseStatusConfig(new ArrayList<>(singleton(OK))));
      actionsResponseStatusConfig.put(CREATE_SINGLE, new ActionResponseStatusConfig(new ArrayList<>(singleton(CREATED))));
      actionsResponseStatusConfig.put(PUT_UPDATE, new ActionResponseStatusConfig(new ArrayList<>(singleton(OK))));
      actionsResponseStatusConfig.put(PATCH_UPDATE, new ActionResponseStatusConfig(new ArrayList<>(singleton(NO_CONTENT))));
      actionsResponseStatusConfig.put(DELETE, new ActionResponseStatusConfig(new ArrayList<>(singleton(NO_CONTENT))));
      actionsResponseStatusConfig.put(CREATE_BULK, new ActionResponseStatusConfig(new ArrayList<>(singleton(OK))));

      return actionsResponseStatusConfig;
    }

    private static MultivaluedMap<String, Object> defaultHeaders() {
      MultivaluedMap<String, Object> defaultHeaders = new MultivaluedHashMap<>();
      defaultHeaders.add(CONTENT_TYPE, APPLICATION_JSON_SCIM);
      return defaultHeaders;
    }
  }
}
