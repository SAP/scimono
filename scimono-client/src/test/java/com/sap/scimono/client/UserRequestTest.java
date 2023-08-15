package com.sap.scimono.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scimono.client.query.ResourcePageQuery.identityPageQuery;
import static com.sap.scimono.client.query.ResourcePageQuery.indexPageQuery;
import static com.sap.scimono.client.util.ResponseBodyAttributeExistenceAssertions.getCommonResourceAssertions;
import static com.sap.scimono.client.util.ResponseBodyAttributeExistenceAssertions.getPagedByIdentityResponseAssertions;
import static com.sap.scimono.client.util.ResponseBodyAttributeExistenceAssertions.getPagedByIndexResponseAssertions;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.glassfish.jersey.client.HttpUrlConnectorProvider.SET_METHOD_WORKAROUND;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.sap.scimono.api.helper.ObjectMapperFactory;
import com.sap.scimono.entity.Address;
import com.sap.scimono.entity.Email;
import com.sap.scimono.entity.Im;
import com.sap.scimono.entity.Name;
import com.sap.scimono.entity.PhoneNumber;
import com.sap.scimono.entity.Photo;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;

public class UserRequestTest {
  private static final String DEFAULT_URL = "http://localhost:7070";
  private static final ObjectMapper JSON_OBJECT_MAPPER = ObjectMapperFactory.createObjectMapper();
  private static final String SCIM_JSON_RESOURCES_ROOT = "/crud/user/";
  private static final String REQUEST_ATTR = "request";
  private static final String RESPONSE_ATTR = "response";

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
  public void testReadUserRequiringUserName() throws IOException {
    String sampleUserId = UUID.randomUUID().toString();
    JsonNode jsonResponse = readResponseUserAsJsonNodeFromFile("read_user_without_user_name.json");

    wireMockServer.stubFor(get(urlEqualTo("/Users/" + sampleUserId)).willReturn(configureMockedResponse(OK, jsonResponse)));

    // @formatter:off
    User responseUser = SCIMClientService.builder(DEFAULT_URL)
        .setUserPropertiesConfiguration(new UserAttributesConfiguration().setUserNameOptional(true))
        .build()
        .buildUserRequest()
        .readSingleUser(sampleUserId)
        .get();

    assertAll(getAllUserAttributeAssertionsWithoutUserName(responseUser));
    // @formatter:on
  }
  
  @Test
  public void testReadUserWithoutRequiringUserName() throws IOException {
    String sampleUserId = UUID.randomUUID().toString();
    JsonNode jsonResponse = readResponseUserAsJsonNodeFromFile("read_user_without_user_name.json");

    wireMockServer.stubFor(get(urlEqualTo("/Users/" + sampleUserId)).willReturn(configureMockedResponse(OK, jsonResponse)));

    // @formatter:off
    assertThrows(ProcessingException.class, () ->
    SCIMClientService.builder(DEFAULT_URL)
        .build()
        .buildUserRequest()
        .readSingleUser(sampleUserId)
        .get());
    // @formatter:on
  }

  @Test
  public void testReadUsersWithPagedByIdentityResponse() throws IOException {
    JsonNode jsonResponse = readResponseUserAsJsonNodeFromFile("read_users_identity_paging.json");
    String startId = "aaaa";

    wireMockServer.stubFor(get(urlEqualTo(String.format("/Users?startId=%s&count=100", startId))).willReturn(configureMockedResponse(OK, jsonResponse)));

    // @formatter:off
    SCIMResponse<PagedByIdentitySearchResult<User>> resp = SCIMClientService.builder(DEFAULT_URL)
        .build()
        .buildUserRequest()
        .readMultipleUsers(identityPageQuery().withStartId(startId).withCount(100));
    PagedByIdentitySearchResult<User>  receivedResponse = resp.get();

    assertAll(
        () -> assertAll(getPagedByIdentityResponseAssertions(receivedResponse)),
        () -> {
          User user = receivedResponse.getResources().stream().findAny().orElseThrow(RuntimeException::new);
          assertAll("Common resourceAttributes", getCommonResourceAssertions(user, User.SCHEMA));
          assertAll(
              () -> assertNotNull(user.getUserName()),
              () -> assertNotNull(user.getDisplayName()),
              () -> assertNotNull(user.isActive())
          );
        }
    );
    // @formatter:on
  }

