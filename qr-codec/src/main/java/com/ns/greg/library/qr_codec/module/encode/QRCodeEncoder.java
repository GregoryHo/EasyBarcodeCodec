package com.ns.greg.library.qr_codec.module.encode;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.ns.greg.library.qr_codec.module.QRCodeContent;
import java.util.EnumMap;
import java.util.Map;

/**
 * Created by Gregory on 2017/7/5.
 */

public class QRCodeEncoder {

  private static final int WHITE = 0xFFFFFFFF;
  private static final int BLACK = 0xFF000000;
  private BarcodeFormat format;
  private Map<EncodeHintType, Object> hints;

  private QRCodeEncoder(BarcodeFormat format, Map<EncodeHintType, Object> hints) {
    this.format = format;
    this.hints = hints;
  }

  public Bitmap encode(@NonNull QRCodeContent content, int width, int height,
      @NonNull Bitmap.Config bitmapConfig) throws WriterException {
    MultiFormatWriter writer = new MultiFormatWriter();
    BitMatrix result = writer.encode(content.json(), format, width, height, hints);
    int outputWidth = result.getWidth();
    int outPutHeight = result.getHeight();
    int[] pixels = new int[outputWidth * outPutHeight];
    for (int h = 0; h < outPutHeight; h++) {
      int offset = h * width;
      for (int w = 0; w < outputWidth; w++) {
        pixels[offset + w] = result.get(w, h) ? BLACK : WHITE;
      }
    }

    Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

    return bitmap;
  }

  public static final class Builder {

    private Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
    private BarcodeFormat format;
    private String characterSet = "UTF-8";
    private ErrorCorrectionLevel level = ErrorCorrectionLevel.H;
    private int margin = 0;

    public Builder setBarcodeFormat(@NonNull BarcodeFormat format) {
      this.format = format;
      return this;
    }

    public Builder setCharacterSet(@NonNull String characterSet) {
      this.characterSet = characterSet;
      return this;
    }

    public Builder setErrorCorrectionLevel(@NonNull ErrorCorrectionLevel level) {
      this.level = level;
      return this;
    }

    public Builder setMargin(int margin) {
      this.margin = margin;
      return this;
    }

    public QRCodeEncoder build() {
      hints.put(EncodeHintType.CHARACTER_SET, characterSet);
      hints.put(EncodeHintType.ERROR_CORRECTION, level);
      hints.put(EncodeHintType.MARGIN, margin);

      return new QRCodeEncoder(format, hints);
    }
  }
}
