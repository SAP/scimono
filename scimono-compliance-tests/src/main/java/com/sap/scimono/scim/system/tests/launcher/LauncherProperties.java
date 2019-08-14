package com.sap.scimono.scim.system.tests.launcher;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

public class LauncherProperties {
  private static final String PROPERTY_NAME_VALUE_DELIMITER = "=";

  private enum ParamName {
    OAUTH_CLIENT_ID("oauth.clientId"),
    OAUTH_SECRET("oauth.secret"),
    OAUTH_SERVICE_URL("oauth.service.url"),
    OAUTH_GRANT("oauth.grant"),
    AUTH_TYPE("auth.type"),
    BASIC_AUTH_USER("basic.auth.user"),
    BASIC_AUTH_PASSWORD("basic.auth.password"),
    SCIM_SERVICE_URL("scim.service.url"),
    TESTS_FILE_PATH("tests.file.path");

    ParamName(String value) {
      this.value = value;
    }

    private String value;

    public String value() {
      return value;
    }

    public static ParamName fromPropertyName(String propertyName) {
      return Arrays.stream(values()).filter(paramName -> paramName.value.equals(propertyName)).findFirst().orElse(null);
    }
  }

  public static class LauncherProperty {
    private String propertyName;
    private String propertyValue;

    private LauncherProperty(String propertyName, String propertyValue) {
      this.propertyName = propertyName;
      this.propertyValue = propertyValue;
    }

    public String getName() {
      return propertyName;
    }

    public String getValue() {
      return propertyValue;
    }
  }
  private final Map<ParamName, LauncherProperty> properties;

  private LauncherProperties(final Map<ParamName, LauncherProperty> properties) {
    this.properties = properties;
  }

  public LauncherProperty getOauthClientId() {
    return properties.get(ParamName.OAUTH_CLIENT_ID);
  }

  public LauncherProperty getOathSecret() {
    return properties.get(ParamName.OAUTH_SECRET);
  }

  public LauncherProperty getOauthServiceUrl() {
    return properties.get(ParamName.OAUTH_SERVICE_URL);
  }

  public LauncherProperty getBasicAuthUser() {
    return properties.get(ParamName.BASIC_AUTH_USER);
  }

  public LauncherProperty getBasicAuthPassword() {
    return properties.get(ParamName.BASIC_AUTH_PASSWORD);
  }

  public LauncherProperty getServiceUrl() {
    return properties.get(ParamName.SCIM_SERVICE_URL);
  }

  public LauncherProperty getAuthType() {
    return properties.get(ParamName.AUTH_TYPE);
  }

  public LauncherProperty getOauthGrantType() {
    return properties.get(ParamName.OAUTH_GRANT);
  }

  public LauncherProperty getTestsFilePath() {
    return properties.get(ParamName.TESTS_FILE_PATH);
  }
  public static LauncherProperties fromCommandLineArguments(String[] args) {
    if(!isArgsInValidFormat(args)) {
      throw new LauncherInitializationException("Invalid format of arguments");
    }

    Map<ParamName, LauncherProperty> properties = new EnumMap<>(ParamName.class);
    for (String arg : args) {
      String[] propertyNameValuePair = arg.split(PROPERTY_NAME_VALUE_DELIMITER);
      ParamName propertyName = ParamName.fromPropertyName(propertyNameValuePair[0]);
      String propertyValue = propertyNameValuePair[1];

      if(propertyName == null) {
        continue;
      }

      properties.put(propertyName, new LauncherProperty(propertyName.value, propertyValue));
    }
    return new LauncherProperties(properties);
  }

  private static boolean isArgsInValidFormat(String[] args) {
    return Stream.of(args).allMatch(arg -> arg.split(PROPERTY_NAME_VALUE_DELIMITER).length == 2);
  }
}
