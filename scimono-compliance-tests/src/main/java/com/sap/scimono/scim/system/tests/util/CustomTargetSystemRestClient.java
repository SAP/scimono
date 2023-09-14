package com.sap.scimono.scim.system.tests.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scimono.api.helper.JacksonResolver;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.schema.Schema;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.scim.system.tests.util.TestProperties.AUTH_TYPE;
import static com.sap.scimono.scim.system.tests.util.TestProperties.BASIC_AUTH_PASSWORD;
import static com.sap.scimono.scim.system.tests.util.TestProperties.BASIC_AUTH_USER;
import static com.sap.scimono.scim.system.tests.util.TestProperties.OAUTH_CLIENT_ID;
import static com.sap.scimono.scim.system.tests.util.TestProperties.OAUTH_GRANT;
import static com.sap.scimono.scim.system.tests.util.TestProperties.OAUTH_SECRET;
import static com.sap.scimono.scim.system.tests.util.TestProperties.OAUTH_SERVICE_URL;
import static com.sap.scimono.scim.system.tests.util.TestProperties.SERVICE_URL;

public class CustomTargetSystemRestClient {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CustomTargetSystemRestClient.class);

  private static final String OAUTH_GRANT_TYPE = "grant_type";
  private static final String CLIENT_CREDENTIALS_GRANT = "client_credentials";

  public static final CustomTargetSystemRestClient INSTANCE = createInstance();

  public static final GenericType<PagedByIdentitySearchResult<User>> USER_LIST_RESPONSE_TYPE_ID_PAGING = new GenericType<PagedByIdentitySearchResult<User>>() {};
  public static final GenericType<PagedByIdentitySearchResult<Group>> GROUP_LIST_RESPONSE_TYPE_ID_PAGING = new GenericType<PagedByIdentitySearchResult<Group>>() {};
  public static final GenericType<PagedByIndexSearchResult<User>> USER_LIST_RESPONSE_TYPE_INDEX_PAGING = new GenericType<PagedByIndexSearchResult<User>>() {};
  public static final GenericType<PagedByIndexSearchResult<Group>> GROUP_LIST_RESPONSE_TYPE_INDEX_PAGING = new GenericType<PagedByIndexSearchResult<Group>>() {};
  public static final GenericType<PagedByIndexSearchResult<Schema>> SCHEMA_LIST_RESPONSE_TYPE = new GenericType<PagedByIndexSearchResult<Schema>>() {};

  private final WebTarget endpoint;
  private final String authHeaderValue;

  public CustomTargetSystemRestClient(final String serviceUrl, final String authHeaderValue) {
    this.authHeaderValue = authHeaderValue;
    this.endpoint = configureRestClientEndpoint(serviceUrl);
  }

  // @formatter:off
  public Response postEntityHttpResponse(final String endpoint, final Object entity) {
    return this.endpoint.path(endpoint)
        .request()
        .header(HttpHeaders.AUTHORIZATION, authHeaderValue)
        .post(Entity.entity(entity, APPLICATION_JSON_SCIM));
  }

  public Response putEntityHttpResponse(final String endpoint, final String entitiyId, final Object entity) {
    return this.endpoint.path(endpoint)
        .path(entitiyId)
        .request()
        .header(HttpHeaders.AUTHORIZATION, authHeaderValue)
        .put(Entity.entity(entity, APPLICATION_JSON_SCIM));
  }

  public Response patchEntityHttpResponse(final String endpoint, final String entitiyId, final Object entity) {
    return this.endpoint.path(endpoint)
        .path(entitiyId)
        .request()
        .header(HttpHeaders.AUTHORIZATION, authHeaderValue)
        .method("PATCH", Entity.entity(entity, APPLICATION_JSON_SCIM));
  }

  public Response getEntityHttpResponse(final String endpoint, final String entityId) {
    return this.endpoint.path(endpoint)
        .path(entityId)
        .request()
        .header(HttpHeaders.AUTHORIZATION, authHeaderValue)
        .get();
  }

  public Response getEntitiesHttpResponse(final String endpoint) {
    return this.endpoint.path(endpoint)
        .request()
        .header(HttpHeaders.AUTHORIZATION, authHeaderValue)
        .get();
  }

  public Response getEntitiesHttpResponse(final String endpoint, Map<String, Object> queryParams) {
    WebTarget configuredEndpoint = this.endpoint.path(endpoint);
    for(Map.Entry<String, Object> queryParam : queryParams.entrySet()) {
      configuredEndpoint = configuredEndpoint.queryParam(queryParam.getKey(), queryParam.getValue());
    }

    return configuredEndpoint
        .request()
        .header(HttpHeaders.AUTHORIZATION, authHeaderValue)
        .get();
  }

  public Response deleteEntityHttpResponse(final String endpoint, final String entityId) {
    return this.endpoint.path(endpoint)
        .path(entityId)
        .request()
        .header(HttpHeaders.AUTHORIZATION, authHeaderValue)
        .delete();
  }

  private WebTarget configureRestClientEndpoint(final String serviceUrl) {
    return ClientBuilder.newClient(new ClientConfig()
        .register(new ClientJacksonResolver())
        .register(JacksonResolver.class)
        .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
        .property(org.glassfish.jersey.test.TestProperties.LOG_TRAFFIC, true)
        .property(org.glassfish.jersey.test.TestProperties.DUMP_ENTITY, true)
        .property(LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT, LoggingFeature.Verbosity.PAYLOAD_ANY)
        .property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_CLIENT, "WARNING"))
        .target(serviceUrl);
     // @formatter:on
  }

  private static boolean isBasicAuthEnabled() {
    return "Basic".equalsIgnoreCase(AUTH_TYPE);
  }

  private static CustomTargetSystemRestClient createInstance() {
    String authorizationHeader = isBasicAuthEnabled() ? getBasicAuthenticationHeader() : getOauthHeader();
    return new CustomTargetSystemRestClient(SERVICE_URL, authorizationHeader);
  }

  private static String getBasicAuthenticationHeader() {
    return "Basic " + new String(Base64.getEncoder().encode((BASIC_AUTH_USER + ":" + BASIC_AUTH_PASSWORD).getBytes()));
  }

  private static String getOauthHeader() {
    return "Bearer " + getToken();
  }
  @SuppressWarnings("unchecked")
  private static String getToken() {
    HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder().credentials(OAUTH_CLIENT_ID, OAUTH_SECRET).build();
    Logger testLogger = Logger.getLogger(CustomTargetSystemRestClient.class.getName());
    LoggingFeature loggingFeature = new LoggingFeature(testLogger, Level.WARNING, LoggingFeature.Verbosity.PAYLOAD_ANY, null);

    Client client = ClientBuilder.newClient();
    client.register(feature);
    client.register(loggingFeature);

    WebTarget target = client.target(OAUTH_SERVICE_URL).queryParam(OAUTH_GRANT_TYPE, OAUTH_GRANT);
    Response response = target.request().post(null);

    ObjectMapper mapper = new ObjectMapper();
    try {
      Map<String, String> map = mapper.readValue(response.readEntity(InputStream.class), Map.class);

      return map.get("access_token");
    } catch (Exception e) {
      logger.error("Unable to get access token", e);
    }

    return "";

  }
}
