package com.ns.greg.library.qr_codec.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.util.Map;

/**
 * Created by Gregory on 2017/7/5.
 */

public class GsonHelper {

  @NonNull public static <T> String createJsonWithDynamicKey(@NonNull Map<String, T> object) {
    Gson gson = new GsonBuilder().serializeNulls().create();

    return gson.toJson(object);
  }

  @Nullable public static <T> Map<String, T> parseJsonWithDynamicKey(@Nullable String json) {
    if (json == null || json.isEmpty()) {
      return null;
    }

    Gson gson = new GsonBuilder().serializeNulls().create();
    return gson.fromJson(json, new TypeToken<Map<String, T>>() {
    }.getType());
  }
}
