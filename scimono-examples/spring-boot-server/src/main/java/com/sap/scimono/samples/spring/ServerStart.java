package com.sap.scimono.samples.spring;

import com.sap.scimono.samples.spring.jaxrs.MySCIMApplication;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ServerStart {
  public static void main(String[] args) {
    SpringApplication.run(ServerStart.class, args);
  }

  @Bean
  public ServletRegistrationBean<ServletContainer> jerseyContainer() {
    ServletRegistrationBean<ServletContainer> jerseyContainer = new ServletRegistrationBean<>(new ServletContainer(), "/scim/*");
    jerseyContainer.addInitParameter("javax.ws.rs.Application", MySCIMApplication.class.getName());
    return jerseyContainer;
  }
}
