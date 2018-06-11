package com.larswerkman.holocolorpicker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/16
 */

abstract class Bar extends View {

    /*
 * Constants used to save/restore the instance state.
 */
    private static final String STATE_PARENT = "parent";
    private static final String STATE_COLOR = "color";
    private static final String STATE_ORIENTATION = "orientation";

    @IntDef({ORIENTATION_VERTICAL, ORIENTATION_HORIZONTAL})
    public @interface Orientation {
    }

    /**
     * Constants used to identify orientation.
     */
    public static final int ORIENTATION_HORIZONTAL = 1;
    public static final int ORIENTATION_VERTICAL = 2;
    /**
     * Default orientation of the bar.
     */
    @Orientation
    private static final int ORIENTATION_DEFAULT = ORIENTATION_HORIZONTAL;

    /**
     * The thickness of the bar.
     */
    private int mBarThickness;

    /**
     * The length of the bar.
     */
    protected int mBarLength;
    private int mPreferredBarLength;

    /**
     * The radius of the pointer.
     */
    private int mBarPointerRadius;

    /**
     * The radius of the halo of the pointer.
     */
    protected int mBarPointerHaloRadius;
    private boolean mBarPointerHaloEnabled;

    /**
     * The position of the pointer on the bar.
     */
    protected int mBarPointerPosition;

    /**
     * {@code Paint} instance used to draw the bar.
     */
    protected Paint mBarPaint;

    /**
     * {@code Paint} instance used to draw the pointer.
     */
    protected Paint mBarPointerPaint;

    /**
     * {@code Paint} instance used to draw the halo of the pointer.
     */
    private Paint mBarPointerHaloPaint;

    /**
     * The rectangle enclosing the bar.
     */
    private RectF mBarRect = new RectF();

    /**
     * {@code true} if the user clicked on the pointer to start the move mode. <br>
     * {@code false} once the user stops touching the screen.
     *
     * @see #onTouchEvent(android.view.MotionEvent)
     */
    private boolean mIsMovingPointer;
    /**
     * The ARGB value of the currently selected color.
     */
    protected int mColor;
    /**
     * An array of floats that can be build into a {@code Color} <br>
     * Where we can extract the color from.
     */
    protected float[] mHSVColor = new float[3];

    /**
     * Used to toggle orientation between vertical and horizontal.
     */
    @Orientation
    private int mOrientation;
    /**
     * {@code ColorPicker} instance used to control the ColorPicker.
     */
    protected ColorPicker mPicker = null;


    public Bar(Context context) {
        super(context);
        init(context, null, 0);
    }

