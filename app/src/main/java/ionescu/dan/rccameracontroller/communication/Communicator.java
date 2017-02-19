package ionescu.dan.rccameracontroller.communication;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Communicator {
    private Mqttt mqttt;
    private SerialCommandFormatter serialCommandFormatter;
    private IncommingRobotCommunicationCallback incommingRobotCommunicationCallback;
    private DirectionsInterpretter directionsInterpretter;

    public Communicator(Mqttt mqttt, SerialCommandFormatter serialCommandFormatter,
                        DirectionsInterpretter directionsInterpretter) {
        this.mqttt = mqttt;
        this.serialCommandFormatter = serialCommandFormatter;
        this.directionsInterpretter = directionsInterpretter;
    }

    public void sendMotionCommand(MoveEvent moveEvent) {
        String command = this.serialCommandFormatter.
                formatDirection(
                        directionsInterpretter.getScaledX(moveEvent),
                        directionsInterpretter.getScaledY(moveEvent)
                );
        Log.d("motion-cmd:", command);
        this.mqttt.send(command);
    }

    public void sendLightCommand(boolean state) {
        String command = this.serialCommandFormatter.formatLights(state);
        Log.d("light-cmd:", command);
        this.mqttt.send(command);
    }

    public void addIncommingMessageListener(final IncommingRobotCommunicationCallback incommingRobotCommunicationCallback) {
        this.mqttt.addMessageArrivedListener(new MessageArrivedCallback() {
            Pattern batteryUpdate = Pattern.compile("B:([0-9/.]{3,4});");
            Pattern distanceUpdate = Pattern.compile("F=([0-9]{1,2}):B=([0-9]{1,2});");

            @Override
            public void messageArrived(String topic, String message) {
                Matcher batteryMatcher = batteryUpdate.matcher(message);
                Matcher distanceMatcher = distanceUpdate.matcher(message);
                if (batteryMatcher.matches()) {
                    incommingRobotCommunicationCallback.batteryLevelUpdated(
                            Float.parseFloat(batteryMatcher.group(1))
                    );
                }
                if (distanceMatcher.matches()) {
                    incommingRobotCommunicationCallback.distanceUpdated(
                            Float.parseFloat(distanceMatcher.group(1)),
                            Float.parseFloat(distanceMatcher.group(2))
                    );
                }
            }
        });
        this.incommingRobotCommunicationCallback = incommingRobotCommunicationCallback;
    }

    public void addConectionStatusListener(final ConnectionStatusCallback connectionStatusCallback) {
        this.mqttt.addConectionStatusListener(connectionStatusCallback);
    }

    public void initialize() {
        this.mqttt.connect();
    }
}