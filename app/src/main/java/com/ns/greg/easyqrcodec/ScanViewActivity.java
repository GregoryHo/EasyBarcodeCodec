package com.ns.greg.easyqrcodec;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.ns.greg.library.qr_codec.AnalysisListener;
import com.ns.greg.library.qr_codec.capture.CaptureView;

/**
 * @author Gregory
 * @since 2017/9/7
 */

public class ScanViewActivity extends AppCompatActivity {

  private CaptureView captureView;

  @Override public void onCreate(@Nullable Bundle savedInstanceState,
      @Nullable PersistableBundle persistentState) {
    super.onCreate(savedInstanceState, persistentState);
    setContentView(R.layout.scan_view_demo);

    captureView = (CaptureView) findViewById(R.id.capture_view);
    captureView.setAnalysisListener(new AnalysisListener() {
      @Override public void onSuccess(String text, Bitmap barcode) {
        System.out.println("onSuccess, " + "text: [" + text + "], barcode: [" + barcode + "]");
      }

      @Override public void onFailure() {

      }
    });
  }

  @Override protected void onResume() {
    super.onResume();
    captureView.onResume();
  }

  @Override protected void onPause() {
    super.onPause();
    captureView.onPause();
  }
}
