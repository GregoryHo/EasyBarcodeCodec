package com.ns.greg.easyqrcodec;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.ns.greg.library.qr_codec.AnalysisListener;
import com.ns.greg.library.qr_codec.CaptureFragment;

/**
 * Created by Gregory on 2017/7/6.
 */

public class ScanActivity extends AppCompatActivity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.scan_demo);
  }

  @Override protected void onResume() {
    super.onResume();

    final CaptureFragment captureFragment = new CaptureFragment();
    captureFragment.setAnalysisListener(new AnalysisListener() {
      @Override public void onSuccess(String text, Bitmap barcode) {
        System.out.println("onSuccess - " + "text = [" + text + "], barcode = [" + barcode + "]");
      }

      @Override public void onFailure() {

      }
    });

    getSupportFragmentManager().beginTransaction()
        .replace(R.id.root, captureFragment)
        .commitAllowingStateLoss();
  }
}
