package ionescu.dan.rccameracontroller;

import android.content.Context;
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
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import ionescu.dan.rccameracontroller.communication.Communicator;
import ionescu.dan.rccameracontroller.communication.IncommingRobotCommunicationCallback;
import ionescu.dan.rccameracontroller.communication.MoveEvent;
import ionescu.dan.rccameracontroller.services.MetaDataContainer;

public class MainActivity extends AppCompatActivity {

    private RcCameraControllerApplication app;
    protected MoveEvent lastMoveEvent;
    private boolean steeringWheelActive = false;
    private Handler sendCommandsHandler = new Handler();
    private Display display;
    private AsyncTask asyncTask;

    @Inject Communicator communicator;

    private Runnable sendCommand = new Runnable() {
        @Override
        public void run() {
            long interval = Long.parseLong(MetaDataContainer.get(
                    getApplicationContext(), "dan.ionescu.rccameracontroller.transmit_command_interval"));
            sendCommandsHandler.postDelayed(sendCommand, interval);
            asyncTask.getStatus();
            if (false == steeringWheelActive) {
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
        app = (RcCameraControllerApplication) getApplication();
        app.getAppComponent().inject(this);
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        communicator.initialize();
        this.initializeBatteryUpdater();
        this.initializeWebview();
        ImageView steeringWheel = (ImageView) findViewById(R.id.steering_wheel);
        steeringWheel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    steeringWheelActive = false;
                } else if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    steeringWheelActive = true;
                }
                //@ToDo replace this hack with a factory
                lastMoveEvent = new MoveEvent(motionEvent.getX() - 30, motionEvent.getY() - 160, 500, 500);
                Log.d("cici", "x:" +  lastMoveEvent.getX() + "  y:" +  lastMoveEvent.getY());
                return true;
            }
        });
        this.sendCommandsHandler.post(sendCommand);
    }

    private void initializeBatteryUpdater() {
        asyncTask = new AsyncTask<Void, Float, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                communicator.addIncommingMessageListener(new IncommingRobotCommunicationCallback() {
                    @Override
                    public void batteryLevelUpdated(float level) {
                        publishProgress(level);
                    }
                });
                return null;
            }

            @Override
            protected void onProgressUpdate(Float... values) {
                Float batteryLevel = values[0];
                Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
                TextView toolbarBatteryText = (TextView) toolbarTop.findViewById(R.id.toolbar_battery_text);
                toolbarBatteryText.setText(Float.toString(batteryLevel) + "%");
            }
        }.execute();
    }

    private void initializeWebview() {
        String streamUrl = MetaDataContainer.get(
                getApplicationContext(), "dan.ionescu.rccameracontroller.stream_endpoint");
        final String streamUsername = MetaDataContainer.get(
                getApplicationContext(), "dan.ionescu.rccameracontroller.stream_username");
        final String streamPassword = MetaDataContainer.get(
                getApplicationContext(), "dan.ionescu.rccameracontroller.stream_password");
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.setInitialScale(getScale());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
            @Override
            public void onReceivedHttpAuthRequest(WebView view,
                                                  HttpAuthHandler handler, String host, String realm) {
                handler.proceed(streamUsername, streamPassword);
            }
        });
        webView.loadUrl(streamUrl);
    }

    private int getScale() {
        Point size = new Point();
        display.getSize(size);
        Double val = new Double(size.x) / new Double(750);
        val = val * 100d;

        return val.intValue();
    }
}