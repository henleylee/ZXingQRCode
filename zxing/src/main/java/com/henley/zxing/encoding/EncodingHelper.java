package com.henley.zxing.encoding;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 二维码/条形码生成辅助类
 *
 * @author Henley
 * @date 2018/6/27 14:45
 */
public class EncodingHelper {

    private static final int BLACK = 0xff000000;
    private static final int WHITE = 0xffffffff;
    private static final int DEFAULT_QRCODE_SIZE = 500;
    private static final int DEFAULT_ONEDCODE_WIDTH = 800;
    private static final int DEFAULT_ONEDCODE_HEIGHT = 200;
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final int[] FANCY_COLORS = {0xFF0094FF, 0xFFFED545, 0xFF000000, 0xFF5ACF00};

    private String content;
    private int width;
    private int height;
    private int color;
    private int[] fancyColors;
    private String charset;
    private Bitmap logoBitmap;
    private Bitmap backgroundBitmap;

    public static EncodingHelper with(String content) {
        return new EncodingHelper(content);
    }

    private EncodingHelper(String content) {
        this.content = requireNonNull(content, "The content is null.");
        this.color = BLACK;
        this.charset = DEFAULT_CHARSET;
    }

    public EncodingHelper width(int width) {
        this.width = requireGreaterThanZero(width, "The width is less than or equal to zero.");
        return this;
    }

    public EncodingHelper height(int height) {
        this.height = requireGreaterThanZero(height, "The height is less than or equal to zero.");
        return this;
    }

    public EncodingHelper size(int size) {
        this.width = requireGreaterThanZero(size, "The size is less than or equal to zero.");
        this.height = requireGreaterThanZero(size, "The size is less than or equal to zero.");
        return this;
    }

    public EncodingHelper size(int width, int height) {
        this.width = requireGreaterThanZero(width, "The width is less than or equal to zero.");
        this.height = requireGreaterThanZero(height, "The height is less than or equal to zero.");
        return this;
    }

    public EncodingHelper color(int color) {
        this.color = color;
        return this;
    }

    public EncodingHelper fancyColors() {
        this.fancyColors = FANCY_COLORS;
        return this;
    }

    public EncodingHelper fancyColors(int[] fancyColors) {
        fancyColors = requireNonNull(fancyColors, "The fancyColors is null.");
        if (fancyColors.length != 4) {
            throw new IllegalArgumentException("The fancyColors's length is less than 4.");
        }
        if (backgroundBitmap != null) {
            throw new IllegalArgumentException("The fancyColors and background cannot be set at the same time.");
        }
        this.fancyColors = fancyColors;
        return this;
    }

    public EncodingHelper charset(String charset) {
        this.charset = requireNonNull(charset, "The charset is null.");
        return this;
    }

    public EncodingHelper logo(Bitmap logo) {
        this.logoBitmap = requireNonNull(logo, "The logo is null.");
        return this;
    }

    public EncodingHelper background(Bitmap background) {
        if (fancyColors != null && fancyColors.length == 4) {
            throw new IllegalArgumentException("The background and fancyColors cannot be set at the same time.");
        }
        this.backgroundBitmap = requireNonNull(background, "The background is null.");
        return this;
    }

    public Bitmap createOneDCode() {
        if (width <= 0) {
            width = DEFAULT_ONEDCODE_WIDTH;
        }
        if (height <= 0) {
            height = DEFAULT_ONEDCODE_HEIGHT;
        }
        if (isContainChinese(content)) {
            throw new IllegalArgumentException("The content cannot contain Chinese.");
        }
        return createOneDCode(content, width, height, color);
    }

    public Bitmap createQRCode() {
        if (width <= 0) {
            width = DEFAULT_QRCODE_SIZE;
        }
        if (height <= 0) {
            height = DEFAULT_QRCODE_SIZE;
        }
        return createQRCode(content, width, height, color, fancyColors, charset, logoBitmap, backgroundBitmap);
    }

