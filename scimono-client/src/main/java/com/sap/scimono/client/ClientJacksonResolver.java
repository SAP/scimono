package com.sap.scimono.client;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_WRITE;
import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scimono.api.helper.ObjectMapperFactory;
import com.sap.scimono.entity.User;

@Consumes(APPLICATION_JSON_SCIM)
@Produces(APPLICATION_JSON_SCIM)
public class ClientJacksonResolver implements ContextResolver<ObjectMapper> {
  private final boolean isUserNameOptional;
  
  public ClientJacksonResolver(boolean isUserNameOptional) {
    this.isUserNameOptional = isUserNameOptional;
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    ObjectMapper mapper = ObjectMapperFactory.createObjectMapper();
    mapper.addMixIn(User.class, UserWithSerializablePassword.class);
    if (isUserNameOptional) {
      mapper.addMixIn(User.class, UserWithUserNameOptionalMixIn.class);
    }
    
    return mapper;
  }

  private static class UserWithSerializablePassword {
    @JsonProperty(access = READ_WRITE)
    private String password;
  }
}
