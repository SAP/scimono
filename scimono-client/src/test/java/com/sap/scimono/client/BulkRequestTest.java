package com.sap.scimono.client;

import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.sap.scimono.client.util.WireMockUtils.emptyResponse;

import java.util.UUID;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.bulk.RequestMethod;
import com.sap.scimono.entity.bulk.RequestOperation;
import com.sap.scimono.entity.patch.PatchBody;

public class BulkRequestTest {
  private static final String DEFAULT_URL = "http://localhost:7070";
  private static final String RESOURCE_ID = UUID.randomUUID().toString();

  private WireMockServer wireMockServer;

  @BeforeEach
  public void setup() {
    wireMockServer = new WireMockServer(7070);
    wireMockServer.start();
  }

  @Test
  public void testSendAllKindOdOperations() {
    RequestOperation createUserOperation = BulkRequest.Operations.createUser(mockUser());
    RequestOperation updateUserOperation = BulkRequest.Operations.updateUser(mockUser(RESOURCE_ID));
    RequestOperation patchUserOperation = BulkRequest.Operations.patchUser(RESOURCE_ID, mockPatchBody());
    RequestOperation deleteUserOperation = BulkRequest.Operations.deleteUser(RESOURCE_ID);

    RequestOperation createGroupOperation = BulkRequest.Operations.createGroup(mockGroup());
    RequestOperation updateGroupOperation = BulkRequest.Operations.updateGroup(mockGroup(RESOURCE_ID));
    RequestOperation patchGroupOperation = BulkRequest.Operations.patchGroup(RESOURCE_ID, mockPatchBody());
    RequestOperation deleteGroupOperation = BulkRequest.Operations.deleteGroup(RESOURCE_ID);

    //@formatter:off
    wireMockServer.stubFor(post(urlEqualTo("/Bulk")).willReturn(emptyResponse(Response.Status.OK)));

    SCIMClientService.builder(DEFAULT_URL)
        .build()
        .newBulkRequest()
        .addBulkOperation(createUserOperation)
        .addBulkOperation(updateUserOperation)
        .addBulkOperation(patchUserOperation)
        .addBulkOperation(deleteUserOperation)
        .addBulkOperation(createGroupOperation)
        .addBulkOperation(updateGroupOperation)
        .addBulkOperation(patchGroupOperation)
        .addBulkOperation(deleteGroupOperation)
        .build()
        .execute();

    wireMockServer.verify(postRequestedFor(urlEqualTo("/Bulk"))
        .withRequestBody(matchCreateUserOperation(createUserOperation.getBulkId()))
        .withRequestBody(matchUpdateUserOperation(updateUserOperation.getBulkId(), RESOURCE_ID))
        .withRequestBody(matchPatchUserOperation(patchUserOperation.getBulkId(), RESOURCE_ID))
        .withRequestBody(matchDeleteUserOperation(deleteUserOperation.getBulkId(), RESOURCE_ID))
        .withRequestBody(matchCreateGroupOperation(createGroupOperation.getBulkId()))
        .withRequestBody(matchUpdateGroupOperation(updateGroupOperation.getBulkId(), RESOURCE_ID))
        .withRequestBody(matchPatchGroupOperation(patchGroupOperation.getBulkId(), RESOURCE_ID))
        .withRequestBody(matchDeleteGroupOperation(deleteGroupOperation.getBulkId(), RESOURCE_ID)));
    //@formatter:on
  }

  @Test
  @DisplayName("test add default failsOnErrors attribute")
  public void testDefaultFailsOnError() {
    BulkRequest bulkRequest = SCIMClientService.builder(DEFAULT_URL)
        .build()
        .newBulkRequest()
        .addBulkOperation(BulkRequest.Operations.createUser(mockUser()))
        .addBulkOperation(BulkRequest.Operations.createUser(mockUser()))
        .build();

    wireMockServer.stubFor(post(urlEqualTo("/Bulk")).willReturn(emptyResponse(Response.Status.OK)));
    bulkRequest.execute();

    wireMockServer.verify(postRequestedFor(urlEqualTo("/Bulk"))
            .withRequestBody(matchingJsonPath("$.[?(@.failOnErrors == 2)]")));
  }

