package com.sap.scimono.scim.system.tests.util;

public class TestProperties {
  public static final String OAUTH_CLIENT_ID = System.getProperty("oauth.clientId");
  public static final String OAUTH_SECRET = System.getProperty("oauth.secret");
  public static final String OAUTH_SERVICE_URL = System.getProperty("oauth.service.url");

  public static final String BASIC_AUTH_ENABLED = System.getProperty("basicAuthEnabled");
  public static final String BASIC_AUTH_USER = System.getProperty("basic.auth.user");
  public static final String BASIC_AUTH_PASSWORD = System.getProperty("basic.auth.password");

  public static final String INTEGRATION_TESTS_HOST = "integration.test.server.url";
  public static final String SERVICE_URL = System.getProperty("scim.service.url");
}
