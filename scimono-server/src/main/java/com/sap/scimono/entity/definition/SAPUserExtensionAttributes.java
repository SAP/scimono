package com.sap.scimono.entity.definition;

import com.sap.scimono.entity.EnterpriseExtension;

import static com.sap.scimono.entity.definition.SAPUserExtensionAttributes.Constants.*;


public enum SAPUserExtensionAttributes implements ScimAttribute<SAPUserExtensionAttributes> {

    LOGINTIME(LOGINTIME_FIELD, null),
    SOURCESYSTEM(SOURCESYSTEM_FIELD, null),
    SOURCESYSTEMID(SOURCESYSTEMID_FIELD, null),
    APPLICATIONID(APPLICATIONID_FIELD, null),
    EMAILTEMPLATESETID(EMAILTEMPLATESETID_FIELD, null),
    SENDMAIL(SENDMAIL_FIELD, null),
    TARGETURL(TARGETURL_FIELD, null),
    MAILVERIFIED(MAILVERIFIED_FIELD, null),
    USERID(USERID_FIELD, null),
    USERUUID(USERUUID_FIELD,null),
    SAPUSERNAME(SAPUSERNAME_FIELD, null),
    TOTPENABLED(TOTPENABLED_FIELD, null),
    WEBAUTHENABLED(WEBAUTHENABLED_FIELD, null),
    MFAENABLED(MFAENABLED_FIELD, null),
    VALIDFROM(VALIDFROM_FIELD, null),
    VALIDTO(VALIDTO_FIELD, null),

    CONTACTPREFERENCES(CONTACTPREFERENCES_FIELD, null),
    CONTACTPREFERENCES_DESCRIPTION(CONTACTPREFERENCES_DESCRIPTION_FIELD, CONTACTPREFERENCES),
    CONTACTPREFERENCES_EMAIL(CONTACTPREFERENCES_EMAIL_FIELD, CONTACTPREFERENCES),
    CONTACTPREFERENCES_TELEPHONE(CONTACTPREFERENCES_TELEPHONE_FIELD, CONTACTPREFERENCES),

    SOCIALIDENTITIES(SOCIALIDENTITIES_FIELD, null),
    SOCIALIDENTITIES_SOCIALID(SOCIALIDENTITIES_SOCIALID_FIELD,SOCIALIDENTITIES),
    SOCIALIDENTITIES_SOCIALPROVIDER(SOCIALIDENTITIES_SOCIALPROVIDER_FIELD, SOCIALIDENTITIES),
    SOCIALIDENTITIES_DATEOFLINKING(SOCIALIDENTITIES_DATEOFLINKING_FIELD, SOCIALIDENTITIES),
    PASSWORDDETAILS(PASSWORDDETAILS_FIELD, null),
    PASSWORDDETAILS_LOGINTIME(PASSWORDDETAILS_LOGINTIME_FIELD, PASSWORDDETAILS),
    PASSWORDDETAILS_FAILEDLOGINATTEMPTS(PASSWORDDETAILS_FAILEDLOGINATTEMPTS_FIELD, PASSWORDDETAILS),
    PASSWORDDETAILS_SETTIME(PASSWORDDETAILS_SETTIME_FIELD, PASSWORDDETAILS),
    PASSWORDDETAILS_STATUS(PASSWORDDETAILS_STATUS_FIELD, PASSWORDDETAILS),
    PASSWORDDETAILS_POLICY(PASSWORDDETAILS_POLICY_FIELD, PASSWORDDETAILS),

    EMAILS(EMAILS_FIELD, null),
    EMAILS_TYPE(EMAILS_TYPE_FIELD, EMAILS),
    EMAILS_VALUE(EMAILS_VALUE_FIELD, EMAILS),
    EMAILS_DISPLAY(EMAILS_DISPLAY_FIELD, EMAILS),
    EMAILS_PRIMARY(EMAILS_PRIMARY_FIELD, EMAILS),
    EMAILS_VERIFIED(EMAILS_VERIFIED_FIELD, EMAILS),
    EMAILS_VERIFIEDTIME(EMAILS_VERIFIEDTIME_FIELD, EMAILS),

    PHONENUMBERS(PHONENUMBERS_FIELD, null),
    PHONENUMBERS_TYPE(PHONENUMBERS_TYPE_FIELD, PHONENUMBERS),
    PHONENUMBERS_VALUE(PHONENUMBERS_VALUE_FIELD, PHONENUMBERS),
    PHONENUMBERS_DISPLAY(PHONENUMBERS_DISPLAY_FIELD, PHONENUMBERS),
    PHONENUMBERS_PRIMARY(PHONENUMBERS_PRIMARY_FIELD, PHONENUMBERS);


    private final ScimAttribute<SAPUserExtensionAttributes> scimAttribute;

    private static final ScimAttributesFactory<SAPUserExtensionAttributes> SAP_EXTENSION_ATTRIBUTES_FACTORY
            = new ScimAttributesFactory<>(SAPUserExtensionAttributes::values);

