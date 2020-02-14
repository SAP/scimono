package com.sap.scimono.samples.spring.jaxrs;

import com.sap.scimono.SCIMApplication;
import com.sap.scimono.callback.users.UsersCallback;
import org.glassfish.jersey.server.ServerProperties;

import java.util.HashMap;
import java.util.Map;

public class MySCIMApplication extends SCIMApplication {
  @Override
  public Map<String, Object> getProperties() {
    Map<String, Object> properties = new HashMap<>(super.getProperties());
    properties.put(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

    return properties;
  }

  @Override
  public UsersCallback getUsersCallback() {
    return new UsersCallbackImpl();
  }
}
