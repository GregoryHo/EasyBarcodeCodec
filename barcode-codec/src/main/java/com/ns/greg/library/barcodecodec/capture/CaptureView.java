package com.ns.greg.library.barcodecodec.capture;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
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
 * @since 2017/9/7
 */

public class CaptureView extends FrameLayout implements SurfaceHolder.Callback, Capture {

  private static final String TAG = "CaptureView";

  private CameraManager cameraManager;
  private boolean hasSurface;
  private CaptureHandler handler;
  private SurfaceView surfaceView;
  private ViewfinderView viewfinderView;
  private AnalysisListener analysisListener;
  private int borderStyle;
  private int borderColor;

  public CaptureView(Context context) {
    this(context, null);
  }

  public CaptureView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CaptureView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    //load styled attributes.
    final TypedArray attributes =
        context.getTheme().obtainStyledAttributes(attrs, R.styleable.CaptureView, defStyle, 0);
    borderStyle =
        attributes.getInteger(R.styleable.CaptureView_border_style, ViewfinderView.FOCUS_CORNERS);
    borderColor = attributes.getColor(R.styleable.CaptureView_border_color,
        getResources().getColor(R.color.viewfinder_bound));

    findView();
  }

  private void findView() {
    LayoutInflater.from(getContext()).inflate(R.layout.capture, this, true);
    surfaceView = findViewById(R.id.preview_view);
    viewfinderView = findViewById(R.id.viewfinder_view);
    viewfinderView.setBoundStyle(getBoundStyle());
    viewfinderView.setBoundColorInt(getBoundColor());
    findViewById(R.id.capture_view).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
      @Override
      public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
          int oldTop, int oldRight, int oldBottom) {
        cameraManager = new CameraManager(getContext(), right - left, bottom - top);
        viewfinderView.setCameraManager(cameraManager);
        v.removeOnLayoutChangeListener(this);
      }
    });
  }

  @ViewfinderView.BoundStyle protected int getBoundStyle() {
    return borderStyle;
  }

  @ColorInt protected int getBoundColor() {
    return borderColor;
  }

  public void onResume() {
    if (hasSurface) {
      initCamera(surfaceView.getHolder());
    } else {
      surfaceView.getHolder().addCallback(this);
    }
  }

  public void onPause() {
    if (handler != null) {
      handler.quitSynchronously();
      handler = null;
    }

    cameraManager.closeDriver();
    if (!hasSurface) {
      surfaceView.getHolder().removeCallback(this);
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
        handler = new CaptureHandler(this, new BarCodeDecoder.Builder().setResultPointCallback(
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
