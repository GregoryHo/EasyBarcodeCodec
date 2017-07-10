package com.ns.greg.library.qr_codec.module.decode;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.HybridBinarizer;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Gregory on 2017/7/5.
 */

public class QRCodeDecoder {

  private Map<DecodeHintType, Object> hints;

  private final MultiFormatReader multiFormatReader;

  public QRCodeDecoder(Map<DecodeHintType, Object> hints) {
    this.hints = hints;
    multiFormatReader = new MultiFormatReader();
    multiFormatReader.setHints(hints);
  }

  public Result decode(Bitmap bitmap) {
    // 開始對圖像資源解碼
    Result result = null;
    try {
      result = multiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(new BitmapLuminanceSource(bitmap))));
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      multiFormatReader.reset();
    }

    return result;
  }

  public Map<DecodeHintType, Object> getHints() {
    return hints;
  }

  public static final class Builder {

    private Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
    private List<BarcodeFormat> formats = new Vector<>();
    private String characterSet = "UTF-8";
    private ResultPointCallback resultPointCallback;

    public Builder setCharacterSet(@NonNull String characterSet) {
      this.characterSet = characterSet;
      return this;
    }

    public Builder addBarcodeFormat(@NonNull BarcodeFormat format) {
      formats.add(format);
      return this;
    }

    public Builder setResultPointCallback(@NonNull ResultPointCallback resultPointCallback) {
      this.resultPointCallback = resultPointCallback;
      return this;
    }

    public QRCodeDecoder build() {
      hints.put(DecodeHintType.CHARACTER_SET, characterSet);
      if (formats.isEmpty()) {
        formats.addAll(DecodeFormatManager.PRODUCT_FORMATS);
        formats.addAll(DecodeFormatManager.INDUSTRIAL_FORMATS);
        formats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
        formats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
        formats.addAll(DecodeFormatManager.AZTEC_FORMATS);
        formats.addAll(DecodeFormatManager.PDF417_FORMATS);
      }

      hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
      if (resultPointCallback != null) {
        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
      }

      return new QRCodeDecoder(hints);
    }
  }
}
