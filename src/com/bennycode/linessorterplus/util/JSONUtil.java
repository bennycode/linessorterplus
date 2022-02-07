package com.bennycode.linessorterplus.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public final class JSONUtil {

  private static final Gson gson = new Gson();

  public static JsonObject getJsonFromString(String input, Charset charset) {
    byte[] bytes = input.getBytes(charset);
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    InputStreamReader isr = new InputStreamReader(bais);
    return JsonParser.parseReader(isr).getAsJsonObject();
  }

  public static String getStringFromJson(JsonObject input) {
    return gson.toJson(input);
  }

  /**
   * Code by Utsav Sinha to recursively sort a JSON object.
   *
   * @param oldJson - The unsorted JSON object
   * @return the sorted JSON object
   * @see <a href="https://stackoverflow.com/a/63794192/451634">Source</a>
   */
  public static JsonObject sortJson(JsonObject oldJson) {
    List<String> keySet = oldJson.keySet().stream().sorted().collect(Collectors.toList());
    JsonObject newJson = new JsonObject();
    for (String key : keySet) {
      JsonElement nestedJson = oldJson.get(key);
      if (nestedJson.isJsonObject()) {
        nestedJson = sortJson(nestedJson.getAsJsonObject());
        newJson.add(key, nestedJson);
      } else if (nestedJson.isJsonArray()) {
        newJson.add(key, nestedJson.getAsJsonArray());
      } else newJson.add(key, nestedJson.getAsJsonPrimitive());
    }
    return newJson;
  }

  public static boolean isValidJSON(List<String> lines, Charset charset) {
    try {
      JSONUtil.getJsonFromString(StringUtils.join(lines, ""), charset);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }
}
