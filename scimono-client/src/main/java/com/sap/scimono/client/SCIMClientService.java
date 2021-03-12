package com.sap.scimono.client;

import static com.sap.scimono.client.RequiredAttribute.USER_USER_NAME;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ContextResolver;

import com.sap.scimono.client.authentication.OauthClientCredentialsAuthenticator;
import com.sap.scimono.client.authentication.TargetSystemAuthenticator;

public class SCIMClientService {
  private Client client;
  private URI serviceUrl;
  
  private WebTarget providerRoot;

  private SCIMClientService(Client client, URI serviceUrl) {
    this.client = client;
    this.serviceUrl = serviceUrl;
    this.providerRoot = client.target(serviceUrl);
  }
  
  private SCIMClientService(WebTarget providerRoot) {
    this.providerRoot = providerRoot;
  }

  public UserRequest buildUserRequest() {
    return buildUserRequest(SCIMRequest.newBuilder());
  }

  public UserRequest buildUserRequest(SCIMRequest.Builder requestBuilder) {
    SCIMRequest request = requestBuilder.build();
    if (client == null || serviceUrl == null) {
      return new UserRequest(providerRoot, request);
    }
    
    if (!request.getRequiredAttributesToBeOptional().contains(USER_USER_NAME)) {
      return new UserRequest(providerRoot, request);
    }
    
    Client newClient = ClientBuilder.newClient(client.getConfiguration());
    newClient.register(new ClientJacksonResolver(true));
    return new UserRequest(newClient.target(serviceUrl), request);
  }

  public GroupRequest buildGroupRequest() {
    return buildGroupRequest(SCIMRequest.newBuilder());
  }

  public GroupRequest buildGroupRequest(SCIMRequest.Builder requestBuilder) {
    return new GroupRequest(providerRoot, requestBuilder.build());
  }

  public SchemaRequest buildSchemaRequest() {
    return buildSchemaRequest(SCIMRequest.newBuilder());
  }

  public SchemaRequest buildSchemaRequest(SCIMRequest.Builder requestBuilder) {
    return new SchemaRequest(providerRoot, requestBuilder.build());
  }

  public ResourceTypeRequest buildResourceTypeRequest() {
    return buildResourceTypeRequest(SCIMRequest.newBuilder());
  }

  public ResourceTypeRequest buildResourceTypeRequest(SCIMRequest.Builder requestBuilder) {
    return new ResourceTypeRequest(providerRoot, requestBuilder.build());
  }

  public ServiceProviderConfigurationRequest buildServiceProviderConfigurationRequest() {
    return buildServiceProviderConfigurationRequest(SCIMRequest.newBuilder());
  }

  public ServiceProviderConfigurationRequest buildServiceProviderConfigurationRequest(SCIMRequest.Builder requestBuilder) {
    return new ServiceProviderConfigurationRequest(providerRoot, requestBuilder.build());
  }

  public static SCIMClientService fromPreconfiguredWebTarget(WebTarget webTarget) {
    return new SCIMClientService(webTarget);
  }

  public static Builder builder(String serviceUrl) {
    return new Builder(UriBuilder.fromPath(serviceUrl).build());
  }

  public static class Builder {
    private final List<Object> resolvers = getDefaultResolvers();
    private final Map<String, Object> properties = new HashMap<>();

    private TargetSystemAuthenticator.Builder<?> targetSystemAuthenticator;
    private URI serviceUrl;

    private Builder(URI serviceUrl) {
      this.serviceUrl = serviceUrl;
    }
    
    public Builder addAuthenticator(TargetSystemAuthenticator.Builder<?> targetSystemAuthenticator) {
      this.targetSystemAuthenticator = targetSystemAuthenticator;
      return this;
    }

    public Builder addResolver(Object resolver) {
      resolvers.add(resolver);
      return this;
    }

    public Builder addProperty(String name, Object value) {
      properties.put(name, value);
      return this;
    }

    private List<Object> getDefaultResolvers() {
      return new ArrayList<>(Collections.singletonList(new ClientJacksonResolver(false)));
    }
    
    public SCIMClientService build() {
      Client client = ClientBuilder.newClient();
      if (targetSystemAuthenticator != null) {
        configureTargetSystemAuthenticator(client);

        client.register(targetSystemAuthenticator.build());
      }

      registerResolvers(client);
      properties.forEach(client::property);
      return new SCIMClientService(client, serviceUrl);
    }

    private void configureTargetSystemAuthenticator(Client client) {
      if (targetSystemAuthenticator instanceof OauthClientCredentialsAuthenticator.Builder) {
        OauthClientCredentialsAuthenticator.Builder authBuilder = (OauthClientCredentialsAuthenticator.Builder) targetSystemAuthenticator;
        if (authBuilder.getHttpClient() == null) {
          authBuilder.setHttpClient(client);
        }
      }
    }

    private void registerResolvers(Client client) {
      registerLastContextResolver(client);
      resolvers.removeIf(ContextResolver.class::isInstance);
      resolvers.forEach(client::register);
    }

    private void registerLastContextResolver(Client client) {
      ListIterator<Object> resolversReverseIterator = resolvers.listIterator(resolvers.size());
      while (resolversReverseIterator.hasPrevious()) {
        Object resolver = resolversReverseIterator.previous();
        if (resolver instanceof ContextResolver) {
          client.register(resolver);
          return;
        }
      }
    }
  }
}
