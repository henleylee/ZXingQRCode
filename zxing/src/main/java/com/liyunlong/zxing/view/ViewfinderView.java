package com.liyunlong.zxing.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.IntDef;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.liyunlong.zxing.R;
import com.liyunlong.zxing.camera.CameraManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author liyunlong
 * @date 2018/6/26 16:00
 */
public class ViewfinderView extends View {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 10L;
    private static final int POINT_SIZE = 6;
    private static final int POINT_OPAQUE = 0xFF;
    /**
     * 扫描区边角的宽
     */
    private static final int CORNER_RECT_WIDTH = 10;
    /**
     * 扫描区边角的高
     */
    private static final int CORNER_RECT_HEIGHT = 60;
    /**
     * 扫描线移动距离
     */
    private static final int SCANNER_LINE_MOVE_DISTANCE = 5;
    /**
     * 扫描线宽度
     */
    private static final int SCANNER_LINE_HEIGHT = 10;
    /**
     * 扫描框中的中间线的与扫描框左右的间隙
     */
    private static final int MIDDLE_LINE_PADDING = 6;

    private Paint paint;
    private TextPaint textPaint;
    private Bitmap resultBitmap;
    //模糊区域颜色
    private int maskColor;
    private int resultColor;
    //扫描区域边框颜色
    private int frameColor;
    //扫描线颜色
    private int laserColor;
    //四角颜色
    private int cornerColor;
    //扫描点的颜色
    private int resultPointColor;
    private int scannerAlpha;
    //扫描区域提示文本
    private String labelText;
    //扫描区域提示文本字体颜色和大小
    private int labelTextColor;
    private float labelTextSize;
    private float labelTextMargin;
    private int labelTextLocation;

    public int scannerStart = 0;
    public int scannerEnd = 0;

    private Collection<ResultPoint> possibleResultPoints;
    private Collection<ResultPoint> lastPossibleResultPoints;

