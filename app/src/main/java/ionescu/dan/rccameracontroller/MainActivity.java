package ionescu.dan.rccameracontroller;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
    protected MoveEvent lastMoveEvent;
    private boolean lastSentLightSwitchState;
    private boolean steeringWheelActive = false;
    private Handler sendCommandsHandler = new Handler();
    private Display display;
    private AsyncTask asyncTask;
    private LightSwitchListener lightSwitchListener;

    @Inject Communicator communicator;

    @Inject DirectionsInterpretter directionsInterpretter;

    @Inject WebviewSetup webviewSetup;

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
            if (!steeringWheelActive) {
                return;
            }

            communicator.sendMotionCommand(lastMoveEvent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_top);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        app = (RcCameraControllerApplication) getApplication();
        app.getAppComponent().inject(this);
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        this.lightSwitchListener = new LightSwitchListener();
        findViewById(R.id.light_switch).bringToFront();
        this.initializeIncommingCommunicationProcesser();
        this.initializeErrorDisplay();
        this.initializeSteeringWheel();
        this.initializeLightButton();
        communicator.initialize();
        this.initializeWebview();

        this.sendCommandsHandler.post(sendCommand);
    }

    private void initializeSteeringWheel() {
        final ImageView steeringWheel = (ImageView) findViewById(R.id.steering_wheel);
        steeringWheel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    steeringWheelActive = false;
                } else if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    steeringWheelActive = true;
                }
                if (motionEvent.getX() < 0 || motionEvent.getY() < 0) {
                    return true;
                }
                lastMoveEvent = MoveEventFactory.createFromMotion(motionEvent, view);
                rotateWheel(steeringWheel);
                return true;
            }
        });
    }

    private void rotateWheel(ImageView steeringWheel) {
        Matrix matrix = new Matrix();
        steeringWheel.setScaleType(ImageView.ScaleType.MATRIX);
        int rotateWheel = directionsInterpretter.getScaledX(lastMoveEvent) * 2;
        matrix.postRotate(rotateWheel, steeringWheel.getDrawable().getBounds().width() / 2,
                steeringWheel.getDrawable().getBounds().height() / 2);
        matrix.postScale(0.415f, 0.415f);
        steeringWheel.setImageMatrix(matrix);
    }

    private void initializeLightButton() {
        final ToggleButton toggleButton = (ToggleButton) findViewById(R.id.light_switch);
        toggleButton.setOnCheckedChangeListener(this.lightSwitchListener);
    }

    private void initializeIncommingCommunicationProcesser() {
        asyncTask = new AsyncTask<Void, Float, Void>() {
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
        }.execute();
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
        webviewSetup
                .configure(webView, getWebviewScale(), streamUsername, streamPassword)
                .loadUrl(streamUrl);
    }

    private int getWebviewScale() {
        Point size = new Point();
        display.getSize(size);
        Double val = new Double(size.x) / new Double(750);
        val = val * 100d;

        return val.intValue();
    }
}