package com.ns.greg.library.qr_codec;

import android.graphics.Bitmap;

/**
 * Created by Gregory on 2017/7/10.
 */

public interface AnalysisListener {

  void onSuccess(String text, Bitmap barcode);

  void onFailure();
}
