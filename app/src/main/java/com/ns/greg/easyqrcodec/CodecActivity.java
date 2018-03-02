package com.ns.greg.easyqrcodec;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.ns.greg.library.easy_encryptor.des.DESEncryptor;
import com.ns.greg.library.barcodecodec.module.BarCodeContent;
import com.ns.greg.library.barcodecodec.module.decode.BarCodeDecoder;
import com.ns.greg.library.barcodecodec.module.encode.BarCodeEncoder;

/**
 * Created by Gregory on 2017/7/10.
 */

public class CodecActivity extends AppCompatActivity {

  private static final int PICK_PHOTO_FROM_GALLERY = 7788;

  private Bitmap barcode_encode;
  private Bitmap qrcode_encode;
  private Bitmap qrcode_decode;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.codec_demo);
  }

  @Override protected void onResume() {
    super.onResume();

    /*--------------------------------
     * Content builder
     *-------------------------------*/
    final BarCodeContent barCodeContent = new BarCodeContent.Builder().add("EasyCodec", true)
        .add("Name", "Greg")
        .add("Age", 26)
        .add("Sex", "Man")
        .build();

    /*--------------------------------
     * Encrypt
     *-------------------------------*/
    DESEncryptor desEncryptor = new DESEncryptor.Builder().setAlgorithm(DESEncryptor.AES)
        .setCipher(DESEncryptor.ECB)
        .setPadding(DESEncryptor.PKCS5_PADDING)
        .build();
    String content = barCodeContent.string();
    final String encryptContent =
        desEncryptor.encrypt2HexString(content.getBytes(), "1234567890abcdef".getBytes());
    ((TextView) findViewById(R.id.encode_string)).setText(encryptContent);

    /*--------------------------------
     * Encode
     *-------------------------------*/
    findViewById(R.id.encode_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        BarCodeEncoder qrcodeEncoder =
            new BarCodeEncoder.Builder().setBarcodeFormat(BarcodeFormat.QR_CODE).build();
        try {
          qrcode_encode = qrcodeEncoder.encode(encryptContent, 480, 480, Bitmap.Config.ARGB_8888);
          ((ImageView) findViewById(R.id.encode_qr_code)).setImageBitmap(qrcode_encode);
        } catch (WriterException e) {
          e.printStackTrace();
        }

        BarCodeEncoder barCodeEncoder =
            new BarCodeEncoder.Builder().setBarcodeFormat(BarcodeFormat.CODE_128).build();
        try {
          barcode_encode = barCodeEncoder.encode("12345678", 480, 240, Bitmap.Config.ARGB_8888);
          ((ImageView) findViewById(R.id.encode_bar_code)).setImageBitmap(barcode_encode);
        } catch (WriterException e) {
          e.printStackTrace();
        }
      }
    });

    /*--------------------------------
     * Select
     *-------------------------------*/
    findViewById(R.id.select_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        startActivityForResult(intent, PICK_PHOTO_FROM_GALLERY);
      }
    });

    /*--------------------------------
     * Decode
     *-------------------------------*/
    findViewById(R.id.decode_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (qrcode_decode != null) {
          BarCodeDecoder decoder =
              new BarCodeDecoder.Builder().addBarcodeFormat(BarcodeFormat.QR_CODE).build();
          Result result = decoder.decode(qrcode_decode);
          String decodeString = result.getText();
          ((TextView) findViewById(R.id.decode_string)).setText(decodeString);
          String key = "EasyCodec";
          BarCodeContent decodeContent = BarCodeContent.parse(result.getText(), key);
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

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == PICK_PHOTO_FROM_GALLERY && resultCode == RESULT_OK && data != null) {
      Uri selectedImage = data.getData();
      String[] filePathColumn = new String[] { MediaStore.Images.Media.DATA };
      Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
      if (cursor != null) {
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        if (picturePath != null) {
          qrcode_decode = BitmapFactory.decodeFile(picturePath);
          ((ImageView) findViewById(R.id.decode_bitmap)).setImageBitmap(qrcode_decode);
        }
      }
    }
  }
}
