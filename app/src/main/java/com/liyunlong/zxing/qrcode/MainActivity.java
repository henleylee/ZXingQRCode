package com.liyunlong.zxing.qrcode;

import android.Manifest;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.liyunlong.zxing.activity.CaptureActivity;
import com.liyunlong.zxing.decoding.DecodeBitmap;
import com.liyunlong.zxing.encoding.EncodingHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                    mTextView.setText(bundle.getString(CaptureActivity.SCAN_RESULT));
                    //显示
                    mImageView.setImageBitmap((Bitmap) data.getParcelableExtra(CaptureActivity.SCAN_BITMAP));
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1: // 扫描二维码/条形码
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CaptureActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
                break;
            case R.id.button2: // 生成二维码
                startActivity(new Intent(this, GeneratorActivity.class));
                break;
            case R.id.button3: // 生成条形码
                String content = qrStrEditText.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    showToast("Text can be not empty");
                    return;
                }
                Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]");
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    showToast("Text can not contain Chinese");
                    return;
                }
                hideSoftInput(qrStrEditText);
                Bitmap oneCodeBitmap = EncodingHelper.with(content).createOneDCode();
                mImageView.setImageBitmap(oneCodeBitmap);
                qrStrEditText.setText("");
                mTextView.setText(content);
                break;
            case R.id.button4: // 生成二维码
                String contentString = qrStrEditText.getText().toString();
                if (TextUtils.isEmpty(contentString)) {
                    showToast("Text can be not empty");
                    return;
                }
                hideSoftInput(qrStrEditText);
                Bitmap qrCodeBitmap = EncodingHelper.with(contentString).createQRCode();
                mImageView.setImageBitmap(qrCodeBitmap);
                qrStrEditText.setText("");
                mTextView.setText(contentString);
                break;
            case R.id.button5: // 识别二维码/条形码
                hideSoftInput(qrStrEditText);
                Result result = DecodeBitmap.decodeQRcodeFromView(mImageView);
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

    /**
     * 关闭软键盘
     */
    private boolean hideSoftInput(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            return inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        return false;
    }

}
