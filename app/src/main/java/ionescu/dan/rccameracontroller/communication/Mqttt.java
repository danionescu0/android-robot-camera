package ionescu.dan.rccameracontroller.communication;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Mqttt implements MqttCallback {
    private String TAG = "mqtt";
    private String sendRobotMovementTopic = "robot/movement";
    private String receiveRobotStatusTopic = "robot/status";
    private String clientId = "AndroidClient";
    private int qos = 2;
    private MqttClient client;
    private MemoryPersistence persistence = new MemoryPersistence();
    private String endpoint;
    private String username;
    private String password;
    private MessageArrivedCallback messageArrivedCallback;
    private ConnectionStatusCallback connectionStatusCallback;

    public Mqttt(String endpoint, String username, String password) {
        this.endpoint = endpoint;
        this.username = username;
        this.password = password;
    }

    public void connect() {
        try {
            client = new MqttClient(endpoint, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setPassword(this.password.toCharArray());
            connOpts.setUserName (this.username);
            client.setCallback(this);
            client.connect(connOpts);
            client.subscribe(this.receiveRobotStatusTopic, qos);
        } catch(MqttException e) {
            this.setConnectionStatusChanged(false);
            Log.e(TAG, "Error connectiong:" + e.getMessage());
            return;
        }
        this.setConnectionStatusChanged(true);
    }

    public void send(String data) {
        try {
            MqttMessage message = new MqttMessage(data.getBytes());
            message.setQos(qos);
            client.publish(sendRobotMovementTopic, message);
        } catch(MqttException e) {
            this.setConnectionStatusChanged(false);
            if (e.getReasonCode() == MqttException.REASON_CODE_CLIENT_NOT_CONNECTED) {
                this.connect();
            }
            Log.e(TAG, "Error sending message:" + e.getMessage());
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d(TAG, message.toString());
        this.messageArrivedCallback.messageArrived(topic, message.toString());
    }

    public void addMessageArrivedListener(MessageArrivedCallback messageArrivedCallback) {
        this.messageArrivedCallback = messageArrivedCallback;
    }

    public void addConectionStatusListener(final ConnectionStatusCallback connectionStatusCallback) {
        this.connectionStatusCallback = connectionStatusCallback;
    }

    @Override
    public void connectionLost(Throwable cause) {
        this.setConnectionStatusChanged(false);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}

    private void setConnectionStatusChanged(Boolean newStatus) {
        if (null == this.connectionStatusCallback) {
            return;
        }
        this.connectionStatusCallback.statusChanged(newStatus);
    }
}