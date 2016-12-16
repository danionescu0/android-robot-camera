package ionescu.dan.rccameracontroller;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import javax.inject.Inject;

import ionescu.dan.rccameracontroller.communication.Communicator;
import ionescu.dan.rccameracontroller.communication.IncommingRobotCommunicationCallback;
import ionescu.dan.rccameracontroller.services.MetaDataContainer;

public class MainActivity extends AppCompatActivity {

    private String TAG = "main";
    private RcCameraControllerApplication app;
    private MotionEvent latestMotionEvent;
    private Handler sendCommandsHandler = new Handler();
    private long transmitCommandsInterval = 300;
    private AsyncTask asyncTask;

    @Inject Communicator communicator;

    private Runnable sendCommand = new Runnable() {
        @Override
        public void run() {
            sendCommandsHandler.postDelayed(sendCommand, transmitCommandsInterval);
            asyncTask.getStatus();
            if (null == latestMotionEvent) {
                return;
            }
            communicator.sendMotionCommand(latestMotionEvent);
            latestMotionEvent = null;
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
        communicator.connect();
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

        this.initializeWebview();
        this.sendCommandsHandler.post(sendCommand);
    }

    private void initializeWebview() {
        String streamUrl = MetaDataContainer.get(
                getApplicationContext(), "dan.ionescu.rccameracontroller.stream_endpoint");
        final String streamUsername = MetaDataContainer.get(
                getApplicationContext(), "dan.ionescu.rccameracontroller.stream_username");
        final String streamPassword = MetaDataContainer.get(
                getApplicationContext(), "dan.ionescu.rccameracontroller.stream_password");
        WebView webView = (WebView) findViewById(R.id.webview);
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
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                latestMotionEvent = motionEvent;
                return false;
            }
        });
        webView.loadUrl(streamUrl);
    }
}