
package com.sap.scimono.entity.config;

import static java.util.Objects.hash;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an authentication scheme as defined by <a href="https://tools.ietf.org/html/rfc7643#section-5">SCIM 2.0, Service Provider Configuration
 * Schema specification</a>.
 *
 * @author i061675
 *
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AuthenticationSchemeSetting implements Serializable {

  public static final AuthenticationSchemeSetting OAUTH = new AuthenticationSchemeSetting("oauth", "OAuth",
      "Authentication scheme using the OAuth 1.0 protocol", "https://tools.ietf.org/html/rfc5849", null);
  public static final AuthenticationSchemeSetting OAUTH2 = new AuthenticationSchemeSetting("oauth2", "OAuth2",
      "Authentication scheme using the OAuth 2.0 protocol", "https://tools.ietf.org/html/rfc6749", null);
  public static final AuthenticationSchemeSetting OAUTH_BEARER_TOKEN = new AuthenticationSchemeSetting("oauthbearertoken", "OAuth Bearer Token",
      "Authentication scheme using the OAuth 2.0 protocol with Bearer Tokens", "https://tools.ietf.org/html/rfc6750", null);
  public static final AuthenticationSchemeSetting HTTP_BASIC = new AuthenticationSchemeSetting("httpbasic", "HTTP Basic",
      "Authentication scheme using the HTTP Basic Standard", "https://tools.ietf.org/html/rfc7617", null);
  public static final AuthenticationSchemeSetting HTTP_DIGEST = new AuthenticationSchemeSetting("httpdigest", "HTTP Digest",
      "Authentication scheme using HTTP Digest", "https://tools.ietf.org/html/rfc7616", null);

  private static final long serialVersionUID = -4043900161237221740L;

  @JsonProperty(required = true)
  private final String type; // NOPMD by I061675
  @JsonProperty(required = true)
  private final String name; // NOPMD by I061675
  @JsonProperty(required = true)
  private final String description; // NOPMD by I061675
  @JsonProperty
  private final String specUri; // NOPMD by I061675
  @JsonProperty
  private final String documentationUri; // NOPMD by I061675

  @JsonCreator
  public AuthenticationSchemeSetting(@JsonProperty(value = "type", required = true) final String type,
      @JsonProperty(value = "name", required = true) final String name,
      @JsonProperty(value = "description", required = true) final String description, @JsonProperty("specUri") final String specUri,
      @JsonProperty("documentationUri") final String documentationUri) {
    this.type = type;
    this.name = name;
    this.description = description;
    this.specUri = specUri;
    this.documentationUri = documentationUri;
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getSpecUri() {
    return specUri;
  }

  public String getDocumentationUri() {
    return documentationUri;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + hash(description);
    result = prime * result + hash(documentationUri);
    result = prime * result + hash(name);
    result = prime * result + hash(specUri);
    result = prime * result + hash(type);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof AuthenticationSchemeSetting)) {
      return false;
    }
    AuthenticationSchemeSetting other = (AuthenticationSchemeSetting) obj;
    if (!Objects.equals(description, other.description)) {
      return false;
    }
    if (!Objects.equals(documentationUri, other.documentationUri)) {
      return false;
    }
    if (!Objects.equals(name, other.name)) {
      return false;
    }
    if (!Objects.equals(specUri, other.specUri)) {
      return false;
    }
    return Objects.equals(type, other.type);
  }

}
