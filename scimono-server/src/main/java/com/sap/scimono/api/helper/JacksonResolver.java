/**
 * Copyright (c) 2016 by SAP Labs Bulgaria, url: http://www.sap.com All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP SE, Walldorf. You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered into with SAP.
 *
 * Created on Dec 12, 2016 by i061675
 *
 */

package com.sap.scimono.api.helper;

import static com.sap.scimono.api.API.APPLICATION_JSON_SCIM;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Due to the specifics of JAX-RS entity provider selection, Jackson's default object mapper is not automatically selected to handle object
 * (de-)serialization because it declares to handle all media types (* / *, e.g. application/json as well as custom/xyz). See
 * <a href="https://jersey.java.net/documentation/latest/message-body-workers.html#providers-selection">Entity Provider selection</a> in Jersey's
 * documentation (JAX-RS reference implementation) for more info on the resolution algorithm specified by JAX-RS.
 * <p>
 * This class forces the JAX-RS implementation to use Jackson's default object mapper by changing how the resolution process works for media type
 * application/scim+json.
 *
 * @author i061675
 *
 */
@Consumes(APPLICATION_JSON_SCIM)
@Produces(APPLICATION_JSON_SCIM)
public class JacksonResolver implements ContextResolver<ObjectMapper> {

  @Override
  public ObjectMapper getContext(final Class<?> type) {
    return ObjectMapperFactory.createObjectMapper();
  }

}
