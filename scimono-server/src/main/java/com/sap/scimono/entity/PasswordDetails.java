package com.sap.scimono.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.scimono.entity.definition.SAPUserExtensionAttributes;
import com.sap.scimono.helper.Strings;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import static com.sap.scimono.entity.definition.ResourceConstants.MultivaluedAttributeConstants.*;
import static java.util.Objects.hash;


/**
 * Java class for PasswordDetails complex type
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class PasswordDetails implements Serializable {

    private static final long serialVersionUID = -1606771200095918850L;


    @JsonProperty(PASSWORDDETAILS_LOGINTIME)
    private final String loginTime;
    @JsonProperty(PASSWORDDETAILS_FAILEDLOGINATTEMPTS)
    private final int failedLoginAttempts;
    @JsonProperty(PASSWORDDETAILS_SETTIME)
    private final String setTime;
    @JsonProperty(PASSWORDDETAILS_STATUS)
    private final String status;
    @JsonProperty(PASSWORDDETAILS_POLICY)
    private final String policy;

    private PasswordDetails(final PasswordDetails.Builder builder) {
        loginTime = builder.loginTime;
        failedLoginAttempts = builder.failedLoginAttempts;
        setTime = builder.setTime;
        status = builder.status;
        policy = builder.policy;

    }


    @JsonCreator
    public PasswordDetails(String id, int failedLoginAttempts, String setTime, String status, String policy) {
        this.loginTime = id;
        this.failedLoginAttempts = failedLoginAttempts;
        this.setTime = setTime;
        this.status = status;
        this.policy = policy;
    }

    private PasswordDetails(final Builder builder, String loginTime, int failedLoginAttempts, String setTime, String status, String policy) {
        this.loginTime = loginTime;
        this.failedLoginAttempts = failedLoginAttempts;
        this.setTime = setTime;
        this.status = status;
        this.policy = policy;
    }


    @JsonIgnore
    public boolean isEmpty() {
        return Strings.isNullOrEmpty(loginTime + failedLoginAttempts + setTime);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + hash(loginTime);
        result = prime * result + hash(failedLoginAttempts);
        result = prime * result + hash(setTime);
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
        if (!(obj instanceof PasswordDetails)) {
            return false;
        }
        PasswordDetails other = (PasswordDetails) obj;
        if (!Objects.equals(loginTime, other.loginTime)) {
            return false;
        }
        if (!Objects.equals(failedLoginAttempts, other.failedLoginAttempts)) {
            return false;
        }
        if (!Objects.equals(setTime, other.setTime)) {
            return false;
        }
        if (!Objects.equals(status, other.status)) {
            return false;
        }
        return (!Objects.equals(policy, other.policy));
    }

    public static class Builder {
        private String loginTime;
        private int failedLoginAttempts;
        private String setTime;
        private String status;
        private String policy;

        public PasswordDetails.Builder setLoginTime(String loginTime) {
            this.loginTime = loginTime;
            return this;
        }

        public PasswordDetails.Builder setFailedLoginAttempts(int failedLoginAttempts) {
            this.failedLoginAttempts = failedLoginAttempts;
            return this;
        }

        public PasswordDetails.Builder setSetTime(String setTime) {
            this.setTime = setTime;
            return this;
        }

        public PasswordDetails.Builder setStatus(String status) {
            this.status = status;
            return this;
        }

        public void setPolicy(String policy) {
            this.policy = policy;
        }

        public Builder() {
            // Default constructor used to instantiate new ContactPreferences objects
        }

        public Builder(PasswordDetails passwordDetails) {
            // Default constructor used to instantiate new contactPreferences objects
            this.loginTime = passwordDetails.loginTime;
            this.failedLoginAttempts = passwordDetails.failedLoginAttempts;
            this.setTime = passwordDetails.setTime;
            this.status = passwordDetails.status;
            this.policy = passwordDetails.policy;
        }

        public Builder(final Map<String, String> passwordDetails) {
            if (passwordDetails != null) {
                loginTime = passwordDetails.get(SAPUserExtensionAttributes.PASSWORD_DETAILS_LOGINTIME.scimName());
                failedLoginAttempts = Integer.parseInt(passwordDetails.get(SAPUserExtensionAttributes.PASSWORD_DETAILS_FAILEDLOGINATTEMPTS.scimName()));
                setTime = passwordDetails.get(SAPUserExtensionAttributes.PASSWORD_DETAILS_SETTIME.scimName());
                status = passwordDetails.get(SAPUserExtensionAttributes.PASSWORD_DETAILS_STATUS.scimName());
                policy = passwordDetails.get(SAPUserExtensionAttributes.PASSWORD_DETAILS_POLICY.scimName());

            }
        }
        public PasswordDetails build() {
            return new PasswordDetails(this);
        }
    }
}



