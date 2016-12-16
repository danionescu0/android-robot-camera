package ionescu.dan.rccameracontroller.communication;

public interface MessageArrivedCallback {
    void messageArrived(String topic, String message);
}
