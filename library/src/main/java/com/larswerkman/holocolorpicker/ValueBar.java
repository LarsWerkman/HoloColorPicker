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

package com.larswerkman.holocolorpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;

import com.larswerkman.holocolorpicker.listener.OnValueChangedListener;

public class ValueBar extends Bar {

    private static final String STATE_VALUE = "value";

    /**
     * Factor used to calculate the position to the Opacity on the bar.
     */
    private float mPosToValFactor;

    /**
     * Factor used to calculate the Opacity to the postion on the bar.
     */
    private float mValToPosFactor;

    /**
     * Interface and listener so that changes in ValueBar are sent
     * to the host activity/fragment
     */
    private OnValueChangedListener onValueChangedListener;

    private float mValue;
    /**
     * Value of the latest entry of the onValueChangedListener.
     */
    private float mOldValue;
    private float mMinValue;
    private float mMaxValue;

    public ValueBar(Context context) {
        super(context);
    }

    public ValueBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ValueBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ValueBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init(Context context, AttributeSet attrs, int defStyle) {
        super.init(context, attrs, defStyle);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ValueBar, defStyle, 0);
        mMinValue = a.getFloat(R.styleable.SaturationBar_bar_minSaturation, 0f);
        if (mMinValue < 0f || mMinValue > 0.8f) {
            mMinValue = 0f;
        }
        mMaxValue = a.getFloat(R.styleable.SaturationBar_bar_maxSaturation, 1.0f);
        if (mMaxValue > 1f || mMaxValue < 0.2f) {
            mMaxValue = 1f;
        }
        a.recycle();

        mPosToValFactor = (mMaxValue - mMinValue) / ((float) mBarLength);
        mValToPosFactor = ((float) mBarLength) / (mMaxValue - mMinValue);
    }

    @Override
    protected void onSizeChanged(int x1, int y1) {
        // Update variables that depend of mBarLength.
        Shader shader;
        if (!isInEditMode()) {
            shader = new LinearGradient(mBarPointerHaloRadius, 0,
                    x1, y1, getBarColors(), null, Shader.TileMode.CLAMP);
        } else {
            shader = new LinearGradient(mBarPointerHaloRadius, 0,
                    x1, y1,
                    new int[]{Color.BLACK, 0xff81ff00}, null,
                    Shader.TileMode.CLAMP);
            Color.colorToHSV(0xff81ff00, mHSVColor);
        }

        mBarPaint.setShader(shader);
        mPosToValFactor = (mMaxValue - mMinValue) / ((float) mBarLength);
        mValToPosFactor = ((float) mBarLength) / (mMaxValue - mMinValue);

        float[] hsvColor = new float[3];
        Color.colorToHSV(mColor, hsvColor);

        if (!isInEditMode()) {
            mBarPointerPosition = Math.round((mValToPosFactor * (hsvColor[2] - mMinValue)) + mBarPointerHaloRadius);
        } else {
            mBarPointerPosition = mBarLength + mBarPointerHaloRadius;
        }
    }


    @Override
    protected int getMinColor() {
        return Color.HSVToColor(new float[]{mHSVColor[0], mHSVColor[1], mMinValue});
    }

    @Override
    protected int getMaxColor() {
        return Color.HSVToColor(new float[]{mHSVColor[0], mHSVColor[1], mMaxValue});
    }

    @Override
    protected void notifyChange() {
        if (onValueChangedListener != null && mOldValue != mValue) {
            onValueChangedListener.onValueChanged(mValue);
            mOldValue = mValue;
        }
    }

    @Override
    protected void changePickView() {
        if (mPicker != null) {
            mPicker.setValue(mValue);
        }
    }

    public float getValue() {
        return mValue;
    }

    /**
     * Set the pointer on the bar. With the opacity value.
     *
     * @param value float between 0 and 1
     */
    public void setValue(@FloatRange(from = 0f, to = 1.0f) float value) {
        if (mValue >= mMinValue && mValue <= mMaxValue && mValue != value) {
            mBarPointerPosition = Math.round((mValToPosFactor * (value - mMinValue)) + mBarPointerHaloRadius);
            mColor = calculateColor(mBarPointerPosition);
            mBarPointerPaint.setColor(mColor);
            changePickView();
            invalidate();
        }
    }

    /**
     * Calculate the color selected by the pointer on the bar.
     *
     * @param coord Coordinate of the pointer.
     */
    @Override
    protected int calculateColor(int coord) {
        coord = coord - mBarPointerHaloRadius;
        if (coord < 0) {
            coord = 0;
        } else if (coord > mBarLength) {
            coord = mBarLength;
        }
        mValue = (mPosToValFactor * coord) + mMinValue;
        if (mValue < mMinValue) {
            mValue = mMinValue;
        } else if (mValue > mMaxValue) {
            mValue = mMaxValue;
        }
        return Color.HSVToColor(new float[]{mHSVColor[0], mHSVColor[1], mValue});
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle superState = (Bundle) super.onSaveInstanceState();
        superState.putFloat(STATE_VALUE, mValue);
        return superState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;
        super.onRestoreInstanceState(savedState);
        setValue(savedState.getFloat(STATE_VALUE));
    }

    public void setOnValueChangedListener(OnValueChangedListener listener) {
        this.onValueChangedListener = listener;
    }

    public OnValueChangedListener getOnValueChangedListener() {
        return this.onValueChangedListener;
    }

}
