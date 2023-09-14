package com.sap.scimono.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.sap.scimono.api.helper.ObjectMapperFactory;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.MemberRef;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

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
import static java.util.UUID.randomUUID;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.glassfish.jersey.client.HttpUrlConnectorProvider.SET_METHOD_WORKAROUND;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class GroupRequestTest {
  private static final String DEFAULT_URL = "http://localhost:7070";
  private static final ObjectMapper JSON_OBJECT_MAPPER = ObjectMapperFactory.createObjectMapper();
  private static final String SCIM_JSON_RESOURCES_ROOT = "/crud/group/";
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
  public void testReadGroupsWithPagedByIdentityResponse() throws IOException {
    JsonNode jsonResponse = readResponseGroupsAsJsonNodeFromFile("read_groups_identity_paging.json");
    String startId = "aaaa";

    wireMockServer.stubFor(get(urlEqualTo(String.format("/Groups?startId=%s&count=100", startId))).willReturn(configureMockedResponse(OK, jsonResponse)));

    // @formatter:off
    SCIMResponse<PagedByIdentitySearchResult<Group>> resp = SCIMClientService.builder(DEFAULT_URL)
        .build()
        .buildGroupRequest()
        .readMultipleGroups(identityPageQuery().withStartId(startId).withCount(100));
    PagedByIdentitySearchResult<Group>  receivedResponse = resp.get();

    assertAll(
        () -> assertAll(getPagedByIdentityResponseAssertions(receivedResponse)),
        () -> {
          Group group = receivedResponse.getResources().stream().findAny().orElseThrow(RuntimeException::new);
          assertAll("Common resourceAttributes", getCommonResourceAssertions(group, Group.SCHEMA));
          assertNotNull(group.getDisplayName());
        }
    );
    // @formatter:on
  }

  @TestFactory
  public Collection<DynamicTest> testReadGroupsWithIndexPagedResponse() {
    return Arrays.asList(
        dynamicTest("read All Groups", getGroupsIndexPagingExecutable("/Groups?startIndex=1&count=100",
            GroupRequest::readAllGroups)),
        dynamicTest("read multiple Groups default index paging",
            getGroupsIndexPagingExecutable("/Groups?startIndex=1&count=100", GroupRequest::readMultipleGroups)),
        dynamicTest("read multiple Groups custom index paging", getGroupsIndexPagingExecutable("/Groups?startIndex=3&count=40",
            scimGroupRequest -> scimGroupRequest.readMultipleGroups(indexPageQuery().withStartIndex(3).withCount(40)))),
        dynamicTest("read multiple Groups without adding page parameters", getGroupsIndexPagingExecutable("/Groups",
            GroupRequest::readMultipleGroupsWithoutPaging))
    );
  }

  @Test
  public void readSingleGroup() throws IOException {
    String sampleGroupId = randomUUID().toString();
    JsonNode jsonResponse = readResponseGroupsAsJsonNodeFromFile("read_group.json");

    wireMockServer.stubFor(get(urlEqualTo("/Groups/" + sampleGroupId)).willReturn(configureMockedResponse(OK, jsonResponse)));

    // @formatter:off
    Group responseGroup = SCIMClientService.builder(DEFAULT_URL)
        .build()
        .buildGroupRequest()
        .readSingleGroup(sampleGroupId)
        .get();

    assertAll(getAllGroupAttributeAssertions(responseGroup));
    // @formatter:on
  }

  @Test
  public void createGroup() throws IOException {
    String groupJsonDataFileName = "create_group.json";
    Group group = readRequestObjectFromFile(groupJsonDataFileName, Group.class);
    JsonNode jsonResponse = readResponseGroupsAsJsonNodeFromFile(groupJsonDataFileName);

    wireMockServer.stubFor(post(urlEqualTo("/Groups")).willReturn(configureMockedResponse(CREATED, jsonResponse)));

    // @formatter:off
    Group responseGroup = SCIMClientService.builder(DEFAULT_URL)
        .build()
        .buildGroupRequest()
        .createGroup(group)
        .get();

    assertAll(getAllGroupAttributeAssertions(responseGroup));
    // @formatter:on
  }

  @Test
  public void patchGroup() throws IOException {
    String sampleGroupId = randomUUID().toString();
    PatchBody patchBody = readRequestObjectFromFile("patch_group.json", PatchBody.class);

    wireMockServer.stubFor(patch(urlEqualTo("/Groups/" + sampleGroupId)).willReturn(noContent()));

    // @formatter:off
    SCIMResponse<Void> response = SCIMClientService.builder(DEFAULT_URL)
        .addProperty(SET_METHOD_WORKAROUND, true)
        .build()
        .buildGroupRequest()
        .patchGroup(patchBody, sampleGroupId);

    assertTrue(response.isSuccess());
    // @formatter:on
  }

  @Test
  public void updateGroup() throws IOException {
    String groupJsonDataFileName = "put_group.json";
    Group group = readRequestObjectFromFile(groupJsonDataFileName, Group.class);
    JsonNode jsonResponse = readResponseGroupsAsJsonNodeFromFile(groupJsonDataFileName);

    wireMockServer.stubFor(put(urlEqualTo("/Groups/" + group.getId())).willReturn(configureMockedResponse(OK, jsonResponse)));

    // @formatter:off
    Group responseGroup = SCIMClientService.builder(DEFAULT_URL)
        .build()
        .buildGroupRequest()
        .updateGroup(group)
        .get();

    assertAll(getAllGroupAttributeAssertions(responseGroup));
    // @formatter:on
  }

  private <T> T readRequestObjectFromFile(String fileName, Class<T> pojoClass) throws IOException {
    // @formatter:off
    JsonNode groupJsonNode = JSON_OBJECT_MAPPER
        .readValue(this.getClass().getResourceAsStream(SCIM_JSON_RESOURCES_ROOT + fileName), ObjectNode.class)
        .get(REQUEST_ATTR);
    // @formatter:on

    return JSON_OBJECT_MAPPER.treeToValue(groupJsonNode, pojoClass);
  }

  private JsonNode readResponseGroupsAsJsonNodeFromFile(String fileName) throws IOException {
    // @formatter:off
    return JSON_OBJECT_MAPPER
        .readValue(this.getClass().getResourceAsStream(SCIM_JSON_RESOURCES_ROOT + fileName), ObjectNode.class)
        .get(RESPONSE_ATTR);
    // @formatter:on
  }

  private Executable getGroupsIndexPagingExecutable(String requestUrl,
      Function<GroupRequest, SCIMResponse<PagedByIndexSearchResult<Group>>> responseFunction) {
    return () -> {
      JsonNode jsonResponse = readResponseGroupsAsJsonNodeFromFile("read_groups_index_paging.json");
      wireMockServer.stubFor(get(urlEqualTo(requestUrl)).willReturn(configureMockedResponse(OK, jsonResponse)));

      // @formatter:off
      SCIMResponse<PagedByIndexSearchResult<Group>> resp = responseFunction.apply(SCIMClientService.builder(DEFAULT_URL).build().buildGroupRequest());
      PagedByIndexSearchResult<Group>  receivedResponse = resp.get();
      assertAll(
        () -> assertAll(getPagedByIndexResponseAssertions(receivedResponse)),
        () -> {
          Group group = receivedResponse.getResources().stream().findAny().orElseThrow(RuntimeException::new);
          assertAll("Common resourceAttributes", getCommonResourceAssertions(group, Group.SCHEMA));
          assertNotNull(group.getDisplayName());
        }
      );
    };
    // @formatter:on
  }

  private List<Executable> getAllGroupAttributeAssertions(Group group) {
    List<Executable> assertions = new ArrayList<>(getCommonResourceAssertions(group, Group.SCHEMA));

    assertions.add(() -> assertNotNull(group.getDisplayName()));
    assertions.add(() -> assertNotNull(group.getMembers()));
    assertions.add(() -> {
      Set<MemberRef> members = group.getMembers();
      assertFalse(members.isEmpty());

      members.forEach(member -> assertAll(
          () -> assertNotNull(member.getType()),
          () -> assertNotNull(member.getValue())
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
