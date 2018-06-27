# ZXingQRCode-master —— 基于ZXing的二维码生成、扫描、识别以及花式二维码

## 效果演示 ##
#### 功能展示： ####
![](/screenshots/功能展示.png)
#### 花式二维码： ####
![](/screenshots/花式二维码.png)


## 1. 扫描二维码/条形码 ##
#### 打开扫描二维码/条形码的Activity： ####
```java
    Intent intent = new Intent();
    intent.setClass(MainActivity.this, CaptureActivity.class);
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

## 2. 生成条形码 ##
```java
    String content = editText.getText().toString(); // 要生成条形码的内容
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
    Bitmap oneCodeBitmap = EncodingHelper.with(content).createOneDCode(); // 生成条形码
    imageView.setImageBitmap(oneCodeBitmap); // 展示生成的条形码
```

## 3. 生成二维码 ##
```java
    String content = editText.getText().toString(); // 要生成二维码的内容
    if (TextUtils.isEmpty(content)) {
        showToast("Text can be not empty");
        return;
    }
    Bitmap qrCodeBitmap = EncodingHelper.with(contentString).createQRCode(); // 生成二维码
    imageView.setImageBitmap(qrCodeBitmap); // 展示生成的二维码
```

## 4. 识别二维码/条形码(DecodeBitmap：解析图片辅助类) ##
#### 4.1 根据View获取图片，并解析二维码将结果封装在Result对象中： ####
```java
    public static Result decodeQRcodeFromView(View view)
```

#### 4.2 根据图片路径获取图片，并解析二维码将结果封装在Result对象中： ####
```java
    public static Result decodeQRcodeFromPath(String bitmapPath)
```

#### 4.3 根据图片路径获取图片，并解析二维码将结果封装在Result对象中： ####
```java
    public static Result decodeQRcodeFromBitmap(Bitmap bitmap)
```

注意：前两种方式得到图片后最终都会调用第三种方式。

## 5. 花式二维码(EncodingHelper：二维码/条形码生成辅助类) ##
#### 5.1 生成二维码(二维码默认大小为500*500，颜色为黑色)： ####
```java
    EncodingHelper.with(String content)
        .createQRCode()
```

#### 5.2 生成二维码(二维码默认大小为500*500)： ####
```java
    EncodingHelper.with(String content)
        .color(int color)
        .createQRCode()
```

#### 5.3 生成二维码： ####
```java
    EncodingHelper.with(String content)
        .size(int size)
        .color(int color)
        .createQRCode()
```

#### 5.4 生成带Logo的二维码(二维码默认大小为500*500，颜色为黑色)： ####
```java
    EncodingHelper.with(String content)
        .logo(Bitmap bitmap)
        .createQRCode()
```

#### 5.5 生成带Logo的二维码(二维码默认颜色为黑色)： ####
```java
    EncodingHelper.with(String content)
        .size(int size)
        .logo(Bitmap bitmap)
        .createQRCode()
```

#### 5.6 生成带Logo的二维码(二维码默认大小为500*500)： ####
```java
    EncodingHelper.with(String content).logo(Bitmap bitmap).createQRCode()
```

#### 5.7 生成带Logo的二维码： ####
```java
    EncodingHelper.with(String content)
        .size(int size).color(int color)
        .logo(Bitmap bitmap)
        .createQRCode()
```

#### 5.8 生成带Logo的二维码(四种颜色)： ####
```java
    EncodingHelper.with(String content)
        .fancyColors()
        .logo(Bitmap bitmap)
        .createQRCode()
```

#### 5.9 生成带Logo的二维码(四种颜色)： ####
```java
    EncodingHelper.with(String content)
        .size(int size)
        .fancyColors()
        .logo(Bitmap bitmap)
        .createQRCode()
```

#### 5.10 生成带Logo的二维码： ####
```java
    EncodingHelper.with(String content)
        .size(int size)
        .fancyColors(int[] fancyColors)
        .logo(Bitmap bitmap)
        .createQRCode()
```

#### 5.11 生成带背景的二维码： ####
```java
    EncodingHelper.with(String content)
        .size(int size)
        .color(int color)
        .background(Bitmap bitmap)
        .createQRCode()
```


