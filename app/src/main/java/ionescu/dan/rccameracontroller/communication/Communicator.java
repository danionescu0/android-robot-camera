package ionescu.dan.rccameracontroller.communication;

import android.util.Log;

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
            @Override
            public void messageArrived(String topic, String message) {
                float level = Float.parseFloat(message.replaceAll("[^0-9.]",""));
                incommingRobotCommunicationCallback.batteryLevelUpdated(level);
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