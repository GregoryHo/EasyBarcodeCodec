package com.ns.greg.library.qr_codec.module.decode;

import android.graphics.Bitmap;
import com.google.zxing.LuminanceSource;

/**
 * Created by Gregory on 2017/7/10.
 */

public class BitmapLuminanceSource extends LuminanceSource {

  private byte[] pixels;

  BitmapLuminanceSource(Bitmap bitmap) {
    super(bitmap.getWidth(), bitmap.getHeight());
    int size = getWidth() * getHeight();
    pixels = new byte[size];
    int[] data = new int[size];
    bitmap.getPixels(data, 0, getWidth(), 0, 0, getWidth(), getHeight());

    for (int i = 0; i < size; i++) {
      pixels[i] = (byte) data[i];
    }
  }

  @Override public byte[] getMatrix() {
    return pixels;
  }

  @Override public byte[] getRow(int y, byte[] row) {
    System.arraycopy(pixels, y * getWidth(), row, 0, getWidth());
    return row;
  }
}
