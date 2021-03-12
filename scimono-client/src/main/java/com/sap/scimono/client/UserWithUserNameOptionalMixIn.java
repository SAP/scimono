package com.sap.scimono.client;

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

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.scimono.entity.Address;
import com.sap.scimono.entity.Email;
import com.sap.scimono.entity.Entitlement;
import com.sap.scimono.entity.GroupRef;
import com.sap.scimono.entity.Im;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.Name;
import com.sap.scimono.entity.PhoneNumber;
import com.sap.scimono.entity.Photo;
import com.sap.scimono.entity.Role;
import com.sap.scimono.entity.X509Certificate;

public class UserWithUserNameOptionalMixIn {
  @JsonCreator
  private UserWithUserNameOptionalMixIn(
      // @formatter:off
      @JsonProperty(ID_FIELD) final String id,
      @JsonProperty(EXTERNAL_ID_FIELD) final String externalId,
      @JsonProperty(META_FIELD) final Meta meta,
      @JsonProperty(value = SCHEMAS_FIELD, required = true) final Set<String> schemas,
      @JsonProperty(USER_NAME_FIELD) final String userName,
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
      @JsonProperty(X509_CERTIFICATES_FIELD) final List<X509Certificate> x509Certificates) {}
    // @formatter:on
}
