package com.sap.scimono.api.request;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RequestedResourceAttributesParser {
  private static final String ATTRIBUTES_DELIMITER = ",";
  private RequestedResourceAttributesParser() {

  }

  public static RequestedResourceAttributes parse(String attributes, String excludedAttributes) {
    return new RequestedResourceAttributes(parse(attributes), parse(excludedAttributes));
  }

  private static List<String> parse(String queryValue) {
    if (queryValue == null) {
      return Collections.emptyList();
    }

    return Arrays.asList(queryValue.split(ATTRIBUTES_DELIMITER));
  }
}
