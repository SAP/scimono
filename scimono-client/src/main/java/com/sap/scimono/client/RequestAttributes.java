package com.sap.scimono.client;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import jakarta.ws.rs.client.WebTarget;

import com.sap.scimono.client.query.SCIMQuery;

public class RequestAttributes implements SCIMQuery {
  private static final String ATTRIBUTES_DELIMITER = ",";
  private static final String ATTRIBUTES_PARAM = "attributes";
  private static final String EXCLUDED_ATTRIBUTES_PARAM = "excludedAttributes";

  private final Set<String> additionalAttributes = new HashSet<>();
  private final Set<String> excludedAttributes = new HashSet<>();

  public void requestAdditional(String attribute) {
    additionalAttributes.add(attribute);
  }

  public void exclude(String additional) {
    excludedAttributes.add(additional);
  }

  public WebTarget apply(WebTarget webTarget) {
    Function<Set<String>, String> requestParamConverter = attrs -> String.join(ATTRIBUTES_DELIMITER, attrs);

    if (!additionalAttributes.isEmpty()) {
      webTarget = webTarget.queryParam(ATTRIBUTES_PARAM, requestParamConverter.apply(additionalAttributes));
    }

    if (!excludedAttributes.isEmpty()) {
      webTarget = webTarget.queryParam(EXCLUDED_ATTRIBUTES_PARAM, requestParamConverter.apply(excludedAttributes));
    }

    return webTarget;
  }
}
