package com.sap.scim.client;

import com.sap.scim.entity.User;
import com.sap.scim.entity.patch.PatchBody;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientRequestFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.sap.scim.client.ResourcePageQuery.identityPageQuery;
import static com.sap.scim.client.ResourcePageQuery.indexPageQuery;
import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.PATCH;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class SCIMResourceRequestInvocationTest {
  private static final String DEFAULT_URL = "http://localhost:7070/idds/scim/v2";

  @TestFactory
  public Collection<DynamicTest> readResourcesTests() {
    // @formatter:off
    return Arrays.asList(
        dynamicTest("Read all Users invocation", () -> {
          Collection<Executable> capturedAssertions = new ArrayList<>();
          assertThrows(ProcessingException.class,
              () -> scimRequestConfigForReadUsersInvocation(DEFAULT_URL + "/Users?startIndex=1&count=100", capturedAssertions)
                  .readAllUsers()
          );
          assertFalse(capturedAssertions.isEmpty());
          assertAll(capturedAssertions);
        }),
        dynamicTest("Read multiple Users invocation with default filter", () -> {
          Collection<Executable> capturedAssertions = new ArrayList<>();
          assertThrows(ProcessingException.class,
              ()-> scimRequestConfigForReadUsersInvocation(DEFAULT_URL + "/Users?startIndex=1&count=100", capturedAssertions)
                  .readMultipleUsers()
          );
          assertFalse(capturedAssertions.isEmpty());
          assertAll(capturedAssertions);
        }),
        dynamicTest("Read multiple Users invocation with index filter", () -> {
          Collection<Executable> capturedAssertions = new ArrayList<>();
          assertThrows(ProcessingException.class,
              () -> scimRequestConfigForReadUsersInvocation(DEFAULT_URL + "/Users?startIndex=4&count=50", capturedAssertions)
              .readMultipleUsers(indexPageQuery().withStartIndexAndCount(4, 50))
          );
          assertFalse(capturedAssertions.isEmpty());
          assertAll(capturedAssertions);
        }),
        dynamicTest("Read multiple Users invocation with identity filter", () -> {
          Collection<Executable> capturedAssertions = new ArrayList<>();
          assertThrows(ProcessingException.class,
              () -> scimRequestConfigForReadUsersInvocation(DEFAULT_URL + "/Users?startId=something&count=50", capturedAssertions)
              .readMultipleUsers(identityPageQuery().withStartIdAndCount("something", 50))
          );
          assertFalse(capturedAssertions.isEmpty());
          assertAll(capturedAssertions);
        })
    );
    // @formatter:on
  }

  @Test
  public void createResource() {
    List<Executable> capturedAssertions = new ArrayList<>();
    // @formatter:off
    UserRequest scimUserRequest = SCIMClientService.builder(DEFAULT_URL)
        .addResolver((ClientRequestFilter) clientRequestContext -> {
          capturedAssertions.add(() -> assertEquals(DEFAULT_URL + "/Users", clientRequestContext.getUri().toString()));
          capturedAssertions.add(() -> assertEquals(POST, clientRequestContext.getMethod()));
          capturedAssertions.add(() -> assertEquals(User.class, clientRequestContext.getEntityClass()));
        })
        .build()
        .buildUserRequest();
    // @formatter:on

    assertThrows(ProcessingException.class, () -> scimUserRequest.createUser(new User.Builder("someUsername").build()));
    assertAll(capturedAssertions);
  }

  @Test
  public void updateResourceWithPut() {
    String randomId = UUID.randomUUID().toString();
    List<Executable> capturedAssertions = new ArrayList<>();

    // @formatter:off
    UserRequest scimUserRequest = SCIMClientService.builder(DEFAULT_URL)
        .addResolver((ClientRequestFilter) clientRequestContext -> {
          capturedAssertions.add(() -> assertEquals(DEFAULT_URL + "/Users/" + randomId, clientRequestContext.getUri().toString()));
          capturedAssertions.add(() -> assertEquals(PUT, clientRequestContext.getMethod()));
          capturedAssertions.add(() -> assertEquals(User.class, clientRequestContext.getEntityClass()));
        })
        .build()
        .buildUserRequest();
    // @formatter:on

    assertThrows(ProcessingException.class, () -> scimUserRequest.updateUser(new User.Builder("someUsername").setId(randomId).build()));
    assertAll(capturedAssertions);
  }

  @Test
  public void updateResourceWithPatch() {
    String randomId = UUID.randomUUID().toString();
    List<Executable> capturedAssertions = new ArrayList<>();

    // @formatter:off
    UserRequest scimUserRequest = SCIMClientService.builder(DEFAULT_URL)
        .addResolver((ClientRequestFilter) clientRequestContext -> {
          capturedAssertions.add(() -> assertEquals(DEFAULT_URL+ "/Users/" + randomId, clientRequestContext.getUri().toString()));
          capturedAssertions.add(() -> assertEquals(PATCH, clientRequestContext.getMethod()));
          capturedAssertions.add(() -> assertEquals(PatchBody.class, clientRequestContext.getEntityClass()));
        })
        .build()
        .buildUserRequest();
    // @formatter:on
    assertThrows(ProcessingException.class, () -> scimUserRequest.patchUser(new PatchBody.Builder().build(), randomId));
    assertAll(capturedAssertions);
  }

  @Test
  public void deleteResource() {
    String randomId = UUID.randomUUID().toString();
    List<Executable> capturedAssertions = new ArrayList<>();

    // @formatter:off
    UserRequest scimUserRequest = SCIMClientService.builder(DEFAULT_URL)
        .addResolver((ClientRequestFilter) clientRequestContext -> {
          capturedAssertions.add(() -> assertEquals(DEFAULT_URL + "/Users/" + randomId, clientRequestContext.getUri().toString()));
          capturedAssertions.add(() -> assertEquals(DELETE, clientRequestContext.getMethod()));
          capturedAssertions.add(() -> assertNull(clientRequestContext.getEntityClass()));
        })
        .build()
        .buildUserRequest();
    // @formatter:on
    assertThrows(ProcessingException.class, () -> scimUserRequest.deleteUser(randomId));
    assertAll(capturedAssertions);
  }

  private UserRequest scimRequestConfigForReadUsersInvocation(String expectedFilter, Collection<Executable> capturedAssertions) {
    // @formatter:off
    return SCIMClientService
        .builder(DEFAULT_URL)
        .addResolver((ClientRequestFilter) clientRequestContext -> {
          capturedAssertions.add(() -> assertEquals(expectedFilter, clientRequestContext.getUri().toString()));
          capturedAssertions.add(() -> assertEquals(GET, clientRequestContext.getMethod()));
          capturedAssertions.add(() -> assertNull(clientRequestContext.getEntityClass()));
        })
        .build()
        .buildUserRequest();
    // @formatter:on
  }
}
