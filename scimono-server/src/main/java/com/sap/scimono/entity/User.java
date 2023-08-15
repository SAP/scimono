
/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2013-2016 tarent solutions GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.sap.scimono.entity;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.ACTIVE_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.ADDRESSES_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.EMAILS_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.ENTITLEMENTS_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.GROUPS_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.IMS_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.LOCALE_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.NAME_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.NICK_NAME_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.PASSWORD_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.PHONE_NUMBERS_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.PHOTOS_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.PREFERRED_LANGUAGE_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.PROFILE_URL_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.ROLES_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.TIMEZONE_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.TITLE_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.USER_NAME_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.USER_TYPE_FIELD;
import static com.sap.scimono.entity.definition.CoreUserAttributes.Constants.X509_CERTIFICATES_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.DISPLAY_NAME_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.EXTERNAL_ID_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.ID_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.META_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.SCHEMAS_FIELD;
import static com.sap.scimono.helper.Objects.firstNonNull;
import static com.sap.scimono.helper.Objects.sameOrEmpty;
import static java.util.Objects.hash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.base.MultiValuedAttribute;
import com.sap.scimono.entity.schema.validation.ValidCoreSchema;
import com.sap.scimono.entity.schema.validation.ValidEmails;
import com.sap.scimono.exception.InvalidInputException;
import com.sap.scimono.helper.Strings;

/**
 * User resources are meant to enable expression of common User information. It should be possible to express most user data with the core attributes.
 * If more information need to be saved in a user object the user extension can be used to store all customized data.
 * <p>
 * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
 * 2.0, section 6</a>
 * </p>
 * <p>
 * client info: The scim schema is mainly meant as a connection link between the OSIAM server and a client like the connector4Java. Some values will
 * not be accepted by the OSIAM server. These specific values have an own client info documentation section.
 * </p>
 */

@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
//TODO check with original
// @JsonFilter("attributeFilter")
public final class User extends Resource<User> {

  public static final String RESOURCE_TYPE_USER = "User";
  public static final String SCHEMA = CORE_SCHEMA + RESOURCE_TYPE_USER;
  private static final long serialVersionUID = -4076516708797425414L;

  private final String userName;
  private final Name name;
  private final String displayName;
  private final String nickName;
  private final String profileUrl;
  private final String title;
  private final String userType;
  private final String preferredLanguage;
  private final String locale;
  private final String timezone;
  private final Boolean active;

  @JsonProperty(access = WRITE_ONLY)
  private final String password;

  @ValidEmails
  private final List<Email> emails;
  @Valid
  private final List<PhoneNumber> phoneNumbers;
  @Valid
  private final List<Im> ims;
  @Valid
  private final List<Photo> photos;
  // Can't really validate that one. value is not acessible
  private final List<Address> addresses;
  @Valid
  private final List<GroupRef> groups;
  @Valid
  private final List<Entitlement> entitlements;
  @Valid
  private final List<Role> roles;
  @Valid
  private final List<X509Certificate> x509Certificates;

