
package com.sap.scimono.api;

import static org.mockito.Mockito.spy;

import jakarta.ws.rs.container.ContainerRequestContext;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class ServletRequestProviderFilterTest {

  @Mock
  private ContainerRequestContext context;

  private ServletRequestProviderFilter underTest = new ServletRequestProviderFilter();

  @Test
  public void testThatRemovContextResolverIsCalledAtTheEndOfResourceParsing() throws Exception {
    underTest.filter(context);
    spy(underTest).removeServletRequestFromContext();
  }

}
