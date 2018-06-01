# Android robot camera
This is the frontend of a mobile camera robot. The robot will stream the
video using UV4l and the application will display the stream using a webview.

Full tutorial on [instructables](https://www.instructables.com/id/Android-Controlled-Robot-Spy-Camera/)

A video demo is available on [youtube](https://youtu.be/6FrEs4C9D-Y)

The raspberry pi and arduino project are located [here](https://github.com/danionescu0/robot-camera-platform)

   The application has only a main screen, in the left of the screen the streamming image is displayed. On the right
there are the controls. The main control is a steering wheel, touch the steering wheel in the direction you
wish the robot to move. Below the steering wheel there is a headlight button, touch it to toggle the light.

On the left side of the screen is a webview with the streammed image, you can adjust the image size in the webview by pinch.

  In the top right corner there is a text like : "- Batt Connected".

* First dash means no obstacles, if there is an obstacle in front or in the back of the robot it will be signaled with a small arrow pointing in front or in the back.

* The "Batt" status is not implemented yet.

* "Connected" means that mqtt server is connected so the robot can be used, the other possible value is "Disconnected"


# Screenshots
![main.jpg](https://github.com/danionescu0/android-robot-camera/blob/master/screenshots/main.jpg)

# Working diagram
![flow-diagram.jpg](https://github.com/danionescu0/android-robot-camera/blob/master/resources/flow-diagram.png)


# Configuration
1. passwords.xml
````
        <!--In the next lines mqttconfiguration needed-->
        <string name="mqtt_endpoint">tcp://host:port</string>
        <string name="mqtt_username">username</string>
        <string name="mqtt_password">password</string>
        <!--Next 3 lines containt endpoint and credentials for UV4l video streamming-->
        <string name="stream_endpoint">http://host:port/stream</string>
        <string name="stream_username">username</string>
        <string name="stream_password">password</string>
````

2. strings.xml

This "transmit_command_interval" value is a default and it's ok to leave it to 100 ms.

````
    <!--how long between motor commands next transmissions (in milliseconds)-->
    <string name="transmit_command_interval">100</string>
````