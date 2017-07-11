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
import com.ns.greg.library.qr_codec.module.QRCodeContent;
import com.ns.greg.library.qr_codec.module.decode.QRCodeDecoder;
import com.ns.greg.library.qr_codec.module.encode.QRCodeEncoder;

/**
 * Created by Gregory on 2017/7/10.
 */

public class CodecActivity extends AppCompatActivity {

  private static final int PICK_PHOTO_FROM_GALLERY = 7788;

  private Bitmap encode;
  private Bitmap decode;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.codec_demo);
  }

  @Override protected void onResume() {
    super.onResume();

    final QRCodeContent qrCodeContent = new QRCodeContent.Builder().add("EasyCodec", true)
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

    String content = qrCodeContent.string();
    final String encryptContent =
        desEncryptor.encrypt2String(content.getBytes(), "1234567890abcdef".getBytes());

    ((TextView) findViewById(R.id.encode_string)).setText(encryptContent);

    /*--------------------------------
     * Encode
     *-------------------------------*/

    findViewById(R.id.encode_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        QRCodeEncoder encoder =
            new QRCodeEncoder.Builder().setBarcodeFormat(BarcodeFormat.QR_CODE).build();
        try {
          encode = encoder.encode(encryptContent, 480, 480, Bitmap.Config.ARGB_8888);
          ((ImageView) findViewById(R.id.encode_bitmap)).setImageBitmap(encode);
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
        if (decode != null) {
          QRCodeDecoder decoder =
              new QRCodeDecoder.Builder().addBarcodeFormat(BarcodeFormat.QR_CODE).build();

          Result result = decoder.decode(decode);
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
          decode = BitmapFactory.decodeFile(picturePath);
          ((ImageView) findViewById(R.id.decode_bitmap)).setImageBitmap(decode);
        }
      }
    }
  }
}
