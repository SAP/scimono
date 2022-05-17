package com.sap.scimono.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.sap.scimono.entity.base.Extension;

import java.util.Map;

import static com.sap.scimono.entity.definition.SAPUserExtensionAttributes.*;

/**
 * Java class for SAP Core User Schema extension. See <a href="https://api.sap.com/api/IdDS_SCIM/schema">SAP API Business Hub</a>.
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public final class SAPUserExtension extends Extension {
    public static final String SAP_USER_EXTENSION_URN = "urn:ietf:params:scim:schemas:extension:sap:2.0:User";

    private static final long serialVersionUID = 1606838139038034633L;

    private SAPUserExtension(final Builder builder) {
        super(builder);
    }

    public String getLoginTime() {
        return getAttributeValueAsString(LOGINTIME.scimName());
    }

    public Integer getSourceSystem() {
        return getAttributeValueAsInteger(SOURCESYSTEM.scimName());
    }

    public String getSourceSystemId() {
        return getAttributeValueAsString(SOURCESYSTEMID.scimName());
    }

    public String getApplicationId() {
        return getAttributeValueAsString(APPLICATIONID.scimName());
    }

    public String getEmailTemplateSetId() {
        return getAttributeValueAsString(EMAILTEMPLATESETID.scimName());
    }

    public Boolean getSendMail() {
        return getAttributeValueAsBoolean(SENDMAIL.scimName());
    }

    public String getTargetUrl() {
        return getAttributeValueAsString(TARGETURL.scimName());
    }

    public Boolean getMailVerified() {
        return getAttributeValueAsBoolean(MAILVERIFIED.scimName());
    }

    public String getUserUuid() {
        return getAttributeValueAsString(USERUUID.scimName());
    }

    public String getUserId() {
        return getAttributeValueAsString(USERID.scimName());
    }

    public String getSapUserName() {
        return getAttributeValueAsString(SAPUSERNAME.scimName());
    }

    public Boolean getTotpEnabled() {
        return getAttributeValueAsBoolean(TOTPENABLED.scimName());
    }

    public Boolean getWebAuthEnabled() {
        return getAttributeValueAsBoolean(WEBAUTHENABLED.scimName());
    }

    public Boolean getMfaEnabled() {
        return getAttributeValueAsBoolean(MFAENABLED.scimName());
    }

    public String getValidFrom() {
        return getAttributeValueAsString(VALIDFROM.scimName());
    }

    public String getValidTo() {
        return getAttributeValueAsString(VALIDTO.scimName());
    }

    public ContactPreferences getContactPreferences(){
        return (ContactPreferences) getAttribute((CONTACT_PREFERENCES.scimName()));
    }


    public static final class Builder extends Extension.Builder {

        public Builder() {
            super(SAP_USER_EXTENSION_URN);
        }

        public Builder(SAPUserExtension sapExtension) {
            super(SAP_USER_EXTENSION_URN);
            setLoginTime(sapExtension.getLoginTime());
            setSourceSystem(sapExtension.getSourceSystem());
            setSourceSystemId(sapExtension.getSourceSystemId());
            setApplicationId(sapExtension.getApplicationId());
            setEmailTemplateSetId(sapExtension.getEmailTemplateSetId());
            setSendMail(sapExtension.getSendMail());
            setTargetUrl(sapExtension.getTargetUrl());
            setMailVerified(sapExtension.getMailVerified());
            setUserUuid(sapExtension.getUserUuid());
            setUserId(sapExtension.getUserId());
            setSapUserName(sapExtension.getSapUserName());
            setTotpEnabled(sapExtension.getTotpEnabled());
            setWebAuthEnabled(sapExtension.getWebAuthEnabled());
            setMfaEnabled(sapExtension.getMfaEnabled());
            setValidFrom(sapExtension.getValidFrom());
            setValidTo(sapExtension.getValidTo());

            ContactPreferences contactPreferences = sapExtension.getContactPreferences();
            if (contactPreferences != null){
                setContactPreferences(new ContactPreferences.Builder(contactPreferences).build());
            }
            /*//todo multivalue
            Manager enterpriseExtensionManager = sapExtension.getManager();
            if (enterpriseExtensionManager != null) {
                setManager(new Manager.Builder(enterpriseExtensionManager).build());
            }*/
        }

        public Builder(final Map<String, Object> values) {
            this();
            setLoginTime((String) values.get(LOGINTIME.scimName()));
            setSourceSystem((Integer) values.get(SOURCESYSTEM.scimName()));
            setSourceSystemId((String) values.get(SOURCESYSTEMID.scimName()));
            setApplicationId((String) values.get(APPLICATIONID.scimName()));
            setEmailTemplateSetId((String) values.get(EMAILTEMPLATESETID.scimName()));
            setSendMail((Boolean) values.get(SENDMAIL.scimName()));
            setTargetUrl((String) values.get(TARGETURL.scimName()));
            setMailVerified((Boolean) values.get(MAILVERIFIED.scimName()));
            setUserUuid((String) values.get(USERUUID.scimName()));
            setUserId((String) values.get(USERID.scimName()));
            setSapUserName((String) values.get(SAPUSERNAME.scimName()));
            setTotpEnabled((Boolean) values.get(TOTPENABLED.scimName()));
            setWebAuthEnabled((Boolean) values.get(WEBAUTHENABLED.scimName()));
            setMfaEnabled((Boolean) values.get(MFAENABLED.scimName()));
            setValidFrom((String) values.get(VALIDFROM.scimName()));
            setValidTo((String) values.get(VALIDTO.scimName()));
/*
    PasswordDetails:
      type: object
  properties:
        loginTime
        failedLoginAttempts
        setTime
        status
        policy
            *
            *
            *

            *
            * PhoneNumber:
      type: object
      required:
      - type
      - value
      properties:
        type:
          type: string
          enum:
          - work
          - mobile
          - other
        value:
          type: string
        display:
          type: string
        primary:
          type: boolean
            *
            *
            *
            *
            *
            *
            *
            *
            * */
            /*setManager(new Manager.Builder((Map<String, String>) values.get(MANAGER.scimName())).build());*/
        }

        // todo multi value attributes / arrays
        /*public Builder setManager(final Manager manager) {
            if (manager != null && !manager.isEmpty()) {
                setAttribute(MANAGER.scimName(), manager);
            } else {
                setAttribute(MANAGER.scimName(), null);
            }

            return this;
        }
*/

        public Builder setLoginTime(final String loginTime) {
            setAttribute(LOGINTIME.scimName(), loginTime);
            return this;
        }

        public Builder setSourceSystem(final Integer sourceSystem) {
            setAttribute(SOURCESYSTEM.scimName(), sourceSystem);
            return this;
        }

        public Builder setSourceSystemId(final String sourceSystemId) {
            setAttribute(SOURCESYSTEMID.scimName(), sourceSystemId);
            return this;
        }

        public Builder setApplicationId(final String applicationId) {
            setAttribute(APPLICATIONID.scimName(), applicationId);
            return this;
        }

        public Builder setEmailTemplateSetId(final String emailTemplateSetId) {
            setAttribute(EMAILTEMPLATESETID.scimName(), emailTemplateSetId);
            return this;
        }

        public Builder setSendMail(final Boolean sendMail) {
            setAttribute(SENDMAIL.scimName(), sendMail);
            return this;
        }

        public Builder setTargetUrl(final String targetUrl) {
            setAttribute(TARGETURL.scimName(), targetUrl);
            return this;
        }

        public Builder setMailVerified(final Boolean mailVerified) {
            setAttribute(MAILVERIFIED.scimName(), mailVerified);
            return this;
        }

        public Builder setUserUuid(final String userUuid) {
            setAttribute(USERUUID.scimName(), userUuid);
            return this;
        }

        public Builder setUserId(final String userId) {
            setAttribute(USERID.scimName(), userId);
            return this;
        }

        public Builder setSapUserName(final String sapUserName) {
            setAttribute(SAPUSERNAME.scimName(), sapUserName);
            return this;
        }

        public Builder setTotpEnabled(final Boolean totpEnabled) {
            setAttribute(TOTPENABLED.scimName(), totpEnabled);
            return this;
        }

        public Builder setWebAuthEnabled(final Boolean webAuthEnabled) {
            setAttribute(WEBAUTHENABLED.scimName(), webAuthEnabled);
            return this;
        }

        public Builder setMfaEnabled(final Boolean mfaEnabled) {
            setAttribute(MFAENABLED.scimName(), mfaEnabled);
            return this;
        }

        public Builder setValidFrom(final String validFrom) {
            setAttribute(VALIDFROM.scimName(), validFrom);
            return this;
        }

        public Builder setValidTo(final String validTo) {
            setAttribute(VALIDTO.scimName(), validTo);
            return this;
        }

        public SAPUserExtension.Builder setContactPreferences(final ContactPreferences contactPreferences) {
            if (contactPreferences != null && !contactPreferences.isEmpty()) {
                setAttribute(CONTACT_PREFERENCES.scimName(), contactPreferences);
            } else {
                setAttribute(CONTACT_PREFERENCES.scimName(), null);
            }
            return this;
        }

        @Override
        public SAPUserExtension build() {
            return new SAPUserExtension(this);
        }
    }

}
