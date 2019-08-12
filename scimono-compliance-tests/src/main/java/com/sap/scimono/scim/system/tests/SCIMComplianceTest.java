/**
 * Copyright (c) 2017 by SAP Labs Bulgaria, url: http://www.sap.com All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP SE, Walldorf. You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered into with SAP.
 *
 * Created on Mar 17, 2017 by i061675
 *
 */

package com.sap.scimono.scim.system.tests;

import com.sap.scimono.client.GroupRequest;
import com.sap.scimono.client.SCIMClientService;
import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.client.SchemaRequest;
import com.sap.scimono.client.UserRequest;
import com.sap.scimono.client.authentication.OauthCredentials;
import com.sap.scimono.client.authentication.TargetSystemBasicAuthenticator;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.User;
import com.sap.scimono.scim.system.tests.util.ClientJacksonResolver;
import com.sap.scimono.scim.system.tests.util.TestReporter;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.sap.scimono.client.authentication.OauthAuthenticatorFactory.clientCredentialsGrantAuthenticator;
import static com.sap.scimono.scim.system.tests.util.TestProperties.BASIC_AUTH_ENABLED;
import static com.sap.scimono.scim.system.tests.util.TestProperties.BASIC_AUTH_PASSWORD;
import static com.sap.scimono.scim.system.tests.util.TestProperties.BASIC_AUTH_USER;
import static com.sap.scimono.scim.system.tests.util.TestProperties.OAUTH_CLIENT_ID;
import static com.sap.scimono.scim.system.tests.util.TestProperties.OAUTH_SECRET;
import static com.sap.scimono.scim.system.tests.util.TestProperties.OAUTH_SERVICE_URL;
import static com.sap.scimono.scim.system.tests.util.TestProperties.SERVICE_URL;
import static com.sap.scimono.scim.system.tests.util.TestUtil.constructResourceLocation;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TestReporter.class)
public abstract class SCIMComplianceTest {
  private static final Logger logger = Logger.getLogger(SCIMComplianceTest.class.getName());

  protected final UserRequest userRequest;
  protected final GroupRequest groupRequest;
  protected final SchemaRequest schemaRequest;

  protected SCIMComplianceTest() {
    SCIMClientService scimClientService = configureScimClientService(SERVICE_URL);
    userRequest = scimClientService.buildUserRequest();
    groupRequest = scimClientService.buildGroupRequest();
    schemaRequest = scimClientService.buildSchemaRequest();
  }

  public static SCIMClientService configureScimClientService(String serviceUrl) {
    // @formatter:off
    SCIMClientService.Builder clientServiceBuilder = SCIMClientService
        .builder(serviceUrl)
        .addResolver(new ClientJacksonResolver())
        .addResolver(new LoggingFeature(logger, Level.WARNING, LoggingFeature.Verbosity.PAYLOAD_ANY, null))
        .addProperty(TestProperties.LOG_TRAFFIC, true)
        .addProperty(TestProperties.DUMP_ENTITY, true)
        .addProperty(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_CLIENT, "WARNING")
        .addProperty(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
    // @formatter:on

    if("true".equalsIgnoreCase(BASIC_AUTH_ENABLED)) {
      clientServiceBuilder.addAuthenticator(TargetSystemBasicAuthenticator.create(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD));
    } else if (OAUTH_CLIENT_ID != null) {
      clientServiceBuilder.addAuthenticator(clientCredentialsGrantAuthenticator(getOauthClient(), OAUTH_SERVICE_URL, new OauthCredentials(
          OAUTH_CLIENT_ID, OAUTH_SECRET)));
    }

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

  protected Executable getMetaAssertions(Resource<?> resource, String resourceType) {
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

  protected Collection<Executable> getResponseStatusAssertions(SCIMResponse<?> scimResponse, boolean expectedToBeSuccess, Response.Status expectedStatus) {
    Executable responseExecutionAssertion = expectedToBeSuccess ?
        () -> assertTrue(scimResponse.isSuccess(), "Verify response is successful"):
        () -> assertFalse(scimResponse.isSuccess(), "Verify response is failure");

    Executable statusCodeAssertion = () -> assertEquals(expectedStatus.getStatusCode(), scimResponse.getStatusCode(), "Verify status code");
    return Arrays.asList(responseExecutionAssertion, statusCodeAssertion);
  }

}