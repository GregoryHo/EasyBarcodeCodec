package com.ns.greg.library.barcodecodec.capture;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import com.google.zxing.Result;
import com.ns.greg.library.barcodecodec.AnalysisListener;
import com.ns.greg.library.barcodecodec.R;
import com.ns.greg.library.barcodecodec.camera.CameraManager;
import com.ns.greg.library.barcodecodec.module.decode.BarCodeDecoder;
import com.ns.greg.library.barcodecodec.widget.ViewfinderResultPointCallback;
import com.ns.greg.library.barcodecodec.widget.ViewfinderView;
import java.io.IOException;

/**
 * @author Gregory
 * @since 2017/7/6
 */

public class CaptureFragment extends Fragment implements SurfaceHolder.Callback, Capture {

  private static final String TAG = "CaptureView";

  private CameraManager cameraManager;
  private boolean hasSurface;
  private CaptureHandler handler;
  private SurfaceView surfaceView;
  private ViewfinderView viewfinderView;
  private AnalysisListener analysisListener;

  public static CaptureFragment newInstance(@ViewfinderView.BoundStyle int boundStyle,
      @ColorRes int boundColor) {
    CaptureFragment captureFragment = new CaptureFragment();
    Bundle bundle = new Bundle();
    bundle.putInt("boundStyle", boundStyle);
    bundle.putInt("boundColor", boundColor);
    captureFragment.setArguments(bundle);
    return captureFragment;
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.capture, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    surfaceView = view.findViewById(R.id.preview_view);
    viewfinderView = view.findViewById(R.id.viewfinder_view);
    Bundle bundle = getArguments();
    if (bundle != null) {
      int boundStyle = bundle.getInt("boundStyle", -1);
      if (boundStyle > -1) {
        viewfinderView.setBoundStyle(boundStyle);
      }

      int boundColor = bundle.getInt("boundColor", -1);
      if (boundColor > -1) {
        viewfinderView.setBoundColorRes(boundColor);
      }
    }

    view.findViewById(R.id.capture_view)
        .addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
          @Override
          public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
              int oldTop, int oldRight, int oldBottom) {
            cameraManager = new CameraManager(getActivity().getApplicationContext(), right - left,
                bottom - top);
            viewfinderView.setCameraManager(cameraManager);
            v.removeOnLayoutChangeListener(this);
          }
        });
  }

  @Override public void onResume() {
    super.onResume();

    if (hasSurface) {
      initCamera(surfaceView.getHolder());
    } else {
      surfaceView.getHolder().addCallback(this);
    }
  }

  private void initCamera(SurfaceHolder surfaceHolder) {
    if (surfaceHolder == null) {
      throw new IllegalStateException("No SurfaceHolder provided");
    }

    if (cameraManager.isOpen()) {
      Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
      return;
    }

    try {
      cameraManager.openDriver(surfaceHolder);
      if (handler == null) {
        handler = new CaptureHandler(this,
            new BarCodeDecoder.Builder().setResultPointCallback(
                new ViewfinderResultPointCallback(viewfinderView)).build(), cameraManager);
      }
    } catch (IOException ioe) {
      Log.w(TAG, ioe);
    } catch (RuntimeException e) {
      // Barcode Scanner has seen crashes in the wild of this variety:
      // java.?lang.?RuntimeException: Fail to connect to camera service
      Log.w(TAG, "Unexpected error initializing camera", e);
    }
  }

  @Override public void onPause() {
    super.onPause();

    if (handler != null) {
      handler.quitSynchronously();
      handler = null;
    }

    cameraManager.closeDriver();
    if (!hasSurface) {
      surfaceView.getHolder().removeCallback(this);
    }
  }

  @Override public void surfaceCreated(SurfaceHolder holder) {
    if (Log.isLoggable(TAG, Log.INFO)) {
      Log.i(TAG, "surfaceCreated");
    }

    if (!hasSurface) {
      hasSurface = true;
      initCamera(holder);
    }
  }

  @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    if (Log.isLoggable(TAG, Log.INFO)) {
      Log.i(TAG, "surfaceChanged.");
    }
  }

  @Override public void surfaceDestroyed(SurfaceHolder holder) {
    if (Log.isLoggable(TAG, Log.INFO)) {
      Log.i(TAG, "surfaceDestroyed");
    }

    hasSurface = false;
  }

  @Override public void drawViewfinder() {
    viewfinderView.drawViewfinder();
  }

  @Override public void handleDecode(Result result, Bitmap barcode) {
    if (analysisListener != null) {
      if (result == null || TextUtils.isEmpty(result.getText())) {
        analysisListener.onFailure();
      } else {
        analysisListener.onSuccess(result.getText(), barcode);
      }
    }
  }

  public void setAnalysisListener(AnalysisListener analysisListener) {
    this.analysisListener = analysisListener;
  }

  public ViewfinderView getViewfinderView() {
    return viewfinderView;
  }

  public CameraManager getCameraManager() {
    return cameraManager;
  }

  public Handler getCaptureHandler() {
    return handler;
  }

  public void restartPreviewAfterDelay(long delayMS) {
    if (handler != null) {
      handler.sendEmptyMessageDelayed(CaptureHandler.RESTART_PREVIEW, delayMS);
    }

    resetStatusView();
  }

  private void resetStatusView() {
    viewfinderView.setVisibility(View.VISIBLE);
  }
}
