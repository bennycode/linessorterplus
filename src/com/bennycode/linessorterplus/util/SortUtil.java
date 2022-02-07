package com.bennycode.linessorterplus.util;

import com.google.gson.JsonObject;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class SortUtil {

  private static final Comparator<String> DEFAULT_COMPARATOR = new DefaultComparator();

  public static StringBuilder sortText(List<String> lines) {
    SortUtil.sortTextInPlace(lines);
    return SortUtil.joinLines(lines);
  }

  public static StringBuilder sortJson(List<String> lines, Charset charset) {
    String input = StringUtils.join(lines, "");
    JsonObject json = JSONUtil.getJsonFromString(input, charset);
    JsonObject sortedJson = JSONUtil.sortJson(json);
    String sortedString = JSONUtil.getStringFromJson(sortedJson);
    return new StringBuilder(sortedString);
  }

  private static StringBuilder joinLines(List<String> lines) {
    StringBuilder builder = new StringBuilder();

    for (String line : lines) {
      builder.append(line);
    }

    return builder;
  }

  private static void sortTextInPlace(List<String> lines) {
    Collections.sort(lines, DEFAULT_COMPARATOR);
  }

  private static class DefaultComparator implements Comparator<String> {

    public int compare(String s1, String s2) {
      return s1.compareToIgnoreCase(s2);
    }
  }
}
