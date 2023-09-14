/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2013-2016 tarent solutions GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.sap.scimono.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class SCIMException extends WebApplicationException {

  private static final long serialVersionUID = 7916443182706462000L;
  private final Type scimType;

  public SCIMException(final Type scimType, final String message) {
    super(message);
    this.scimType = scimType;
  }

  public SCIMException(final Type scimType, final String message, final Response.Status status) {
    super(message, status);
    this.scimType = scimType;
  }

  public SCIMException(final SCIMException scimException) {
    super(scimException.getMessage(), scimException.getResponse().getStatus());
    this.scimType = scimException.scimType;
  }

  public String getScimType() {
    return scimType == null ? null : scimType.toJson();
  }

  public enum Type {
    // @formatter:off
    INVALID_FILTER("invalidFilter"),
    TOO_MANY("tooMany"),
    UNIQUENESS("uniqueness"),
    MUTABILITY("mutability"),
    INVALID_SYNTAX("invalidSyntax"),
    INVALID_PATH("invalidPath"),
    NO_TARGET("noTarget"),
    INVALID_VALUE("invalidValue"),
    INVALID_VERSION("invalidVers"),
    SENSITIVE("sensitive");
    // @formatter:on

    private final String jsonRepresentation;

    Type(final String jsonRepresentation) {
      this.jsonRepresentation = jsonRepresentation;
    }

    public String toJson() {
      return jsonRepresentation;
    }
  }

}
