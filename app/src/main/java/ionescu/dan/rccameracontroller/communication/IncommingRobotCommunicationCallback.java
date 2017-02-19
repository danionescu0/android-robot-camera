package ionescu.dan.rccameracontroller.communication;

public interface IncommingRobotCommunicationCallback {
    void batteryLevelUpdated(float level);
    void distanceUpdated(float front, float back);
}
