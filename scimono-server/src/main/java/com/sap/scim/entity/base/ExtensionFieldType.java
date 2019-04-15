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

package com.sap.scim.entity.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.scim.exception.InvalidInputException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Date;


/**
 * This enum like class represents the valid extension field types. Instances of this class also define methods for converting these types from and to
 * {@link String}.
 *
 * @param <T> the actual type this {@link ExtensionFieldType} represents
 */
public abstract class ExtensionFieldType<T> implements Serializable {
  private static final Logger logger = LoggerFactory.getLogger(ExtensionFieldType.class);

  private static final long serialVersionUID = 5665143978696725609L;
  /**
   * Since this field can not be serialized, it will be created when the object is de-serialized (see readObject()).
   */
  private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC"));

  /**
   * ExtensionFieldType for the Scim type String (actual type is {@link String})
   */
  public static final ExtensionFieldType<String> STRING = new ExtensionFieldType<String>("string") {
    private static final long serialVersionUID = 1L;

    @Override
    public String fromString(final String stringValue) {
      ensureValueIsNotNull(stringValue);
      return stringValue;
    }

    @Override
    public String toString(final String value) {
      ensureValueIsNotNull(value);
      return value;
    }

    @Override
    public String from(final Object value) {
      ensureValueIsNotNull(value);

      return value.toString();
    }
  };
  /**
   * ExtensionFieldType for the Scim type Integer (actual type is {@link BigInteger})
   */
  public static final ExtensionFieldType<BigInteger> INTEGER = new ExtensionFieldType<BigInteger>("integer") {
    private static final long serialVersionUID = 1L;

    @Override
    public BigInteger fromString(final String stringValue) {
      ensureValueIsNotNull(stringValue);

      BigInteger attrValue = null;
      try {
        attrValue = new BigInteger(stringValue);
      } catch (final NumberFormatException e) {
        throw createValidationException(stringValue, "BigInteger");
      }

      return attrValue;
    }

    @Override
    public String toString(final BigInteger value) {
      ensureValueIsNotNull(value);
      return value.toString();
    }

    @Override
    public BigInteger from(final Object value) {
      ensureValueIsNotNull(value);

      if (value instanceof BigInteger) {

        return (BigInteger) value;
      } else if (value instanceof Integer) {

        return BigInteger.valueOf((Integer) value);
      } else {
        throw createValidationException(value.toString(), "integer");
      }
    }
  };
  /**
   * ExtensionFieldType for the Scim type Decimal (actual type is {@link BigDecimal})
   */
  public static final ExtensionFieldType<BigDecimal> DECIMAL = new ExtensionFieldType<BigDecimal>("decimal") {
    private static final long serialVersionUID = 1L;

    @Override
    public BigDecimal fromString(final String stringValue) {
      ensureValueIsNotNull(stringValue);

      BigDecimal attrValue = null;
      try {
        attrValue = new BigDecimal(stringValue);
      } catch (final NumberFormatException e) {
        throw createValidationException(stringValue, "BigDecimal");
      }

      return attrValue;
    }

    @Override
    public String toString(final BigDecimal value) {
      ensureValueIsNotNull(value);
      return value.toString();
    }

    @Override
    public BigDecimal from(final Object value) {
      ensureValueIsNotNull(value);

      if (value instanceof Integer) {

        return BigDecimal.valueOf((Integer) value);
      } else if (value instanceof BigDecimal) {

        return (BigDecimal) value;
      } else {
        throw createValidationException(value.toString(), "decimal");
      }
    }
  };
  /**
   * ExtensionFieldType for the Scim type Boolean (actual type is {@link Boolean})
   */
  public static final ExtensionFieldType<Boolean> BOOLEAN = new ExtensionFieldType<Boolean>("boolean") {
    private static final long serialVersionUID = 1L;

    @Override
    public Boolean fromString(final String stringValue) {
      ensureValueIsNotNull(stringValue);
      if (!"true".equals(stringValue) && !"false".equals(stringValue)) {
        throw createValidationException(stringValue, "boolean");
      }

      return Boolean.valueOf(stringValue);
    }

    @Override
    public String toString(final Boolean value) {
      ensureValueIsNotNull(value);
      return value.toString();
    }

    @Override
    public Boolean from(final Object value) {
      ensureValueIsNotNull(value);

      if (value instanceof Boolean) {

        return (Boolean) value;
      } else {
        throw createValidationException(value.toString(), "boolean");
      }

    }
  };
  /**
   * ExtensionFieldType for the Scim type Binary (actual type is {@link ByteBuffer})
   */
  public static final ExtensionFieldType<String> BINARY = new ExtensionFieldType<String>("binary") {
    private static final long serialVersionUID = 1L;

    @Override
    public String fromString(final String stringValue) {
      ensureValueIsNotNull(stringValue);
      return stringValue;
    }

    @Override
    public String toString(final String value) {
      ensureValueIsNotNull(value);
      return value;
    }

    @Override
    public String from(final Object value) {
      try {
        byte[] valueDecoded = Base64.getDecoder().decode((String) value);
        return new String(valueDecoded);
      } catch (ClassCastException | IllegalArgumentException e) {
        logger.error("Cannot decode binary attribute", e);
        throw createValidationException(value.toString(), "binary");
      }
    }
  };
  /**
   * ExtensionFieldType for the Scim type Reference (actual type is {@link URI})
   */
  public static final ExtensionFieldType<URI> REFERENCE = new ExtensionFieldType<URI>("reference") {
    private static final long serialVersionUID = 1L;

    @Override
    public URI fromString(final String stringValue) {
      ensureValueIsNotNull(stringValue);
      try {
        return new URI(stringValue);
      } catch (final URISyntaxException e) {
        logger.error("Cannot parse URI", e);
        throw createValidationException(stringValue, "URI");
      }
    }

    @Override
    public String toString(final URI value) {
      ensureValueIsNotNull(value);
      return value.toString();
    }

    @Override
    public URI from(final Object value) {
      ensureValueIsNotNull(value);

      if (value instanceof URI) {

        return (URI) value;
      } else if (value instanceof String) {

        return this.fromString((String) value);
      } else {
        throw createValidationException(value.toString(), "URI");
      }

    }
  };

