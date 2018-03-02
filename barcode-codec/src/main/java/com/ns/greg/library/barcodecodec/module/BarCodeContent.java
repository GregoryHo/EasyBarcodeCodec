package com.ns.greg.library.barcodecodec.module;

import com.ns.greg.library.barcodecodec.utils.GsonHelper;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gregory
 * @since 2017/7/5
 */

public class BarCodeContent {

  private Map<String, Object> jsonMap;

  private BarCodeContent(Map<String, Object> jsonMap) {
    this.jsonMap = jsonMap;
  }

  public Map<String, Object> getJsonMap() {
    return jsonMap;
  }

  public String string() {
    return GsonHelper.createJsonWithDynamicKey(jsonMap);
  }

  public static BarCodeContent parse(String json, String verifyKey) {
    Map<String, Object> map = null;
    try {
      map = GsonHelper.parseJsonWithDynamicKey(json);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (map == null || !map.containsKey(verifyKey)) {
      return null;
    }

    return new BarCodeContent(map);
  }

  public static final class Builder {

    private Map<String, Object> jsonMap;

    public Builder() {
      this.jsonMap = new HashMap<>();
    }

    public Builder add(String filedName, String value) {
      jsonMap.put(filedName, value);
      return this;
    }

    public Builder add(String filedName, int value) {
      jsonMap.put(filedName, value);
      return this;
    }

    public Builder add(String filedName, double value) {
      jsonMap.put(filedName, value);
      return this;
    }

    public Builder add(String filedName, boolean value) {
      jsonMap.put(filedName, value);
      return this;
    }

    public BarCodeContent build() {
      return new BarCodeContent(jsonMap);
    }
  }
}
