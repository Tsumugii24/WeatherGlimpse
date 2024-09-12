package com.yui.weatherglimpse.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.yui.weatherglimpse.R;

public class WeatherMoodView extends View {
    private Paint paint;
    private int mood = 0; // 0: 晴朗, 1: 多云, 2: 雨天

    public WeatherMoodView(Context context) {
        super(context);
        init();
    }

    public WeatherMoodView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 4;

        // 绘制脸
        paint.setColor(ContextCompat.getColor(getContext(), R.color.yellow));
        canvas.drawCircle(centerX, centerY, radius, paint);

        // 绘制眼睛
        paint.setColor(ContextCompat.getColor(getContext(), R.color.black));
        canvas.drawCircle(centerX - radius / 3, centerY - radius / 3, radius / 10, paint);
        canvas.drawCircle(centerX + radius / 3, centerY - radius / 3, radius / 10, paint);

        // 根据心情绘制嘴巴
        switch (mood) {
            case 0: // 晴朗 - 微笑
                canvas.drawArc(centerX - radius / 2, centerY - radius / 2, 
                               centerX + radius / 2, centerY + radius / 2, 
                               45, 90, false, paint);
                break;
            case 1: // 多云 - 平直
                canvas.drawLine(centerX - radius / 2, centerY + radius / 3, 
                                centerX + radius / 2, centerY + radius / 3, paint);
                break;
            case 2: // 雨天 - 皱眉
                canvas.drawArc(centerX - radius / 2, centerY + radius / 4, 
                               centerX + radius / 2, centerY + radius, 
                               225, 90, false, paint);
                break;
        }
    }

    public void setMood(int mood) {
        this.mood = mood;
        invalidate();
    }
}