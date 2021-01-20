package com.shrek.klib.ui.material;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shrek.klib.colligate.DimenUtils;
import com.shrek.klib.ui.CommonUiSetup;
import com.shrek.klib.ui.R;

public class MaterialCheckBox extends RelativeLayout {

	int backgroundColor = CommonUiSetup.INSTANCE.getPramaryColor();
	final int disabledBackgroundColor = Color.parseColor("#E2E2E2");
	
	Check checkView;
	int beforeBackground;
	boolean press = false;
	boolean check = false;
	boolean animation = false;
	public boolean isLastTouch = false;
	
	OnCheckListener onCheckListener;

	public MaterialCheckBox(Context context,String text,final boolean isCheck) {
		super(context);
		setMinimumHeight(DimenUtils.dpToPx(25, context));
		setMinimumWidth(DimenUtils.dpToPx(25, context));

		setBackgroundResource(R.drawable.background_checkbox);
		post(new Runnable() {

			@Override
			public void run() {
				setChecked(isCheck);
				setPressed(false);
				changeBackgroundColor(getResources().getColor(
						android.R.color.transparent));
			}
		});

		checkView = new Check(getContext());
		checkView.setId(R.id.btn_datetime_cancel);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(DimenUtils.dpToPx(20,
				context), DimenUtils.dpToPx(20, context));
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		checkView.setLayoutParams(params);
		addView(checkView);
		
		TextView textView = new TextView(getContext());
		RelativeLayout.LayoutParams textViewLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		textViewLayoutParams.addRule(RelativeLayout.RIGHT_OF, checkView.getId());
		textViewLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		textViewLayoutParams.setMargins(10, 0, 0, 0);
		textView.setLayoutParams(textViewLayoutParams);
		textView.setText(text);

		addView(textView);
	}

	@Override
	public void invalidate() {
		checkView.invalidate();
		super.invalidate();
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		invalidate();
		if (isEnabled()) {
			isLastTouch = true;
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				changeBackgroundColor((check) ? makePressColor() : Color
						.parseColor("#446D6D6D"));
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				changeBackgroundColor(getResources().getColor(
						android.R.color.transparent));
				press = false;
				if ((event.getX() <= getWidth() && event.getX() >= 0)
						&& (event.getY() <= getHeight() && event.getY() >= 0)) {
					isLastTouch = false;
					check = !check;
					if (onCheckListener != null)
						onCheckListener.onCheck(MaterialCheckBox.this, check);
					if (check) {
						step = 0;
					}
					if (check)
						checkView.changeBackground();
				}
			}
		}
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(animation){ invalidate(); }
			
		if (press) {
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor((check) ? makePressColor() : Color
					.parseColor("#446D6D6D"));
			canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2,
					paint);
			invalidate();
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if(enabled)
			setBackgroundColor(beforeBackground);
		else
			setBackgroundColor(disabledBackgroundColor);
		invalidate();
	}

	private void changeBackgroundColor(int color) {
		LayerDrawable layer = (LayerDrawable) getBackground();
		GradientDrawable shape = (GradientDrawable) layer
				.findDrawableByLayerId(R.id.shape_bacground);
		shape.setColor(color);
	}

	/**
	 * Make a dark color to press effect
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
		return Color.argb(70, r, g, b);
	}

	@Override
	public void setBackgroundColor(int color) {
		backgroundColor = color;
		if (isEnabled()){
			beforeBackground = backgroundColor;
		}
		changeBackgroundColor(color);
	}

	public void setChecked(boolean check) {
		invalidate();
		this.check = check;
		setPressed(false);
		changeBackgroundColor(getResources().getColor(
				android.R.color.transparent));
		if (check) {
			step = 0;
		}
		if (check)
			checkView.changeBackground();

	}

	public boolean isCheck() {
		return check;
	}

	@Override
	protected void onAnimationStart() {
		super.onAnimationStart();
		animation = true;
	}

	@Override
	protected void onAnimationEnd() {
		super.onAnimationEnd();
		animation = false;
	}

	// Indicate step in check animation
	int step = 0;
	int stepUnitVal = 0;

	// View that contains checkbox
	class Check extends View {

		Bitmap sprite;

		public Check(Context context) {
			super(context);
			setBackgroundResource(R.drawable.background_checkbox_uncheck);
			sprite = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.common_sprite_check);
		}

		public void changeBackground() {
			if (check) {
				setBackgroundResource(R.drawable.background_checkbox_check);
				LayerDrawable layer = (LayerDrawable) getBackground();
				GradientDrawable shape = (GradientDrawable) layer
						.findDrawableByLayerId(R.id.shape_bacground);
				shape.setColor(backgroundColor);
			} else {
				setBackgroundResource(R.drawable.background_checkbox_uncheck);
			}
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			if (check) {
				if (step < 11){
					step++;
					invalidate();
				}
			} else {
				if (step >= 0){
					step--;
					invalidate();
				}
				if (step == -1){
					invalidate();
					changeBackground();
				}
			}
			
			if(stepUnitVal == 0) {
				BitmapDrawable drawable = (BitmapDrawable)(getResources().getDrawable(R.drawable.common_sprite_check));
				Bitmap bitmap = drawable.getBitmap();
				stepUnitVal = bitmap.getWidth()/12;
			}
			Rect src = new Rect(stepUnitVal * step, 0, (stepUnitVal * step) + stepUnitVal, stepUnitVal);
			Rect dst = new Rect(0, 0, this.getWidth() - 2, this.getHeight());
			canvas.drawBitmap(sprite, src, dst, null);

		}

	}

	public void setOncheckListener(OnCheckListener onCheckListener) {
		this.onCheckListener = onCheckListener;
	}

	public interface OnCheckListener {
		public void onCheck(MaterialCheckBox view, boolean check);
	}

}