  /**
   * ExtensionFieldType for the Scim type DateTime (actual type is {@link Date}). Valid values are in ISO DateTimeFormat with the timeZone UTC like
   * '2011-08-01T18:29:49.000Z'
   */
  public static final ExtensionFieldType<Date> DATETIME = new ExtensionFieldType<Date>("datetime") {
    private static final long serialVersionUID = 1L;

    @Override
    public java.sql.Timestamp fromString(final String stringValue) {
      try {

        return java.sql.Timestamp.from(ZonedDateTime.from(dateTimeFormatter.parse(stringValue)).toInstant());
      } catch (DateTimeParseException e) {
        logger.error("Cannot parse datetime", e);
        throw createValidationException(stringValue, "Date");
      }
    }

    @Override
    public String toString(final Date value) {
      ensureValueIsNotNull(value);
      return dateTimeFormatter.format(Instant.ofEpochMilli(value.getTime()));
    }

    @Override
    public java.sql.Timestamp from(final Object value) {
      ensureValueIsNotNull(value);

      if (value instanceof Date) {

        return java.sql.Timestamp.from(Instant.ofEpochMilli(((Date) value).getTime()));
      } else if (value instanceof String) {

        return this.fromString((String) value);
      } else {
        throw createValidationException(value.toString(), "Date");
      }
    }
  };
  private final String name;

  public ExtensionFieldType(final String name) {
    this.name = name;
  }

  /**
   * Retrieves a {@link ExtensionFieldType} by its name.
   *
   * @param name the name of the {@link ExtensionFieldType} as it is returned by toString()
   * @return the {@link ExtensionFieldType} based on the given name
   * @throws InvalidInputException if there is no {@link ExtensionFieldType} with the given name
   */
  public static ExtensionFieldType<?> valueOf(final String name) {
    switch (name) {
      case "string":
        return STRING;
      case "integer":
        return INTEGER;
      case "decimal":
        return DECIMAL;
      case "boolean":
        return BOOLEAN;
      case "datetime":
        return DATETIME;
      case "binary":
        return BINARY;
      case "reference":
        return REFERENCE;
      default:
        throw new InvalidInputException(String.format("Type \"%s\" is not a valid data type!", name));
    }
  }

  /**
   * Converts the given {@link String} to the actual type.
   *
   * @param stringValue the {@link String} value to be converted
   * @return the given {@link String} value converted to the actual Type
   */
  public abstract T fromString(String stringValue);

  public abstract Object from(Object value);

  /**
   * Converts a value of the actual type to {@link String}.
   *
   * @param value the value to be converted
   * @return the given value as {@link String}
   */
  public abstract String toString(T value);

  /**
   * Returns the name of the {@link ExtensionFieldType}
   *
   * @return the name of the {@link ExtensionFieldType}
   */
  public final String getName() {
    return name;
  }

  /**
   * Returns a string representation of the {@link ExtensionFieldType} which is its name.
   */
  @Override
  public String toString() {
    return getName();
  }

  protected void ensureValueIsNotNull(final Object value) {
    if (value == null) {
      throw new InvalidInputException("The given value cannot be null.");
    }
  }

  protected InvalidInputException createValidationException(final String stringValue, final String targerType) {
    return new InvalidInputException(String.format("The attribute value \"%s\" is not valid value for type %s.", stringValue, targerType));
  }
}
