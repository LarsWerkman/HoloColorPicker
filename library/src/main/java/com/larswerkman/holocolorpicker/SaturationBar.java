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

import com.larswerkman.holocolorpicker.listener.OnSaturationChangedListener;

public class SaturationBar extends Bar {

    private static final String STATE_SATURATION = "saturation";
    /**
     * Factor used to calculate the position to the Opacity on the bar.
     */
    private float mPosToSatFactor;

    /**
     * Factor used to calculate the Opacity to the postion on the bar.
     */
    private float mSatToPosFactor;

    /**
     * Interface and listener so that changes in SaturationBar are sent
     * to the host activity/fragment
     */
    private OnSaturationChangedListener onSaturationChangedListener;

    private float mSaturation;
    /**
     * Saturation of the latest entry of the onSaturationChangedListener.
     */
    private float mOldSaturation;
    private float mMinSaturation;
    private float mMaxSaturation;

    public SaturationBar(Context context) {
        super(context);
    }

    public SaturationBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SaturationBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SaturationBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init(Context context, AttributeSet attrs, int defStyle) {
        super.init(context, attrs, defStyle);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SaturationBar, defStyle, 0);
        mMinSaturation = a.getFloat(R.styleable.SaturationBar_bar_minSaturation, 0f);
        if (mMinSaturation < 0f || mMinSaturation > 0.8f){
            mMinSaturation = 0f;
        }
        mMaxSaturation = a.getFloat(R.styleable.SaturationBar_bar_maxSaturation, 1.0f);
        if (mMaxSaturation > 1f || mMaxSaturation < 0.2f){
            mMaxSaturation = 1f;
        }
        a.recycle();

        mPosToSatFactor = (mMaxSaturation - mMinSaturation) / ((float) mBarLength);
        mSatToPosFactor = ((float) mBarLength) / (mMaxSaturation - mMinSaturation);
    }

    @Override
    protected void onSizeChanged(int x1, int y1) {
        // Update variables that depend of mBarLength.
        Shader shader;
        if (!isInEditMode()) {
            shader = new LinearGradient(mBarPointerHaloRadius, 0,
                    x1, y1, getBarColors(), null,
                    Shader.TileMode.CLAMP);
        } else {
            shader = new LinearGradient(mBarPointerHaloRadius, 0,
                    x1, y1, new int[]{
                    Color.WHITE, 0xff81ff00}, null, Shader.TileMode.CLAMP);
            Color.colorToHSV(0xff81ff00, mHSVColor);
        }

        mBarPaint.setShader(shader);
        mPosToSatFactor = (mMaxSaturation - mMinSaturation) / ((float) mBarLength);
        mSatToPosFactor = ((float) mBarLength) / (mMaxSaturation - mMinSaturation);

        float[] hsvColor = new float[3];
        Color.colorToHSV(mColor, hsvColor);

        if (!isInEditMode()) {
            mBarPointerPosition = Math.round((mSatToPosFactor * (hsvColor[1] - mMinSaturation))
                    + mBarPointerHaloRadius);
        } else {
            mBarPointerPosition = mBarLength + mBarPointerHaloRadius;
        }
    }

    @Override
    protected int getMinColor() {
        return Color.HSVToColor(new float[]{mHSVColor[0], mMinSaturation, mHSVColor[2]});
    }

    @Override
    protected int getMaxColor() {
        return Color.HSVToColor(new float[]{mHSVColor[0], mMaxSaturation, mHSVColor[2]});
    }

    @Override
    protected void notifyChange() {
        if (onSaturationChangedListener != null && mOldSaturation != mSaturation) {
            onSaturationChangedListener.onSaturationChanged(mSaturation);
            mOldSaturation = mSaturation;
        }
    }

    @Override
    protected void changePickView() {
        if (mPicker != null){
            mPicker.setSaturation(mSaturation);
        }
    }

    public float getSaturation() {
        return mSaturation;
    }

    /**
     * Set the pointer on the bar. With the opacity value.
     *
     * @param saturation float between 0 and 1
     */
    public void setSaturation(@FloatRange(from = 0f, to = 1.0f) float saturation) {
        if (mSaturation >= mMinSaturation && mSaturation <= mMaxSaturation && mSaturation != saturation) {
            mBarPointerPosition = Math.round((mSatToPosFactor * (saturation - mMinSaturation)))
                    + mBarPointerHaloRadius;
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
        mSaturation = mPosToSatFactor * coord + mMinSaturation;
        if (mSaturation < mMinSaturation){
            mSaturation = mMinSaturation;
        }else if (mSaturation > mMaxSaturation){
            mSaturation = mMaxSaturation;
        }
        return Color.HSVToColor(new float[]{mHSVColor[0], mSaturation, mHSVColor[2]});
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle superState = (Bundle) super.onSaveInstanceState();
        superState.putFloat(STATE_SATURATION, mSaturation);
        return superState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;
        super.onRestoreInstanceState(savedState);
        setSaturation(savedState.getFloat(STATE_SATURATION));
    }

    public void setOnSaturationChangedListener(OnSaturationChangedListener listener) {
        this.onSaturationChangedListener = listener;
    }

    public OnSaturationChangedListener getOnSaturationChangedListener() {
        return this.onSaturationChangedListener;
    }
}
