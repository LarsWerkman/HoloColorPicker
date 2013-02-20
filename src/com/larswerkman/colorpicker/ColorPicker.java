/*
 * Copyright 2012 Lars Werkman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.larswerkman.colorpicker;

import android.content.Context;
import android.content.res.Resources;
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

/**
 * Displays a holo-themed color picker.
 * 
 * <p>
 * Use {@link #getColor()} to retrieve the selected color. <br>
 * Use {@link #addSVBar(SVBar)} to add a Saturation/Value Bar. <br>
 * Use {@link #addOpacityBar(OpacityBar)} to add a Opacity Bar.
 * </p>
 */
public class ColorPicker extends View {
	/*
	 * Constants used to save/restore the instance state.
	 */
	private static final String STATE_PARENT = "parent";
	private static final String STATE_ANGLE = "angle";
	private static final String STATE_OLD_COLOR = "color";

	/**
	 * Colors to construct the color wheel using {@link SweepGradient}.
	 * 
	 * <p>
	 * Note: The algorithm in {@link #normalizeColor(int)} highly depends on
	 * these exact values. Be aware that {@link #setColor(int)} might break if
	 * you change this array.
	 * </p>
	 */
	private static final int[] COLORS = new int[] { 0xFFFF0000, 0xFFFF00FF,
			0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };

	/**
	 * {@code Paint} instance used to draw the color wheel.
	 */
	private Paint mColorWheelPaint;

	/**
	 * {@code Paint} instance used to draw the pointer's "halo".
	 */
	private Paint mPointerHaloPaint;

	/**
	 * {@code Paint} instance used to draw the pointer (the selected color).
	 */
	private Paint mPointerColor;

	/**
	 * The width of the color wheel thickness.
	 */
	private int mColorWheelThickness;

	/**
	 * The radius of the color wheel.
	 */
	private int mColorWheelRadius;
	private int mPreferredColorWheelRadius;

	/**
	 * The radius of the center circle inside the color wheel.
	 */
	private int mColorCenterRadius;
	private int mPreferredColorCenterRadius;

	/**
	 * The radius of the halo of the center circle inside the color wheel.
	 */
	private int mColorCenterHaloRadius;
	private int mPreferredColorCenterHaloRadius;

	/**
	 * The radius of the pointer.
	 */
	private int mColorPointerRadius;

	/**
	 * The radius of the halo of the pointer.
	 */
	private int mColorPointerHaloRadius;

	/**
	 * The rectangle enclosing the color wheel.
	 */
	private RectF mColorWheelRectangle = new RectF();

	/**
	 * The rectangle enclosing the center inside the color wheel.
	 */
	private RectF mCenterRectangle = new RectF();

	/**
	 * {@code true} if the user clicked on the pointer to start the move mode. <br>
	 * {@code false} once the user stops touching the screen.
	 * 
	 * @see #onTouchEvent(MotionEvent)
	 */
	private boolean mUserIsMovingPointer = false;

	/**
	 * The ARGB value of the currently selected color.
	 */
	private int mColor;

	/**
	 * The ARGB value of the center with the old selected color.
	 */
	private int mCenterOldColor;

	/**
	 * The ARGB value of the center with the new selected color.
	 */
	private int mCenterNewColor;

	/**
	 * Number of pixels the origin of this view is moved in X- and Y-direction.
	 * 
	 * <p>
	 * We use the center of this (quadratic) View as origin of our internal
	 * coordinate system. Android uses the upper left corner as origin for the
	 * View-specific coordinate system. So this is the value we use to translate
	 * from one coordinate system to the other.
	 * </p>
	 * 
	 * <p>
	 * Note: (Re)calculated in {@link #onMeasure(int, int)}.
	 * </p>
	 * 
	 * @see #onDraw(Canvas)
	 */
	private float mTranslationOffset;

	/**
	 * The pointer's position expressed as angle (in rad).
	 */
	private float mAngle;

	/**
	 * {@code Paint} instance used to draw the center with the old selected
	 * color.
	 */
	private Paint mCenterOldPaint;

	/**
	 * {@code Paint} instance used to draw the center with the new selected
	 * color.
	 */
	private Paint mCenterNewPaint;

	/**
	 * {@code Paint} instance used to draw the halo of the center selected
	 * colors.
	 */
	private Paint mCenterHaloPaint;

	/**
	 * An array of floats that can be build into a {@code Color} <br>
	 * Where we can extract the Saturation and Value from.
	 */
	private float[] mHSV = new float[3];