  @JsonCreator
  private User(
      // @formatter:off
      @JsonProperty(ID_FIELD) final String id,
      @JsonProperty(EXTERNAL_ID_FIELD) final String externalId,
      @JsonProperty(META_FIELD) final Meta meta,
      @JsonProperty(value = SCHEMAS_FIELD, required = true) final Set<String> schemas,
      @JsonProperty(value = USER_NAME_FIELD, required = true) final String userName,
      @JsonProperty(NAME_FIELD) final Name name,
      @JsonProperty(DISPLAY_NAME_FIELD) final String displayName,
      @JsonProperty(NICK_NAME_FIELD) final String nickName,
      @JsonProperty(PROFILE_URL_FIELD) final String profileUrl,
      @JsonProperty(TITLE_FIELD) final String title,
      @JsonProperty(USER_TYPE_FIELD) final String userType,
      @JsonProperty(PREFERRED_LANGUAGE_FIELD) final String preferredLanguage,
      @JsonProperty(LOCALE_FIELD) final String locale,
      @JsonProperty(TIMEZONE_FIELD) final String timezone,
      @JsonProperty(ACTIVE_FIELD) final Boolean active,
      @JsonProperty(PASSWORD_FIELD) final String password,
      @JsonProperty(EMAILS_FIELD) final List<Email> emails,
      @JsonProperty(PHONE_NUMBERS_FIELD) final List<PhoneNumber> phoneNumbers,
      @JsonProperty(IMS_FIELD) final List<Im> ims,
      @JsonProperty(PHOTOS_FIELD) final List<Photo> photos,
      @JsonProperty(ADDRESSES_FIELD) final List<Address> addresses,
      @JsonProperty(GROUPS_FIELD) final List<GroupRef> groups,
      @JsonProperty(ENTITLEMENTS_FIELD) final List<Entitlement> entitlements,
      @JsonProperty(ROLES_FIELD) final List<Role> roles,
      @JsonProperty(X509_CERTIFICATES_FIELD) final List<X509Certificate> x509Certificates) {
    // @formatter:on
    super(id, externalId, meta, schemas);
    this.userName = userName != null ? userName : "";
    this.name = name;
    this.displayName = displayName;
    this.nickName = nickName;
    this.profileUrl = profileUrl;
    this.title = title;
    this.userType = userType;
    this.preferredLanguage = preferredLanguage;
    this.locale = locale;
    this.timezone = timezone;
    this.active = active;
    this.password = password;

    this.emails = sameOrEmpty(emails);
    this.phoneNumbers = sameOrEmpty(phoneNumbers);
    this.ims = sameOrEmpty(ims);
    this.photos = sameOrEmpty(photos);
    this.addresses = sameOrEmpty(addresses);
    this.groups = sameOrEmpty(groups);
    this.entitlements = sameOrEmpty(entitlements);
    this.roles = sameOrEmpty(roles);
    this.x509Certificates = sameOrEmpty(x509Certificates);
  }

  private User(final Builder builder) {
    super(builder);
    userName = builder.userName;
    name = builder.name;
    displayName = builder.displayName;
    nickName = builder.nickName;
    profileUrl = builder.profileUrl;
    title = builder.title;
    userType = builder.userType;
    preferredLanguage = builder.preferredLanguage;
    locale = builder.locale;
    timezone = builder.timezone;
    active = builder.active;
    password = builder.password;

    emails = builder.emails;
    phoneNumbers = builder.phoneNumbers;
    ims = builder.ims;
    photos = builder.photos;
    addresses = builder.addresses;
    groups = builder.groups;
    entitlements = builder.entitlements;
    roles = builder.roles;
    x509Certificates = builder.x509Certificates;
  }

  /**
   * Gets the unique identifier for the User.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the user name
   */
  @JsonInclude(Include.ALWAYS)
  public String getUserName() {
    return userName;
  }

  /**
   * Gets the components of the User's real name.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the real {@link Name} of the {@link User}
   */
  public Name getName() {
    return name;
  }

  /**
   * Gets the name of the User, suitable for display to end-users.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the display name of the {@link User}
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Gets the casual way to address the user in real life,
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the nickname of the {@link User}
   */
  public String getNickName() {
    return nickName;
  }

  /**
   * Gets a fully qualified URL to a page representing the User's online profile.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the progile URL of the {@link User}
   */
  public String getProfileUrl() {
    return profileUrl;
  }

  /**
   * The user's title, such as "Vice President."
   *
   * @return the title of the {@link User}
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the type of the {@link User}
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the type of the {@link User}
   */
  public String getUserType() {
    return userType;
  }

  /**
   * Gets the preferred written or spoken language of the User in ISO 3166-1 alpha 2 format, e.g. "DE" or "US".
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the preferred language of the {@link User}
   */
  public String getPreferredLanguage() {
    return preferredLanguage;
  }

  /**
   * Gets the default location of the User in ISO 639-1 two letter language code, e.g. 'de_DE' or 'en_US'
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the default location of the {@link User}
   */
  public String getLocale() {
    return locale;
  }

  /**
   * Gets the User's time zone in the "Olson" timezone database format
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the time zone of the {@link User}
   */
  public String getTimezone() {
    return timezone;
  }

  /**
   * Gets a Boolean that indicates the User's administrative status.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the active status of the {@link User}
   */
  public Boolean isActive() {
    return active;
  }

  /**
   * Gets the password from the User.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema
   * 2.0, section 6</a>
   * </p>
   *
   * @return the password of the {@link User}
   */
  public String getPassword() {
    return password;
  }

  /**
   * Gets all E-mail addresses for the User.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema
   * 2.0, section 6.2</a>
   * </p>
   *
   * @return the email addresses of the {@link User}
   */
  public List<Email> getEmails() {
    return Collections.unmodifiableList(emails);
  }

