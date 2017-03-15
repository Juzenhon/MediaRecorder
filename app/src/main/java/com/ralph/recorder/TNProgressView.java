package com.ralph.recorder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Description :
 * Created by zhuxinhong on 2016/12/28.
 * Job number：135198
 * Phone ：13520295328
 * Email：zhuxinhong@syswin.com
 * Person in charge : zhuxinhong
 * Leader：zhuxinhong
 */
public class TNProgressView extends View {

    private int duration;

    private long startTime;

    private Rect rect = new Rect();

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean isNormal = true;

    private int normalColor, cancelColor;

    private boolean isProgressing = false;

    public TNProgressView(Context context) {
        this(context, null);
    }

    public TNProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        normalColor = Color.CYAN;
        cancelColor = Color.RED;
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (duration != 0) {
            long delta = System.currentTimeMillis() - startTime;
            if(delta < duration){
                float ratio = delta / 2.0f / duration;
                int left = (int) (getWidth() * ratio);
                if (isNormal) {
                    mPaint.setColor(normalColor);
                } else {
                    mPaint.setColor(cancelColor);
                }
                rect.set(left, 0, getWidth() - left, getBottom());
                canvas.drawRect(rect, mPaint);
            }else {
                isProgressing = false;
            }
        }
        if(isProgressing){
            invalidate();
        }
    }

    /**
     *
     * @param duration second
     */
    public void setDuration(int duration) {
        this.duration = duration * 1000;
        isNormal = true;
        invalidate();
    }

    public void start(){
        isProgressing = true;
        startTime = System.currentTimeMillis();
        invalidate();
    }

    public void stop(){
        isProgressing = false;
        invalidate();
    }

    public void setNormal(boolean normal) {
        isNormal = normal;
    }



}
