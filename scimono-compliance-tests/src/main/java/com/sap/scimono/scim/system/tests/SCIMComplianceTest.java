package com.sap.scimono.scim.system.tests;

import static com.sap.scimono.client.authentication.OauthAuthenticatorFactory.clientCredentialsGrantAuthenticator;
import static com.sap.scimono.scim.system.tests.util.TestProperties.AUTH_TYPE;
import static com.sap.scimono.scim.system.tests.util.TestProperties.BASIC_AUTH_PASSWORD;
import static com.sap.scimono.scim.system.tests.util.TestProperties.BASIC_AUTH_USER;
import static com.sap.scimono.scim.system.tests.util.TestProperties.OAUTH_CLIENT_ID;
import static com.sap.scimono.scim.system.tests.util.TestProperties.OAUTH_GRANT;
import static com.sap.scimono.scim.system.tests.util.TestProperties.OAUTH_SECRET;
import static com.sap.scimono.scim.system.tests.util.TestProperties.OAUTH_SERVICE_URL;
import static com.sap.scimono.scim.system.tests.util.TestProperties.SERVICE_URL;
import static com.sap.scimono.scim.system.tests.util.TestProperties.HEADERS;
import static com.sap.scimono.scim.system.tests.util.TestUtil.constructResourceLocation;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;

import com.sap.scimono.client.GroupRequest;
import com.sap.scimono.client.SCIMClientService;
import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.client.SchemaRequest;
import com.sap.scimono.client.UserRequest;
import com.sap.scimono.client.SCIMRequest;
import com.sap.scimono.client.authentication.OauthCredentials;
import com.sap.scimono.client.authentication.OauthDeviceIdAuthenticator;
import com.sap.scimono.client.authentication.TargetSystemAuthenticator;
import com.sap.scimono.client.authentication.TargetSystemBasicAuthenticator;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.User;
import com.sap.scimono.scim.system.tests.util.ClientJacksonResolver;
import com.sap.scimono.scim.system.tests.util.TestReporter;

@ExtendWith(TestReporter.class)
public abstract class SCIMComplianceTest {
  private static final Logger logger = Logger.getLogger(SCIMComplianceTest.class.getName());

  private static TargetSystemAuthenticator.Builder<?> targetSystemAuthenticator;

  protected final UserRequest userRequest;
  protected final GroupRequest groupRequest;
  protected final SchemaRequest schemaRequest;

  static {
    if ("Basic".equalsIgnoreCase(AUTH_TYPE)) {
      targetSystemAuthenticator = TargetSystemBasicAuthenticator.create(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD);
    } else if ("Oauth".equalsIgnoreCase(AUTH_TYPE)) {
      targetSystemAuthenticator = getOauthAuthenticatorBuilder();
    }
  }

  protected SCIMComplianceTest() {
    SCIMClientService scimClientService = configureScimClientService(SERVICE_URL);
    SCIMRequest.Builder builder = SCIMRequest.newBuilder();
    requestWithCustomHeaders(builder);

    userRequest = scimClientService.buildUserRequest(builder);
    groupRequest = scimClientService.buildGroupRequest(builder);
    schemaRequest = scimClientService.buildSchemaRequest(builder);
  }

  public static SCIMRequest.Builder requestWithCustomHeaders(SCIMRequest.Builder newBuilder) {
    if (HEADERS == null) {
      return newBuilder;
    }

    Pattern p = Pattern.compile(Pattern.quote("'") + "(.*?)" + Pattern.quote("'"));
    Matcher m = p.matcher(HEADERS);
    while (m.find()) {
      String[] header = m.group(1).split(":");
      newBuilder.addHeader(header[0], header[1]);
    }
    return newBuilder;
  }


