package com.larswerkman.holocolorpicker.listener;


/**
 * An interface that is called whenever a new color has been selected.
 * Currently it is always called when the color wheel has been released.
 */
public interface OnColorSelectedListener {
    void onColorSelected(int color);
}
