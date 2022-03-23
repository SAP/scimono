package com.sap.scimono.scim.system.tests;

import static com.sap.scimono.entity.definition.CoreUserAttributes.META_LAST_MODIFIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.scimono.entity.Email;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.scim.system.tests.extensions.GroupClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.GroupFailSafeClient;
import com.sap.scimono.scim.system.tests.extensions.UserClientScimResponseExtension;
import com.sap.scimono.scim.system.tests.extensions.UserFailSafeClient;
import com.sap.scimono.scim.system.tests.util.TestData;

public class DeltaReadResourcesTest extends SCIMHttpResponseCodeTest {

    private static final Logger logger = LoggerFactory.getLogger(DeltaReadResourcesTest.class);

    @RegisterExtension
    UserClientScimResponseExtension resourceAwareUserRequest = UserClientScimResponseExtension.forClearingAfterEachExecutions(userRequest);

    @RegisterExtension
    GroupClientScimResponseExtension resourceAwareGroupRequest = GroupClientScimResponseExtension.forClearingAfterEachExecutions(groupRequest);

    private final UserFailSafeClient userFailSafeClient = resourceAwareUserRequest.getFailSafeClient();
    private final GroupFailSafeClient groupFailSafeClient = resourceAwareGroupRequest.getFailSafeClient();

    @Test
    @DisplayName("Test filter users by meta.lastModified using gt operator")
    public void testFilterUsersByLastModifiedUsingGtOperator() throws InterruptedException {
        final User testUser = createUser("testDeltaReadUsers1");
        final String lastModifiedOfUser1 = testUser.getMeta().getLastModified().toString();

        List<User> filteredUsers = getUsersFilteredByLastModified(lastModifiedOfUser1);
        assertTrue(filteredUsers.isEmpty(), "No users expected if lastModified works. Users in response:" + filteredUsers);

        //Delay so the new user has different lastModified time.
        TimeUnit.SECONDS.sleep(1);

        User testUser2 = createUser("testDeltaReadUsers2");

        filteredUsers = getUsersFilteredByLastModified(lastModifiedOfUser1);
        assertFalse(filteredUsers.isEmpty(), "There should be one user in the response");
        assertEquals(1, filteredUsers.size(), "There should be one user in the response");
        User filteredUserInResponse = filteredUsers.get(0);
        assertEquals(testUser2, filteredUserInResponse, "Filtering by lastModified should return newly created user.");

    }

    @Test
    @DisplayName("Test filter groups by meta.lastModified using gt operator")
    public void testFilterGroupsByLastModifiedUsingGtOperator() throws InterruptedException {
        final Group testGroup = createGroup("testDeltaReadGroups1");
        final String lastModifiedOfGroup1 = testGroup.getMeta().getLastModified().toString();

        List<Group> filteredGroups = getGroupsFilteredByLastModified(lastModifiedOfGroup1);
        assertTrue(filteredGroups.isEmpty(), "No groups expected if lastModified works. Groups in response:" + filteredGroups);

        //Delay so the new group has different lastModified time.
        TimeUnit.SECONDS.sleep(1);

        Group testGroup2 = createGroup("testDeltaReadGroups2");

        filteredGroups = getGroupsFilteredByLastModified(lastModifiedOfGroup1);
        assertFalse(filteredGroups.isEmpty(), "There should be one group in the response");
        assertEquals(1, filteredGroups.size(), "There should be one group in the response");
        Group filteredGroupInResponse = filteredGroups.get(0);
        assertEquals(testGroup2, filteredGroupInResponse, "Filtering by lastModified should return newly created group.");

    }

    private List<User> getUsersFilteredByLastModified(String lastModifiedUser) {
        return userFailSafeClient.getAllByFilter(generateFilterQueryGreaterThan(META_LAST_MODIFIED.relativePath(), lastModifiedUser));
    }

    private List<Group> getGroupsFilteredByLastModified(String lastModifiedGroup) {
        return groupFailSafeClient.getAllByFilter(generateFilterQueryGreaterThan(META_LAST_MODIFIED.relativePath(), lastModifiedGroup));
    }

    private User createUser(String userName) {
        logger.info("Creating User: {}", userName);
        Email email = buildPersonalEmail(userName);
        return userFailSafeClient.create(TestData.setAttributesToATestUser(userName).addEmails(Collections.singletonList(email)).build());
    }

    private Group createGroup(String displayName) {
        logger.info("Creating Group: {}", displayName);
        return groupFailSafeClient.create(TestData.buildGroup(displayName));
    }

    public static Email buildPersonalEmail(String emailValue) {
        // @formatter:off
        return new Email.Builder()
                .setDisplay("emailValue")
                .setValue(emailValue + "@example.com")
                .setType(Email.Type.HOME)
                .setPrimary(true)
                .build();
        // @formatter:on
    }

    private String generateFilterQueryGreaterThan(final String attributeName, final String attributeValue) {
        return String.format("%s gt \"%s\"", attributeName, attributeValue);
    }

}