  @Test
  @DisplayName("test add value to failsOnErrors attribute")
  public void testAddSpecificValueToFailsOnError() {
    int failsOnErrors = 5;
    RequestOperation op = BulkRequest.Operations.createUser(mockUser());
    BulkRequest bulkRequest = SCIMClientService.builder(DEFAULT_URL).build()
        .newBulkRequest().withFailsOnError(failsOnErrors).addBulkOperation(op).build();

    wireMockServer.stubFor(post(urlEqualTo("/Bulk")).willReturn(emptyResponse(Response.Status.OK)));
    bulkRequest.execute();

    wireMockServer.verify(postRequestedFor(urlEqualTo("/Bulk"))
        .withRequestBody(matchingJsonPath(String.format("$.[?(@.failOnErrors == %d)]", failsOnErrors))));
  }

  private static StringValuePattern matchCreateUserOperation(String bulkId) {
    return matchingJsonPath(matchOperation(
        matchMethod(RequestMethod.POST),
        matchBulkId(bulkId),
        matchPath("/Users"),
        matchNonEmptyData()
    ));
  }

  private static StringValuePattern matchUpdateUserOperation(String bulkId, String id) {
    return matchingJsonPath(matchOperation(
        matchMethod(RequestMethod.PUT),
        matchBulkId(bulkId),
        matchPath("/Users/" + id),
        matchNonEmptyData()
    ));
  }

  private static StringValuePattern matchPatchUserOperation(String bulkId, String id) {
    return matchingJsonPath(matchOperation(
        matchMethod(RequestMethod.PATCH),
        matchBulkId(bulkId),
        matchPath("/Users/" + id),
        matchNonEmptyData()
    ));
  }

  private static StringValuePattern matchDeleteUserOperation(String bulkId, String id) {
    return matchingJsonPath(matchOperation(
        matchMethod(RequestMethod.DELETE),
        matchBulkId(bulkId),
        matchPath("/Users/" + id),
        matchNoData()
    ));
  }

  private static StringValuePattern matchCreateGroupOperation(String bulkId) {
    return matchingJsonPath(matchOperation(
        matchMethod(RequestMethod.POST),
        matchBulkId(bulkId),
        matchPath("/Groups"),
        matchNonEmptyData()
    ));
  }

  private static StringValuePattern matchUpdateGroupOperation(String bulkId, String id) {
    return matchingJsonPath(matchOperation(
        matchMethod(RequestMethod.PUT),
        matchBulkId(bulkId),
        matchPath("/Groups/" + id),
        matchNonEmptyData()
    ));
  }

  private static StringValuePattern matchPatchGroupOperation(String bulkId, String id) {
    return matchingJsonPath(matchOperation(
        matchMethod(RequestMethod.PATCH),
        matchBulkId(bulkId),
        matchPath("/Groups/" + id),
        matchNonEmptyData()
    ));
  }

  private static StringValuePattern matchDeleteGroupOperation(String bulkId, String id) {
    return matchingJsonPath(matchOperation(
        matchMethod(RequestMethod.DELETE),
        matchBulkId(bulkId),
        matchPath("/Groups/" + id),
        matchNoData()
    ));
  }

  private static String matchOperation(String... subMatchers) {
    return String.format("$.Operations[?(%s)]", String.join(" && ", subMatchers));
  }

  private static String matchMethod(RequestMethod method) {
    return String.format("@.method == '%s'", method.name());
  }

  private static String matchBulkId(String bulkId) {
    return String.format("@.bulkId == '%s'", bulkId);
  }

  private static String matchPath(String path) {
    return String.format("@.path == '%s'", path);
  }

  private static String matchNonEmptyData() {
    return "@.data != null";
  }

  private static String matchNoData() {
    return "!@.data";
  }

  private static User mockUser() {
    return mockUser(null);
  }

  private static Group mockGroup() {
    return mockGroup(null);
  }

  private static Group mockGroup(String id) {
    return new Group.Builder().setId(id).build();
  }

  private static User mockUser(String id) {
    return new User.Builder().setId(id).build();
  }

  private static PatchBody mockPatchBody() {
    return new PatchBody.Builder().build();
  }

  @AfterEach
  public void closeResources() {
    wireMockServer.stop();
  }
}
