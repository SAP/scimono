package com.sap.scimono.scim.system.tests.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ext.ContextResolver;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static com.sap.scimono.scim.system.tests.util.TestProperties.DATETIME_PATTERN;

@Consumes({"*/*"})
@Produces({"*/*"})
public class ClientJacksonResolver implements ContextResolver<ObjectMapper> {

  @Override
  public ObjectMapper getContext(final Class<?> type) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    mapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
    mapper.registerModule(configureJavaTimeModule());
    return mapper;
  }

  private JavaTimeModule configureJavaTimeModule() {
    JavaTimeModule javaTimeModule = new JavaTimeModule();
    if(DATETIME_PATTERN == null) {
      return javaTimeModule;
    }

    javaTimeModule.addDeserializer(Instant.class, new JsonDeserializer<Instant>() {
      @Override
      public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
        return Instant.from(dateTimeFormatter.parse(jsonParser.getText()));
      }
    });

    return javaTimeModule;
  }
}