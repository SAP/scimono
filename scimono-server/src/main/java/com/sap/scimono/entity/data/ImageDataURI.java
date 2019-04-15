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

package com.sap.scimono.entity.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.sap.scimono.exception.InvalidInputException;

/**
 * A URI of the form data:image/[image extension][;base64],<data>
 */
public class ImageDataURI extends DataURI {

  private static final String IMAGE_MIME_TYPE = "data:image/";

  /**
   * @param imageUri A String presenting a URI of the form data:image/[image extension][;base64],<data>
   * @throws InvalidInputException If the given string violates RFC 2396, as augmented by the above deviations
   */
  public ImageDataURI(final String imageUri) {
    super(imageUri);
    if (!toString().startsWith(IMAGE_MIME_TYPE)) {
      throw new InvalidInputException("The given URI '" + imageUri + "' is not a image data URI.");
    }
  }

  /**
   * @param imageUri A URI of the form data:image/[image extension][;base64],<data>
   * @throws InvalidInputException if the URI doesn't expects the schema
   */
  public ImageDataURI(final URI imageUri) {
    super(imageUri);
    if (!toString().startsWith(IMAGE_MIME_TYPE)) {
      throw new InvalidInputException("The given URI '" + imageUri.toString() + "' is not a image data URI.");
    }
  }

  /**
   * @param inputStream a inputStream which will be transformed into an DataURI
   * @throws IOException if the stream can not be read
   * @throws InvalidInputException if the inputStream can't be converted into an URI
   */
  public ImageDataURI(final InputStream inputStream) throws IOException {
    super(inputStream);
    if (!toString().startsWith(IMAGE_MIME_TYPE)) {
      throw new InvalidInputException("The given input stream is not an image.");
    }
  }

}
