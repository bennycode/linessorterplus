package com.bennycode.linessorterplus.util;

import com.google.gson.*;
import com.intellij.openapi.util.text.StringUtil;
import java.util.List;
import java.util.stream.Collectors;

public final class JSONUtil {

  private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

  public static JsonObject getJsonFromString(String input) {
    return JsonParser.parseString(input).getAsJsonObject();
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

  public static boolean isValidJSON(List<String> lines) {
    try {
      JSONUtil.getJsonFromString(StringUtil.join(lines, ""));
      return true;
    } catch (Exception ex) {
      return false;
    }
  }
}
