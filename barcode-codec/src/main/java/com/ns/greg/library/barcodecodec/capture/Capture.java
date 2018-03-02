package com.ns.greg.library.barcodecodec.capture;

import android.graphics.Bitmap;
import com.google.zxing.Result;

/**
 * @author Gregory
 * @since 2017/9/7
 */

interface Capture {

  void drawViewfinder();

  void handleDecode(Result result, Bitmap barcode);
}