	/**
	 * {@code SVBar} instance used to control the Saturation/Value bar.
	 */
	private SVBar mSVbar = null;

	/**
	 * {@code OpacityBar} instance used to control the Opacity bar.
	 */
	private OpacityBar mOpacityBar = null;

	/**
	 * {@code onColorChangedListener} instance of the onColorChangedListener
	 */
	private OnColorChangedListener onColorChangedListener;

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

	/**
	 * An interface that is called whenever the color is changed. Currently it
	 * is always called when the color is changes.
	 * 
	 * @author lars
	 * 
	 */
	public interface OnColorChangedListener {
		public void onColorChanged(int color);
	}

	/**
	 * Set a onColorChangedListener
	 * 
	 * @param {@code OnColorChangedListener}
	 */
	public void setOnColorChangedListener(OnColorChangedListener listener) {
		this.onColorChangedListener = listener;
	}

	/**
	 * Gets the onColorChangedListener
	 * 
	 * @return {@code OnColorChangedListener}
	 */
	public OnColorChangedListener getOnColorChangedListener() {
		return this.onColorChangedListener;
	}

	private void init(AttributeSet attrs, int defStyle) {
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.ColorPicker, defStyle, 0);
		final Resources b = getContext().getResources();

		mColorWheelThickness = a.getDimensionPixelSize(
				R.styleable.ColorPicker_color_wheel_thickness,
				b.getDimensionPixelSize(R.dimen.color_wheel_thickness));
		mColorWheelRadius = a.getDimensionPixelSize(
				R.styleable.ColorPicker_color_wheel_radius,
				b.getDimensionPixelSize(R.dimen.color_wheel_radius));
		mPreferredColorWheelRadius = mColorWheelRadius;
		mColorCenterRadius = a.getDimensionPixelSize(
				R.styleable.ColorPicker_color_center_radius,
				b.getDimensionPixelSize(R.dimen.color_center_radius));
		mPreferredColorCenterRadius = mColorCenterRadius;
		mColorCenterHaloRadius = a.getDimensionPixelSize(
				R.styleable.ColorPicker_color_center_halo_radius,
				b.getDimensionPixelSize(R.dimen.color_center_halo_radius));
		mPreferredColorCenterHaloRadius = mColorCenterHaloRadius;
		mColorPointerRadius = a.getDimensionPixelSize(
				R.styleable.ColorPicker_color_pointer_radius,
				b.getDimensionPixelSize(R.dimen.color_pointer_radius));
		mColorPointerHaloRadius = a.getDimensionPixelSize(
				R.styleable.ColorPicker_color_pointer_halo_radius,
				b.getDimensionPixelSize(R.dimen.color_pointer_halo_radius));

		a.recycle();

		mAngle = (float) (-Math.PI / 2);

		Shader s = new SweepGradient(0, 0, COLORS, null);

		mColorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mColorWheelPaint.setShader(s);
		mColorWheelPaint.setStyle(Paint.Style.STROKE);
		mColorWheelPaint.setStrokeWidth(mColorWheelThickness);

		mPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPointerHaloPaint.setColor(Color.BLACK);
		mPointerHaloPaint.setAlpha(0x50);

		mPointerColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPointerColor.setColor(calculateColor(mAngle));

		mCenterNewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCenterNewPaint.setColor(calculateColor(mAngle));
		mCenterNewPaint.setStyle(Paint.Style.FILL);

		mCenterOldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCenterOldPaint.setColor(calculateColor(mAngle));
		mCenterOldPaint.setStyle(Paint.Style.FILL);

		mCenterHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCenterHaloPaint.setColor(Color.BLACK);
		mCenterHaloPaint.setAlpha(0x00);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		// All of our positions are using our internal coordinate system.
		// Instead of translating
		// them we let Canvas do the work for us.
		canvas.translate(mTranslationOffset, mTranslationOffset);

		// Draw the color wheel.
		canvas.drawOval(mColorWheelRectangle, mColorWheelPaint);

		float[] pointerPosition = calculatePointerPosition(mAngle);

		// Draw the pointer's "halo"
		canvas.drawCircle(pointerPosition[0], pointerPosition[1],
				mColorPointerHaloRadius, mPointerHaloPaint);

		// Draw the pointer (the currently selected color) slightly smaller on
		// top.
		canvas.drawCircle(pointerPosition[0], pointerPosition[1],
				mColorPointerRadius, mPointerColor);

		// Draw the halo of the center colors.
		canvas.drawCircle(0, 0, mColorCenterHaloRadius, mCenterHaloPaint);

