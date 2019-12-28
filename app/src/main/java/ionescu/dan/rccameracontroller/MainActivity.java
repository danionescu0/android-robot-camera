package ionescu.dan.rccameracontroller;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import javax.inject.Inject;

import ionescu.dan.rccameracontroller.communication.Communicator;
import ionescu.dan.rccameracontroller.communication.ConnectionStatusCallback;
import ionescu.dan.rccameracontroller.communication.DirectionsInterpretter;
import ionescu.dan.rccameracontroller.communication.IncommingRobotCommunicationCallback;
import ionescu.dan.rccameracontroller.communication.MoveEvent;
import ionescu.dan.rccameracontroller.communication.MoveEventFactory;
import ionescu.dan.rccameracontroller.listeners.LightSwitchListener;
import ionescu.dan.rccameracontroller.services.MetaDataContainer;

public class MainActivity extends AppCompatActivity {
    private RcCameraControllerApplication app;
    private boolean lastSentLightSwitchState;
    private boolean steeringWheelActive = false;
    private Handler sendCommandsHandler = new Handler();
    private Display display;
    private AsyncTask asyncTask;
    private LightSwitchListener lightSwitchListener;
    private ImageView steeringWheel;
    private MoveEvent lastMoveEvent;

    @Inject Communicator communicator;

    @Inject DirectionsInterpretter directionsInterpretter;

    @Inject
    VideoStreamWebviewSetup videoStreamWebviewSetup;

    @Inject
    WheelRotate wheelRotate;

    private class IncommingCommunicationProcesser extends AsyncTask<Void, Float, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            communicator.addIncommingMessageListener(new IncommingRobotCommunicationCallback() {
                @Override
                public void batteryLevelUpdated(float level) {
                    publishProgress(level);
                }

                @Override
                public void distanceUpdated(float front, float back) {
                    publishProgress(null, front, back);
                }
            });

            return null;
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            if (null != values[0]) {
                updateBatteryStatus(values[0]);
            }
            if (null != values[1] && null != values[2]) {
                updateObstacleStatus(values[1], values[2]);
            }
        }
    }

    private class SteeringWheelListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            steeringWheelActive = true;
            if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                steeringWheelActive = false;
            }
            if (motionEvent.getX() < 0 || motionEvent.getY() < 0) {
                return true;
            }
            lastMoveEvent = MoveEventFactory.createFromMotion(motionEvent, view);
            steeringWheel = wheelRotate.getRotated(steeringWheel, lastMoveEvent);

            return true;
        }
    }

    private Runnable sendCommand = new Runnable() {
        @Override
        public void run() {
            long interval = Long.parseLong(MetaDataContainer.get(
                    getApplicationContext(), "dan.ionescu.rccameracontroller.transmit_command_interval"));
            sendCommandsHandler.postDelayed(sendCommand, interval);
            asyncTask.getStatus();
            if (lightSwitchListener.getLastSwitchState() != lastSentLightSwitchState) {
                communicator.sendLightCommand(lightSwitchListener.getLastSwitchState());
                lastSentLightSwitchState = lightSwitchListener.getLastSwitchState();
            }
            if (steeringWheelActive) {
                communicator.sendMotionCommand(lastMoveEvent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_top);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.app = (RcCameraControllerApplication) getApplication();
        this.app.getAppComponent().inject(this);
        this.steeringWheel = (ImageView) findViewById(R.id.steering_wheel);
        this.lightSwitchListener = new LightSwitchListener();
        this.display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        findViewById(R.id.light_switch).bringToFront();
        this.asyncTask = new IncommingCommunicationProcesser().execute();
        this.initializeErrorDisplay();
        this.steeringWheel.setOnTouchListener(new SteeringWheelListener());
        this.initializeLightButton();
        this.communicator.initialize();
        this.initializeWebview();
        this.sendCommandsHandler.post(sendCommand);
    }

    private void initializeLightButton() {
        ((ToggleButton) findViewById(R.id.light_switch))
                .setOnCheckedChangeListener(this.lightSwitchListener);
    }

    private void updateBatteryStatus(Float batteryLevel) {
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        TextView toolbarBatteryText = (TextView) toolbarTop.findViewById(R.id.toolbar_battery_text);
        toolbarBatteryText.setText(Float.toString(batteryLevel) + "%");
    }

    private void updateObstacleStatus(Float front, Float back) {
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        TextView toolbarObstacleText = (TextView) toolbarTop.findViewById(R.id.toolbar_obstacle_text);
        String status = "no obstacles";
        if (front > 0) {
            status = "obstacle ahead";
        }
        else if (back > 0) {
            status = "obstacle behind";
        }
        toolbarObstacleText.setText(status);
    }

    private void initializeErrorDisplay() {
        asyncTask = new AsyncTask<Void, Boolean, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                communicator.addConectionStatusListener(new ConnectionStatusCallback() {
                    @Override
                    public void statusChanged(Boolean newStatus) {
                        publishProgress(newStatus);
                    }
                });
                return null;
            }

            @Override
            protected void onProgressUpdate(Boolean... values) {
                updateErrorStatus(values[0]);
            }
        }.execute();
    }

    private void updateErrorStatus(boolean newStatus) {
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        TextView toolbarBatteryText = (TextView) toolbarTop.findViewById(R.id.toolbar_connection_status);
        toolbarBatteryText.setText(newStatus ? getResources().getString(R.string.connection_on) :
                getResources().getString(R.string.connection_off));
    }

    private void initializeWebview() {
        String streamUrl = MetaDataContainer.get(
                getApplicationContext(), "dan.ionescu.rccameracontroller.stream_endpoint");
        final String streamUsername = MetaDataContainer.get(
                getApplicationContext(), "dan.ionescu.rccameracontroller.stream_username");
        final String streamPassword = MetaDataContainer.get(
                getApplicationContext(), "dan.ionescu.rccameracontroller.stream_password");
        WebView webView = (WebView) findViewById(R.id.webview);
        this.videoStreamWebviewSetup
                .configure(webView, streamUsername, streamPassword)
                .loadUrl(streamUrl);
    }
}