    public Bar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public Bar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, 0);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Bar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    protected void init(Context context, AttributeSet attrs, int defStyle) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Bar, defStyle, 0);
        final Resources b = context.getResources();
        mBarThickness = a.getDimensionPixelSize(R.styleable.Bar_bar_thickness, b.getDimensionPixelSize(R.dimen.bar_thickness));
        mBarLength = a.getDimensionPixelSize(R.styleable.Bar_bar_length, b.getDimensionPixelSize(R.dimen.bar_length));
        mPreferredBarLength = mBarLength;
        mBarPointerRadius = a.getDimensionPixelSize(R.styleable.Bar_bar_pointer_radius, b.getDimensionPixelSize(R.dimen.bar_pointer_radius));
        mBarPointerHaloRadius = a.getDimensionPixelSize(R.styleable.Bar_bar_pointer_halo_radius, b.getDimensionPixelSize(R.dimen.bar_pointer_halo_radius));
        mBarPointerHaloEnabled = a.getBoolean(R.styleable.Bar_bar_pointer_halo_enabled, true);
        mOrientation = a.getInteger(R.styleable.Bar_bar_orientation, ORIENTATION_DEFAULT);
        int color = a.getColor(R.styleable.Bar_bar_pointer_halo_color, Color.argb(127, 0, 0, 0));
        a.recycle();

        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mBarPointerPosition = mBarLength + mBarPointerHaloRadius;

        mBarPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPointerHaloPaint.setColor(color);

        mBarPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int intrinsicSize = mPreferredBarLength
                + (mBarPointerHaloRadius * 2);

        // Variable orientation
        int measureSpec;
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            measureSpec = widthMeasureSpec;
        } else {
            measureSpec = heightMeasureSpec;
        }
        int lengthMode = MeasureSpec.getMode(measureSpec);
        int lengthSize = MeasureSpec.getSize(measureSpec);

        int length;
        if (lengthMode == MeasureSpec.EXACTLY) {
            length = lengthSize;
        } else if (lengthMode == MeasureSpec.AT_MOST) {
            length = Math.min(intrinsicSize, lengthSize);
        } else {
            length = intrinsicSize;
        }

        int barPointerHaloRadiusx2 = mBarPointerHaloRadius * 2;
        mBarLength = length - barPointerHaloRadiusx2;
        if (mOrientation == ORIENTATION_VERTICAL) {
            setMeasuredDimension(barPointerHaloRadiusx2,
                    (mBarLength + barPointerHaloRadiusx2));
        } else {
            setMeasuredDimension((mBarLength + barPointerHaloRadiusx2),
                    barPointerHaloRadiusx2);
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Fill the rectangle instance based on orientation
        int x1, y1;
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            x1 = (mBarLength + mBarPointerHaloRadius);
            y1 = mBarThickness;
            mBarLength = w - (mBarPointerHaloRadius * 2);
            mBarRect.set(mBarPointerHaloRadius,
                    (mBarPointerHaloRadius - (mBarThickness / 2)),
                    (mBarLength + (mBarPointerHaloRadius)),
                    (mBarPointerHaloRadius + (mBarThickness / 2)));
        } else {
            x1 = mBarThickness;
            y1 = (mBarLength + mBarPointerHaloRadius);
            mBarLength = h - (mBarPointerHaloRadius * 2);
            mBarRect.set((mBarPointerHaloRadius - (mBarThickness / 2)),
                    mBarPointerHaloRadius,
                    (mBarPointerHaloRadius + (mBarThickness / 2)),
                    (mBarLength + (mBarPointerHaloRadius)));
        }
        onSizeChanged(x1, y1);
    }

    protected abstract void onSizeChanged(int x1, int y1);

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the bar.
        canvas.drawRect(mBarRect, mBarPaint);

        // Calculate the center of the pointer.
        int cX, cY;
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            cX = mBarPointerPosition;
            cY = mBarPointerHaloRadius;
        } else {
            cX = mBarPointerHaloRadius;
            cY = mBarPointerPosition;
        }

        // Draw the pointer halo.
        if (mBarPointerHaloEnabled) {
            canvas.drawCircle(cX, cY, mBarPointerHaloRadius, mBarPointerHaloPaint);
        }
        // Draw the pointer.
        canvas.drawCircle(cX, cY, mBarPointerRadius, mBarPointerPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);

        // Convert coordinates to our internal coordinate system
        float dimen;
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            dimen = event.getX();
        } else {
            dimen = event.getY();
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsMovingPointer = true;
                // Check whether the user pressed on (or near) the pointer
                if (dimen >= (mBarPointerHaloRadius)
                        && dimen <= (mBarPointerHaloRadius + mBarLength)) {
                    mBarPointerPosition = Math.round(dimen);
                    mColor = calculateColor(Math.round(dimen));
                    mBarPointerPaint.setColor(mColor);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsMovingPointer) {
                    // Move the the pointer on the bar.
                    mColor = calculateColor(Math.round(dimen));
                    if (dimen >= mBarPointerHaloRadius
                            && dimen <= (mBarPointerHaloRadius + mBarLength)) {
                        mBarPointerPosition = Math.round(dimen);
                        mBarPointerPaint.setColor(mColor);
                        changePickView();
                        invalidate();
                    } else if (dimen < mBarPointerHaloRadius) {
                        mBarPointerPosition = mBarPointerHaloRadius;

                        mBarPointerPaint.setColor(mColor);
                        changePickView();
                        invalidate();
                    } else if (dimen > (mBarPointerHaloRadius + mBarLength)) {
                        mBarPointerPosition = mBarPointerHaloRadius + mBarLength;
                        mBarPointerPaint.setColor(mColor);
                        changePickView();
                        invalidate();
                    }
                }
                notifyChange();
                break;
            case MotionEvent.ACTION_UP:
                mIsMovingPointer = false;
                break;
        }
        return true;
    }

    protected abstract int getMinColor();

    protected abstract int getMaxColor();

    protected abstract void notifyChange();

    /**
     * Set the bar color. <br>
     * <br>
     * Its discouraged to use this method.
     *
     * @param color
     */
    public void setColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if (!Arrays.equals(mHSVColor, hsv)){
            mHSVColor = hsv;
            int x1, y1;
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                x1 = (mBarLength + mBarPointerHaloRadius);
                y1 = mBarThickness;
            } else {
                x1 = mBarThickness;
                y1 = (mBarLength + mBarPointerHaloRadius);
            }
            mColor = calculateColor(mBarPointerPosition);
            Shader shader = new LinearGradient(mBarPointerHaloRadius, 0,
                    x1, y1, getBarColors(), null, Shader.TileMode.CLAMP);
            mBarPaint.setShader(shader);
            mBarPointerPaint.setColor(mColor);
            invalidate();
        }

    }

    /**
     * Set the bar color. <br>
     * <br>
     * Its discouraged to use this method.
     *
     * @param hsv
     */
    public void setColor(float[] hsv) {
        if (!Arrays.equals(mHSVColor, hsv)){
            System.arraycopy(hsv, 0, mHSVColor, 0, 3);
            int x1, y1;
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                x1 = (mBarLength + mBarPointerHaloRadius);
                y1 = mBarThickness;
            } else {
                x1 = mBarThickness;
                y1 = (mBarLength + mBarPointerHaloRadius);
            }
            mColor = calculateColor(mBarPointerPosition);
            Shader shader = new LinearGradient(mBarPointerHaloRadius, 0,
                    x1, y1, getBarColors(), null, Shader.TileMode.CLAMP);
            mBarPaint.setShader(shader);
            mBarPointerPaint.setColor(mColor);
            invalidate();
        }

    }

    protected int[] getBarColors() {
        return new int[]{getMinColor(), getMaxColor()};
    }

    protected abstract void changePickView();

    /**
     * Calculate the color selected by the pointer on the bar.
     *
     * @param coord Coordinate of the pointer.
     */
    protected abstract int calculateColor(int coord);

    /**
     * Get the currently selected color.
     *
     * @return The ARGB value of the currently selected color.
     */
    public int getColor() {
        return mColor;
    }


    public void setColorPicker(ColorPicker picker) {
        mPicker = picker;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        Bundle state = new Bundle();
        state.putParcelable(STATE_PARENT, superState);
        state.putFloatArray(STATE_COLOR, mHSVColor);
        state.putInt(STATE_ORIENTATION, mOrientation);
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;
        Parcelable superState = savedState.getParcelable(STATE_PARENT);
        super.onRestoreInstanceState(superState);
        mOrientation = savedState.getInt(STATE_ORIENTATION, ORIENTATION_HORIZONTAL);
        setColor(savedState.getFloatArray(STATE_COLOR));
    }

}
