package com.sap.scimono.client;


public class UserPropertiesConfiguration {
  private boolean isUserNameOptional = false;
  
  public boolean isUserNameOptional() {
    return isUserNameOptional;
  }
  
  public void setUserNameOptional(boolean isUserNameOptional) {
    this.isUserNameOptional = isUserNameOptional;
  }
}
