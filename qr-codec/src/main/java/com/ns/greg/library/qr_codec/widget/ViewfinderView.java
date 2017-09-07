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

package com.ns.greg.library.qr_codec.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;
import com.google.zxing.ResultPoint;
import com.ns.greg.library.qr_codec.R;
import com.ns.greg.library.qr_codec.camera.CameraManager;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

  private static final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192, 128, 64 };
  private static final long ANIMATION_DELAY = 80L;
  private static final int CURRENT_POINT_OPACITY = 0xA0;
  private static final int MAX_RESULT_POINTS = 20;
  private static final int POINT_SIZE = 6;

  public static final int FOCUS_CORNERS = 0;
  public static final int FOCUS_FRAME = 1;
  private int style;

  @IntDef({FOCUS_CORNERS, FOCUS_FRAME})
  @Retention(RetentionPolicy.SOURCE)
  public @interface BoundStyle {

  }

  private int frameStyle = FOCUS_FRAME;
  private CameraManager cameraManager;
  private final Paint paint;
  private Bitmap resultBitmap;
  private final int maskColor;
  private final int resultColor;
  private final int resultPointColor;
  private int boundColor;
  private int scannerAlpha;
  private List<ResultPoint> possibleResultPoints;
  private List<ResultPoint> lastPossibleResultPoints;
  // Side length
  private int boundSideLength;
  // The paint width
  private int boundWidth;

  // This constructor is used when the class is built from an XML resource.
  public ViewfinderView(Context context, AttributeSet attrs) {
    super(context, attrs);

    // Initialize these once for performance rather than calling them every time in onDraw().
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Resources resources = getResources();
    maskColor = resources.getColor(R.color.viewfinder_mask);
    resultColor = resources.getColor(R.color.result_view);
    boundColor = resources.getColor(R.color.viewfinder_bound);
    resultPointColor = resources.getColor(R.color.possible_result_points);
    scannerAlpha = 0;
    possibleResultPoints = new ArrayList<>(5);
    lastPossibleResultPoints = null;
    boundSideLength = dp2px(getContext(), 20f);
    boundWidth = dp2px(getContext(), 2f);
  }

  public void setCameraManager(CameraManager cameraManager) {
    this.cameraManager = cameraManager;
  }

  public void setBoundStyle(int style) {
    this.style = style;
  }

  public void setBoundColorRes(int colorId) {
    this.boundColor = getResources().getColor(colorId);
  }

  public void setBoundColorInt(int colorRes) {
    this.boundColor = colorRes;
  }

  @SuppressLint("DrawAllocation") @Override public void onDraw(Canvas canvas) {
    if (cameraManager == null) {
      return; // not ready yet, early draw before done configuring
    }

    Rect frame = cameraManager.getFramingRect();
    Rect previewFrame = cameraManager.getFramingRectInPreview();
    if (frame == null || previewFrame == null) {
      return;
    }

    int width = canvas.getWidth();
    int height = canvas.getHeight();

    // Draw the exterior (i.e. outside the framing rect) darkened
    paint.setColor(resultBitmap != null ? resultColor : maskColor);
    canvas.drawRect(0, 0, width, frame.top, paint);
    canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
    canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
    canvas.drawRect(0, frame.bottom + 1, width, height, paint);

    if (resultBitmap != null) {
      // Draw the opaque result bitmap over the scanning rectangle
      paint.setAlpha(CURRENT_POINT_OPACITY);
      canvas.drawBitmap(resultBitmap, null, frame, paint);
    } else {
      if (frameStyle == FOCUS_CORNERS) {
        drawFocusCorners(canvas, frame);
      } else {
        drawFocusFrame(canvas, frame);
      }

      float scaleX = frame.width() / (float) previewFrame.width();
      float scaleY = frame.height() / (float) previewFrame.height();

      List<ResultPoint> currentPossible = possibleResultPoints;
      List<ResultPoint> currentLast = lastPossibleResultPoints;
      int frameLeft = frame.left;
      int frameTop = frame.top;
      if (currentPossible.isEmpty()) {
        lastPossibleResultPoints = null;
      } else {
        possibleResultPoints = new ArrayList<>(5);
        lastPossibleResultPoints = currentPossible;
        paint.setAlpha(CURRENT_POINT_OPACITY);
        paint.setColor(resultPointColor);
        synchronized (currentPossible) {
          for (ResultPoint point : currentPossible) {
            canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                frameTop + (int) (point.getY() * scaleY), POINT_SIZE, paint);
          }
        }
      }

      if (currentLast != null) {
        paint.setAlpha(CURRENT_POINT_OPACITY / 2);
        paint.setColor(resultPointColor);
        synchronized (currentLast) {
          float radius = POINT_SIZE / 2.0f;
          for (ResultPoint point : currentLast) {
            canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                frameTop + (int) (point.getY() * scaleY), radius, paint);
          }
        }
      }

      // Request another update at the animation interval, but only repaint the laser line,
      // not the entire viewfinder mask.
      postInvalidateDelayed(ANIMATION_DELAY, frame.left - POINT_SIZE, frame.top - POINT_SIZE,
          frame.right + POINT_SIZE, frame.bottom + POINT_SIZE);
    }
  }

  private void drawFocusCorners(Canvas canvas, Rect frame) {
    paint.setColor(boundColor);

    // Left top corner
    canvas.drawRect(frame.left, frame.top, frame.left + boundSideLength, frame.top + boundWidth,
        paint);
    canvas.drawRect(frame.left, frame.top, frame.left + boundWidth, frame.top + boundSideLength,
        paint);

    // Left bottom corner
    canvas.drawRect(frame.left, frame.bottom - boundWidth, frame.left + boundSideLength,
        frame.bottom, paint);
    canvas.drawRect(frame.left, frame.bottom - boundSideLength, frame.left + boundWidth,
        frame.bottom, paint);

    // Right top corner
    canvas.drawRect(frame.right - boundSideLength, frame.top, frame.right, frame.top + boundWidth,
        paint);
    canvas.drawRect(frame.right - boundWidth, frame.top, frame.right, frame.top + boundSideLength,
        paint);

    // Right bottom corner
    canvas.drawRect(frame.right - boundSideLength, frame.bottom - boundWidth, frame.right,
        frame.bottom, paint);
    canvas.drawRect(frame.right - boundWidth, frame.bottom - boundSideLength, frame.right,
        frame.bottom, paint);
  }

  private void drawFocusFrame(Canvas canvas, Rect frame) {
    paint.setColor(boundColor);

    // Left line
    canvas.drawRect(frame.left, frame.top, frame.left + boundWidth, frame.bottom, paint);

    // Top line
    canvas.drawRect(frame.left, frame.top, frame.right, frame.top + boundWidth, paint);

    // Right line
    canvas.drawRect(frame.right - boundWidth, frame.top, frame.right, frame.bottom, paint);

    // Bottom line
    canvas.drawRect(frame.left, frame.bottom - boundWidth, frame.right, frame.bottom, paint);
  }

  public void drawViewfinder() {
    Bitmap resultBitmap = this.resultBitmap;
    this.resultBitmap = null;
    if (resultBitmap != null) {
      resultBitmap.recycle();
    }

    invalidate();
  }

  /**
   * Draw a bitmap with the result points highlighted instead of the live scanning display.
   *
   * @param barcode An image of the decoded barcode.
   */
  public void drawResultBitmap(Bitmap barcode) {
    resultBitmap = barcode;
    invalidate();
  }

  public void addPossibleResultPoint(ResultPoint point) {
    List<ResultPoint> points = possibleResultPoints;
    synchronized (points) {
      points.add(point);
      int size = points.size();
      if (size > MAX_RESULT_POINTS) {
        // trim it
        points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
      }
    }
  }

  public static int dp2px(Context context, float dpValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }
}
