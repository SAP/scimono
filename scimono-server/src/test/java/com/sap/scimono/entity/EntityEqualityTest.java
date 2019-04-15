
package com.sap.scimono.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class EntityEqualityTest {

    @Test
    public void testSameValueSameTypePhoneEquality() {
        PhoneNumber.Builder phone1 = new PhoneNumber.Builder();
        phone1.setValue("+35988888");
        phone1.setType(PhoneNumber.Type.HOME);

        PhoneNumber.Builder phone2 = new PhoneNumber.Builder();
        phone2.setValue("+35988888");
        phone2.setType(PhoneNumber.Type.HOME);

        assertEquals(phone1.build(), phone2.build());
    }

    @Test
    public void testSameValueDifferentTypePhoneEquality() {
        PhoneNumber.Builder phone1 = new PhoneNumber.Builder();
        phone1.setValue("+35988888");
        phone1.setType(PhoneNumber.Type.HOME);

        PhoneNumber.Builder phone2 = new PhoneNumber.Builder();
        phone2.setValue("+35988888");
        phone2.setType(PhoneNumber.Type.MOBILE);

        assertNotEquals(phone1.build(), phone2.build());
    }

    @Test
    public void testSameValueNoTypePhoneEquality() {
        PhoneNumber.Builder phone1 = new PhoneNumber.Builder();
        phone1.setValue("+35988888");
        phone1.setType(PhoneNumber.Type.HOME);

        PhoneNumber.Builder phone2 = new PhoneNumber.Builder();
        phone2.setValue("+35988888");

        assertNotEquals(phone1.build(), phone2.build());
    }

    @Test
    public void testSameValueSameTypeAddressEquality() {
        Address.Builder address1 = new Address.Builder();
        address1.setStreetAddress("tsarigradsko shose");
        address1.setType(Address.Type.HOME);

        Address.Builder address2 = new Address.Builder();
        address2.setStreetAddress("tsarigradsko shose");
        address2.setType(Address.Type.HOME);

        assertEquals(address1.build(), address2.build());
    }

    @Test
    public void testSameValueDifferentTypeAddressEquality() {
        Address.Builder address1 = new Address.Builder();
        address1.setStreetAddress("tsarigradsko shose");
        address1.setType(Address.Type.HOME);

        Address.Builder address2 = new Address.Builder();
        address2.setStreetAddress("tsarigradsko shose");
        address2.setType(Address.Type.WORK);

        assertNotEquals(address1.build(), address2.build());
    }

    @Test
    public void testSameValueNoTypeAddressEquality() {
        Address.Builder address1 = new Address.Builder();
        address1.setStreetAddress("tsarigradsko shose");
        address1.setType(Address.Type.HOME);

        Address.Builder address2 = new Address.Builder();
        address2.setStreetAddress("tsarigradsko shose");

        assertNotEquals(address1.build(), address2.build());
    }

    @Test
    public void testSameValueSameTypeEntitlementEquality() {
        Entitlement.Builder entitlement1 = new Entitlement.Builder();
        entitlement1.setValue("POWER_USER");
        entitlement1.setType(Entitlement.Type.of("standard"));

        Entitlement.Builder entitlement2 = new Entitlement.Builder();
        entitlement2.setValue("POWER_USER");
        entitlement2.setType(Entitlement.Type.of("standard"));

        assertEquals(entitlement1.build(), entitlement2.build());
    }

    @Test
    public void testSameValueDifferentTypeEntitlementEquality() {
        Entitlement.Builder entitlement1 = new Entitlement.Builder();
        entitlement1.setValue("POWER_USER");
        entitlement1.setType(Entitlement.Type.of("standard"));

        Entitlement.Builder entitlement2 = new Entitlement.Builder();
        entitlement2.setValue("POWER_USER");
        entitlement2.setType(Entitlement.Type.of("custom"));

        assertNotEquals(entitlement1.build(), entitlement2.build());
    }

    @Test
    public void testSameValueNoTypeEntitlementEquality() {
        Entitlement.Builder entitlement1 = new Entitlement.Builder();
        entitlement1.setValue("POWER_USER");
        entitlement1.setType(Entitlement.Type.of("standard"));

        Entitlement.Builder entitlement2 = new Entitlement.Builder();
        entitlement2.setValue("POWER_USER");

        assertNotEquals(entitlement1.build(), entitlement2.build());
    }

    @Test
    public void testSameValueSameTypePhotoEquality() {
        Photo.Builder photo1 = new Photo.Builder();
        photo1.setValue(URI.create("http://abc"));
        photo1.setType(Photo.Type.PHOTO);

        Photo.Builder photo2 = new Photo.Builder();
        photo2.setValue(URI.create("http://abc"));
        photo2.setType(Photo.Type.PHOTO);

        assertEquals(photo1.build(), photo2.build());
    }

    @Test
    public void testSameValueDifferentTypePhotoEquality() {
        Photo.Builder photo1 = new Photo.Builder();
        photo1.setValue(URI.create("http://abc"));
        photo1.setType(Photo.Type.PHOTO);

        Photo.Builder photo2 = new Photo.Builder();
        photo2.setValue(URI.create("http://abc"));
        photo2.setType(Photo.Type.THUMBNAIL);

        assertNotEquals(photo1.build(), photo2.build());
    }

    @Test
    public void testSameValueNoTypePhotoEquality() {
        Photo.Builder photo1 = new Photo.Builder();
        photo1.setValue(URI.create("http://abc"));
        photo1.setType(Photo.Type.PHOTO);

        Photo.Builder photo2 = new Photo.Builder();
        photo2.setValue(URI.create("http://abc"));

        assertNotEquals(photo1.build(), photo2.build());
    }

    @Test
    public void testSameValueSameTypeRoleEquality() {
        Role.Builder role1 = new Role.Builder();
        role1.setValue("USER");
        role1.setType(Role.Type.of("standard"));

        Role.Builder role2 = new Role.Builder();
        role2.setValue("USER");
        role2.setType(Role.Type.of("standard"));

        Assertions.assertEquals(role1.build(), role2.build());
    }

    @Test
    public void testSameValueDifferentTypeRoleEquality() {
        Role.Builder role1 = new Role.Builder();
        role1.setValue("USER");
        role1.setType(Role.Type.of("standard"));

        Role.Builder role2 = new Role.Builder();
        role2.setValue("USER");
        role2.setType(Role.Type.of("custom"));

        assertNotEquals(role1.build(), role2.build());
    }

    @Test
    public void testSameValueNoTypeRoleEquality() {
        Role.Builder role1 = new Role.Builder();
        role1.setValue("USER");
        role1.setType(Role.Type.of("standard"));

        Role.Builder role2 = new Role.Builder();
        role2.setValue("USER");

        assertNotEquals(role1.build(), role2.build());
    }

    @Test
    public void testSameValueSameTypeCertificateEquality() {
        X509Certificate.Builder certificate1 = new X509Certificate.Builder();
        certificate1.setValue("certificate");
        certificate1.setType(X509Certificate.Type.of("standard"));

        X509Certificate.Builder certificate2 = new X509Certificate.Builder();
        certificate2.setValue("certificate");
        certificate2.setType(X509Certificate.Type.of("standard"));

        assertEquals(certificate1.build(), certificate2.build());
    }

    @Test
    public void testSameValueDifferentTypeCertificateEquality() {
        X509Certificate.Builder certificate1 = new X509Certificate.Builder();
        certificate1.setValue("certificate");
        certificate1.setType(X509Certificate.Type.of("standard"));

        X509Certificate.Builder certificate2 = new X509Certificate.Builder();
        certificate2.setValue("certificate");
        certificate2.setType(X509Certificate.Type.of("custom"));

        assertNotEquals(certificate1.build(), certificate2.build());
    }

    @Test
    public void testSameValueNoTypeCertificateEquality() {
        X509Certificate.Builder certificate1 = new X509Certificate.Builder();
        certificate1.setValue("certificate");
        certificate1.setType(X509Certificate.Type.of("standard"));

        X509Certificate.Builder certificate2 = new X509Certificate.Builder();
        certificate2.setValue("certificate");

        assertNotEquals(certificate1.build(), certificate2.build());
    }


}
