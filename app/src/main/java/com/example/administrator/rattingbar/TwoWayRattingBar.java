package com.example.administrator.rattingbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Administrator on 2017/10/30.
 */

public class TwoWayRattingBar extends View {

    Paint paint;

    Paint textPaint;

    private int color_line_normal;

    private int color_line_select;

    private float leftProgress = 0f;

    private float rightProgress = 1f;

    private float pressX;

    private Bitmap leftProgressIcon;

    private Bitmap rightProgressIcon;

    private float stroke_width_normal;

    private float stroke_width_select;

    private int num = 14000;

    Rect bounds = new Rect();


    private OnProgressChangeListener onProgressChangeListener;

    public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        this.onProgressChangeListener = onProgressChangeListener;
    }

    public interface OnProgressChangeListener {
        void onLeftProgressChange(float progress);

        void onRightProgressChange(float progress);
    }

    public TwoWayRattingBar(Context context) {
        super(context);
        init(context, null, 0);
    }

    public TwoWayRattingBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public TwoWayRattingBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.TwoWayRattingBar, defStyleAttr, 0);
        color_line_normal = a.getColor(R.styleable.TwoWayRattingBar_color_line_normal, Color.parseColor("#dedede"));
        color_line_select = a.getColor(R.styleable.TwoWayRattingBar_color_line_select, Color.parseColor("#238dfb"));
        stroke_width_normal = a.getDimension(R.styleable.TwoWayRattingBar_stroke_width_normal, 2f);
        stroke_width_select = a.getDimension(R.styleable.TwoWayRattingBar_stroke_width_select, 4f);
        leftProgressIcon = BitmapFactory.decodeResource(context.getResources(), a.getResourceId(R.styleable.TwoWayRattingBar_progress_icon, R.mipmap.ic_height_knob));
        rightProgressIcon = BitmapFactory.decodeResource(context.getResources(), a.getResourceId(R.styleable.TwoWayRattingBar_progress_icon, R.mipmap.ic_height_knob));

        float text_size = a.getDimension(R.styleable.TwoWayRattingBar_text_size, 24f);
        int text_color = a.getColor(R.styleable.TwoWayRattingBar_text_color, color_line_select);
        a.recycle();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(text_size);
        textPaint.setColor(text_color);
        textPaint.setStyle(Paint.Style.STROKE);
    }

    public void setLeftProgress(float progress) {
        float end_progress;
        end_progress = rightProgress - (leftProgressIcon.getWidth() * 1.0f / getWidth()) * 2;
        if (progress <= 0f) {
            progress = 0f;
        }
        if (progress >= end_progress) {
            progress = end_progress;
        }
        this.leftProgress = progress;
        if (onProgressChangeListener != null) {
            onProgressChangeListener.onLeftProgressChange(progress);
        }
        invalidate();
    }

    public void setRightProgress(float progress) {
        float start_progress;
        start_progress = leftProgress + (leftProgressIcon.getWidth() * 1.0f / getWidth()) * 2;
        if (progress <= start_progress) {
            progress = start_progress;
        }
        if (progress >= 1f) {
            progress = 1f;
        }
        this.rightProgress = progress;
        if (onProgressChangeListener != null) {
            onProgressChangeListener.onRightProgressChange(progress);
        }
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 获取对应高度
        int height = getHeight();
        // 获取对应宽度
        int width = getWidth();
        //先画全部进度条
        paint.setColor(color_line_normal);
        paint.setStrokeWidth(stroke_width_normal);
        canvas.drawLine(0, height / 2, width, height / 2, paint);

        //左侧按钮
        float startProgressX = width * leftProgress;
        canvas.drawBitmap(leftProgressIcon, startProgressX, (height - leftProgressIcon.getHeight()) / 2.0f, paint);

        //右侧按钮
        float endProgressX = width * rightProgress - rightProgressIcon.getWidth();
        canvas.drawBitmap(rightProgressIcon, endProgressX, (height - rightProgressIcon.getHeight()) / 2.0f, paint);
        //两个按钮之间的进度线
        float startX = startProgressX + leftProgressIcon.getWidth();
        paint.setColor(color_line_select);
        paint.setStrokeWidth(stroke_width_select);
        //画当前进度线
        canvas.drawLine(startX, height / 2, endProgressX, height / 2, paint);

        //画刻度值
        String left_num = String.valueOf((int) (leftProgress * num));
        String right_num = String.valueOf((int) (rightProgress * num));
        textPaint.getTextBounds(right_num, 0, right_num.length(), bounds);
        canvas.drawText(left_num, startProgressX, height / 2 - leftProgressIcon.getHeight() / 2, textPaint);
        canvas.drawText(right_num, endProgressX - bounds.width() / 2, height / 2 + rightProgressIcon.getHeight(), textPaint);
    }

    /**
     * @param pointX
     * @param pointY
     * @return 0表示触摸点不在两头按钮范围内，1表示触摸点落在左侧按钮范围内，2表示触摸点落在右侧按钮范围内
     */
    private int checkTouchStatus(float pointX, float pointY) {
        //注释掉的是Y轴上触摸点是否落在按钮的高度范围之内，之所以注释是为了触摸的范围大一点
//        if (pointY >= (getHeight() - leftProgressIcon.getHeight()) / 2.0f && pointY <= getHeight() - (getHeight() - leftProgressIcon.getHeight()) / 2.0f) {
        if (pointX >= getWidth() * leftProgress && pointX <= getWidth() * leftProgress + leftProgressIcon.getWidth()) {
            return 1;
        }
        if (pointX >= getWidth() * rightProgress - rightProgressIcon.getWidth() && pointX <= getWidth() * rightProgress) {
            return 2;
        }
//        }
        return 0;
    }

    private int touchStatus;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //down的时候记录下触摸点X位置以及触摸点是否落在有效范围的状态
                touchStatus = checkTouchStatus(event.getX(), event.getY());
                pressX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                //触摸点落在有效范围内则跟随手指移动
                float change_progress = (event.getX() - pressX) / getWidth();
                if (touchStatus == 1) {
                    setLeftProgress(leftProgress + change_progress);
                } else if (touchStatus == 2) {
                    setRightProgress(rightProgress + change_progress);
                }
                pressX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                touchStatus = 0;
                break;
            case MotionEvent.ACTION_CANCEL:
                touchStatus = 0;
                break;
        }
        return true;
    }
}
