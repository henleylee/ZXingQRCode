package com.liyunlong.zxing.decoding;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
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
     * 根据View获取图片，并解析二维码将结果封装在Result对象中
     *
     * @param view View对象
     */
    public static Result decodeQRcodeFromView(View view) {
        //设置缓存
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        //从缓存中获取当前屏幕的图片,创建一个DrawingCache的拷贝，因为DrawingCache得到的位图在禁用后会被回收
        Bitmap bitmap = view.getDrawingCache();
        if (bitmap != null) {
            bitmap = Bitmap.createBitmap(bitmap);
        }
        //禁用DrawingCahce否则会影响性能 ,而且不禁止会导致每次截图到保存的是第一次截图缓存的位图
        view.setDrawingCacheEnabled(false);
        return decodeQRcodeFromBitmap(bitmap);
    }

    /**
     * 根据图片路径获取图片，并解析二维码将结果封装在Result对象中
     *
     * @param bitmapPath 图片的绝对路径
     */
    public static Result decodeQRcodeFromPath(String bitmapPath) {
        //获取到待解析的图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bitmapPath, options);
        options.inSampleSize = options.outHeight / 400;
        if (options.inSampleSize <= 0) {
            options.inSampleSize = 1; //防止其值小于或等于0
        }
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath, options);
        return decodeQRcodeFromBitmap(bitmap);
    }

    /**
     * 根据Bitmap获取图片，并解析二维码将结果封装在Result对象中
     *
     * @param bitmap 二维码图片
     */
    public static Result decodeQRcodeFromBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        //新建一个RGBLuminanceSource对象，将bitmap图片传给此对象
        RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(bitmap);
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

}
