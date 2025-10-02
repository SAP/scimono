
package com.sap.scimono.entity.definition;

public interface ResourceConstants {
  String ID_FIELD = "id";
  String DISPLAY_NAME_FIELD = "displayName";
  String SCHEMAS_FIELD = "schemas";
  String EXTERNAL_ID_FIELD = "externalId";
  String META_FIELD = "meta";
  String META_CREATED_FIELD = "created";
  String META_LAST_MODIFIED_FIELD = "lastModified";
  String META_ATTRIBUTES_FIELD = "attributes";
  String META_RESOURCE_TYPE_FIELD = "resourceType";
  String META_LOCATION_FIELD = "location";
  String META_VERSION_FIELD = "version";

  interface MultivaluedAttributeConstants {
    String TYPE_FIELD = "type";
    String VALUE_FIELD = "value";
    String OPERATION_FIELD = "operation";
    String DISPLAY_FIELD = "display";
    String PRIMARY_FIELD = "primary";
    String REF_FIELD = "$ref";
    String CONTACT_PREFERENCES_EMAIL = "email";
    String CONTACT_PREFERENCES_TELEPHONE = "telephone";
    String SOCIALIDENTITIES_SOCIALID = "socialId";
    String SOCIALIDENTITIES_SOCIALPROVIDER = "socialProvider";
    String SOCIALIDENTITIES_DATEOFLINKING = "dateOfLinking";
    String PASSWORDDETAILS_LOGINTIME = "loginTime";
    String PASSWORDDETAILS_FAILEDLOGINATTEMPTS = "failedLoginAttempts";
    String PASSWORDDETAILS_SETTIME = "setTime";
    String PASSWORDDETAILS_STATUS = "status";
    String PASSWORDDETAILS_POLICY = "policy";
  }
}
