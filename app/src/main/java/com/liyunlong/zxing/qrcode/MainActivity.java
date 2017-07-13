package com.liyunlong.zxing.qrcode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.liyunlong.zxing.activity.MipcaActivityCapture;
import com.liyunlong.zxing.decoding.DecodeBitmap;
import com.liyunlong.zxing.encoding.EncodingHandler;

import static com.liyunlong.zxing.qrcode.R.id.result;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int SCANNIN_GREQUEST_CODE = 1;
    /**
     * 显示扫描结果
     */
    private TextView mTextView;
    /**
     * 显示扫描拍的图片
     */
    private ImageView mImageView;

    /**
     * 输入框产生二维码
     *
     * @param savedInstanceState
     */
    private EditText qrStrEditText;
    private Toast toast;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            }
        }

        mTextView = (TextView) findViewById(result);
        mImageView = (ImageView) findViewById(R.id.qrcode_bitmap);
        qrStrEditText = (EditText) findViewById(R.id.et_qr_string);

        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        findViewById(R.id.button5).setOnClickListener(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    //显示扫描到的内容
                    mTextView.setText(bundle.getString(MipcaActivityCapture.SCAN_RESULT));
                    //显示
                    mImageView.setImageBitmap((Bitmap) data.getParcelableExtra(MipcaActivityCapture.SCAN_BITMAP));
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1: // 扫描二维码/条形码
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MipcaActivityCapture.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
                break;
            case R.id.button2: // 生成条形码
                String content = qrStrEditText.getText().toString();
                int size = content.length();
                for (int i = 0; i < size; i++) {
                    int c = content.charAt(i);
                    if ((19968 <= c && c < 40623)) {
                        Toast.makeText(getApplicationContext(), "text not be chinese", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (!TextUtils.isEmpty(content)) {
                    Bitmap oneCodeBitmap = EncodingHandler.createOneDCode(content);
                    mImageView.setImageBitmap(oneCodeBitmap);
                    qrStrEditText.setText("");
                    mTextView.setText(content);
                }
                break;
            case R.id.button3: // 生成二维码
                String contentString = qrStrEditText.getText().toString();
                if (!contentString.equals("")) {
                    Bitmap qrCodeBitmap = EncodingHandler.createQRCode(contentString);
                    mImageView.setImageBitmap(qrCodeBitmap);
                    qrStrEditText.setText("");
                    mTextView.setText(contentString);
                } else {
                    Toast.makeText(getApplicationContext(), "Text can be not empty", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button4: // 生成二维码
                startActivity(new Intent(this, GeneratorActivity.class));
                break;
            case R.id.button5: // 识别二维码/条形码
                Result result = DecodeBitmap.parseQRcodeFromView(mImageView);
//                View rootView = getWindow().getDecorView().getRootView();
//                Result result = DecodeBitmap.parseQRcodeFromView(rootView);
                if (result == null) {
                    showToast("识别失败");
                } else {
                    showToast("识别结果：" + result.getText());
                }
                break;
        }
    }

    private void showToast(String message) {
        if (toast == null) {
            toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

}
