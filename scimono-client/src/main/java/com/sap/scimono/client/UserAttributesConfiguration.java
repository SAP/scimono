package com.sap.scimono.client;


public class UserAttributesConfiguration {
  private boolean isUserNameOptional = false;
  
  public boolean isUserNameOptional() {
    return isUserNameOptional;
  }
  
  public void setUserNameOptional(boolean isUserNameOptional) {
    this.isUserNameOptional = isUserNameOptional;
  }
}
