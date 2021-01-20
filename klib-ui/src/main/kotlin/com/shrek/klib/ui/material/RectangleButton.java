package com.shrek.klib.ui.material;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shrek.klib.view.adaptation.DimensAdapter;

public class RectangleButton extends MaterialLayout {

    TextView textButton;

    //圆角角度
    int cornerRadius;
    //阴影宽度
    int shadowWidth;

    public RectangleButton(Context context) {
        super(context);
        setDefaultProperties();
    }

    @Override
    protected void setDefaultProperties() {
        super.setDefaultProperties();
        cornerRadius = (int) DimensAdapter.INSTANCE.getDip2();
        shadowWidth = (int)DimensAdapter.INSTANCE.getDip2();

        if (textButton == null) {
            textButton = new TextView(getContext());
            textButton.setTextColor(Color.WHITE);
//            textButton.setTypeface(null, Typeface.BOLD);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            int hVal = (int)DimensAdapter.INSTANCE.getDip8();
            int vVal = (int)DimensAdapter.INSTANCE.getDip12();
            params.setMargins(hVal,vVal,hVal, vVal);
            textButton.setLayoutParams(params);
            addView(textButton);
        }
    }


    public void setPadding(int paddingLeft,int paddingTop, int paddingRight, int paddingBottom){
        textButton.setPadding(paddingLeft,paddingTop,paddingRight,paddingBottom);
    }

    public void setBtnText(String text, int textColor) {
        textButton.setText(text);
        textButton.setTextColor(textColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (status != ButtonStatus.NORMAL) {
            int dip1 = (int)DimensAdapter.INSTANCE.getDip1();
            int dip2 = (int)DimensAdapter.INSTANCE.getDip2();
            Rect src = new Rect(0, 0, getWidth() - dip2, getHeight() - dip2);
            Rect dst = new Rect(dip1, dip1, getWidth() - dip2, getHeight() - dip2);
            canvas.drawBitmap(makeCircle(), src, dst, null);
        }

        if(status == ButtonStatus.TOUCH_UP){
            invalidate();
        }
    }


    /**
     * 设置按钮的颜色
     * @param colorVal
     */
    public void setButtonColor(int colorVal) {
        GradientDrawable mDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] { 0x80000000,
                0x50000000 });
        mDrawable.setShape(GradientDrawable.RECTANGLE);
        mDrawable.setCornerRadius(cornerRadius);
//        mDrawable.setGradientRadius((float) (Math.sqrt(2) * 60));

        GradientDrawable bottomDrawable = new GradientDrawable();
        bottomDrawable.setColor(colorVal);
        bottomDrawable.setShape(GradientDrawable.RECTANGLE);
        bottomDrawable.setCornerRadius(cornerRadius);
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{mDrawable,bottomDrawable});

        layerDrawable.setLayerInset(1,0,0,shadowWidth,shadowWidth);
        layerDrawable.setLayerInset(0,shadowWidth,shadowWidth,0,0);

        setBackgroundDrawable(layerDrawable);

        setRippleColor(colorVal);
    }

    public TextView getTextButton() {
        return textButton;
    }

    /**
     *
     * @param cornerRadius 单位像素
     */
    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    /**
     *
     * @param shadowWidth 单位像素
     */
    public void setShadowWidth(int shadowWidth) {
        this.shadowWidth = shadowWidth;
    }
}
