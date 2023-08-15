package com.sap.scimono.client;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import jakarta.ws.rs.core.Response;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActionResponseStatusConfigTest {
  @ParameterizedTest(name = "Test that response status: {0} is part of expected Responses")
  @EnumSource(value = Response.Status.class, names = {"OK", "CREATED"})
  void testIfResponseStatusIsPartOfSuccessfulResponseStatuses(Response.Status responseStatus) {
    ActionResponseStatusConfig responseStatusConfig = new ActionResponseStatusConfig(Arrays.asList(Response.Status.OK, Response.Status.CREATED));
    // @formatter:off
    assertAll(
        () -> assertTrue(responseStatusConfig.isSuccess(responseStatus)),
        () -> assertTrue(responseStatusConfig.isSuccess(responseStatus.getStatusCode()))
    );
    // @formatter:on
  }

  @ParameterizedTest(name = "Test that response status: {0} is NOT part of expected Responses")
  @EnumSource(value = Response.Status.class, names = {"NOT_FOUND", "NOT_IMPLEMENTED"})
  void testIfResponseStatusIsNOTPartOfSuccessfulResponseStatuses(Response.Status responseStatus) {
    ActionResponseStatusConfig responseStatusConfig = new ActionResponseStatusConfig(Arrays.asList(Response.Status.OK, Response.Status.CREATED));
    // @formatter:off
    assertAll(
        () -> assertFalse(responseStatusConfig.isSuccess(responseStatus)),
        () -> assertFalse(responseStatusConfig.isSuccess(responseStatus.getStatusCode()))
    );
    // @formatter:on
  }

  // @formatter:off
  @ParameterizedTest
  @CsvSource({
      "CREATE_SINGLE, 201",
      "GET_ALL, 200",
  }) void testIfResponseStatusIsPartOfDefaultSuccessfulResponseStatuses(
      @AggregateWith(ScimOperationResponseStatusesArgumentAggregator.class) Map.Entry<ResourceAction, Collection<Response.Status>> actionResponses) {

    ActionResponseStatusConfig defaultResponseStatusConfig = SCIMRequest.newBuilder().build()
        .getScimActionResponseStatusConfig(actionResponses.getKey());

    assertTrue(actionResponses.getValue().stream().allMatch(defaultResponseStatusConfig::isSuccess));
  }
  // @formatter:on

  // @formatter:off
  @ParameterizedTest
  @CsvSource({
      "CREATE_SINGLE, 200, 300",
      "GET_ALL, 201, 401",
  }) void testIfResponseStatusIsNotPartOfDefaultConfiguration(
      @AggregateWith(ScimOperationResponseStatusesArgumentAggregator.class) Map.Entry<ResourceAction, Collection<Response.Status>> actionResponses) {

    ActionResponseStatusConfig defaultResponseStatusConfig = SCIMRequest.newBuilder().build()
        .getScimActionResponseStatusConfig(actionResponses.getKey());

    assertFalse(actionResponses.getValue().stream().allMatch(defaultResponseStatusConfig::isSuccess));
  }
  // @formatter:on

  private static class ScimOperationResponseStatusesArgumentAggregator implements ArgumentsAggregator {

    @Override
    public Object aggregateArguments(ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      ResourceAction resourceAction = ResourceAction.valueOf(argumentsAccessor.getString(0));

      // @formatter:off
      Collection<Response.Status> responseStatuses = IntStream.range(1, argumentsAccessor.size())
          .map(argumentsAccessor::getInteger)
          .mapToObj(Response.Status::fromStatusCode)
          .collect(Collectors.toSet());
      // @formatter:on

      return new AbstractMap.SimpleEntry<>(resourceAction, responseStatuses);
    }
  }
}
