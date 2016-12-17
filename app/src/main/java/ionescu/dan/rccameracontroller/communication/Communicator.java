package ionescu.dan.rccameracontroller.communication;

import android.view.Display;
import android.view.MotionEvent;

public class Communicator {
    private Mqttt mqttt;
    private Display display;
    private MotorCommandFormatter motorCommandFormatter;
    private IncommingRobotCommunicationCallback incommingRobotCommunicationCallback;

    public Communicator(Mqttt mqttt, MotorCommandFormatter motorCommandFormatter) {
        this.mqttt = mqttt;
        this.motorCommandFormatter = motorCommandFormatter;
    }

    public void sendMotionCommand(MotionEvent motionEvent) {
        String command = this.motorCommandFormatter.formatDirection(motionEvent.getX(), motionEvent.getY(), display);
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

    public void initialize(Display display) {
        this.display = display;
        this.mqttt.connect();
    }
}