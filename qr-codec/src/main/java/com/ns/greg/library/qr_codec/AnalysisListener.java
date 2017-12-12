package com.ns.greg.library.qr_codec;

import android.graphics.Bitmap;

/**
 * Created by Gregory on 2017/7/10.
 */

public interface AnalysisListener {

  /**
   * Analysis succeeded
   *
   * @param text the analysis text
   * @param barcode the capture bitmap
   */
  void onSuccess(String text, Bitmap barcode);

  /**
   * Analysis failure
   */
  void onFailure();
}