    SAPUserExtensionAttributes(String attributeName, SAPUserExtensionAttributes parent) {
        scimAttribute = new ScimAttributeImpl<>(attributeName, EnterpriseExtension.ENTERPRISE_URN, parent, SAPUserExtensionAttributes::values);
    }

    @Override
    public boolean isTopLevelAttribute() {
        return scimAttribute.isTopLevelAttribute();
    }

    @Override
    public String scimName() {
        return scimAttribute.scimName();
    }

    @Override
    public String fullAttributePath() {
        return scimAttribute.fullAttributePath();
    }

    @Override
    public String relativePath() {
        return scimAttribute.relativePath();
    }

    @Override
    public SAPUserExtensionAttributes subAttributeFrom(String childAttributeName) {
        return scimAttribute.subAttributeFrom(childAttributeName);
    }

    @Override
    public SAPUserExtensionAttributes getParent() {
        return null;
    }

    @Override
    public String getSchemaId() {
        return null;
    }

    public interface Constants {
        //($date-time)
        String LOGINTIME_FIELD = "loginTime";
        String SOURCESYSTEM_FIELD = "sourceSystem";
        String SOURCESYSTEMID_FIELD = "sourceSystemId";
        // Specifies the id of the application from which the email template set is going to be chosen.
        String APPLICATIONID_FIELD = "applicationId";

        // Specifies the id of the email template set which is going to be used to create an activation mail message.
        String EMAILTEMPLATESETID_FIELD = "emailTemplateSetId";

        // Specifies if an activation mail should be sent.
        String SENDMAIL_FIELD = "sendMail";

        // Specifies an application link to which an user will be redirected after account activation.
        String TARGETURL_FIELD = "targetUrl";

        // Specifies if the primary e-mail is verified.
        String MAILVERIFIED_FIELD = "mailVerified";

        String USERUUID_FIELD = "userUuid";
        String USERID_FIELD = "userId";
        String SAPUSERNAME_FIELD = "sapUserName";
        // Specifies if time-based one-time password authentication is enabled.
        String TOTPENABLED_FIELD = "totpEnabled";

        //   Specifies if web authentication is enabled.
        String WEBAUTHENABLED_FIELD = "webAuthEnabled";

        // Specifies if multi-factor authentication is enabled.
        String MFAENABLED_FIELD = "mfaEnabled";

        // Specifies the time and date from which the user is valid.
        String VALIDFROM_FIELD = "validFrom";

        // Specifies the time and date to which the user is valid.
        String VALIDTO_FIELD = "validTo";


        // Specifies the contact preferences of the user. The allowed values for each preference is "yes", "no" or "unknown".
        String CONTACTPREFERENCES_FIELD = "contactPreferences";
        String CONTACTPREFERENCES_DESCRIPTION_FIELD = "description";

        String CONTACTPREFERENCES_EMAIL_FIELD = "email";
        String CONTACTPREFERENCES_TELEPHONE_FIELD = "telephone";


        String SOCIALIDENTITIES_FIELD = "socialIdentities";
        String SOCIALIDENTITIES_SOCIALID_FIELD = "socialId";
        String SOCIALIDENTITIES_SOCIALPROVIDER_FIELD = "socialProvider";
        // ($date-time)
        String SOCIALIDENTITIES_DATEOFLINKING_FIELD = "dateOfLinking";


        // ($date-time)
        String PASSWORDDETAILS_FIELD = "passwordDetails";
        String PASSWORDDETAILS_LOGINTIME_FIELD = "loginTime";
        String PASSWORDDETAILS_FAILEDLOGINATTEMPTS_FIELD = "failedLoginAttempts";
        // ($date-time)
        String PASSWORDDETAILS_SETTIME_FIELD = "setTime";
        String PASSWORDDETAILS_STATUS_FIELD = "status";
        String PASSWORDDETAILS_POLICY_FIELD = "policy";

        // Values should be equal to the values of the emails attribute in the user core schema when creating or updating user.
        String EMAILS_FIELD = "emails";
        String EMAILS_TYPE_FIELD = "type";
        String EMAILS_VALUE_FIELD = "value";
        String EMAILS_DISPLAY_FIELD = "display";
        String EMAILS_PRIMARY_FIELD = "primary";
        String EMAILS_VERIFIED_FIELD = "verified";
        String EMAILS_VERIFIEDTIME_FIELD = "verifiedTime";

        String PHONENUMBERS_FIELD = "phoneNumbers";
        // Enum: [ work, mobile, other ]
        String PHONENUMBERS_TYPE_FIELD = "type";
        String PHONENUMBERS_VALUE_FIELD = "value";
        String PHONENUMBERS_DISPLAY_FIELD = "display";
        String PHONENUMBERS_PRIMARY_FIELD = "primary";
    }
}
