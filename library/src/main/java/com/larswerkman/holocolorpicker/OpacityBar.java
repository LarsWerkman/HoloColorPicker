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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;

import com.larswerkman.holocolorpicker.listener.OnOpacityChangedListener;

public class OpacityBar extends Bar {

    private static final String STATE_OPACITY = "opacity";

    /**
     * Factor used to calculate the position to the Opacity on the bar.
     */
    private float mPosToOpacFactor;

    /**
     * Factor used to calculate the Opacity to the postion on the bar.
     */
    private float mOpacToPosFactor;

    /**
     * Interface and listener so that changes in OpacityBar are sent
     * to the host activity/fragment
     */
    private OnOpacityChangedListener onOpacityChangedListener;

    private int mOpacity;
    /**
     * Opacity of the latest entry of the onOpacityChangedListener.
     */
    private int mOldOpacity;

    private int mMinOpacity;
    private int mMaxOpacity;

    public OpacityBar(Context context) {
        super(context);
    }

    public OpacityBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OpacityBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public OpacityBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init(Context context, AttributeSet attrs, int defStyle) {
        super.init(context, attrs, defStyle);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OpacityBar, defStyle, 0);
        mMinOpacity = a.getInteger(R.styleable.OpacityBar_bar_minOpacity, 0x00);
        if (mMinOpacity < 0x00 || mMinOpacity > 0xF6){
            mMinOpacity = 0x00;
        }
        mMaxOpacity = a.getInteger(R.styleable.OpacityBar_bar_maxOpacity, 0xFF);
        if (mMaxOpacity > 0xFF || mMaxOpacity < 0x0a){
            mMaxOpacity = 0xFF;
        }
        a.recycle();
        mPosToOpacFactor = (mMaxOpacity - mMinOpacity) / ((float) mBarLength);
        mOpacToPosFactor = ((float) mBarLength) / (mMaxOpacity - mMinOpacity);
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
                    0x0081ff00, 0xff81ff00}, null, Shader.TileMode.CLAMP);
            Color.colorToHSV(0xff81ff00, mHSVColor);
        }

        mBarPaint.setShader(shader);
        mPosToOpacFactor = (mMaxOpacity - mMinOpacity) / ((float) mBarLength);
        mOpacToPosFactor = ((float) mBarLength) / (mMaxOpacity - mMinOpacity);

        float[] hsvColor = new float[3];
        Color.colorToHSV(mColor, hsvColor);

        if (!isInEditMode()) {
            mBarPointerPosition = Math.round((mOpacToPosFactor * (Color.alpha(mColor) - mMinOpacity))
                    + mBarPointerHaloRadius);
        } else {
            mBarPointerPosition = mBarLength + mBarPointerHaloRadius;
        }
    }

    @Override
    protected int getMinColor() {
        return Color.HSVToColor(mMinOpacity, mHSVColor);
    }

    @Override
    protected int getMaxColor() {
        return Color.HSVToColor(mMaxOpacity, mHSVColor);
    }

    @Override
    protected void notifyChange() {
        if (onOpacityChangedListener != null && mOldOpacity != mOpacity) {
            onOpacityChangedListener.onOpacityChanged(mOpacity);
            mOldOpacity = mOpacity;
        }
    }

    @Override
    protected void changePickView() {
        if (mPicker != null){
            mPicker.setOpacity(mOpacity);
        }
    }

    /**
     * Set the pointer on the bar. With the opacity value.
     *
     * @param opacity float between 0 and 255
     */
    public void setOpacity(@IntRange(from = 0, to = 255) int opacity) {
        if (mOpacity >= mMinOpacity && mOpacity <= mMaxOpacity && mOpacity != opacity) {
            mBarPointerPosition = Math.round((mOpacToPosFactor * (opacity - mMinOpacity))) + mBarPointerHaloRadius;
            mColor = calculateColor(mBarPointerPosition);
            mBarPointerPaint.setColor(mColor);
            changePickView();
            invalidate();
        }
    }

    /**
     * Get the currently selected opacity.
     *
     * @return The int value of the currently selected opacity.
     */
    public int getOpacity() {
        return mOpacity;
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
        mOpacity = Math.round(mPosToOpacFactor * coord + mMinOpacity);
        if (mOpacity < mMinOpacity) {
            mOpacity = mMinOpacity;
        } else if (mOpacity > mMaxOpacity) {
            mOpacity = mMaxOpacity;
        }
        return Color.HSVToColor(mOpacity, mHSVColor);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle superState = (Bundle) super.onSaveInstanceState();
        superState.putInt(STATE_OPACITY, mOpacity);
        return superState;
    }

    //
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;
        super.onRestoreInstanceState(savedState);
        setOpacity(savedState.getInt(STATE_OPACITY));
    }

    public void setOnOpacityChangedListener(OnOpacityChangedListener listener) {
        this.onOpacityChangedListener = listener;
    }

    public OnOpacityChangedListener getOnOpacityChangedListener() {
        return this.onOpacityChangedListener;
    }
}