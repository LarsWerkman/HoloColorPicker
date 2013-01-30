<h1>Android Holo ColorPicker</h1>

Marie Schweiz made a beautifull new design for the Holo ColorPicker which added a lot of new functionality.

You can now set the Saturation and Value of a color.
Also its possible to set the Opacity for a color.

You can also set the last selected color and see the difference with the new selected color.

![image](https://lh6.googleusercontent.com/-Rn5TDr6QoG4/UQk8OPpsPEI/AAAAAAAAAX0/TKlibuBjupo//framed_HoloColorPicker.png)
![image](https://lh3.googleusercontent.com/-2JFzIZ4ote8/UQk8OCCJH9I/AAAAAAAAAX4/dO5i-qWnhUs//framed_HoloColorPicker2.png)


<h2>Documentation</h2>
To add the ColorPicker to your layout add this to your xml

	<com.larswerkman.colorpicker.ColorPicker
        android:id="@+id/picker"
                android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
        
        
To add a Saturation/Value bar to your layout add this to your xml

    <com.larswerkman.colorpicker.SVBar
        android:id="@+id/svbar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
        
The same goes for the Opacity bar

	<com.larswerkman.colorpicker.OpacityBar
        android:id="@+id/opacitybar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>

To connect the bars with the colorpicker and to get the selected color.

	ColorPicker picker = (ColorPicker)
	     findViewById(R.id.picker);
	SVBar svBar = (SVBar) findViewById(R.id.svbar);
	OpacityBar opacityBar = (OpacityBar)
	     findViewById(R.id.opacitybar);
		
	picker.addSVBar(svBar);
	picker.addOpacityBar(opacityBar);

	//To get the color
	picker.getColor();
	
	//To set the old selected color u can do it like this
	picker.setOldCenterColor(picker.getColor());
	
<H2>License</H2>
	
 	 Copyright 2012 Lars Werkman
 	
 	 Licensed under the Apache License, Version 2.0 (the 	"License");
 	 you may not use this file except in compliance with 	the License.
 	 You may obtain a copy of the License at
 	
 	     http://www.apache.org/licenses/LICENSE-2.0
 	
 	 Unless required by applicable law or agreed to in 	writing, software
	 distributed under the License is distributed on an 	"AS IS" BASIS,
 	 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 	either express or implied.
 	 See the License for the specific language governing 	permissions and
 	 limitations under the License.
 	

<h2>Devoleped By</h2>
**Lars Werkman**
