package com.ns.greg.library.qr_codec.module;

import com.ns.greg.library.qr_codec.utils.GsonHelper;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gregory on 2017/7/5.
 */

public class QRCodeContent {

  private Map<String, Object> jsonMap;

  private QRCodeContent(Map<String, Object> jsonMap) {
    this.jsonMap = jsonMap;
  }

  public Map<String, Object> getJsonMap() {
    return jsonMap;
  }

  public String json() {
    return GsonHelper.createJsonWithDynamicKey(jsonMap);
  }

  public static QRCodeContent parse(String json, String verifyKey) {
    Map<String, Object> map = GsonHelper.parseJsonWithDynamicKey(json);
    if (map == null || !map.containsKey(verifyKey)) {
      return null;
    }

    return new QRCodeContent(map);
  }

  public static final class Builder {

    private Map<String, Object> jsonMap = new HashMap<>();

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

    public QRCodeContent build() {
      return new QRCodeContent(jsonMap);
    }
  }
}