    @IntDef({
            LabelTextLocation.TOP,
            LabelTextLocation.BOTTOM
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface LabelTextLocation {
        int TOP = 0;
        int BOTTOM = 1;
    }

    public ViewfinderView(Context context) {
        this(context, null);
    }

    public ViewfinderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewfinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        handleTypedArray(context, attrs);
        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint();
        paint.setAntiAlias(true);
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        scannerAlpha = 0;
        possibleResultPoints = new HashSet<>(5);
    }

    /**
     * 初始化自定义属性信息
     *
     * @param context
     * @param attrs
     */
    private void handleTypedArray(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ViewfinderView);
        try {
            laserColor = typedArray.getColor(R.styleable.ViewfinderView_laser_color, 0xFF0098FF);
            cornerColor = typedArray.getColor(R.styleable.ViewfinderView_corner_color, 0xFF0098FF);
            frameColor = typedArray.getColor(R.styleable.ViewfinderView_frame_color, 0xFF0098FF);
            resultPointColor = typedArray.getColor(R.styleable.ViewfinderView_result_point_color, 0xC0FFFF00);
            maskColor = typedArray.getColor(R.styleable.ViewfinderView_mask_color, 0x60000000);
            resultColor = typedArray.getColor(R.styleable.ViewfinderView_result_color, 0xB0000000);
            labelText = typedArray.getString(R.styleable.ViewfinderView_label_text);
            labelTextColor = typedArray.getColor(R.styleable.ViewfinderView_label_text_color, 0xFFFFFFFF);
            labelTextSize = typedArray.getDimension(R.styleable.ViewfinderView_label_text_size, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, getResources().getDisplayMetrics()));
            labelTextMargin = typedArray.getDimension(R.styleable.ViewfinderView_label_text_margin, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, getResources().getDisplayMetrics()));
            labelTextLocation = typedArray.getInt(R.styleable.ViewfinderView_label_text_location, LabelTextLocation.BOTTOM);
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //中间的扫描框，你要修改扫描框的大小，去CameraManager里面修改
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) {
            return;
        }
        if (scannerStart == 0 || scannerEnd == 0) {
            scannerStart = frame.top;
            scannerEnd = frame.bottom;
        }

        //获取屏幕的宽和高
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // 绘制扫描框外面的阴影部分
        drawExterior(canvas, frame, width, height);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(POINT_OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        } else {
            // Draw a two pixel solid black border inside the framing rect
            drawFrame(canvas, frame);
            // 绘制边角
            drawCorner(canvas, frame);
            //绘制提示信息
            drawTextInfo(canvas, frame);
            // Draw a red "laser scanner" line through the middle to show decoding is active
            drawLaserScanner(canvas, frame);

            drawPossibleResultPoints(canvas, frame);

            // 只刷新扫描框的内容，其他地方不刷新(指定重绘区域，该方法会在子线程中执行)
            postInvalidateDelayed(ANIMATION_DELAY,
                    frame.left - POINT_SIZE,
                    frame.top - POINT_SIZE,
                    frame.right + POINT_SIZE,
                    frame.bottom + POINT_SIZE);
        }
    }

    /**
     * 绘制扫描框外面的阴影部分
     *
     * @param canvas
     * @param frame
     * @param width
     * @param height
     */
    private void drawExterior(Canvas canvas, Rect frame, int width, int height) {
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);
    }

    /**
     * 绘制扫描区边框
     *
     * @param canvas
     * @param frame
     */
    private void drawFrame(Canvas canvas, Rect frame) {
        paint.setColor(frameColor);
        canvas.drawRect(frame.left, frame.top, frame.right + 1, frame.top + 2, paint);
        canvas.drawRect(frame.left, frame.top + 2, frame.left + 2, frame.bottom - 1, paint);
        canvas.drawRect(frame.right - 1, frame.top, frame.right + 1, frame.bottom - 1, paint);
        canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1, frame.bottom + 1, paint);
    }

    /**
     * 绘制扫描框边角
     *
     * @param canvas
     * @param frame
     */
    private void drawCorner(Canvas canvas, Rect frame) {
        paint.setColor(cornerColor);
        //左上
        canvas.drawRect(frame.left, frame.top, frame.left + CORNER_RECT_WIDTH, frame.top + CORNER_RECT_HEIGHT, paint);
        canvas.drawRect(frame.left, frame.top, frame.left + CORNER_RECT_HEIGHT, frame.top + CORNER_RECT_WIDTH, paint);
        //右上
        canvas.drawRect(frame.right - CORNER_RECT_WIDTH, frame.top, frame.right, frame.top + CORNER_RECT_HEIGHT, paint);
        canvas.drawRect(frame.right - CORNER_RECT_HEIGHT, frame.top, frame.right, frame.top + CORNER_RECT_WIDTH, paint);
        //左下
        canvas.drawRect(frame.left, frame.bottom - CORNER_RECT_WIDTH, frame.left + CORNER_RECT_HEIGHT, frame.bottom, paint);
        canvas.drawRect(frame.left, frame.bottom - CORNER_RECT_HEIGHT, frame.left + CORNER_RECT_WIDTH, frame.bottom, paint);
        //右下
        canvas.drawRect(frame.right - CORNER_RECT_WIDTH, frame.bottom - CORNER_RECT_HEIGHT, frame.right, frame.bottom, paint);
        canvas.drawRect(frame.right - CORNER_RECT_HEIGHT, frame.bottom - CORNER_RECT_WIDTH, frame.right, frame.bottom, paint);
    }

    /**
     * 绘制文本
     *
     * @param canvas
     * @param frame
     */
    private void drawTextInfo(Canvas canvas, Rect frame) {
        if (!TextUtils.isEmpty(labelText)) {
            textPaint.setColor(labelTextColor);
            textPaint.setTextSize(labelTextSize);
            textPaint.setTextAlign(Paint.Align.CENTER);
            if (labelTextLocation == LabelTextLocation.BOTTOM) {
                canvas.drawText(labelText, frame.left + frame.width() / 2, frame.bottom + labelTextMargin, textPaint);
            } else {
                canvas.drawText(labelText, frame.left + frame.width() / 2, frame.top - labelTextMargin, textPaint);
            }
        }
    }

    /**
     * 绘制扫描线
     *
     * @param canvas
     * @param frame
     */
    private void drawLaserScanner(Canvas canvas, Rect frame) {
        paint.setColor(laserColor);

        // 线性渐变
        LinearGradient linearGradient = new LinearGradient(
                frame.left, scannerStart,
                frame.left, scannerStart + SCANNER_LINE_HEIGHT,
                shadeColor(laserColor),
                laserColor,
                Shader.TileMode.MIRROR);

        // 环形渲染
        RadialGradient radialGradient = new RadialGradient(
                (float) (frame.left + frame.width() / 2),
                (float) (scannerStart + SCANNER_LINE_HEIGHT / 2),
                360f,
                laserColor,
                shadeColor(laserColor),
                Shader.TileMode.MIRROR);

        paint.setShader(radialGradient);
        if (scannerStart <= scannerEnd) {
            //矩形
            // canvas.drawRect(frame.left, scannerStart, frame.right, scannerStart + SCANNER_LINE_HEIGHT, paint);
            //椭圆
            RectF rectF = new RectF(frame.left + MIDDLE_LINE_PADDING, scannerStart - SCANNER_LINE_HEIGHT / 2, frame.right - MIDDLE_LINE_PADDING, scannerStart + SCANNER_LINE_HEIGHT / 2);
            canvas.drawOval(rectF, paint);
            scannerStart += SCANNER_LINE_MOVE_DISTANCE;
        } else {
            scannerStart = frame.top;
        }
        paint.setShader(null);
    }

    /**
     * 处理颜色模糊
     *
     * @param color
     */
    public int shadeColor(int color) {
        String hax = Integer.toHexString(color);
        String result = "20" + hax.substring(2);
        return Integer.valueOf(result, 16);
    }

    /**
     * @param canvas
     * @param frame
     */
    private void drawPossibleResultPoints(Canvas canvas, Rect frame) {
        Collection<ResultPoint> currentPossible = possibleResultPoints;
        Collection<ResultPoint> currentLast = lastPossibleResultPoints;
        if (currentPossible.isEmpty()) {
            lastPossibleResultPoints = null;
        } else {
            possibleResultPoints = new HashSet<>(5);
            lastPossibleResultPoints = currentPossible;
            paint.setAlpha(POINT_OPAQUE);
            paint.setColor(resultPointColor);
            for (ResultPoint point : currentPossible) {
                canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), POINT_SIZE, paint);
            }
        }
        if (currentLast != null) {
            paint.setAlpha(POINT_OPAQUE / 2);
            paint.setColor(resultPointColor);
            float radius = POINT_SIZE / 2.0f;
            for (ResultPoint point : currentLast) {
                canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), radius, paint);
            }
        }
    }

    public void addPossibleResultPoint(ResultPoint point) {
        if (possibleResultPoints != null) {
            possibleResultPoints.add(point);
        }
    }

    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        this.resultBitmap = null;
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live
     * scanning display.
     *
     * @param resultBitmap An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap resultBitmap) {
        this.resultBitmap = resultBitmap;
        invalidate();
    }

}