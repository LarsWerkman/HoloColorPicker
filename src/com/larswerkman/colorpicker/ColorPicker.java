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
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorPicker extends View {
	private Paint mPaint;
	private Paint mCenterPaint;
	private Paint mCenterPaintColor;
	private int[] mColors;
	private float[] mPointerPosition = new float[2];
	private boolean isFirstTime = true;
	private int mWheelSize;
	private int mPointerSize;
	private RectF colorWheelRectangle = new RectF();
	private static final float PI = 3.1415926f;
	private boolean onPointer = false;
	private int color;

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

		mColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF,
				0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };
		Shader s = new SweepGradient(0, 0, mColors, null);

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setShader(s);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(mWheelSize);

		mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCenterPaint.setColor(Color.BLACK);
		mCenterPaint.setStrokeWidth(5);
		mCenterPaint.setAlpha(0x60);

		mCenterPaintColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCenterPaintColor.setStrokeWidth(5);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		colorWheelRectangle.set(-mColorWheelRadius, -mColorWheelRadius, mColorWheelRadius,
				mColorWheelRadius);

		canvas.translate(mTranslationOffset, mTranslationOffset);
		canvas.drawOval(colorWheelRectangle, mPaint);

		if (isFirstTime) {
			mPointerPosition = calculatePointerPosition((float) (-Math.PI / 2));
			if (isInEditMode()) {
				mPointerPosition[0] = 0;
				mPointerPosition[1] = -mColorWheelRadius;
			}
			mCenterPaintColor.setColor(interpColor(mColors, calculateUnit()));
			isFirstTime = false;
		}
		canvas.drawCircle(mPointerPosition[0], mPointerPosition[1],
				mPointerSize, mCenterPaint);
		canvas.drawCircle(mPointerPosition[0], mPointerPosition[1],
				(float) (mPointerSize / 1.2), mCenterPaintColor);
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
	}

	private int ave(int s, int d, float p) {
		return s + java.lang.Math.round(p * (d - s));
	}

	private float calculateUnit(){
		float angle = (float) java.lang.Math.atan2(mPointerPosition[1],
				mPointerPosition[0]);
		float unit = angle / (2 * PI);
		if (unit < 0) {
			unit += 1;
		}
		return unit;
	}

	private int interpColor(int colors[], float unit) {
		if (unit <= 0) {
			return colors[0];
		}
		if (unit >= 1) {
			return colors[colors.length - 1];
		}

		float p = unit * (colors.length - 1);
		int i = (int) p;
		p -= i;

		int c0 = colors[i];
		int c1 = colors[i + 1];
		int a = ave(Color.alpha(c0), Color.alpha(c1), p);
		int r = ave(Color.red(c0), Color.red(c1), p);
		int g = ave(Color.green(c0), Color.green(c1), p);
		int b = ave(Color.blue(c0), Color.blue(c1), p);

		color = Color.argb(a, r, g, b);
		return Color.argb(a, r, g, b);
	}

	public int getColor() {
		return color;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Convert coordinates to our internal coordinate system
		float x = event.getX() - mTranslationOffset;
		float y = event.getY() - mTranslationOffset;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (x >= (mPointerPosition[0] - 48) && x <= (mPointerPosition[0] + 48)
					&& y >= (mPointerPosition[1] - 48) && y <= (mPointerPosition[1] + 48)) {
				onPointer = true;
				invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (onPointer) {
				float angle = (float) java.lang.Math.atan2(y, x);
				mPointerPosition = calculatePointerPosition(angle);

				float unit = angle / (2 * PI);
				if (unit < 0) {
					unit += 1;
				}
				mCenterPaintColor.setColor(interpColor(mColors, unit));
				invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			onPointer = false;
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
		return new SavedState(superState, mPointerPosition[0],
				mPointerPosition[1], booleanToInt(isFirstTime), calculateUnit());
	}

	private int booleanToInt(boolean bool) {
		if (bool) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		mPointerPosition[0] = savedState.getPositionX();
		mPointerPosition[1] = savedState.getPositionY();
		isFirstTime = savedState.getBoolFirstTime();
		mCenterPaintColor.setColor(interpColor(mColors, savedState.getUnit()));
	}

	protected static class SavedState extends BaseSavedState {

		private final float positionX;
		private final float positionY;
		private final int boolFirstTime;
		private final float unit;

		private SavedState(Parcelable superState, float positionX,
				float positionY, int boolFirstTime, float unit) {
			super(superState);
			this.positionX = positionX;
			this.positionY = positionY;
			this.boolFirstTime = boolFirstTime;
			this.unit = unit;
		}

		private SavedState(Parcel in) {
			super(in);
			positionX = in.readFloat();
			positionY = in.readFloat();
			boolFirstTime = in.readInt();
			unit = in.readFloat();
		}

		public float getPositionX() {
			return positionX;
		}

		public float getPositionY() {
			return positionY;
		}

		public boolean getBoolFirstTime() {
			if(boolFirstTime == 0){
				return true;
			} else {
				return false;
			}
		}

		public float getUnit(){
			return unit;
		}

		@Override
		public void writeToParcel(Parcel destination, int flags) {
			super.writeToParcel(destination, flags);
			destination.writeFloat(positionX);
			destination.writeFloat(positionY);
			destination.writeInt(boolFirstTime);
			destination.writeFloat(unit);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}

		};

	}

}
