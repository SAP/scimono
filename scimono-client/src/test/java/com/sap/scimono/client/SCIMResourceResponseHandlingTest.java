package com.sap.scimono.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.sap.scimono.api.helper.ObjectMapperFactory;
import com.sap.scimono.client.query.ResourcePageQuery;
import com.sap.scimono.entity.ErrorResponse;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.paging.PagedResult;
import com.sap.scimono.entity.patch.PatchBody;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.client.ResourceAction.CREATE_SINGLE;
import static com.sap.scimono.client.ResourceAction.DELETE;
import static com.sap.scimono.client.ResourceAction.GET_ALL;
import static com.sap.scimono.client.ResourceAction.GET_SINGLE;
import static com.sap.scimono.client.ResourceAction.PATCH_UPDATE;
import static com.sap.scimono.client.ResourceAction.PUT_UPDATE;
import static com.sap.scimono.client.query.ResourcePageQuery.identityPageQuery;
import static com.sap.scimono.client.query.ResourcePageQuery.indexPageQuery;
import static java.util.UUID.randomUUID;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.NOT_IMPLEMENTED;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.glassfish.jersey.client.HttpUrlConnectorProvider.SET_METHOD_WORKAROUND;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class SCIMResourceResponseHandlingTest {
  private static final String DEFAULT_URL = "http://localhost:7070";
  private static final ObjectMapper JSON_OBJECT_MAPPER = ObjectMapperFactory.createObjectMapper();

  private WireMockServer wireMockServer;

  @BeforeEach
  public void setup() {
    wireMockServer = new WireMockServer(7070);
    wireMockServer.start();
  }

  @AfterEach
  public void closeResources() {
    wireMockServer.stop();
  }

  @Test
  public void testReadAllUsers() throws JsonProcessingException {
    List<User> users = generateUsers(200);

    // @formatter:off
    PagedByIndexSearchResult<User> firstPageResponseEntity =
        new PagedByIndexSearchResult<>(new PagedResult<>(200, users.stream().limit(100).collect(Collectors.toList())), 1);
    PagedByIndexSearchResult<User> secondPageResponseEntity =
        new PagedByIndexSearchResult<>(new PagedResult<>(200, users.stream().skip(100).limit(100).collect(Collectors.toList())), 1);

    wireMockServer.stubFor(get(urlEqualTo("/Users?startIndex=1&count=100"))
        .willReturn(configureMockedResponse(OK, APPLICATION_JSON_SCIM, firstPageResponseEntity)));

    wireMockServer.stubFor(get(urlEqualTo("/Users?startIndex=101&count=100"))
        .willReturn(configureMockedResponse(OK, APPLICATION_JSON_SCIM, secondPageResponseEntity)));

    SCIMClientService scimClientService = SCIMClientService.builder(DEFAULT_URL).build();
    final SCIMResponse<PagedByIndexSearchResult<User>> usersResult = scimClientService.buildUserRequest().readAllUsers();

    assertAll(() -> {
      assertTrue(usersResult.isSuccess());

      PagedByIndexSearchResult<User> usersPage = usersResult.get();
      assertAll(
          () -> assertEquals(200, usersPage.getTotalResults()),
          () -> assertEquals(1, usersPage.getStartIndex()),
          () -> assertEquals(200, usersPage.getItemsPerPage()),
          () -> assertEquals(200, usersPage.getResources().size())
      );
    });
    // @formatter:on
  }

  @Test
  public void testReadMultipleUsersDefaultFilter() throws JsonProcessingException {
    int totalUsersCount = 150;
    int returnedUsersCount = 100;
    // @formatter:off

    wireMockServer.stubFor(get(urlEqualTo("/Users?startIndex=1&count=100"))
            .willReturn(configureMockedResponse(OK, APPLICATION_JSON_SCIM,
                new PagedByIndexSearchResult<>(new PagedResult<>(totalUsersCount, generateUsers(returnedUsersCount)), 1))));

    final SCIMResponse<PagedByIndexSearchResult<User>> usersResult = SCIMClientService.builder(DEFAULT_URL).build()
        .buildUserRequest()
        .readMultipleUsers();

    assertAll(() -> {
      assertTrue(usersResult.isSuccess());

      PagedByIndexSearchResult<User> usersPage = usersResult.get();
      assertAll(
          () -> assertEquals(totalUsersCount, usersPage.getTotalResults()),
          () -> assertEquals(1, usersPage.getStartIndex()),
          () -> assertEquals(returnedUsersCount, usersPage.getItemsPerPage()),
          () -> assertEquals(returnedUsersCount, usersPage.getResources().size())
      );
    });
    // @formatter:on
  }

  @Test
  public void testReadMultipleUsersIndexFilter() throws JsonProcessingException {
    int totalUsersCount = 150;
    int returnedUsersCount = 50;
    int startIndex = 30;

    // @formatter:off
    wireMockServer.stubFor(get(urlEqualTo(String.format("/Users?startIndex=%d&count=%d", startIndex, returnedUsersCount)))
            .willReturn(configureMockedResponse(OK, APPLICATION_JSON_SCIM,
                new PagedByIndexSearchResult<>(new PagedResult<>(totalUsersCount, generateUsers(returnedUsersCount)), startIndex))));

    final SCIMResponse<PagedByIndexSearchResult<User>> usersResult = SCIMClientService.builder(DEFAULT_URL).build()
        .buildUserRequest()
        .readMultipleUsers(indexPageQuery().withStartIndex(startIndex).withCount(returnedUsersCount));

    assertAll(() -> {
      assertTrue(usersResult.isSuccess());

      PagedByIndexSearchResult<User> usersPage = usersResult.get();
      assertAll(
          () -> assertEquals(totalUsersCount, usersPage.getTotalResults()),
          () -> assertEquals(startIndex, usersPage.getStartIndex()),
          () -> assertEquals(returnedUsersCount, usersPage.getItemsPerPage()),
          () -> assertEquals(returnedUsersCount, usersPage.getResources().size())
      );
    });
    // @formatter:on
  }

  @Test
  public void testReadMultipleUsersIdentityFilter() throws JsonProcessingException {
    int totalUsersCount = 150;
    int returnedUsersCount = 100;
    String startId = "something";
    String nextId = "end";

    // @formatter:off
     wireMockServer.stubFor(get(urlEqualTo(String.format("/Users?startId=%s&count=%d", startId, returnedUsersCount)))
            .willReturn(configureMockedResponse(OK, APPLICATION_JSON_SCIM,
                new PagedByIdentitySearchResult<>(new PagedResult<>(totalUsersCount, generateUsers(returnedUsersCount)), returnedUsersCount, startId, nextId))));

    final SCIMResponse<PagedByIdentitySearchResult<User>> usersResult = SCIMClientService.builder(DEFAULT_URL).build()
        .buildUserRequest()
        .readMultipleUsers(ResourcePageQuery.identityPageQuery().withStartId(startId).withCount(returnedUsersCount));

    assertAll(() -> {
      assertTrue(usersResult.isSuccess());

      PagedByIdentitySearchResult<User> usersPage = usersResult.get();
      assertAll(
          () -> assertEquals(totalUsersCount, usersPage.getTotalResults()),
          () -> assertEquals(startId, usersPage.getStartId()),
          () -> assertEquals(nextId, usersPage.getNextId()),
          () -> assertEquals(returnedUsersCount, usersPage.getItemsPerPage()),
          () -> assertEquals(returnedUsersCount, usersPage.getResources().size())
      );
    });
    // @formatter:on
  }

  @TestFactory
  public Collection<DynamicTest> testReadMultipleUsersSCIMException() throws JsonProcessingException {
    // @formatter:off
    ResponseDefinitionBuilder stubResponse = configureMockedResponse(NOT_IMPLEMENTED, APPLICATION_JSON_SCIM,
                Collections.singleton(new ErrorResponse(NOT_IMPLEMENTED.getStatusCode(), null, "An internal error occurred.")));

    Consumer<SCIMResponse<?>> assertions = scimResponse -> {
       assertFalse(scimResponse.isSuccess());

      UnexpectedResponse error = scimResponse.getError();
      assertAll(
          () -> assertTrue(error.isSCIMError()),
          () -> assertFalse(error.asSCIMErrors().isEmpty())
      );
    };

    return createDynamicTestsForMultipleScimOperations(stubResponse, assertions);
    // @formatter:on
  }

  @TestFactory
  public Collection<DynamicTest> testScimRequestHandleUnexpectedResponseStatus() throws JsonProcessingException {
    ResponseDefinitionBuilder stubResponse = configureMockedResponse(NOT_IMPLEMENTED, APPLICATION_JSON_SCIM, JSON_OBJECT_MAPPER.createObjectNode());
    return createDynamicTestsForMultipleScimOperations(stubResponse, scimResponse -> assertFalse(scimResponse.isSuccess()));
  }

  @TestFactory
  public Collection<DynamicTest> testScimRequestHandleUnexpectedException() throws JsonProcessingException {
    // @formatter:off
    ResponseDefinitionBuilder stubResponse = configureMockedResponse(NOT_IMPLEMENTED, APPLICATION_JSON_SCIM,
        JSON_OBJECT_MAPPER.createObjectNode().set("attribute", new TextNode("error")));

    Consumer<SCIMResponse<?>> assertions = scimResponse -> {
       assertFalse(scimResponse.isSuccess());
       assertFalse(scimResponse.getError().isSCIMError());
    };

    return createDynamicTestsForMultipleScimOperations(stubResponse, assertions);
    // @formatter:on
  }

  @Test
  public void testCreateUserSuccessfulResponse() throws JsonProcessingException {
    String userId = randomUUID().toString();
    User user = new User.Builder("testUser").build();

    // @formatter:off
    wireMockServer.stubFor(post(anyUrl())
        .willReturn(configureMockedResponse(CREATED, APPLICATION_JSON_SCIM, new User.Builder(user).setId(userId).build())));

    SCIMResponse<User> response = SCIMClientService
        .builder(DEFAULT_URL)
        .addProperty(SET_METHOD_WORKAROUND, true)
        .build()
        .buildUserRequest()
        .createUser(user);

    assertAll(
        () -> assertTrue(response.isSuccess()),
        () -> assertDoesNotThrow(response::get)
    );
    // @formatter:on
  }

  @Test
  public void testUpdateUserSuccessfulResponse() throws JsonProcessingException {
    User user = new User.Builder("testUser").setId(randomUUID().toString()).build();
    wireMockServer.stubFor(put(anyUrl()).willReturn(configureMockedResponse(OK, APPLICATION_JSON_SCIM, user)));

    // @formatter:off
    SCIMResponse<User> response = SCIMClientService
        .builder(DEFAULT_URL)
        .addProperty(SET_METHOD_WORKAROUND, true)
        .build()
        .buildUserRequest()
        .updateUser(user);

    assertAll(
        () -> assertTrue(response.isSuccess()),
        () -> assertDoesNotThrow(response::get)
    );
    // @formatter:on
  }

  @Test
  public void testDeleteUserSuccessfulResponse() {
    wireMockServer.stubFor(delete(anyUrl()).willReturn(noContent()));
    // @formatter:off
    SCIMResponse<Void> response = SCIMClientService
        .builder(DEFAULT_URL)
        .build()
        .buildUserRequest()
        .deleteUser(randomUUID().toString());

    assertAll(
        () -> assertTrue(response.isSuccess()),
        () -> assertDoesNotThrow(response::get)
    );
    // @formatter:on
  }

  @Test
  public void testPatchUserSuccessfulResponse() {
    wireMockServer.stubFor(patch(anyUrl()).willReturn(noContent()));

    // @formatter:off
    SCIMResponse<Void> response = SCIMClientService
        .builder(DEFAULT_URL)
        .addProperty(SET_METHOD_WORKAROUND, true)
        .build()
        .buildUserRequest()
        .patchUser(new PatchBody.Builder().build(), randomUUID().toString());

    assertAll(
        () -> assertTrue(response.isSuccess()),
        () -> assertDoesNotThrow(response::get)
    );
    // @formatter:on
  }

  @TestFactory
  public Collection<DynamicTest> testScimOperationNonCompliantUserResponse() throws JsonProcessingException {
    // @formatter:off
    SCIMRequest.Builder requestBuilder = SCIMRequest.newBuilder()
        .setExpectedResponseStatuses(DELETE, OK, NOT_IMPLEMENTED)
        .setExpectedResponseStatuses(CREATE_SINGLE, NOT_IMPLEMENTED)
        .setExpectedResponseStatuses(GET_SINGLE, OK, NOT_IMPLEMENTED)
        .setExpectedResponseStatuses(GET_ALL, OK, NOT_IMPLEMENTED)
        .setExpectedResponseStatuses(PUT_UPDATE, OK, NOT_IMPLEMENTED)
        .setExpectedResponseStatuses(PATCH_UPDATE, OK, NOT_IMPLEMENTED);

    ResponseDefinitionBuilder stubResponse = configureMockedResponse(NOT_IMPLEMENTED, APPLICATION_JSON_SCIM,
        JSON_OBJECT_MAPPER.createObjectNode().set("attribute", new TextNode("error")));

    Consumer<SCIMResponse<?>> assertions = scimResponse -> {
       assertTrue(scimResponse.isSuccess());
       assertThrows(ProcessingException.class, scimResponse::get);
    };

    wireMockServer.stubFor(any(anyUrl()).willReturn(stubResponse));
    String testUserId = randomUUID().toString();
    User testUser = new User.Builder("user").setId(testUserId).build();

    UserRequest request = SCIMClientService
        .builder(DEFAULT_URL)
        .addProperty(SET_METHOD_WORKAROUND, true)
        .build()
        .buildUserRequest(requestBuilder);

    return Arrays.asList(
        dynamicTest("Read multiple Users index paging", () -> assertions.accept(request.readMultipleUsers(indexPageQuery()))),
        dynamicTest("Read multiple Users identity paging", () -> assertions.accept(request.readMultipleUsers(identityPageQuery()))),
        dynamicTest("Read multiple Users without paging", () -> assertions.accept(request.readMultipleUsersWithoutPaging())),
        dynamicTest("Create User", () -> assertions.accept(request.createUser(testUser))),
        dynamicTest("Update User", () -> assertions.accept(request.updateUser(testUser)))
    );
  }

  private Collection<DynamicTest> createDynamicTestsForMultipleScimOperations(ResponseDefinitionBuilder stubResponse, Consumer<SCIMResponse<?>> assertions) {
    wireMockServer.stubFor(any(anyUrl()).willReturn(stubResponse));

    String testUserId = randomUUID().toString();
    User testUser = new User.Builder("user").setId(testUserId).build();

    UserRequest request = SCIMClientService
        .builder(DEFAULT_URL)
        .addProperty(SET_METHOD_WORKAROUND, true)
        .build()
        .buildUserRequest();

    return Arrays.asList(
        dynamicTest("Read all Users", () -> assertions.accept(request.readAllUsers())),
        dynamicTest("Read multiple Users index paging", () -> assertions.accept(request.readMultipleUsers(indexPageQuery()))),
        dynamicTest("Read multiple Users identity paging", () -> assertions.accept(request.readMultipleUsers(identityPageQuery()))),
        dynamicTest("Read multiple Users without paging", () -> assertions.accept(request.readMultipleUsersWithoutPaging())),
        dynamicTest("Create User", () -> assertions.accept(request.createUser(testUser))),
        dynamicTest("Update User", () -> assertions.accept(request.updateUser(testUser))),
        dynamicTest("Patch User", () -> assertions.accept(request.patchUser(new PatchBody.Builder().build(), testUserId))),
        dynamicTest("Delete User", () -> assertions.accept(request.deleteUser(testUserId)))
    );
  }

  private List<User> generateUsers(int count) {
    // @formatter:off
    return IntStream.range(0, count)
        .mapToObj(value -> new User.Builder("User" + value).setId(randomUUID().toString()).build())
        .collect(Collectors.toList());
     // @formatter:on
  }

  private ResponseDefinitionBuilder configureMockedResponse(Response.Status responseStatus, String contentType, Object body)
      throws JsonProcessingException {
    // @formatter:off
    return aResponse()
        .withStatus(responseStatus.getStatusCode())
        .withHeader(CONTENT_TYPE, contentType)
        .withBody(JSON_OBJECT_MAPPER.writeValueAsString(body));
    // @formatter:on
  }
}