    /**
     * 生成条形码(条形码内容不能有中文)
     *
     * @param content 条形码内容
     * @param width   条形码宽度
     * @param height  条形码高度
     * @param color   条形码颜色
     */
    private static Bitmap createOneDCode(String content, int width, int height, int color) {
        try {
            // 生成一维条码,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1); // default is 4
            BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.CODE_128, width, height, hints);
            int matrixWidth = matrix.getWidth();
            int matrixHeight = matrix.getHeight();
            int[] pixels = new int[matrixWidth * matrixHeight];
            for(int y = 0; y < matrixHeight; y++) {
                for(int x = 0; x < matrixWidth; x++) {
                    if(matrix.get(x, y)) {
                        pixels[y * matrixWidth + x] = color;
                    } else {
                        pixels[y * matrixWidth + x] = WHITE;
                    }
                }
            }
            Bitmap result = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888);
            // 通过像素数组生成bitmap
            result.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight);
            return result;
        } catch (WriterException e) {
            return null;
        }
    }

    /**
     * 生成二维码
     *
     * @param content          二维码内容
     * @param width            二维码宽度
     * @param height           二维码高度
     * @param color            二维码颜色
     * @param fancyColors      二维码花式颜色(左上角、左下角、右上角、右下角)
     * @param charset          二维码字符集编码
     * @param logoBitmap       二维码LOGO
     * @param backgroundBitmap 二维码背景
     */
    private static Bitmap createQRCode(String content, int width, int height, int color, int[] fancyColors, String charset, Bitmap logoBitmap, Bitmap backgroundBitmap) {
        try {
            int logoHalfWidth = 0;
            if (logoBitmap != null) {
                logoHalfWidth = Math.max(width, height) / 10;
                // 将logo图片按martix设置的信息缩放
                logoBitmap = Bitmap.createScaledBitmap(logoBitmap, width, height, false);
                Matrix matrix = new Matrix();
                float sx = (float) 2 * logoHalfWidth / logoBitmap.getWidth();
                float sy = (float) 2 * logoHalfWidth / logoBitmap.getHeight();
                matrix.setScale(sx, sy);
                // 设置缩放信息，将logo图片按martix设置的信息缩放
                logoBitmap = Bitmap.createBitmap(logoBitmap, 0, 0, logoBitmap.getWidth(), logoBitmap.getHeight(), matrix, false);
            }
            if (backgroundBitmap != null) {
                // 将背景图片按martix设置的信息缩放
                backgroundBitmap = Bitmap.createScaledBitmap(backgroundBitmap, width, height, false);
            }
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, charset);
            hints.put(EncodeHintType.MARGIN, 1); // default is 4
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int matrixWidth = bitMatrix.getWidth(); // 矩阵高度
            int matrixHeight = bitMatrix.getHeight(); // 矩阵宽度
            int halfW = matrixWidth / 2;
            int halfH = matrixHeight / 2;
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (logoBitmap != null
                            && x > halfW - logoHalfWidth && x < halfW + logoHalfWidth
                            && y > halfH - logoHalfWidth && y < halfH + logoHalfWidth) {//该位置用于存放图片信息
                        //记录图片每个像素信息
                        pixels[y * matrixWidth + x] = logoBitmap.getPixel(x - halfW + logoHalfWidth, y - halfH + logoHalfWidth);
                    } else {
                        if (bitMatrix.get(x, y)) {
                            if (fancyColors != null && fancyColors.length == 4) {
                                if (x < width / 2 && y <= height / 2) { // 左上角
                                    pixels[y * width + x] = fancyColors[0];
                                } else if (x <= width / 2 && y >= height / 2) { // 左下角
                                    pixels[y * width + x] = fancyColors[1];
                                } else if (x >= width / 2 && y < height / 2) { // 右上角
                                    pixels[y * width + x] = fancyColors[2];
                                } else { // 右下角
                                    pixels[y * width + x] = fancyColors[3];
                                }
                            } else if (backgroundBitmap != null) {
                                pixels[y * width + x] = backgroundBitmap.getPixel(x, y);
                            } else {
                                pixels[y * width + x] = color;
                            }
                        } else {
                            pixels[y * height + x] = WHITE;
                        }
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            return null;
        }
    }

    /**
     * 是否包含中文
     *
     * @param content
     */
    private static boolean isContainChinese(String content) {
        Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]");
        Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }

    private static <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }

    private static int requireGreaterThanZero(int value, String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

}
