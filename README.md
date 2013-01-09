<h1>Android Holo ColorPicker</h1>

A Holo themed colorpicker designed by Marie Schweiz and Nick Butcher.

![image](https://lh3.googleusercontent.com/-RzpEyLl-1xM/UOMyztql1gI/AAAAAAAAATs/UKBuqZZtaZw//HoloColorPickerFramed1.png)
![image](https://lh4.googleusercontent.com/-sXAPd8onJ_8/UOMyzjA6c4I/AAAAAAAAATo/DY4kIzo7TtU//HoloColorPickerFramed2.png)


<h2>Documentation</h2>
Add this to your xml

	<com.larswerkman.colorpicker.ColorPicker
        android:id="@+id/picker"
                android:layout_width="285dp"
        android:layout_height="290dp"/>
        
To change the thickness of the wheel and the pointer you can add this.
 
 	app:wheel_size="2"
    app:pointer_size="4"

To get the color of the colorpicker

	ColorPicker picker = (ColorPicker) findViewById(R.id.picker);
	
	picker.getColor();

<h2>Devoleped By</h2>
**Lars Werkman**