# Android robot camera
This is the frontend of a mobile camera robot. The robot will stream the
video using UV4l.
The platform will receive commands using mqtt, and will transmit things
like battery level and infrared senzors around the robot.

The raspberry pi and arduino project are located [here](https://github.com/danionescu0/robot-camera-platform).

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
````
    <!--how long between motor commands next transmissions (in milliseconds)-->
    <string name="transmit_command_interval">100</string>
````