  /**
   * try to extract an email from the User. If the User has a primary email address this email will be returned. If not the first email address found
   * will be returned. If no Email has been found email.isPresent() == false
   *
   * @return an email if found
   */
  @JsonIgnore
  public Optional<Email> getPrimaryOrFirstEmail() {
    for (final Email email : emails) {
      if (email.isPrimary()) {
        return Optional.of(email);
      }
    }

    if (!emails.isEmpty()) {
      return Optional.of(emails.get(0));
    }

    return Optional.empty();
  }

  /**
   * Gets the phone numbers for the user.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema
   * 2.0, section 6.2</a>
   * </p>
   *
   * @return the phone numbers of the {@link User}
   */
  public List<PhoneNumber> getPhoneNumbers() {
    return Collections.unmodifiableList(phoneNumbers);
  }

  /**
   * Gets the instant messaging address for the user.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema
   * 2.0, section 6.2</a>
   * </p>
   *
   * @return the ims of the {@link User}
   */
  public List<Im> getIms() {
    return Collections.unmodifiableList(ims);
  }

  /**
   * Gets the URL's of the photos of the user.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema
   * 2.0, section 6.2</a>
   * </p>
   *
   * @return the photo URL's of the {@link User}
   */
  public List<Photo> getPhotos() {
    return Collections.unmodifiableList(photos);
  }

  /**
   * Gets the physical mailing addresses for this user.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema
   * 2.0, section 6.2</a>
   * </p>
   *
   * @return the addresses of the {@link User}
   */
  public List<Address> getAddresses() {
    return Collections.unmodifiableList(addresses);
  }

  /**
   * Gets a list of groups that the user belongs to.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema
   * 2.0, section 6.2</a>
   * </p>
   *
   * @return a list of all {@link Group}s where the {@link User} is a member of
   */
  public List<GroupRef> getGroups() {
    return Collections.unmodifiableList(groups);
  }

  /**
   * Gets a list of entitlements for the user that represent a thing the User has.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema
   * 2.0, section 6.2</a>
   * </p>
   *
   * @return a list of all entitlements of the {@link User}
   */
  public List<Entitlement> getEntitlements() {
    return Collections.unmodifiableList(entitlements);
  }

  /**
   * Gets a list of roles for the user that collectively represent who the User is e.g., 'Student', "Faculty"
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema
   * 2.0, section 6.2</a>
   * </p>
   *
   * @return a list of the roles of the {@link User}
   */
  public List<Role> getRoles() {
    return Collections.unmodifiableList(roles);
  }

  /**
   * Gets a list of certificates issued to the user. Values are Binary and DER encoded x509.
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema
   * 2.0, section 6.2</a>
   * </p>
   *
   * @return a list of the certificates of the {@link User}
   */
  public List<X509Certificate> getX509Certificates() {
    return Collections.unmodifiableList(x509Certificates);
  }

  @Override
  public String toString() {
    Map<String, Object> valuesToDisplay = new LinkedHashMap<>();

    valuesToDisplay.put(USER_NAME_FIELD, userName);
    valuesToDisplay.put(NAME_FIELD, name);
    valuesToDisplay.put(DISPLAY_NAME_FIELD, displayName);
    valuesToDisplay.put(NICK_NAME_FIELD, nickName);
    valuesToDisplay.put(PROFILE_URL_FIELD, profileUrl);
    valuesToDisplay.put(TITLE_FIELD, title);
    valuesToDisplay.put(USER_TYPE_FIELD, userType);
    valuesToDisplay.put(PREFERRED_LANGUAGE_FIELD, preferredLanguage);
    valuesToDisplay.put(LOCALE_FIELD, locale);
    valuesToDisplay.put(TIMEZONE_FIELD, timezone);
    valuesToDisplay.put(ACTIVE_FIELD, active);
    valuesToDisplay.put("password", password);
    valuesToDisplay.put(EMAILS_FIELD, emails);
    valuesToDisplay.put(PHONE_NUMBERS_FIELD, phoneNumbers);
    valuesToDisplay.put(IMS_FIELD, ims);
    valuesToDisplay.put(PHOTOS_FIELD, photos);
    valuesToDisplay.put(ADDRESSES_FIELD, addresses);
    valuesToDisplay.put(GROUPS_FIELD, groups);
    valuesToDisplay.put(ENTITLEMENTS_FIELD, entitlements);
    valuesToDisplay.put(ROLES_FIELD, roles);
    valuesToDisplay.put(X509_CERTIFICATES_FIELD, x509Certificates);
    valuesToDisplay.put(ID_FIELD, getId());
    valuesToDisplay.put(EXTERNAL_ID_FIELD, getExternalId());
    valuesToDisplay.put(META_FIELD, getMeta());
    valuesToDisplay.put(SCHEMAS_FIELD, getSchemas());
    valuesToDisplay.put("extensions", getExtensions());

    return Strings.createPrettyEntityString(valuesToDisplay, this.getClass());
  }

