
package com.sap.scimono.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public interface Objects {

  static <T> List<T> sameOrEmpty(final List<T> list) {
    return list != null ? list : new ArrayList<>();
  }

   static <K, V> Map<K, V> sameOrEmpty(final Map<K, V> map) {
    return map != null ? map : new HashMap<>();
  }

   static Object[] mergeArrays(final Object[] firstArray, final Object[] secondArray) {
    Object[] result = new Object[firstArray.length + secondArray.length];
    System.arraycopy(firstArray, 0, result, 0, firstArray.length);
    System.arraycopy(secondArray, 0, result, firstArray.length, secondArray.length);

    return result;
  }

   static Object[] mergeArrays(final Object[] firstArray, final Object[] secondArray, final int insertIndex) {
    Object[] result = new Object[firstArray.length + secondArray.length];
    System.arraycopy(firstArray, 0, result, 0, insertIndex);
    System.arraycopy(secondArray, 0, result, insertIndex, secondArray.length);
    System.arraycopy(firstArray, insertIndex, result, insertIndex + secondArray.length, firstArray.length - insertIndex);

    return result;
  }

  static <T> T firstNonNull(T... objects) {
    for (T obj : objects) {
      if (obj != null) {
        return obj;
      }
    }

    return null;
  }

  static byte[] toByteArray(final InputStream inputStream) throws IOException {
    byte[] buffer = new byte[8192];
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
      output.write(buffer, 0, bytesRead);
    }

    return output.toByteArray();
  }
  
  
  static boolean stringsEqualsIgnoreCase(String a, String b) {
    return (a == b) || (a != null && a.equalsIgnoreCase(b));
  }
}
