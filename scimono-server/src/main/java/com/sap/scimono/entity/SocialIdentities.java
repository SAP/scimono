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
 * Java class for SocialIdentities complex type
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class SocialIdentities implements Serializable {

    private static final long serialVersionUID = -1606777800095918850L;


    @JsonProperty(PASSWORDDETAILS_LOGINTIME)
    private final String id;
    @JsonProperty(PASSWORDDETAILS_FAILEDLOGINATTEMPTS)
    private final String dateoflinking;
    @JsonProperty(PASSWORDDETAILS_SETTIME)
    private final String socialProvider;


    @JsonCreator
    public SocialIdentities(String id, String dateoflinking, String socialProvider) {
        this.id = id;
        this.dateoflinking = dateoflinking;
        this.socialProvider = socialProvider;
    }

    private SocialIdentities(final Builder builder, String id, String dateOflinking, String socialProvider) {
        this.id = id;
        this.dateoflinking = dateOflinking;
        this.socialProvider = socialProvider;
    }


    @JsonIgnore
    public boolean isEmpty() {
        return Strings.isNullOrEmpty(id + dateoflinking + socialProvider);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + hash(id);
        result = prime * result + hash(dateoflinking);
        result = prime * result + hash(socialProvider);
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
        if (!(obj instanceof SocialIdentities)) {
            return false;
        }
        SocialIdentities other = (SocialIdentities) obj;
        if (!Objects.equals(id, other.id)) {
            return false;
        }
        if (!Objects.equals(dateoflinking, other.dateoflinking)) {
            return false;
        }
        return (!Objects.equals(socialProvider, other.socialProvider));
    }

    public static class Builder {
        private String id;
        private String socialProvider;
        private String dateofLinking;

        public SocialIdentities.Builder setId(String id) {
            this.id = id;
            return this;
        }

        public SocialIdentities.Builder setSocialProvider(String socialProvider) {
            this.socialProvider = socialProvider;
            return this;
        }

        public SocialIdentities.Builder setDateofLinking(String dateofLinking) {
            this.dateofLinking = dateofLinking;
            return this;
        }

        public Builder() {
            // Default constructor used to instantiate new ContactPreferences objects
        }

        public Builder(SocialIdentities contactPreferences) {
            // Default constructor used to instantiate new contactPreferences objects
            this.dateofLinking = contactPreferences.dateoflinking;
            this.id = contactPreferences.id;
            this.socialProvider = contactPreferences.socialProvider;
        }

        public Builder(final Map<String, String> contactPreferencesValues) {
            if (contactPreferencesValues != null) {
                id = contactPreferencesValues.get(SAPUserExtensionAttributes.SOCIAL_IDENTITIES_SOCIAL_ID.scimName());
                socialProvider = contactPreferencesValues.get(SAPUserExtensionAttributes.SOCIAL_IDENTITIES_SOCIAL_PROVIDER.scimName());
                dateofLinking = contactPreferencesValues.get(SAPUserExtensionAttributes.SOCIAL_IDENTITIES_DATE_OF_LINKING.scimName());
            }
        }

        public SocialIdentities build() {
            return new SocialIdentities(this, id, dateofLinking, socialProvider);
        }
    }
}



