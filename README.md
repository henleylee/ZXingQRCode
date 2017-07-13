# ZXingQRCode-master —— 基于ZXing的二维码生成、扫描、识别以及花式二维码

## 效果演示 ##
#### 功能展示： ####
![](/screenshots/功能展示.jpg)
#### 花式二维码： ####
![](/screenshots/花式二维码.png)

## 扫描二维码/条形码 ##
#### 打开扫描二维码/条形码的Activity： ####
```java
    Intent intent = new Intent();
    intent.setClass(MainActivity.this, MipcaActivityCapture.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
```

#### 在onActivityResult()方法中处理扫描结果： ####
```java
@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
        case SCANNIN_GREQUEST_CODE:
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                // 扫描到的内容
                String result = bundle.getString(MipcaActivityCapture.SCAN_RESULT)
                // 扫描到的图片
                Bitmap bitmap = (Bitmap) data.getParcelableExtra(MipcaActivityCapture.SCAN_BITMAP)
                // 处理扫描结果
            }
            break;
        }
    }
```

## 生成条形码 ##
```java
    String content = editText.getText().toString(); // 要生成条形码的内容
    int size = content.length();
    for (int i = 0; i < size; i++) {
        int c = content.charAt(i);
        if ((19968 <= c && c < 40623)) { // 不能包含中文
            Toast.makeText(getApplicationContext(), "text not be chinese", Toast.LENGTH_SHORT).show();
            return;
        }
    }
    if (!TextUtils.isEmpty(content)) {
        Bitmap oneCodeBitmap = EncodingHandler.createOneDCode(content); // 生成条形码
        mImageView.setImageBitmap(oneCodeBitmap); // 展示生成的条形码
    }
```

## 生成二维码 ##
```java
    String content = editText.getText().toString(); // 要生成二维码的内容
    if (!TextUtils.isEmpty(content)) {
        Bitmap qrCodeBitmap = EncodingHandler.createQRCode(content); // 生成二维码
        mImageView.setImageBitmap(qrCodeBitmap); // 展示生成的二维码
    }
```
## 识别二维码/条形码(DecodeBitmap：解析图片辅助类) ##
#### 根据View获取图片，并解析二维码将结果封装在Result对象中： ####
```java
    public static Result parseQRcodeFromView(View view)
```

#### 根据图片路径获取图片，并解析二维码将结果封装在Result对象中： ####
```java
    public static Result parseQRcodeFromPath(String bitmapPath)
```

#### 根据图片路径获取图片，并解析二维码将结果封装在Result对象中： ####
```java
    public static Result parseQRcodeFromBitmap(Bitmap bitmap)
```
注意：前两种方式得到图片后最终都会调用第三种方式。

## 花式二维码(EncodingHandler：二维码/条形码生成辅助类) ##
#### 生成二维码(二维码默认大小为500*500，颜色为黑色)： ####
```java
    public static Bitmap createQRCode(String content)
```

#### 生成二维码(二维码默认大小为500*500)： ####
```java
    public static Bitmap createQRCode(String content, int color)
```

#### 生成二维码： ####
```java
    public static Bitmap createQRCode(String content, int size, int color)
```

#### 生成带Logo的二维码(二维码默认大小为500*500，颜色为黑色)： ####
```java
    public static Bitmap createQRCodeWithLogo(String content, Bitmap bitmap)
```

#### 生成带Logo的二维码(二维码默认颜色为黑色)： ####
```java
    public static Bitmap createQRCodeWithLogo(String content, int size, Bitmap bitmap)
```

#### 生成带Logo的二维码(二维码默认大小为500*500)： ####
```java
    public static Bitmap createQRCodeWithLogo(String content, Bitmap bitmap, int color)
```

#### 生成带Logo的二维码： ####
```java
    public static Bitmap createQRCodeWithLogo(String content, int size, Bitmap bitmap, int color)
```

#### 生成带Logo的二维码(四种颜色)： ####
```java
    public static Bitmap createQRCodeWithLogo2(String content, Bitmap bitmap)
```

#### 生成带Logo的二维码(四种颜色)： ####
```java
    public static Bitmap createQRCodeWithLogo2(String content, int size, Bitmap bitmap)
```

#### 生成带Logo的二维码： ####
```java
    public static Bitmap createQRCodeWithLogo2(String content, int size, Bitmap bitmap, int leftTopColor, int leftBottomColor, int rightTopColor, int rightBottomColor)
```


