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
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;

public class ColorPicker extends View {
	private Paint mPaint;
	private Paint mCenterPaint;
	private Paint mCenterPaintColor;
	private int[] mColors;
	private Path mCenterPath = new Path();
	private PathMeasure mPathMeasure = new PathMeasure();
	private float[] mPointerPosition = new float[2];
	private boolean isFirstTime = true;
	private float radians;
	private int mWheelSize;
	private int mPointerSize;
	private RectF colorWheelRectangle = new RectF();
	private static final float PI = 3.1415926f;
	private boolean onPointer = false;
	private int color;

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
		radians = ((getWidth() - getPaddingLeft() - getPaddingRight()) * 0.5f);
		colorWheelRectangle.set(-radians + mPointerSize, -radians + mPointerSize, radians
				- mPointerSize, radians - mPointerSize);
		mCenterPath.addOval(colorWheelRectangle, Path.Direction.CW);
		mPathMeasure.setPath(mCenterPath, true);

		canvas.translate(radians, radians);
		canvas.drawOval(colorWheelRectangle, mPaint);

		if (isFirstTime) {
			mPointerPosition = findMinDistanceVector(0, (int) -radians);
			if (isInEditMode()) {
				mPointerPosition[0] = 0;
				mPointerPosition[1] = -radians + mPointerSize;
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
		int max = Math.max(width, height);
		setMeasuredDimension(max, max);
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
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if ((x - radians) >= (mPointerPosition[0] - 48)
					&& (x - radians) <= (mPointerPosition[0] + 48)
					&& (y - radians) >= (mPointerPosition[1] - 48)
					&& (y - radians) <= (mPointerPosition[1] + 48)) {
				onPointer = true;
				invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (onPointer) {
				mPointerPosition = findMinDistanceVector((int) (x - radians),
						((int) (y - radians)));
				float angle = (float) java.lang.Math.atan2((y - radians), (x - radians));
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

	private float[] findMinDistanceVector(int x, int y) {

		float[] xy = new float[2];
		float[] ten = new float[2];

		float distanceVectorOld = Float.MAX_VALUE;
		float distanceVectorNew = 0;
		float[] minXY = new float[2];
		for (float distance = 0; distance < mPathMeasure.getLength(); distance++) {

			mPathMeasure.getPosTan(distance, xy, ten);

			distanceVectorNew = dist(x, y, xy[0], xy[1]);

			if (distanceVectorNew < distanceVectorOld) {

				minXY[0] = xy[0];
				minXY[1] = xy[1];
				distanceVectorOld = distanceVectorNew;

			}
		}

		return minXY;
	}

	private float dist(float x1, float y1, float x2, float y2) {
		float distX = x1 - x2;
		float distY = y1 - y2;
		return FloatMath.sqrt(distX * distX + distY * distY);
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
