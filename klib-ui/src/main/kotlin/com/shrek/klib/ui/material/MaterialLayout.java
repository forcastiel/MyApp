package com.shrek.klib.ui.material;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shrek.klib.view.adaptation.DimensAdapter;

public abstract class MaterialLayout extends RelativeLayout {
    enum ButtonStatus {
        TOUCH_DOWN, TOUCH_UP, RIPPLE_FINISH, NORMAL
    }

    protected ButtonStatus status = ButtonStatus.NORMAL;
    int background;

    float rippleSpeed = 12f;
//	int rippleSize = 3;

    //花费的动画时间(MS)
    float spendRippleTime = 300f;

    Integer rippleColor;
    OnClickListener onClickListener;
    int backgroundColor = Color.parseColor("#1E88E5");
    TextView textButton;

    // ### RIPPLE EFFECT ###
    float x = -1, y = -1;
    //弧度 limitSize限制的尺寸  错过即停止绘画
    float radius = -1, limitSize = 0;
    long lastDrawTime = 0;

    Paint paint = new Paint();

    public MaterialLayout(Context context) {
        super(context);
        setDefaultProperties();
        if (rippleColor == null)
            rippleColor = Color.parseColor("#50000000");
    }

    protected void setDefaultProperties() {
        // Background shape
        setBackgroundResource(background);
        setBackgroundColor(backgroundColor);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            x = event.getX();
            y = event.getY();
            if (event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_MOVE) {
                switchStatus(ButtonStatus.TOUCH_DOWN);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                switchStatus(ButtonStatus.TOUCH_UP);
            } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                switchStatus(ButtonStatus.NORMAL);
            }
        }
        return true;
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (!gainFocus) {
            x = -1;
            y = -1;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // super.onInterceptTouchEvent(ev);
        return true;
    }

    public Bitmap makeCircle() {
        Bitmap output = Bitmap.createBitmap(
                getWidth() - (int) DimensAdapter.INSTANCE.getDip2(), getHeight()
                        - (int)DimensAdapter.INSTANCE.getDip2(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawARGB(0, 0, 0, 0);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(rippleColor);
        canvas.drawCircle(x, y, radius, paint);

        if (radius < limitSize) {
            final long CurTime = System.currentTimeMillis();
            float timeGap = CurTime - lastDrawTime;
            lastDrawTime = CurTime;

            radius += (rippleSpeed * timeGap);
        } else {
            switchStatus(ButtonStatus.NORMAL);
            if (onClickListener != null) {
                onClickListener.onClick(this);
            }
        }
        return output;
    }

    /**
     * 水波纹的动画 是否在進行中
     *
     * @return
     */
    protected boolean isRippleAvaid() {
        return lastDrawTime > 0;
    }

    /**
     * 点击的坐标是否在区域内
     *
     * @return
     */
    private boolean isPositionAvaid(float touchX, float touchY) {
        return touchX > 0
                && touchX < getWidth()
                && touchY > 0
                && touchY < getHeight();
    }

    private void reset() {
        x = -1;
        y = -1;
        rippleSpeed = 0;
        radius = 0;
        limitSize = 0;
        lastDrawTime = 0;
    }

    /**
     * Make a dark color to ripple effect
     *
     * @return
     */
    protected int makePressColor() {
        int r = (this.backgroundColor >> 16) & 0xFF;
        int g = (this.backgroundColor >> 8) & 0xFF;
        int b = (this.backgroundColor >> 0) & 0xFF;
        r = (r - 30 < 0) ? 0 : r - 30;
        g = (g - 30 < 0) ? 0 : g - 30;
        b = (b - 30 < 0) ? 0 : b - 30;
        return Color.rgb(r, g, b);
    }

    /**
     * 计算 滑动速度
     *
     * @return
     */
    private void computeRippleSpeed(float touchX, float touchY) {
        float maxGap = 0;
        maxGap = Math.abs(touchX - getLeft());
        maxGap = Math.max(maxGap, Math.abs(getLeft() + getWidth() - touchX));
        maxGap = Math.max(maxGap, Math.abs(getTop() + getWidth() - touchY));
        maxGap = Math.max(maxGap, Math.abs(getTop() - touchY));

        limitSize = maxGap;
        rippleSpeed = maxGap / spendRippleTime;

        radius = paint.getTextSize();
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        onClickListener = l;
    }

    void switchStatus(ButtonStatus swStatus) {
        if (status != ButtonStatus.TOUCH_DOWN && status == swStatus) {
            return;
        }

        status = swStatus;
        switch (status) {
            case NORMAL:
                reset();
                invalidate();
                break;
            case TOUCH_DOWN:
                if (isPositionAvaid(x, y)) {
                    computeRippleSpeed(x, y);
                    invalidate();
                } else {
                    switchStatus(ButtonStatus.NORMAL);
                }
                break;
            case TOUCH_UP:
                computeRippleSpeed(x, y);
                lastDrawTime = System.currentTimeMillis();
                invalidate();
                break;
            case RIPPLE_FINISH:
                switchStatus(ButtonStatus.NORMAL);
                break;
            }
    }

    public void setText(String text) {
        textButton.setText(text);
    }

    public void setTextColor(int color) {
        textButton.setTextColor(color);
    }

    public TextView getTextView() {
        return textButton;
    }

    public String getText() {
        return textButton.getText().toString();
    }

    public void setRippleColor(Integer rippleColor) {
        this.backgroundColor = rippleColor;
        this.rippleColor = makePressColor();
    }
}