  @TestFactory
  public Collection<DynamicTest> testReadUsersWithIndexPagedResponse() throws IOException {
    return Arrays.asList(
        dynamicTest("read All Users", getUsersIndexPagingExecutable("/Users?startIndex=1&count=100",
            UserRequest::readAllUsers)),
        dynamicTest("read multiple Users default index paging",
            getUsersIndexPagingExecutable("/Users?startIndex=1&count=100", UserRequest::readMultipleUsers)),
        dynamicTest("read multiple Users custom index paging", getUsersIndexPagingExecutable("/Users?startIndex=3&count=40",
            scimUserRequest -> scimUserRequest.readMultipleUsers(indexPageQuery().withStartIndex(3).withCount(40)))),
        dynamicTest("read multiple Users without adding page parameters", getUsersIndexPagingExecutable("/Users",
            UserRequest::readMultipleUsersWithoutPaging))
    );
  }

  @Test
  public void readSingleUser() throws IOException {
    String sampleUserId = UUID.randomUUID().toString();
    JsonNode jsonResponse = readResponseUserAsJsonNodeFromFile("read_user.json");

    wireMockServer.stubFor(get(urlEqualTo("/Users/" + sampleUserId)).willReturn(configureMockedResponse(OK, jsonResponse)));

    // @formatter:off
    User responseUser = SCIMClientService.builder(DEFAULT_URL)
        .build()
        .buildUserRequest()
        .readSingleUser(sampleUserId)
        .get();

    assertAll(getAllUserAttributeAssertions(responseUser));
    // @formatter:on
  }

  @Test
  public void createUser() throws IOException {
    String userJsonDataFileName = "create_user.json";
    User user = readRequestObjectFromFile(userJsonDataFileName, User.class);
    JsonNode jsonResponse = readResponseUserAsJsonNodeFromFile(userJsonDataFileName);

    wireMockServer.stubFor(post(urlEqualTo("/Users")).willReturn(configureMockedResponse(CREATED, jsonResponse)));

    // @formatter:off
    User responseUser = SCIMClientService.builder(DEFAULT_URL)
        .build()
        .buildUserRequest()
        .createUser(user)
        .get();

    assertAll(getAllUserAttributeAssertions(responseUser));
    // @formatter:on
  }

  @Test
  public void patchUser() throws IOException {
    String sampleUserId = UUID.randomUUID().toString();
    PatchBody patchBody = readRequestObjectFromFile("patch_user.json", PatchBody.class);

    wireMockServer.stubFor(patch(urlEqualTo("/Users/" + sampleUserId)).willReturn(noContent()));

    // @formatter:off
    SCIMResponse<Void> response = SCIMClientService.builder(DEFAULT_URL)
        .addProperty(SET_METHOD_WORKAROUND, true)
        .build()
        .buildUserRequest()
        .patchUser(patchBody, sampleUserId);

    assertTrue(response.isSuccess());
    // @formatter:on
  }

  @Test
  public void updateUser() throws IOException {
    String userJsonDataFileName = "put_user.json";
    User user = readRequestObjectFromFile(userJsonDataFileName, User.class);
    JsonNode jsonResponse = readResponseUserAsJsonNodeFromFile(userJsonDataFileName);

    wireMockServer.stubFor(put(urlEqualTo("/Users/" + user.getId())).willReturn(configureMockedResponse(OK, jsonResponse)));

    // @formatter:off
    User responseUser = SCIMClientService.builder(DEFAULT_URL)
        .build()
        .buildUserRequest()
        .updateUser(user)
        .get();

    assertAll(getAllUserAttributeAssertions(responseUser));
    // @formatter:on
  }

  private <T> T readRequestObjectFromFile(String fileName, Class<T> pojoClass) throws IOException {
    // @formatter:off
    JsonNode userJsonNode = JSON_OBJECT_MAPPER
        .readValue(this.getClass().getResourceAsStream(SCIM_JSON_RESOURCES_ROOT + fileName), ObjectNode.class)
        .get(REQUEST_ATTR);
    // @formatter:on

    return JSON_OBJECT_MAPPER.treeToValue(userJsonNode, pojoClass);
  }

  private JsonNode readResponseUserAsJsonNodeFromFile(String fileName) throws IOException {
    // @formatter:off
    return JSON_OBJECT_MAPPER
        .readValue(this.getClass().getResourceAsStream(SCIM_JSON_RESOURCES_ROOT + fileName), ObjectNode.class)
        .get(RESPONSE_ATTR);
    // @formatter:on
  }