  @Override
  @ValidCoreSchema(SCHEMA)
  public Set<String> getSchemas() {
    return super.getSchemas();
  }

  @Override
  public Builder builder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + hash(active);
    result = prime * result + hash(addresses);
    result = prime * result + hash(displayName);
    result = prime * result + hash(emails);
    result = prime * result + hash(entitlements);
    result = prime * result + hash(groups);
    result = prime * result + hash(ims);
    result = prime * result + hash(locale);
    result = prime * result + hash(name);
    result = prime * result + hash(nickName);
    result = prime * result + hash(password);
    result = prime * result + hash(phoneNumbers);
    result = prime * result + hash(photos);
    result = prime * result + hash(preferredLanguage);
    result = prime * result + hash(profileUrl);
    result = prime * result + hash(roles);
    result = prime * result + hash(timezone);
    result = prime * result + hash(title);
    result = prime * result + hash(userName);
    result = prime * result + hash(userType);
    result = prime * result + hash(x509Certificates);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof User)) {
      return false;
    }
    final User other = (User) obj;
    if (!Objects.equals(active, other.active)) {
      return false;
    }
    if (!Objects.equals(addresses, other.addresses)) {
      return false;
    }
    if (!Objects.equals(displayName, other.displayName)) {
      return false;
    }
    if (!Objects.equals(emails, other.emails)) {
      return false;
    }
    if (!Objects.equals(entitlements, other.entitlements)) {
      return false;
    }
    if (!Objects.equals(groups, other.groups)) {
      return false;
    }
    if (!Objects.equals(ims, other.ims)) {
      return false;
    }
    if (!Objects.equals(locale, other.locale)) {
      return false;
    }
    if (!Objects.equals(name, other.name)) {
      return false;
    }
    if (!Objects.equals(nickName, other.nickName)) {
      return false;
    }
    if (!Objects.equals(password, other.password)) {
      return false;
    }
    if (!Objects.equals(phoneNumbers, other.phoneNumbers)) {
      return false;
    }
    if (!Objects.equals(photos, other.photos)) {
      return false;
    }
    if (!Objects.equals(preferredLanguage, other.preferredLanguage)) {
      return false;
    }
    if (!Objects.equals(profileUrl, other.profileUrl)) {
      return false;
    }
    if (!Objects.equals(roles, other.roles)) {
      return false;
    }
    if (!Objects.equals(timezone, other.timezone)) {
      return false;
    }
    if (!Objects.equals(title, other.title)) {
      return false;
    }
    if (!com.sap.scimono.helper.Objects.stringsEqualsIgnoreCase(userName, other.userName)) {
      return false;
    }
    if (!Objects.equals(userType, other.userType)) {
      return false;
    }
    return Objects.equals(x509Certificates, other.x509Certificates);
  }

  /**
   * Builder class that is used to build {@link User} instances
   */
  public static final class Builder extends Resource.Builder<User> {
    private String userName;
    private String password;
    private Boolean active;
    private String timezone;
    private String locale;
    private String preferredLanguage;
    private String userType;
    private String title;
    private String profileUrl;
    private String nickName;
    private String displayName;
    private Name name;
    private List<Email> emails = new ArrayList<>();
    private List<PhoneNumber> phoneNumbers = new ArrayList<>();
    private List<Im> ims = new ArrayList<>();
    private List<Photo> photos = new ArrayList<>();
    private List<Address> addresses = new ArrayList<>();
    private List<GroupRef> groups = new ArrayList<>();
    private List<Entitlement> entitlements = new ArrayList<>();
    private List<Role> roles = new ArrayList<>();
    private List<X509Certificate> x509Certificates = new ArrayList<>();

    /**
     * creates a new User.Builder based on the given userName and user. All values of the given user will be copied expect the userName will be be
     * overridden by the given one
     *
     * @param userName the new userName of the user
     * @param user a existing user
     */
    @SuppressWarnings("unchecked")
    Builder(final String userName, final User user) {
      super(user);
      addSchema(SCHEMA);
      if (user != null) {
        this.userName = user.userName;
        name = user.name;
        displayName = user.displayName;
        nickName = user.nickName;
        profileUrl = user.profileUrl;
        title = user.title;
        userType = user.userType;
        preferredLanguage = user.preferredLanguage;
        locale = user.locale;
        timezone = user.timezone;
        active = user.active;
        password = user.password;
        emails = new ArrayList<>(firstNonNull(user.emails, emails));
        phoneNumbers = new ArrayList<>(firstNonNull(user.phoneNumbers, phoneNumbers));
        ims = new ArrayList<>(firstNonNull(user.ims, ims));
        photos = new ArrayList<>(firstNonNull(user.photos, photos));
        addresses = new ArrayList<>(firstNonNull(user.addresses, addresses));
        groups = new ArrayList<>(firstNonNull(user.groups, groups));
        entitlements = new ArrayList<>(firstNonNull(user.entitlements, entitlements));
        roles = new ArrayList<>(firstNonNull(user.roles, roles));
        x509Certificates = new ArrayList<>(firstNonNull(user.x509Certificates, x509Certificates));
      }
      if (!Strings.isNullOrEmpty(userName)) {
        this.userName = userName;
      }
    }

    /**
     * Constructs a new builder by with a set userName
     *
     * @param userName Unique identifier for the User (See {@link User#getUserName()})
     * @throws InvalidInputException if the given userName is null or empty
     */
    public Builder(final String userName) {
      this(userName, null);
      if (Strings.isNullOrEmpty(userName)) {
        throw new InvalidInputException("userName must not be null or empty.");
      }
    }

    /**
     * Creates a new builder without a userName
     */
    public Builder() {
      this(null, null);
    }

    /**
     * Constructs a new builder by copying all values from the given {@link User}
     *
     * @param user a old {@link User}
     * @throws InvalidInputException if the given user is null
     */
    public Builder(final User user) {
      this(null, user);
      if (user == null) {
        throw new InvalidInputException("The given user must not be null");
      }
    }

    /**
     * Sets the userName (See {@link User#getUserName()}).
     *
     * @param userName the user name of the {@link User}
     * @return the builder itself
     */
    public Builder setUserName(final String userName) {
      this.userName = userName;
      return this;
    }

    /**
     * Sets the components of the {@link User}'s real name (See {@link User#getName()}).
     *
     * @param name the name object of the {@link User}
     * @return the builder itself
     */
    public Builder setName(final Name name) {
      if (name != null && !name.isEmpty()) {
        this.name = name;
      } else {
        this.name = null;
      }
      return this;
    }

    /**
     * Sets the display name (See {@link User#getDisplayName()}).
     *
     * @param displayName the display name of the {@link User}
     * @return the builder itself
     */
    public Builder setDisplayName(final String displayName) {
      this.displayName = displayName;
      return this;
    }

    /**
     * Sets the nick name (See {@link User#getNickName()}).
     *
     * @param nickName the nick name of the {@link User}
     * @return the builder itself
     */
    public Builder setNickName(final String nickName) {
      this.nickName = nickName;
      return this;
    }

    /**
     * Sets the profile URL (See {@link User#getProfileUrl()}).
     *
     * @param profileUrl the profil URL of the {@link User}
     * @return the builder itself
     */
    public Builder setProfileUrl(final String profileUrl) {
      this.profileUrl = profileUrl;
      return this;
    }

    /**
     * Sets the user's title (See {@link User#getTitle()}).
     *
     * @param title the title of the {@link User}
     * @return the builder itself
     */
    public Builder setTitle(final String title) {
      this.title = title;
      return this;
    }

    /**
     * Sets the user type (See {@link User#getUserType()}).
     *
     * @param userType the type of the {@link User}
     * @return the builder itself
     */
    public Builder setUserType(final String userType) {
      this.userType = userType;
      return this;
    }

    /**
     * Sets the preferred language of the USer (See {@link User#getPreferredLanguage()}).
     *
     * @param preferredLanguage sets the preferred language of the {@link User}
     * @return the builder itself
     */
    public Builder setPreferredLanguage(final String preferredLanguage) {
      this.preferredLanguage = preferredLanguage;
      return this;
    }

    /**
     * Sets the default location of the User (See {@link User#getLocale()}).
     *
     * @param locale sets the local of the {@link User}
     * @return the builder itself
     */
    public Builder setLocale(final String locale) {
      this.locale = locale;
      return this;
    }

    /**
     * Sets the User's time zone (See {@link User#getTimezone()}).
     *
     * @param timezone sets the time zone of the {@link User}
     * @return the builder itself
     */
    public Builder setTimezone(final String timezone) {
      this.timezone = timezone;
      return this;
    }

    /**
     * Sets a Boolean value indicating the User's administrative status. (See {@link User#isActive()})
     *
     * @param active the active status of the {@link User}
     * @return the builder itself
     */
    public Builder setActive(final boolean active) {
      this.active = active;
      return this;
    }

    /**
     * Sets the User's clear text password (See {@link User#getPassword()}).
     *
     * @param password the password as clear text
     * @return the builder itself
     */
    public Builder setPassword(final String password) {
      this.password = password;
      return this;
    }

    /**
     * Adds the E-mail addresses for the User (See {@link User#getEmails()}).
     *
     * @param emails a collection of email to be added
     * @return the builder itself
     */
    public Builder addEmails(final Collection<Email> emails) {
      if (emails != null) {
        for (final Email email : emails) {
          addEmail(email);
        }
      }
      return this;
    }

    /**
     * adds an email to the User
     *
     * @param email an email to add
     * @return the builder itself
     */
    public Builder addEmail(final Email email) {
      if (email == null || isMultivaluedAttributeExistInCollection(email, emails)) {
        return this;
      }

      if (email.isPrimary() && MultiValuedAttribute.isCollectionContainsPrimaryAttributes(emails)) {
        emails.replaceAll(e -> new Email.Builder(e).setPrimary(false).build());
      }

      emails.add(new Email.Builder(email).build());
      return this;
    }

    /**
     * removes all email from the actual User
     *
     * @return the builder itself
     */
    public Builder removeEmails() {
      emails.clear();
      return this;
    }

    /**
     * removes one single email from the User
     *
     * @param email an email to be removed
     * @return the builder itself
     */
    public Builder removeEmail(final Email email) {
      emails.remove(email);
      return this;
    }

    /**
     * Adds the phone numbers for the User (See {@link User#getPhoneNumbers()}).
     *
     * @param phoneNumbers the phone numbers of the the {@link User}
     * @return the builder itself
     */
    public Builder addPhoneNumbers(final Collection<PhoneNumber> phoneNumbers) {
      if (phoneNumbers != null) {
        for (PhoneNumber phoneNumber : phoneNumbers) {
          addPhoneNumber(phoneNumber);
        }
      }
      return this;
    }

    /**
     * adds an phoneNumber to the User
     *
     * @param phoneNumber a phoneNumber to add
     * @return the builder itself
     */
    public Builder addPhoneNumber(final PhoneNumber phoneNumber) {
      if (phoneNumber == null || isMultivaluedAttributeExistInCollection(phoneNumber, phoneNumbers)) {
        return this;
      }

      if (phoneNumber.isPrimary() && MultiValuedAttribute.isCollectionContainsPrimaryAttributes(phoneNumbers)) {
        phoneNumbers.replaceAll(ph -> new PhoneNumber.Builder(ph).setPrimary(false).build());
      }

      phoneNumbers.add(new PhoneNumber.Builder(phoneNumber).build());
      return this;
    }

    /**
     * removes all phoneNumbers from the actual User
     *
     * @return the builder itself
     */
    public Builder removePhoneNumbers() {
      phoneNumbers.clear();
      return this;
    }

    /**
     * removes one single phoneNumber from the User
     *
     * @param phoneNumber an phoneNumber to be removed
     * @return the builder itself
     */
    public Builder removePhoneNumber(final PhoneNumber phoneNumber) {
      phoneNumbers.remove(phoneNumber);
      return this;
    }

    /**
     * Adds the instant messaging addresses for the User (See {@link User#getIms()}).
     *
     * @param ims a collection of the ims of the {@link User}
     * @return the builder itself
     */
    public Builder addIms(final Collection<Im> ims) {
      if (ims != null) {
        for (Im im : ims) {
          addIm(im);
        }
      }
      return this;
    }

    /**
     * adds an Im to the User
     *
     * @param im a Im to add
     * @return the builder itself
     */
    public Builder addIm(final Im im) {
      if (im == null || isMultivaluedAttributeExistInCollection(im, ims)) {
        return this;
      }

      if (im.isPrimary() && MultiValuedAttribute.isCollectionContainsPrimaryAttributes(ims)) {
        ims.replaceAll(i -> new Im.Builder(i).setPrimary(false).build());
      }

      ims.add(new Im.Builder(im).build());
      return this;
    }

    /**
     * removes all ims from the actual User
     *
     * @return the builder itself
     */
    public Builder removeIms() {
      ims.clear();
      return this;
    }

    /**
     * removes one single im from the User
     *
     * @param im a im to be removed
     * @return the builder itself
     */
    public Builder removeIm(final Im im) {
      ims.remove(im);
      return this;
    }

    /**
     * Adds the URL's of photo's of the User (See {@link User#getPhotos()}).
     *
     * @param photos the photos of the {@link User}
     * @return the builder itself
     */
    public Builder addPhotos(final Collection<Photo> photos) {
      if (photos != null) {
        for (Photo photo : photos) {
          addPhoto(photo);
        }
      }
      return this;
    }

    /**
     * adds an Photo to the User
     *
     * @param photo a Photo to add
     * @return the builder itself
     */
    public Builder addPhoto(final Photo photo) {
      if (photo == null || isMultivaluedAttributeExistInCollection(photo, photos)) {
        return this;
      }

      if (photo.isPrimary() && MultiValuedAttribute.isCollectionContainsPrimaryAttributes(photos)) {
        photos.replaceAll(ph -> new Photo.Builder(ph).setPrimary(false).build());
      }

      photos.add(new Photo.Builder(photo).build());
      return this;
    }

    /**
     * removes all Photos from the actual User
     *
     * @return the builder itself
     */
    public Builder removePhotos() {
      photos.clear();
      return this;
    }

    /**
     * removes one single Photo from the User
     *
     * @param photo a photo to be removed
     * @return the builder itself
     */
    public Builder removePhoto(final Photo photo) {
      photos.remove(photo);
      return this;
    }

    /**
     * Adds the physical mailing addresses for this User (See {@link User#getAddresses()}).
     *
     * @param addresses a collection of the addresses of the {@link User}
     * @return the builder itself
     */
    public Builder addAddresses(final Collection<Address> addresses) {
      if (addresses != null) {
        for (Address address : addresses) {
          addAddress(address);
        }
      }
      return this;
    }

    /**
     * adds an Address to the User
     *
     * @param address a Address to add
     * @return the builder itself
     */
    public Builder addAddress(final Address address) {
      if (address != null) {
        addresses.add(new Address.Builder(address).build());
      }
      return this;
    }

    /**
     * removes all Addresses from the actual User
     *
     * @return the builder itself
     */
    public Builder removeAddresses() {
      addresses.clear();
      return this;
    }

    /**
     * removes one single Photo from the User
     *
     * @param address a Address to be removed
     * @return the builder itself
     */
    public Builder removeAddress(final Address address) {
      addresses.remove(address);
      return this;
    }

    /**
     * Adds all groups in the provided list to the user's groups.
     *
     * @param groups groups of the User
     * @return the builder itself
     */
    public Builder addGroups(final List<GroupRef> groups) {
      this.groups.addAll(groups);
      return this;
    }

    /**
     * Adds a single group to the user's groups.
     *
     * @param group
     * @return the builder itself
     */
    public Builder addGroup(final GroupRef group) {
      groups.add(group);
      return this;
    }

    /**
     * Removes a single group from the user's groups.
     *
     * @param group
     * @return the builder itself
     */
    public Builder removeGroup(final GroupRef group) {
      groups.remove(group);
      return this;
    }

    /**
     * Removes all groups from the user's groups.
     *
     * @return the builder itself
     */
    public Builder removeGroups() {
      groups.clear();
      return this;
    }

    /**
     * Adds a collection of entitlements for the User (See {@link User#getEntitlements()}).
     *
     * @param entitlements the entitlements of the {@link User}
     * @return the builder itself
     */
    public Builder addEntitlements(final Collection<Entitlement> entitlements) {
      if (entitlements != null) {
        for (Entitlement entitlement : entitlements) {
          addEntitlement(entitlement);
        }
      }
      return this;
    }

    /**
     * adds an Entitlement to the User
     *
     * @param entitlement a Entitlement to add
     * @return the builder itself
     */
    public Builder addEntitlement(final Entitlement entitlement) {
      if (entitlement == null || isMultivaluedAttributeExistInCollection(entitlement, entitlements)) {
        return this;
      }

      if (entitlement.isPrimary() && MultiValuedAttribute.isCollectionContainsPrimaryAttributes(entitlements)) {
        entitlements.replaceAll(ent -> new Entitlement.Builder(ent).setPrimary(false).build());
      }

      entitlements.add(new Entitlement.Builder(entitlement).build());
      return this;
    }

    /**
     * removes all Entitlements from the actual User
     *
     * @return the builder itself
     */
    public Builder removeEntitlements() {
      entitlements.clear();
      return this;
    }

    /**
     * removes one single Entitlement from the User
     *
     * @param entitlement a Entitlement to be removed
     * @return the builder itself
     */
    public Builder removeEntitlement(final Entitlement entitlement) {
      entitlements.remove(entitlement);
      return this;
    }

    /**
     * Sets a list of roles for the User (See {@link User#getRoles()}).
     *
     * @param roles a list of roles
     * @return the builder itself
     */
    public Builder addRoles(final Collection<Role> roles) {
      if (roles != null) {
        for (Role role : roles) {
          addRole(role);
        }
      }
      return this;
    }

    /**
     * adds an Role to the User
     *
     * @param role a Role to add
     * @return the builder itself
     */
    public Builder addRole(final Role role) {
      if (role == null || isMultivaluedAttributeExistInCollection(role, roles)) {
        return this;
      }

      if (role.isPrimary() && MultiValuedAttribute.isCollectionContainsPrimaryAttributes(roles)) {
        roles.replaceAll(r -> new Role.Builder(r).setPrimary(false).build());
      }

      roles.add(new Role.Builder(role).build());
      return this;
    }

    /**
     * removes all Roles from the actual User
     *
     * @return the builder itself
     */
    public Builder removeRoles() {
      roles.clear();
      return this;
    }

    /**
     * removes one single Role from the User
     *
     * @param role a Role to be removed
     * @return the builder itself
     */
    public Builder removeRole(final Role role) {
      roles.remove(role);
      return this;
    }

    /**
     * Sets a collection of certificates issued to the User (See {@link User#getX509Certificates()}).
     *
     * @param x509Certificates the certificates of the {@link User}
     * @return the builder itself
     */
    public Builder addX509Certificates(final Collection<X509Certificate> x509Certificates) {
      if (x509Certificates != null) {
        for (X509Certificate x509Certificate : x509Certificates) {
          addX509Certificate(x509Certificate);
        }
      }
      return this;
    }

    /**
     * adds an X509Certificate to the User
     *
     * @param x509Certificate a X509Certificate to add
     * @return the builder itself
     */
    public Builder addX509Certificate(final X509Certificate x509Certificate) {
      if (x509Certificate == null  || isMultivaluedAttributeExistInCollection(x509Certificate, x509Certificates)) {
        return this;
      }

      if (x509Certificate.isPrimary() && MultiValuedAttribute.isCollectionContainsPrimaryAttributes(x509Certificates)) {
        x509Certificates.replaceAll(current -> new X509Certificate.Builder(current).setPrimary(false).build());
      }

      x509Certificates.add(new X509Certificate.Builder(x509Certificate).build());
      return this;
    }

    /**
     * removes all X509Certificates from the actual User
     *
     * @return the builder itself
     */
    public Builder removeX509Certificates() {
      x509Certificates.clear();
      return this;
    }

    /**
     * removes one single X509Certificate from the User
     *
     * @param x509Certificate a X509Certificate to be removed
     * @return the builder itself
     */
    public Builder removeX509Certificate(final X509Certificate x509Certificate) {
      x509Certificates.remove(x509Certificate);
      return this;
    }

    public Builder setGroups(List<GroupRef> groups){
      this.groups = groups;
      return this;
    }

    @Override
    public Builder setMeta(final Meta meta) {
      super.setMeta(meta);
      return this;
    }

    @Override
    public Builder setExternalId(final String externalId) {
      super.setExternalId(externalId);
      return this;
    }

    @Override
    public Builder setId(final String id) {
      super.setId(id);
      return this;
    }

    @Override
    protected void addSchema(final String schema) {
      super.addSchema(schema);
    }

    @Override
    public Builder addExtensions(final Collection<Extension> extensions) {
      super.addExtensions(extensions);
      return this;
    }

    @Override
    public Builder addExtension(final Extension extension) {
      super.addExtension(extension);
      return this;
    }

    @Override
    public Builder removeExtensions() {
      super.removeExtensions();
      return this;
    }

    @Override
    public Builder removeExtension(final String urn) {
      super.removeExtension(urn);
      return this;
    }

    @Override
    public User build() {
      return new User(this);
    }
  }

}
