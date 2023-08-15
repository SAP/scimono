package com.sap.scimono.entity.bulk;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;

import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.exception.InternalScimonoException;

public class RequestOperationTest {
  @ParameterizedTest(name = "Test getting correct resource type from path: {0}")
  @ValueSource(strings = {
      "/Groups/abcd",
      "Groups/abcd/",
      "/Groups/abcd/",
      "Groups",
      "/Groups",
      "Groups/"
  })
  public void testGettingResourceTypeFromCorrectPath(String path) {
    RequestOperation operation = new RequestOperation.Builder().setPath(path).setMethod(RequestMethod.POST).build();
    assertEquals(Group.RESOURCE_TYPE_GROUP, operation.getResourceType());
  }

  @ParameterizedTest(name = "Test failing when getting resource type form invalid path: {0}")
  @ValueSource(strings = {
      "/abcd",
      "Groupsabcd/",
      "http://sample.sample/scim/Groups/abcd",
      "/Groupsabcd"
  })
  public void testGettingResourceTypeFromInvalidPath(String path) {
    RequestOperation operation = new RequestOperation.Builder().setPath(path).setMethod(RequestMethod.POST).build();
    assertThrows(InternalScimonoException.class, operation::getResourceType);
  }

  @ParameterizedTest(name = "Test getting resource id from correct path that contains it: {0}")
  @ValueSource(strings = {
      "/Groups/abcd",
      "Groups/abcd/",
      "/Groups/abcd/",
  })
  public void testGettingResourceIdFromCorrectResourcePath(String path) {
    RequestOperation operation = new RequestOperation.Builder().setPath(path).setMethod(RequestMethod.PUT).build();
    assertEquals("abcd", operation.getResourceId().orElseThrow(AssertionFailedError::new));
  }

  @Test
  @DisplayName("Test getting resource id from POST operation if resource data is present")
  public void testGetResourceIdFromFromPostOperationIfDataIsPresent() {
    String resourceId = "abcd";
    RequestOperation operation = new RequestOperation.Builder()
        .setPath("Groups")
        .setData(new Group.Builder().setId(resourceId).build())
        .setMethod(RequestMethod.POST)
        .build();

    assertEquals(resourceId, operation.getResourceId().orElseThrow(AssertionFailedError::new));
  }

  @Test
  @DisplayName("Test resource id is empty for POST operation if resource data is NOT present")
  public void testGetResourceIdFromFromPostOperationIfDataIsNotPresent() {
    RequestOperation operation = new RequestOperation.Builder()
        .setPath("Groups")
        .setMethod(RequestMethod.POST)
        .build();

    assertFalse(operation.getResourceId().isPresent());
  }

  @TestFactory
  @DisplayName("Test getting request data")
  public Collection<DynamicTest> testGetData() {
    Object wrongData = new Object();
    RequestOperation operationWithWrongData = new RequestOperation.Builder().setData(wrongData).setMethod(RequestMethod.POST).build();
    Function<Object, RequestOperation> operationWithData = data -> new RequestOperation.Builder().setData(data).setMethod(RequestMethod.POST).build();

    return Arrays.asList(
        dynamicTest("Fail when getting wrong data as PatchBody",
            () -> assertThrows(BulkOperationCastingException.class, operationWithWrongData::getDataAsPatch)),
        dynamicTest("Fail when getting wrong data as User",
            () -> assertThrows(BulkOperationCastingException.class, operationWithWrongData::getDataAsUser)),
        dynamicTest("Fail when getting wrong data as PatchBody",
            () -> assertThrows(BulkOperationCastingException.class, operationWithWrongData::getDataAsGroup)),
        dynamicTest("Successfully getting PatchBody data",
            () -> assertDoesNotThrow(() -> operationWithData.apply(new PatchBody.Builder().build())).getDataAsPatch()),
        dynamicTest("Successfully getting User data",
            () -> assertDoesNotThrow(() -> operationWithData.apply(new User.Builder().build())).getDataAsUser()),
        dynamicTest("Successfully getting Group data",
            () -> assertDoesNotThrow(() -> operationWithData.apply(new Group.Builder().build())).getDataAsGroup())
    );
  }

}
