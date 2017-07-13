package com.liyunlong.zxing.decoding;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Hashtable;

/**
 * 解析图片辅助类
 *
 * @author liyunlong
 * @date 2017/2/3 16:08
 */
public class DecodeBitmap {

    private static final String CHARACTER_SET = "UTF-8";

    /**
     * 获取View绘制缓存中的图片，并解析二维码
     *
     * @param view View对象
     */
    public static Result parseQRcodeFromView(View view) {
        //设置缓存
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        //从缓存中获取当前屏幕的图片,创建一个DrawingCache的拷贝，因为DrawingCache得到的位图在禁用后会被回收
        Bitmap bitmap = view.getDrawingCache();
        Result result = null;
        if (bitmap != null) {
            bitmap = Bitmap.createBitmap(bitmap);
            result = parseQRcodeFromBitmap(bitmap);
        }
        //禁用DrawingCahce否则会影响性能 ,而且不禁止会导致每次截图到保存的是第一次截图缓存的位图
        view.setDrawingCacheEnabled(false);
        return result;
    }

    /**
     * 解析二维码图片,返回结果封装在Result对象中
     *
     * @param bitmapPath 图片的绝对路径
     */
    private Result parseQRcodeFromPath(String bitmapPath) {
        //获取到待解析的图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath, options);
        options.inSampleSize = options.outHeight / 400;
        if (options.inSampleSize <= 0) {
            options.inSampleSize = 1; //防止其值小于或等于0
        }
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(bitmapPath, options);
        return parseQRcodeFromBitmap(bitmap);
    }

    /**
     * 解析二维码图片，并将返回结果封装在Result对象中
     *
     * @param bitmap 二维码图片
     */
    public static Result parseQRcodeFromBitmap(Bitmap bitmap) {
        //新建一个RGBLuminanceSource对象，将bitmap图片传给此对象
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] data = new int[width * height];
        bitmap.getPixels(data, 0, width, 0, 0, width, height);
        RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(width, height, data);
        //将图片转换成二进制图片
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(rgbLuminanceSource));
        //解析转换类型UTF-8
        Hashtable<DecodeHintType, String> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, CHARACTER_SET);
        //初始化解析对象
        MultiFormatReader reader = new MultiFormatReader();
        //开始解析
        Result result = null;
        try {
            result = reader.decode(binaryBitmap, hints);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

//    private static class RGBLuminanceSource extends LuminanceSource {
//
//        private byte bitmapPixels[];
//
//        RGBLuminanceSource(Bitmap bitmap) {
//            super(bitmap.getWidth(), bitmap.getHeight());
//
//            // 首先，要取得该图片的像素数组内容
//            int[] data = new int[bitmap.getWidth() * bitmap.getHeight()];
//            this.bitmapPixels = new byte[bitmap.getWidth() * bitmap.getHeight()];
//            bitmap.getPixels(data, 0, getWidth(), 0, 0, getWidth(), getHeight());
//
//            // 将int数组转换为byte数组，也就是取像素值中蓝色值部分作为辨析内容
//            for (int i = 0; i < data.length; i++) {
//                this.bitmapPixels[i] = (byte) data[i];
//            }
//        }
//
//        @Override
//        public byte[] getMatrix() {
//            // 返回我们生成好的像素数据
//            return bitmapPixels;
//        }
//
//        @Override
//        public byte[] getRow(int y, byte[] row) {
//            // 这里要得到指定行的像素数据
//            System.arraycopy(bitmapPixels, y * getWidth(), row, 0, getWidth());
//            return row;
//        }
//    }

}
