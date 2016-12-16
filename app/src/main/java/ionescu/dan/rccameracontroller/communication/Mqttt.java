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
    private String TAG = "main";
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
        } catch(MqttException me) {
            Log.d(TAG,"msg "+me.getMessage());
        }
    }

    public void send(String data) {
        try {
            MqttMessage message = new MqttMessage(data.getBytes());
            message.setQos(qos);
            client.publish(sendRobotMovementTopic, message);
        } catch(MqttException me) {
            if (me.getReasonCode() == MqttException.REASON_CODE_CLIENT_NOT_CONNECTED) {
                this.connect();
            }
            Log.d(TAG,"msg "+me.getMessage());
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

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "connection loast");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}
}