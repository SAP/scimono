
package com.sap.scimono.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface Strings {

  static String join(final String... strings) {
    return Arrays.stream(strings).collect(Collectors.joining());
  }

  static boolean isNullOrEmpty(final String string) {
    return string == null || string.trim().isEmpty();
  }

  static boolean isNullOrEmpty(final char[] string) {
    return string == null || string.length == 0;
  }

  static boolean isNotNullOrEmpty(final String string) {
    return !isNullOrEmpty(string);
  }

  static boolean allNullOrEmpty(final String... strings) {
    return Arrays.stream(strings).allMatch(Strings::isNullOrEmpty);
  }

  static boolean allNotNullOrEmpty(final String... strings) {
    return Arrays.stream(strings).allMatch(Strings::isNotNullOrEmpty);
  }

  static boolean containsIgnoreCase(String str1, String str2) {
    return str1.toLowerCase(Locale.ENGLISH).contains(str2.toLowerCase(Locale.ENGLISH));
  }

  static boolean startsWithIgnoreCase(String str1, String str2) {
    return str1.toLowerCase(Locale.ENGLISH).startsWith(str2.toLowerCase(Locale.ENGLISH));
  }

  /**
   * Returns a regex to match the specified string only at the end of other strings.
   *
   * @param lastPartToMatch
   * @return
   */
  static String lastOccurrence(final String lastPartToMatch) {
    String regex = lastPartToMatch + "$";

    if (lastPartToMatch.startsWith("$")) {
      regex = "\\" + regex;
    }

    return regex;
  }

  /**
   * Checks whether a string is matches any of the strings in a collection. The strings in the collection
   * to be matched can contain wildcard (asterisk *) character, which would mean "any string" is matched
   * in this place.
   *
   * @param checkedString              The string we compare against a collection of possibly wildcarded strings
   * @param wildcardedStringCollection A collection of strings any of which may contain wildcards
   * @return true if the checkedString matches any of the strings in the collection (with wildcards)
   */
  static boolean wildcardedMatch(final String checkedString, final List<String> wildcardedStringCollection) {
    List<Pattern> wildcardedStringsPatterns = new ArrayList<>();
    for (String wildcardedString : wildcardedStringCollection) {
      wildcardedStringsPatterns.add(Pattern.compile(wildcardedString.replace("*", ".*")));
    }

    for (Pattern currentPattern : wildcardedStringsPatterns) {
      if (currentPattern.matcher(checkedString).matches()) {
        return true;
      }
    }

    return false;
  }

  /**
   * Removes the "strip" string from the start of "str.
   *
   * @param str   the full string
   * @param strip the part to remove from the start
   * @return
   */
  static String stripStart(String str, String strip) {
    if (str == null) {
      return null;
    }

    int start = 0;
    int strLength = str.length();

    if (strip == null) {
      while ((start != strLength) && Character.isWhitespace(str.charAt(start))) {
        start++;
      }
    } else {
      while ((start != strLength) && (strip.indexOf(str.charAt(start)) != -1)) {
        start++;
      }
    }
    return str.substring(start);
  }

  static String createPrettyEntityString(Map<String, Object> attributes, Class<?> entityClass) {
    StringBuilder pattern = new StringBuilder("%s [");
    List<Object> result = new ArrayList<>();
    result.add(entityClass.getName());
    for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
      pattern.append("%s=%s, ");
      result.add(attribute.getKey());
      result.add(attribute.getValue());
    }
    pattern.append("]");

    return String.format(pattern.toString(), result.toArray(new Object[0]));

  }

}