  public static SCIMClientService configureScimClientService(final String serviceUrl) {
    // @formatter:off
    SCIMClientService.Builder clientServiceBuilder = SCIMClientService
        .builder(serviceUrl)
        .addResolver(new LoggingFeature(logger, Level.WARNING, LoggingFeature.Verbosity.PAYLOAD_ANY, null))
        .addProperty(TestProperties.LOG_TRAFFIC, true)
        .addProperty(TestProperties.DUMP_ENTITY, true)
        .addProperty(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_CLIENT, "WARNING")
        .addProperty(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
        .addResolver(new ClientJacksonResolver());
    // @formatter:on

    clientServiceBuilder.addAuthenticator(targetSystemAuthenticator);

    return clientServiceBuilder.build();
  }

  private static Client getOauthClient() {
    LoggingFeature loggingFeature = new LoggingFeature(logger, Level.WARNING, LoggingFeature.Verbosity.PAYLOAD_ANY, null);

    Client client = ClientBuilder.newClient();
    client.register(loggingFeature);
    return client;
  }

  protected boolean isUserFetchedInResponse(final String userId, final List<User> fetchedUsers) {
    return fetchedUsers.stream().anyMatch(user -> user.getId().equals(userId));
  }

  protected boolean isGroupFetchedInResponse(final String groupId, final List<Group> fetchedGroups) {
    return fetchedGroups.stream().anyMatch(group -> group.getId().equals(groupId));
  }

  protected Executable getMetaAssertions(final Resource<?> resource, final String resourceType) {
    // @formatter:off
    Meta meta = resource.getMeta();

    return () ->  assertAll( "Verify 'meta' attributes",
        () -> assertNotNull(meta, "Verify meta existence"),
        () -> assertEquals(resourceType.toLowerCase(), meta.getResourceType().toLowerCase(), "verify 'resourceType'"),
        () -> assertNotNull(meta.getLocation(), "verify location 'location' is not empty"),
        () -> assertTrue(meta.getLocation().endsWith(constructResourceLocation(resource)), "verify location is correct"),
        () -> assertNotNull(meta.getCreated(), "verify location 'created' is not empty"),
        () -> assertNotNull(meta.getLastModified(), "verify location 'lastModified' is not empty"),
        () -> assertNotNull(meta.getVersion(), "verify location 'version' is not empty")
    );
  }

  protected Collection<Executable> getResponseStatusAssertions(final SCIMResponse<?> scimResponse, final boolean expectedToBeSuccess, final Response.Status expectedStatus) {
    Executable responseExecutionAssertion = expectedToBeSuccess ?
        () -> assertTrue(scimResponse.isSuccess(), "Verify response is successful"):
        () -> assertFalse(scimResponse.isSuccess(), "Verify response is failure");

    Executable statusCodeAssertion = () -> assertEquals(expectedStatus.getStatusCode(), scimResponse.getStatusCode(), "Verify status code");
    return Arrays.asList(responseExecutionAssertion, statusCodeAssertion);
  }

  private static TargetSystemAuthenticator.Builder<?> getOauthAuthenticatorBuilder() {
    if("device_id".equalsIgnoreCase(OAUTH_GRANT)) {
      return new OauthDeviceIdAuthenticator.Builder()
          .setHttpClient(getOauthClient())
          .setOauthUrl(UriBuilder.fromPath(OAUTH_SERVICE_URL).build())
          .setOauthCredentials(new OauthCredentials(OAUTH_CLIENT_ID, OAUTH_SECRET));
    } else {
      return clientCredentialsGrantAuthenticator(getOauthClient(), OAUTH_SERVICE_URL, new OauthCredentials(OAUTH_CLIENT_ID, OAUTH_SECRET));
    }
  }

  protected Executable getResourcesWithStartIndexEqualTotalResultsAssertions(int startIndex, int readCount, PagedByIndexSearchResult<?> resourcesPage) {
    // @formatter:off
    return () -> assertAll("Verify Correct ListResponse values",
        () -> assertEquals(startIndex, resourcesPage.getStartIndex(), "Verify 'startIndex"),
        () -> assertEquals(startIndex, resourcesPage.getTotalResults(), "Verify 'totalResults' is equal to created Users"),
        () -> assertTrue(resourcesPage.getItemsPerPage() <= readCount, "Verify 'itemsPerPage' is less than or equal to count param: " + readCount),
        () -> assertEquals(1, resourcesPage.getResources().size(), "Verify 'Resources' list size is equal to 'ItemsPerPage'"));
    // @formatter:on
  }
}