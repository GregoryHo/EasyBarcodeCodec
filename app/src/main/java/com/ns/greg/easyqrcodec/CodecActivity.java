package com.ns.greg.easyqrcodec;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.ns.greg.library.qr_codec.module.QRCodeContent;
import com.ns.greg.library.qr_codec.module.decode.QRCodeDecoder;
import com.ns.greg.library.qr_codec.module.encode.QRCodeEncoder;

/**
 * Created by Gregory on 2017/7/10.
 */

public class CodecActivity extends AppCompatActivity {

  private Bitmap encode;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.codec_demo);
  }

  @Override protected void onResume() {
    super.onResume();

    /*--------------------------------
     * Encode
     *-------------------------------*/

    final QRCodeContent encodeContent = new QRCodeContent.Builder().add("EasyCodec", true)
        .add("Name", "Greg")
        .add("Age", 26)
        .add("Sex", "Man")
        .build();

    ((TextView) findViewById(R.id.encode_string)).setText(encodeContent.json());

    findViewById(R.id.encode_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        QRCodeEncoder encoder =
            new QRCodeEncoder.Builder().setBarcodeFormat(BarcodeFormat.QR_CODE).build();
        try {
          encode = encoder.encode(encodeContent, 480, 480, Bitmap.Config.ARGB_8888);
          ((ImageView) findViewById(R.id.encode_bitmap)).setImageBitmap(encode);
        } catch (WriterException e) {
          e.printStackTrace();
        }
      }
    });

    /*--------------------------------
     * Decode
     *-------------------------------*/

    findViewById(R.id.decode_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (encode != null) {
          QRCodeDecoder decoder =
              new QRCodeDecoder.Builder().addBarcodeFormat(BarcodeFormat.QR_CODE).build();

          Result result = decoder.decode(encode);
          String decodeString = result.getText();
          ((TextView) findViewById(R.id.decode_string)).setText(decodeString);
          String key = "EasyCodec";
          QRCodeContent decodeContent = QRCodeContent.parse(result.getText(), key);
          if (decodeContent == null) {
            Toast.makeText(getApplicationContext(), "Invalid QR code, verify key : " + key,
                Toast.LENGTH_SHORT).show();
          } else {
            Toast.makeText(getApplicationContext(), "Valid QR code, verify key : " + key,
                Toast.LENGTH_SHORT).show();
          }
        }
      }
    });
  }
}
