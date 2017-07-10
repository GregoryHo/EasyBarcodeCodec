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

package com.ns.greg.library.qr_codec;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.google.zxing.Result;
import com.ns.greg.library.qr_codec.camera.CameraManager;
import com.ns.greg.library.qr_codec.module.decode.DecodeThread;
import com.ns.greg.library.qr_codec.module.decode.QRCodeDecoder;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureFragmentHandler extends Handler {

  private static final String TAG = CaptureFragmentHandler.class.getSimpleName();

  public static final int QUIT = 0;
  public static final int DECODE = 1;
  public static final int DECODE_SUCCEEDED = 2;
  public static final int DECODE_FAILED = 3;
  public static final int RESTART_PREVIEW = 4;

  private final CaptureFragment captureFragment;
  private final DecodeThread decodeThread;
  private State state;
  private final CameraManager cameraManager;

  private enum State {
    PREVIEW, SUCCESS, DONE
  }

  CaptureFragmentHandler(CaptureFragment captureFragment, QRCodeDecoder qrCodeDecoder,
      CameraManager cameraManager) {
    this.captureFragment = captureFragment;
    decodeThread = new DecodeThread(captureFragment, qrCodeDecoder);
    decodeThread.start();
    state = State.SUCCESS;

    // Start ourselves capturing previews and decoding.
    this.cameraManager = cameraManager;
    cameraManager.startPreview();
    restartPreviewAndDecode();
  }

  @Override public void handleMessage(Message message) {
    switch (message.what) {
      case RESTART_PREVIEW:
        restartPreviewAndDecode();
        break;

      case DECODE_SUCCEEDED:
        state = State.SUCCESS;
        Bundle bundle = message.getData();
        Bitmap barcode = null;
        if (bundle != null) {
          byte[] compressedBitmap = bundle.getByteArray(DecodeThread.BARCODE_BITMAP);
          if (compressedBitmap != null) {
            barcode =
                BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
            // Mutable copy:
            barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
          }
        }

        captureFragment.handleDecode((Result) message.obj, barcode);
        break;

      case DECODE_FAILED:
        // We're decoding as fast as possible, so when one decode fails, start another.
        state = State.PREVIEW;
        cameraManager.requestPreviewFrame(decodeThread.getHandler(), DECODE);
        break;

      /*case R.id.return_scan_result:
        activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
        activity.finish();
        break;

      case R.id.launch_product_query:
        String url = (String) message.obj;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setData(Uri.parse(url));

        ResolveInfo resolveInfo =
            activity.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String browserPackageName = null;
        if (resolveInfo != null && resolveInfo.activityInfo != null) {
          browserPackageName = resolveInfo.activityInfo.packageName;
          Log.d(TAG, "Using browser in package " + browserPackageName);
        }

        // Needed for default Android browser / Chrome only apparently
        if ("com.android.browser".equals(browserPackageName) || "com.android.chrome".equals(browserPackageName)) {
          intent.setPackage(browserPackageName);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          intent.putExtra(Browser.EXTRA_APPLICATION_ID, browserPackageName);
        }

        try {
          activity.startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
          Log.w(TAG, "Can't find anything to handle VIEW of URI " + url);
        }
        break;*/

      default:
        break;
    }
  }

  public void quitSynchronously() {
    state = State.DONE;
    cameraManager.stopPreview();
    Message quit = Message.obtain(decodeThread.getHandler(), QUIT);
    quit.sendToTarget();
    try {
      // Wait at most half a second; should be enough time, and stopScan() will timeout quickly
      decodeThread.join(500L);
    } catch (InterruptedException e) {
      // continue
    }

    // Be absolutely sure we don't send any queued up messages
    removeMessages(DECODE_SUCCEEDED);
    removeMessages(DECODE_FAILED);
  }

  private void restartPreviewAndDecode() {
    if (state == State.SUCCESS) {
      state = State.PREVIEW;
      cameraManager.requestPreviewFrame(decodeThread.getHandler(), DECODE);
      captureFragment.drawViewfinder();
    }
  }
}
