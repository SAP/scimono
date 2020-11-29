package com.sap.scimono.api.request;

import java.util.List;

public class RequestedResourceAttributes {
  private final List<String> attributesToBeReturned;
  private final List<String> attributesToBeExcluded;

  RequestedResourceAttributes(List<String> attributesToBeReturned, List<String> attributesToBeExcluded) {
    this.attributesToBeReturned = attributesToBeReturned;
    this.attributesToBeExcluded = attributesToBeExcluded;
  }

  public List<String> getAttributesToBeReturned() {
    return attributesToBeReturned;
  }

  public List<String> getAttributesToBeExcluded() {
    return attributesToBeExcluded;
  }
}
