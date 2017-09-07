/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ns.greg.library.qr_codec.module.decode;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.ns.greg.library.qr_codec.camera.CameraManager;
import com.ns.greg.library.qr_codec.capture.CaptureHandler;
import java.util.concurrent.CountDownLatch;

/**
 * This thread does all the heavy lifting of decoding the images.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class DecodeThread extends Thread {

  public static final String BARCODE_BITMAP = "barcode_bitmap";
  public static final String BARCODE_SCALED_FACTOR = "barcode_scaled_factor";

  private final CaptureHandler captureHandler;
  private final QRCodeDecoder qrCodeDecoder;
  private final CameraManager cameraManager;
  private final CountDownLatch handlerInitLatch;
  private Handler handler;

  public DecodeThread(CaptureHandler captureHandler, QRCodeDecoder qrCodeDecoder,
      CameraManager cameraManager) {
    this.captureHandler = captureHandler;
    this.qrCodeDecoder = qrCodeDecoder;
    this.cameraManager = cameraManager;
    handlerInitLatch = new CountDownLatch(1);

    Log.i("DecodeThread", "Hints: " + qrCodeDecoder.getHints());
  }

  public Handler getHandler() {
    try {
      handlerInitLatch.await();
    } catch (InterruptedException ie) {
      // continue?
    }
    return handler;
  }

  @Override public void run() {
    Looper.prepare();
    handler = new DecodeHandler(captureHandler, cameraManager, qrCodeDecoder.getHints());
    handlerInitLatch.countDown();
    Looper.loop();
  }
}