  private Executable getUsersIndexPagingExecutable(String requestUrl,
      Function<UserRequest, SCIMResponse<PagedByIndexSearchResult<User>>> responseFunction) {
    return () -> {
      JsonNode jsonResponse = readResponseUserAsJsonNodeFromFile("read_users_index_paging.json");
      wireMockServer.stubFor(get(urlEqualTo(requestUrl)).willReturn(configureMockedResponse(OK, jsonResponse)));

      // @formatter:off
      SCIMResponse<PagedByIndexSearchResult<User>> resp = responseFunction.apply(SCIMClientService.builder(DEFAULT_URL).build().buildUserRequest());
      PagedByIndexSearchResult<User>  receivedResponse = resp.get();
      assertAll(
        () -> assertAll(getPagedByIndexResponseAssertions(receivedResponse)),
        () -> {
          User user = receivedResponse.getResources().stream().findAny().orElseThrow(RuntimeException::new);
          assertAll("Common resourceAttributes", getCommonResourceAssertions(user, User.SCHEMA));
          assertAll(
              () -> assertNotNull(user.getUserName()),
              () -> assertNotNull(user.getDisplayName()),
              () -> assertNotNull(user.isActive())
          );
        }
      );
    };
    // @formatter:on
  }

  private List<Executable> getAllUserAttributeAssertions(User user) {
    List<Executable> assertions = getAllUserAttributeAssertionsWithoutUserName(user);
    assertions.add(() -> assertNotNull(user.getUserName()));
    
    return assertions;
    
  }

  private List<Executable> getAllUserAttributeAssertionsWithoutUserName(User user) {
    List<Executable> assertions = new ArrayList<>(getCommonResourceAssertions(user, User.SCHEMA));

    assertions.add(() -> assertNotNull(user.getName()));
    assertions.add(() -> assertNotNull(user.isActive()));
    assertions.add(() -> {
      Name name = user.getName();
      assertAll(
          () -> assertNotNull(name.getGivenName()),
          () -> assertNotNull(name.getMiddleName()),
          () -> assertNotNull(name.getFamilyName()),
          () -> assertNotNull(name.getHonorificPrefix()),
          () -> assertNotNull(name.getHonorificSuffix()),
          () -> assertNotNull(name.getFormatted())
      );
    });
    assertions.add(() -> assertNotNull(user.getNickName()));
    assertions.add(() -> assertNotNull(user.getProfileUrl()));
    assertions.add(() -> assertNotNull(user.getTitle()));
    assertions.add(() -> assertNotNull(user.getUserType()));
    assertions.add(() -> assertNotNull(user.getPreferredLanguage()));
    assertions.add(() -> assertNotNull(user.getLocale()));
    assertions.add(() -> assertNotNull(user.getTimezone()));
    assertions.add(() -> assertNotNull(user.getEmails()));
    assertions.add(() -> {
      List<Email> emails = user.getEmails();
      assertFalse(emails.isEmpty());

      emails.forEach(email -> assertAll(
          () -> assertNotNull(email.getType()),
          () -> assertNotNull(email.getValue())
      ));
    });
    assertions.add(() -> assertNotNull(user.getAddresses()));
    assertions.add(() -> {
      List<Address> addresses = user.getAddresses();
      assertFalse(addresses.isEmpty());

      addresses.forEach(address -> assertAll(
          () -> assertNotNull(address.getType()),
          () -> assertNotNull(address.getStreetAddress()),
          () -> assertNotNull(address.getLocality()),
          () -> assertNotNull(address.getRegion()),
          () -> assertNotNull(address.getPostalCode()),
          () -> assertNotNull(address.getCountry()),
          () -> assertNotNull(address.getFormatted())
      ));
    });
    assertions.add(() -> assertNotNull(user.getIms()));
    assertions.add(() -> {
      List<Im> ims = user.getIms();
      assertFalse(ims.isEmpty());

      ims.forEach(im -> assertAll(
          () -> assertNotNull(im.getType()),
          () -> assertNotNull(im.getValue())
      ));
    });
    assertions.add(() -> assertNotNull(user.getPhotos()));
    assertions.add(() -> {
      List<Photo> photos = user.getPhotos();
      assertFalse(photos.isEmpty());

      photos.forEach(photo -> assertAll(
          () -> assertNotNull(photo.getType()),
          () -> assertNotNull(photo.getValue())
      ));
    });
    assertions.add(() -> assertNotNull(user.getPhoneNumbers()));
    assertions.add(() -> {
      List<PhoneNumber> phoneNumbers = user.getPhoneNumbers();
      assertFalse(phoneNumbers.isEmpty());

      phoneNumbers.forEach(email -> assertAll(
          () -> assertNotNull(email.getType()),
          () -> assertNotNull(email.getValue())
      ));
    });

    return assertions;
  }

  private ResponseDefinitionBuilder configureMockedResponse(Response.Status responseStatus, Object body) throws JsonProcessingException {
    // @formatter:off
    return aResponse()
        .withStatus(responseStatus.getStatusCode())
        .withHeader(CONTENT_TYPE, APPLICATION_JSON_SCIM)
        .withBody(JSON_OBJECT_MAPPER.writeValueAsString(body));
    // @formatter:on
  }
}
