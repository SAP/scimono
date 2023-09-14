package com.sap.scimono.client;

import com.sap.scimono.entity.User;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.exception.InvalidFilterException;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientRequestFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.sap.scimono.client.query.ResourcePageQuery.identityPageQuery;
import static com.sap.scimono.client.query.ResourcePageQuery.indexPageQuery;
import static java.net.URLEncoder.encode;
import static jakarta.ws.rs.HttpMethod.DELETE;
import static jakarta.ws.rs.HttpMethod.GET;
import static jakarta.ws.rs.HttpMethod.PATCH;
import static jakarta.ws.rs.HttpMethod.POST;
import static jakarta.ws.rs.HttpMethod.PUT;
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
        readAllUsersInvocation(),
        readAllUsersWithFilterInvocation(),
        readMultipleUsersInvocationDefaultPaging(),
        readMultipleUsersInvocationDefaultPagingAndFilter(),
        readMultipleUsersInvocationWithIndexPaging(),
        readMultipleUsersInvocationWithIndexPagingAndFilter(),
        readMultipleUsersInvocationWithIdentityPaging(),
        readMultipleUsersInvocationWithIdentityPagingAndFilter(),
        readMultipleUsersInvocationWithoutPaging(),
        readMultipleUsersInvocationWithFilterAndWithoutPaging(),
        readMultipleUsersInvocationWithRequestedAttributes()
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

  @ParameterizedTest(name = "Test reading users with incorrect filter - {0} ")
  @ValueSource(strings = {"userName prs", "userName eqss \"something\"", "userName pr andsss emails.value pr"})
  public void readResourcesWithIncorrectFilter(String filter) {
    UserRequest scimUserRequest = SCIMClientService.builder(DEFAULT_URL).build().buildUserRequest();

    assertAll(
        () -> assertThrows(InvalidFilterException.class, () -> scimUserRequest.readMultipleUsers(filter)),
        () -> assertThrows(InvalidFilterException.class, () -> scimUserRequest.readMultipleUsersWithoutPaging(filter)),
        () -> assertThrows(InvalidFilterException.class, () -> scimUserRequest.readAllUsers(filter)),
        () -> assertThrows(InvalidFilterException.class, () -> scimUserRequest.readMultipleUsers(indexPageQuery(), filter)),
        () -> assertThrows(InvalidFilterException.class, () -> scimUserRequest.readMultipleUsers(identityPageQuery(), filter))
    );
  }

  @ParameterizedTest(name = "Test reading users with valid filter - {0} ")
  @ValueSource(strings = {"userName pr", "userName eq \"something\"", "userName pr and emails.value pr" })
  public void readResourcesWithValidFilter(String filter) {
    UserRequest scimUserRequest = SCIMClientService.builder(DEFAULT_URL).build().buildUserRequest();

    assertAll(
        () -> assertThrows(ProcessingException.class, () -> scimUserRequest.readMultipleUsers(filter)),
        () -> assertThrows(ProcessingException.class, () -> scimUserRequest.readMultipleUsersWithoutPaging(filter)),
        () -> assertThrows(ProcessingException.class, () -> scimUserRequest.readAllUsers(filter)),
        () -> assertThrows(ProcessingException.class, () -> scimUserRequest.readMultipleUsers(indexPageQuery(), filter)),
        () -> assertThrows(ProcessingException.class, () -> scimUserRequest.readMultipleUsers(identityPageQuery(), filter))
    );
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

  private DynamicTest readAllUsersInvocation() {
    return dynamicTest("Read all Users invocation", () -> {
      Collection<Executable> capturedAssertions = new ArrayList<>();
      UserRequest userRequest = scimRequestConfigForReadUsersInvocation(DEFAULT_URL + "/Users?startIndex=1&count=100", capturedAssertions);

      assertThrows(ProcessingException.class, userRequest::readAllUsers);
      assertFalse(capturedAssertions.isEmpty());
      assertAll(capturedAssertions);
    });
  }

  private DynamicTest readAllUsersWithFilterInvocation() {
    return dynamicTest("Read all Users with filter invocation", () -> {
      Collection<Executable> capturedAssertions = new ArrayList<>();
      String filter = "userName eq \"something\"";
      String path = DEFAULT_URL + "/Users?startIndex=1&count=100&filter=" + encode(filter, "UTF-8");
      UserRequest userRequest = scimRequestConfigForReadUsersInvocation(path, capturedAssertions);

      assertThrows(ProcessingException.class, () -> userRequest.readAllUsers(filter));
      assertFalse(capturedAssertions.isEmpty());
      assertAll(capturedAssertions);
    });
  }

  private DynamicTest readMultipleUsersInvocationDefaultPaging() {
    return dynamicTest("Read multiple Users invocation with default paging", () -> {
      Collection<Executable> capturedAssertions = new ArrayList<>();
      String path = DEFAULT_URL + "/Users?startIndex=1&count=100";
      UserRequest userRequest = scimRequestConfigForReadUsersInvocation(path, capturedAssertions);

      assertThrows(ProcessingException.class, userRequest::readMultipleUsers);
      assertFalse(capturedAssertions.isEmpty());
      assertAll(capturedAssertions);
    });
  }

  private DynamicTest readMultipleUsersInvocationWithRequestedAttributes() {
    return dynamicTest("Read multiple Users invocation with requested and excluded attributes", () -> {
      Collection<Executable> capturedAssertions = new ArrayList<>();
      String expectedFilter = DEFAULT_URL + "/Users?attributes=displayName&excludedAttributes=groups";
      UserRequest userRequest = scimRequestConfigForReadUsersInvocation(expectedFilter, capturedAssertions);

      assertThrows(ProcessingException.class,
          ()-> userRequest.readMultipleUsers(RequestDetails.builder()
              .requestAttribute("displayName")
              .excludeAttribute("groups")
              .build())
      );
      assertFalse(capturedAssertions.isEmpty());
      assertAll(capturedAssertions);
    });
  }

  private DynamicTest readMultipleUsersInvocationDefaultPagingAndFilter() {
    return dynamicTest("Read multiple Users invocation with default paging and filter", () -> {
      Collection<Executable> capturedAssertions = new ArrayList<>();
      String filter = "userName eq \"something\"";
      String path = DEFAULT_URL + "/Users?startIndex=1&count=100&filter=" + encode(filter, "UTF-8");
      UserRequest userRequest = scimRequestConfigForReadUsersInvocation(path, capturedAssertions);

      assertThrows(ProcessingException.class, () -> userRequest.readMultipleUsers(filter));
      assertFalse(capturedAssertions.isEmpty());
      assertAll(capturedAssertions);
    });
  }


  private DynamicTest readMultipleUsersInvocationWithIndexPaging() {
    return dynamicTest("Read multiple Users invocation with index paging", () -> {
      Collection<Executable> capturedAssertions = new ArrayList<>();
      String path = DEFAULT_URL + "/Users?startIndex=4&count=50";
      UserRequest userRequest = scimRequestConfigForReadUsersInvocation(path, capturedAssertions);

      assertThrows(ProcessingException.class, () -> userRequest.readMultipleUsers(indexPageQuery().withStartIndex(4).withCount(50)));
      assertFalse(capturedAssertions.isEmpty());
      assertAll(capturedAssertions);
    });
  }

  private DynamicTest readMultipleUsersInvocationWithIndexPagingAndFilter() {
    return dynamicTest("Read multiple Users invocation with index paging and filter", () -> {
      Collection<Executable> capturedAssertions = new ArrayList<>();
      String filter = "userName eq \"something\"";
      String path = DEFAULT_URL + "/Users?startIndex=4&count=50&filter=" + encode(filter, "UTF-8");
      UserRequest userRequest = scimRequestConfigForReadUsersInvocation(path, capturedAssertions);

      assertThrows(ProcessingException.class, () -> userRequest.readMultipleUsers(indexPageQuery().withStartIndex(4).withCount(50), filter));
      assertFalse(capturedAssertions.isEmpty());
      assertAll(capturedAssertions);
    });
  }

  private DynamicTest readMultipleUsersInvocationWithIdentityPaging() {
    return dynamicTest("Read multiple Users invocation with identity paging", () -> {
      Collection<Executable> capturedAssertions = new ArrayList<>();
      String path = DEFAULT_URL + "/Users?startId=something&count=50";
      UserRequest userRequest = scimRequestConfigForReadUsersInvocation(path, capturedAssertions);

      assertThrows(ProcessingException.class, () -> userRequest.readMultipleUsers(identityPageQuery().withStartId("something").withCount(50)));
      assertFalse(capturedAssertions.isEmpty());
      assertAll(capturedAssertions);
    });
  }

  private DynamicTest readMultipleUsersInvocationWithIdentityPagingAndFilter() {
    return dynamicTest("Read multiple Users invocation with identity paging and filter", () -> {
      Collection<Executable> capturedAssertions = new ArrayList<>();
      String filter = "userName eq \"something\"";
      String path = DEFAULT_URL + "/Users?startId=something&count=50&filter=" + encode(filter, "UTF-8");
      UserRequest userRequest = scimRequestConfigForReadUsersInvocation(path, capturedAssertions);

      assertThrows(ProcessingException.class,
          () -> userRequest.readMultipleUsers(identityPageQuery().withStartId("something").withCount(50), filter)
      );
      assertFalse(capturedAssertions.isEmpty());
      assertAll(capturedAssertions);
    });
  }

  private DynamicTest readMultipleUsersInvocationWithoutPaging() {
    return dynamicTest("Read multiple Users invocation without paging", () -> {
      Collection<Executable> capturedAssertions = new ArrayList<>();
      UserRequest userRequest = scimRequestConfigForReadUsersInvocation(DEFAULT_URL + "/Users", capturedAssertions);

      assertThrows(ProcessingException.class, userRequest::readMultipleUsersWithoutPaging);
      assertFalse(capturedAssertions.isEmpty());
      assertAll(capturedAssertions);
    });
  }

  private DynamicTest readMultipleUsersInvocationWithFilterAndWithoutPaging() {
    return dynamicTest("Read multiple Users invocation with filter and without paging", () -> {
      Collection<Executable> capturedAssertions = new ArrayList<>();
      String filter = "userName eq \"something\"";
      String path = DEFAULT_URL + "/Users?filter=" + encode(filter, "UTF-8");
      UserRequest userRequest = scimRequestConfigForReadUsersInvocation(path, capturedAssertions);

      assertThrows(ProcessingException.class, () -> userRequest.readMultipleUsersWithoutPaging(filter));
      assertFalse(capturedAssertions.isEmpty());
      assertAll(capturedAssertions);
    });
  }
}
