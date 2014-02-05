<h1>Android Holo ColorPicker</h1>

Marie Schweiz made a beautifull new design for the Holo ColorPicker which added a lot of new functionality.

You can now set the Saturation and Value of a color.
Also its possible to set the Opacity for a color.

You can also set the last selected color and see the difference with the new selected color.

Demo can be found on my Google Drive [here](https://docs.google.com/file/d/0BwclyDTlLrdXRzVnTGJvTlRfU2s/edit) if interested. the code of the sample can be found at a gist [here](https://gist.github.com/LarsWerkman/4754528)

![image](https://lh6.googleusercontent.com/-Rn5TDr6QoG4/UQk8OPpsPEI/AAAAAAAAAX0/TKlibuBjupo//framed_HoloColorPicker.png)
![image](https://lh4.googleusercontent.com/-GtJYDCQdnVo/UVW4ML7WIuI/AAAAAAAAAj4/YKHEUnhvLhA//framed_colorpicker.png)

<h3>UDPATE</h3>
Now bars can change there orientation, Thanks to [tonyr59h](https://github.com/tonyr59h)
also the gradle build version was updated to 0.7.+
![image](https://lh5.googleusercontent.com/-3KSukk_S94Y/UvKiNER-OBI/AAAAAAAAA-k/8SPfOmFhLjE//device-2014-02-05-180704_framed.png)


<h2>Documentation</h2>

To add the ColorPicker to your layout add this to your xml
```xml
<com.larswerkman.holocolorpicker.ColorPicker
    android:id="@+id/picker"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"/>
```       
        
To add a Saturation/Value bar to your layout add this to your xml
```xml
<com.larswerkman.holocolorpicker.SVBar
    android:id="@+id/svbar"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"/>
```       
The same goes for the Opacity bar
```xml
<com.larswerkman.holocolorpicker.OpacityBar
    android:id="@+id/opacitybar"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"/>
```

Saturation bar
```xml
<com.larswerkman.holocolorpicker.SaturationBar
    android:id="@+id/saturationbar"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"/>
```

and a Value bar
```xml
<com.larswerkman.holocolorpicker.ValueBar
    android:id="@+id/valuebar"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"/>
```

To connect the bars with the colorpicker and to get the selected color.
```java
ColorPicker picker = (ColorPicker) findViewById(R.id.picker);
SVBar svBar = (SVBar) findViewById(R.id.svbar);
OpacityBar opacityBar = (OpacityBar) findViewById(R.id.opacitybar);
SaturationBar saturationBar = (SaturationBar) findViewById(R.id.saturationbar);
ValueBar valueBar = (ValueBar) findViewById(R.id.valuebar);
	
picker.addSVBar(svBar);
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
```	

<H2>Dependency</H2>
Adding it as a dependency to your project.

	dependencies {
    	compile 'com.larswerkman:HoloColorPicker:1.2'
	}

<H2>License</H2>
	
 	 Copyright 2012 Lars Werkman
 	
 	 Licensed under the Apache License, Version 2.0 (the "License");
 	 you may not use this file except in compliance with the License.
 	 You may obtain a copy of the License at
 	
 	     http://www.apache.org/licenses/LICENSE-2.0
 	
 	 Unless required by applicable law or agreed to in writing, software
	 distributed under the License is distributed on an "AS IS" BASIS,
 	 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 	 See the License for the specific language governing permissions and
 	 limitations under the License.
 	

<h2>Devoleped By</h2>
**Lars Werkman**
