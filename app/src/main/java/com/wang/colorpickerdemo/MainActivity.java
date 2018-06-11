package com.wang.colorpickerdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;
import com.larswerkman.holocolorpicker.listener.OnColorChangedListener;

public class MainActivity extends AppCompatActivity implements OnColorChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ColorPicker picker = findViewById(R.id.picker);
        OpacityBar opacityBar = findViewById(R.id.opacitybar);
        SaturationBar saturationBar = findViewById(R.id.saturationbar);
        ValueBar valueBar = findViewById(R.id.valuebar);

//        picker.addSVBar(svBar);
        picker.addOpacityBar(opacityBar);
        picker.addSaturationBar(saturationBar);
        picker.addValueBar(valueBar);



//To get the color
        picker.getColor();

//To set the old selected color u can do it like this
        picker.setOldCenterColor(picker.getColor());
// adds listener to the colorpicker which is implemented
//in the activity
        picker.setOnColorChangedListener(this);

//to turn of showing the old color
//        picker.setShowOldCenterColor(false);

    }

    @Override
    public void onColorChanged(int color) {

    }
}
