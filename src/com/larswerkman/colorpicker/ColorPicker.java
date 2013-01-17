/*
* Copyright 2012 Lars Werkman
*
* Licensed under the Apache License, Version 2.0 (the 	"License");
* you may not use this file except in compliance with 	the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in 	writing, software
* distributed under the License is distributed on an 	"AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 	either express or implied.
* See the License for the specific language governing 	permissions and
* limitations under the License.
*/

package com.larswerkman.colorpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorPicker extends View {
	/*
	 * Constants used to save/restore the instance state.
	 */
	private static final String STATE_PARENT = "parent";
	private static final String STATE_ANGLE = "angle";

	/**
	 * Colors to construct the color wheel using {@link SweepGradient}.
	 */
	private static final int[] COLORS = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF,
		0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };


	private Paint mColorWheelPaint;
	private Paint mPointerHaloPaint;
	private Paint mPointerColor;
	private int mWheelSize;
	private int mPointerSize;
	private RectF mColorWheelRectangle = new RectF();
	private boolean mUserIsMovingPointer = false;
	private int mColor;

	/**
	 * Number of pixels the origin of this view is moved in X- and Y-direction.
	 *
	 * <p>
	 * We use the center of this (quadratic) View as origin of our internal coordinate system.
	 * Android uses the upper left corner as origin for the View-specific coordinate system. So this
	 * is the value we use to translate from one coordinate system to the other.
	 * </p>
	 *
	 * <p>Note: (Re)calculated in {@link #onMeasure(int, int)}.</p>
	 *
	 * @see #onDraw(Canvas)
	 */
	private float mTranslationOffset;

	/**
	 * Radius of the color wheel in pixels.
	 *
	 * <p>Note: (Re)calculated in {@link #onMeasure(int, int)}.</p>
	 */
	private float mColorWheelRadius;

	/**
	 * The pointer's position expressed as angle (in rad).
	 */
	private float mAngle;


	public ColorPicker(Context context) {
		super(context);
		init(null, 0);
	}

	public ColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public ColorPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.ColorPicker, defStyle, 0);

		mWheelSize = a.getInteger(R.styleable.ColorPicker_wheel_size, 16);
		mPointerSize = a.getInteger(R.styleable.ColorPicker_pointer_size, 48);

		a.recycle();

		Shader s = new SweepGradient(0, 0, COLORS, null);

		mColorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mColorWheelPaint.setShader(s);
		mColorWheelPaint.setStyle(Paint.Style.STROKE);
		mColorWheelPaint.setStrokeWidth(mWheelSize);

		mPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPointerHaloPaint.setColor(Color.BLACK);
		mPointerHaloPaint.setStrokeWidth(5);
		mPointerHaloPaint.setAlpha(0x60);

		mPointerColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPointerColor.setStrokeWidth(5);

		mAngle = (float) (-Math.PI / 2);
		mPointerColor.setColor(calculateColor(mAngle));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.translate(mTranslationOffset, mTranslationOffset);
		canvas.drawOval(mColorWheelRectangle, mColorWheelPaint);

		float[] pointerPosition = calculatePointerPosition(mAngle);
		canvas.drawCircle(pointerPosition[0], pointerPosition[1],
				mPointerSize, mPointerHaloPaint);
		canvas.drawCircle(pointerPosition[0], pointerPosition[1],
				(float) (mPointerSize / 1.2), mPointerColor);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = getDefaultSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		int min = Math.min(width, height);
		setMeasuredDimension(min, min);

		mTranslationOffset = min * 0.5f;
		mColorWheelRadius = mTranslationOffset - mPointerSize;

		mColorWheelRectangle.set(-mColorWheelRadius, -mColorWheelRadius, mColorWheelRadius,
				mColorWheelRadius);
	}

	private int ave(int s, int d, float p) {
		return s + java.lang.Math.round(p * (d - s));
	}

	/**
	 * Calculate the color using the supplied angle.
	 *
	 * @param angle
	 *         The selected color's position expressed as angle (in rad).
	 *
	 * @return The ARGB value of the color on the color wheel at the specified angle.
	 */
	private int calculateColor(float angle) {
		float unit = (float) (angle / (2 * Math.PI));
		if (unit < 0) {
			unit += 1;
		}

		if (unit <= 0) {
			return COLORS[0];
		}
		if (unit >= 1) {
			return COLORS[COLORS.length - 1];
		}

		float p = unit * (COLORS.length - 1);
		int i = (int) p;
		p -= i;

		int c0 = COLORS[i];
		int c1 = COLORS[i + 1];
		int a = ave(Color.alpha(c0), Color.alpha(c1), p);
		int r = ave(Color.red(c0), Color.red(c1), p);
		int g = ave(Color.green(c0), Color.green(c1), p);
		int b = ave(Color.blue(c0), Color.blue(c1), p);

		mColor = Color.argb(a, r, g, b);
		return Color.argb(a, r, g, b);
	}

	public int getColor() {
		return mColor;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Convert coordinates to our internal coordinate system
		float x = event.getX() - mTranslationOffset;
		float y = event.getY() - mTranslationOffset;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			float[] pointerPosition = calculatePointerPosition(mAngle);
			if (x >= (pointerPosition[0] - 48) && x <= (pointerPosition[0] + 48)
					&& y >= (pointerPosition[1] - 48) && y <= (pointerPosition[1] + 48)) {
				mUserIsMovingPointer = true;
				invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mUserIsMovingPointer) {
				mAngle = (float) java.lang.Math.atan2(y, x);
				mPointerColor.setColor(calculateColor(mAngle));
				invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			mUserIsMovingPointer = false;
			break;
		}
		return true;
	}

	/**
	 * Calculate the pointer's coordinates on the color wheel using the supplied angle.
	 *
	 * @param angle
	 *         The position of the pointer expressed as angle (in rad).
	 *
	 * @return The coordinates of the pointer's center in our internal coordinate system.
	 */
	private float[] calculatePointerPosition(float angle) {
		float x = (float) (mColorWheelRadius * Math.cos(angle));
		float y = (float) (mColorWheelRadius * Math.sin(angle));

		return new float[] { x, y };
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		Bundle state = new Bundle();
		state.putParcelable(STATE_PARENT, superState);
		state.putFloat(STATE_ANGLE, mAngle);

		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle savedState = (Bundle) state;

		Parcelable superState = savedState.getParcelable(STATE_PARENT);
		super.onRestoreInstanceState(superState);

		mAngle = savedState.getFloat(STATE_ANGLE);
		mPointerColor.setColor(calculateColor(mAngle));
	}
}
