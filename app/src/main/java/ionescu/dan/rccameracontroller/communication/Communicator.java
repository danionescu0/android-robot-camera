package ionescu.dan.rccameracontroller.communication;

import android.util.Log;

public class Communicator {
    private Mqttt mqttt;
    private MotorCommandFormatter motorCommandFormatter;
    private IncommingRobotCommunicationCallback incommingRobotCommunicationCallback;

    public Communicator(Mqttt mqttt, MotorCommandFormatter motorCommandFormatter) {
        this.mqttt = mqttt;
        this.motorCommandFormatter = motorCommandFormatter;
    }

    public void sendMotionCommand(MoveEvent moveEvent) {
        String command = this.motorCommandFormatter.formatDirection(
                moveEvent.getX(), moveEvent.getY(), moveEvent.getMaxX(), moveEvent.getMaxY());
        Log.d("cici", command);
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