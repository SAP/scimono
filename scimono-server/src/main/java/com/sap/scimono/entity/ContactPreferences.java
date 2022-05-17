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
 * Java class for ContactPreferences complex type
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class ContactPreferences implements Serializable {

    private static final long serialVersionUID = -1606197800095918850L;


    @JsonProperty(CONTACT_PREFERENCES_EMAIL)
    private final String contact_preferences_email;
    @JsonProperty(CONTACT_PREFERENCES_TELEPHONE)
    private final String contact_preferences_telephone;

    @JsonCreator
    public ContactPreferences(@JsonProperty(CONTACT_PREFERENCES_EMAIL) final String email, @JsonProperty(CONTACT_PREFERENCES_TELEPHONE) final String telephone) {
        this.contact_preferences_email = email;
        this.contact_preferences_telephone = telephone;
    }

    private ContactPreferences(final Builder builder) {
        contact_preferences_email = builder.contact_preferences_email;
        contact_preferences_telephone = builder.contact_preferences_telephone;
    }

    public String getEmail() {
        return contact_preferences_email;
    }

    public String getTelephone() {
        return contact_preferences_telephone;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return Strings.isNullOrEmpty(contact_preferences_email + contact_preferences_telephone);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + hash(contact_preferences_email);
        result = prime * result + hash(contact_preferences_telephone);
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
        if (!(obj instanceof com.sap.scimono.entity.ContactPreferences)) {
            return false;
        }
        ContactPreferences other = (ContactPreferences) obj;
        if (!Objects.equals(contact_preferences_email, other.contact_preferences_email)) {
            return false;
        }
        return (!Objects.equals(contact_preferences_telephone, other.contact_preferences_telephone));
    }

    public static class Builder {
        private String contact_preferences_email;
        private String contact_preferences_telephone;

        public Builder() {
            // Default constructor used to instantiate new ContactPreferences objects
        }

        public Builder(ContactPreferences contactPreferences) {
            // Default constructor used to instantiate new contactPreferences objects
            this.contact_preferences_email = contactPreferences.contact_preferences_email;
            this.contact_preferences_telephone = contactPreferences.contact_preferences_telephone;
        }

        public Builder(final Map<String, String> contactPreferencesValues) {
            if (contactPreferencesValues != null) {
                contact_preferences_email = contactPreferencesValues.get(SAPUserExtensionAttributes.CONTACT_PREFERENCES_EMAIL.scimName());
                contact_preferences_telephone = contactPreferencesValues.get(SAPUserExtensionAttributes.CONTACT_PREFERENCES_TELEPHONE.scimName());
            }
        }

        public ContactPreferences.Builder setEmail(final String email) {
            this.contact_preferences_email = email;
            return this;
        }

        public ContactPreferences.Builder setTelephone(final String telephone) {
            this.contact_preferences_telephone = telephone;
            return this;
        }

        public ContactPreferences build() {
            return new ContactPreferences(this);
        }
    }
}



