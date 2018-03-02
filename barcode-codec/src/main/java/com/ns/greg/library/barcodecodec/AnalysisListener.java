package com.ns.greg.library.barcodecodec;

import android.graphics.Bitmap;

/**
 * @author Gregory
 * @since 2017/7/10
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