		// Draw the old selected color in the center.
		canvas.drawArc(mCenterRectangle, 90, 180, true, mCenterOldPaint);

		// Draw the new selected color in the center.
		canvas.drawArc(mCenterRectangle, 270, 180, true, mCenterNewPaint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int intrinsicSize = 2 * (mPreferredColorWheelRadius + mColorPointerHaloRadius);

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width;
		int height;

		if (widthMode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else if (widthMode == MeasureSpec.AT_MOST) {
			width = Math.min(intrinsicSize, widthSize);
		} else {
			width = intrinsicSize;
		}

		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else if (heightMode == MeasureSpec.AT_MOST) {
			height = Math.min(intrinsicSize, heightSize);
		} else {
			height = intrinsicSize;
		}

		int min = Math.min(width, height);
		setMeasuredDimension(min, min);
		mTranslationOffset = min * 0.5f;

		// fill the rectangle instances.
		mColorWheelRadius = min / 2 - mColorWheelThickness
				- (mColorPointerHaloRadius / 2);
		mColorWheelRectangle.set(-mColorWheelRadius, -mColorWheelRadius,
				mColorWheelRadius, mColorWheelRadius);

		mColorCenterRadius = (int) ((float) mPreferredColorCenterRadius * ((float) mColorWheelRadius / (float) mPreferredColorWheelRadius));
		mColorCenterHaloRadius = (int) ((float) mPreferredColorCenterHaloRadius * ((float) mColorWheelRadius / (float) mPreferredColorWheelRadius));
		mCenterRectangle.set(-mColorCenterRadius, -mColorCenterRadius,
				mColorCenterRadius, mColorCenterRadius);
	}

	private int ave(int s, int d, float p) {
		return s + java.lang.Math.round(p * (d - s));
	}

	/**
	 * Calculate the color using the supplied angle.
	 * 
	 * @param angle
	 *            The selected color's position expressed as angle (in rad).
	 * 
	 * @return The ARGB value of the color on the color wheel at the specified
	 *         angle.
	 */
	private int calculateColor(float angle) {
		float unit = (float) (angle / (2 * Math.PI));
		if (unit < 0) {
			unit += 1;
		}

		if (unit <= 0) {
			mColor = COLORS[0];
			return COLORS[0];
		}
		if (unit >= 1) {
			mColor = COLORS[COLORS.length - 1];
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

	/**
	 * Get the currently selected color.
	 * 
	 * @return The ARGB value of the currently selected color.
	 */
	public int getColor() {
		return mCenterNewColor;
	}

	/**
	 * Set the color to be highlighted by the pointer. </br> </br> If the
	 * instances {@code SVBar} and the {@code OpacityBar} aren't null the color
	 * will also be set to them
	 * 
	 * @param color
	 *            The RGB value of the color to highlight. If this is not a
	 *            color displayed on the color wheel a very simple algorithm is
	 *            used to map it to the color wheel. The resulting color often
	 *            won't look close to the original color. This is especially
	 *            true for shades of grey. You have been warned!
	 */
	public void setColor(int color) {
		mAngle = colorToAngle(color);
		mPointerColor.setColor(calculateColor(mAngle));

		// check of the instance isn't null
		if (mOpacityBar != null) {
			// set the value of the opacity
			mOpacityBar.setColor(mColor);
			mOpacityBar.setOpacity(Color.alpha(color));
		}

		// check if the instance isn't null
		if (mSVbar != null) {
			// the array mHSV will be filled with the HSV values of the color.
			Color.colorToHSV(color, mHSV);
			mSVbar.setColor(mColor);

			// because of the design of the Saturation/Value bar,
			// we can only use Saturation or Value every time.
			// Here will be checked which we shall use.
			if (mHSV[1] < mHSV[2]) {
				mSVbar.setSaturation(mHSV[1]);
			} else { // if (mHSV[1] > mHSV[2]) {
				mSVbar.setValue(mHSV[2]);
			}
		}

		invalidate();
	}

	/**
	 * Convert a color to an angle.
	 * 
	 * @param color
	 *            The RGB value of the color to "find" on the color wheel.
	 *            {@link #normalizeColor(int)} will be used to map this color to
	 *            one on the color wheel if necessary.
	 * 
	 * @return The angle (in rad) the "normalized" color is displayed on the
	 *         color wheel.
	 */
	private float colorToAngle(int color) {
		int[] colorInfo = normalizeColor(color);
		int normColor = colorInfo[0];
		int colorMask = colorInfo[1];
		int shiftValue = colorInfo[2];

		int anchorColor = (normColor & ~colorMask);

		// Find the "anchor" color in the COLORS array
		for (int i = 0; i < COLORS.length - 1; i++) {
			if (COLORS[i] == anchorColor) {
				int nextValue = COLORS[i + 1];

				double value;
				double decimals = ((normColor >> shiftValue) & 0xFF) / 255D;

				// Find out if the gradient our color belongs to goes from the
				// element just found to
				// the next element in the array.
				if ((nextValue & colorMask) != (anchorColor & colorMask)) {
					// Compute value depending of the gradient direction
					if (nextValue < anchorColor) {
						value = i + 1 - decimals;
					} else {
						value = i + decimals;
					}
				} else {
					// It's a gradient from this element to the previous element
					// in the array.

					// Wrap to the end of the array if the "anchor" color is the
					// first element.
					int index = (i == 0) ? COLORS.length - 1 : i;
					int prevValue = COLORS[index - 1];

					// Compute value depending of the gradient direction
					if (prevValue < anchorColor) {
						value = index - 1 + decimals;
					} else {
						value = index - decimals;
					}
				}

				// Calculate the angle in rad (from -PI to PI)
				float angle = (float) (2 * Math.PI * value / (COLORS.length - 1));
				if (angle > Math.PI) {
					angle -= 2 * Math.PI;
				}

				return angle;
			}
		}

		// This shouldn't happen
		return 0;
	}

	/**
	 * "Normalize" the supplied color.
	 * 
	 * <p>
	 * This will set the lowest value of R,G,B to 0, the highest to 255, and
	 * will keep the middle value.<br>
	 * For values close to those on the color wheel this will result in close
	 * matches. For other values, especially shades of grey this will produce
	 * funny results.
	 * </p>
	 * 
	 * @param color
	 *            The color to "normalize".
	 * 
	 * @return An {@code int} array with the following contents:
	 *         <ol>
	 *         <li>The ARGB value of the "normalized" color.</li>
	 *         <li>A mask with all bits {@code 0} but those for the byte
	 *         representing the "middle value" that remains unchanged in the
	 *         "normalized" color.</li>
	 *         <li>The number of bits the "normalized" color has to be shifted
	 *         to the right so the "middle value" is in the lower 8 bits.</li>
	 *         </ol>
	 */
	private int[] normalizeColor(int color) {
		// Remove S and V from color
		Color.colorToHSV(color, mHSV);
		mHSV[1] = 1f;
		mHSV[2] = 1f;
		color = Color.HSVToColor(mHSV);

		// Normalize
		int red = Color.red(color);
		int green = Color.green(color);
		int blue = Color.blue(color);

		int newRed = red;
		int newGreen = green;
		int newBlue = blue;

		int maskRed = 0;
		int maskGreen = 0;
		int maskBlue = 0;
		int shiftValue;

		if (red <= green && red <= blue) {
			// Red is the smallest component
			newRed = 0;
			if (green > blue) {
				// Green is the largest component
				shiftValue = 0;
				maskBlue = 0xFF;
				newGreen = 0xFF;
			} else {
				// We make blue the largest component
				shiftValue = 8;
				maskGreen = 0xFF;
				newBlue = 0xFF;
			}
		} else if (green <= red && green <= blue) {
			// Green is the smallest component
			newGreen = 0;
			if (red > blue) {
				// Red is the largest component
				shiftValue = 0;
				maskBlue = 0xFF;
				newRed = 0xFF;
			} else {
				// We make blue the largest component
				shiftValue = 16;
				maskRed = 0xFF;
				newBlue = 0xFF;
			}
		} else {
			// We make blue the smallest component
			newBlue = 0;
			if (red > green) {
				// Red is the largest component
				shiftValue = 8;
				maskGreen = 0xFF;
				newRed = 0xFF;
			} else {
				// We make green the largest component
				shiftValue = 16;
				maskRed = 0xFF;
				newGreen = 0xFF;
			}
		}

		int normColor = Color.argb(255, newRed, newGreen, newBlue);
		int colorMask = Color.argb(0, maskRed, maskGreen, maskBlue);

		return new int[] { normColor, colorMask, shiftValue };
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Convert coordinates to our internal coordinate system
		float x = event.getX() - mTranslationOffset;
		float y = event.getY() - mTranslationOffset;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// Check whether the user pressed on the pointer.
			float[] pointerPosition = calculatePointerPosition(mAngle);
			if (x >= (pointerPosition[0] - mColorPointerHaloRadius)
					&& x <= (pointerPosition[0] + mColorPointerHaloRadius)
					&& y >= (pointerPosition[1] - mColorPointerHaloRadius)
					&& y <= (pointerPosition[1] + mColorPointerHaloRadius)) {
				mUserIsMovingPointer = true;
				invalidate();
			}
			// Check wheter the user pressed on the center.
			if (x >= -mColorCenterRadius && x <= mColorCenterRadius
					&& y >= -mColorCenterRadius && y <= mColorCenterRadius) {
				mCenterHaloPaint.setAlpha(0x50);
				setColor(getOldCenterColor());
				mCenterNewPaint.setColor(getOldCenterColor());
				invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mUserIsMovingPointer) {
				mAngle = (float) java.lang.Math.atan2(y, x);
				mPointerColor.setColor(calculateColor(mAngle));

				// Check wheter there is a Saturation/Value bar or/and
				// OpacityBar connected.
				if (mSVbar != null && mOpacityBar == null) {
					mSVbar.setColor(mColor);
				} else if (mSVbar == null && mOpacityBar != null) {
					mOpacityBar.setColor(mColor);
				} else if (mSVbar != null && mOpacityBar != null) {
					mOpacityBar.setColor(mColor);
					mSVbar.setColor(mColor);
				} else {
					setNewCenterColor(mCenterNewColor = calculateColor(mAngle));
				}
				invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			mUserIsMovingPointer = false;
			mCenterHaloPaint.setAlpha(0x00);
			invalidate();
			break;
		}
		getParent().requestDisallowInterceptTouchEvent(true);
		return true;
	}

	/**
	 * Calculate the pointer's coordinates on the color wheel using the supplied
	 * angle.
	 * 
	 * @param angle
	 *            The position of the pointer expressed as angle (in rad).
	 * 
	 * @return The coordinates of the pointer's center in our internal
	 *         coordinate system.
	 */
	private float[] calculatePointerPosition(float angle) {
		float x = (float) (mColorWheelRadius * Math.cos(angle));
		float y = (float) (mColorWheelRadius * Math.sin(angle));

		return new float[] { x, y };
	}

	/**
	 * Add a Saturation/Value bar to the color wheel.
	 * 
	 * @param bar
	 *            The instance of the Saturation/Value bar.
	 */
	public void addSVBar(SVBar bar) {
		mSVbar = bar;
		// Give an instance of the color picker to the Saturation/Value bar.
		mSVbar.setColorPicker(this);
		mSVbar.setColor(mColor);
	}

	/**
	 * Add a Opacity bar to the color wheel.
	 * 
	 * @param bar
	 *            The instance of the Opacity bar.
	 */
	public void addOpacityBar(OpacityBar bar) {
		mOpacityBar = bar;
		// Give an instance of the color picker to the Opacity bar.
		mOpacityBar.setColorPicker(this);
		mOpacityBar.setColor(mColor);
	}

	/**
	 * Change the color of the center which indicates the new color.
	 * 
	 * @param color
	 *            int of the color.
	 */
	public void setNewCenterColor(int color) {
		mCenterNewColor = color;
		mCenterNewPaint.setColor(color);
		if (mCenterOldColor == 0) {
			mCenterOldColor = color;
			mCenterOldPaint.setColor(color);
		}
		if (onColorChangedListener != null) {
			onColorChangedListener.onColorChanged(color);
		}
		invalidate();
	}

	/**
	 * Change the color of the center which indicates the old color.
	 * 
	 * @param color
	 *            int of the color.
	 */
	public void setOldCenterColor(int color) {
		mCenterOldColor = color;
		mCenterOldPaint.setColor(color);
		invalidate();
	}

	public int getOldCenterColor() {
		return mCenterOldColor;
	}

	/**
	 * Used to change the color of the {@code OpacityBar} used by the
	 * {@code SVBar} if there is an change in color.
	 * 
	 * @param color
	 *            int of the color used to change the opacity bar color.
	 */
	public void changeOpacityBarColor(int color) {
		if (mOpacityBar != null) {
			mOpacityBar.setColor(color);
		}
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		Bundle state = new Bundle();
		state.putParcelable(STATE_PARENT, superState);
		state.putFloat(STATE_ANGLE, mAngle);
		state.putInt(STATE_OLD_COLOR, mCenterOldColor);

		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle savedState = (Bundle) state;

		Parcelable superState = savedState.getParcelable(STATE_PARENT);
		super.onRestoreInstanceState(superState);

		mAngle = savedState.getFloat(STATE_ANGLE);
		setOldCenterColor(savedState.getInt(STATE_OLD_COLOR));
		mPointerColor.setColor(calculateColor(mAngle));
	}
}