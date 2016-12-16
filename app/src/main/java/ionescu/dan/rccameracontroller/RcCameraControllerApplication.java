package ionescu.dan.rccameracontroller;

import android.app.Application;

import ionescu.dan.rccameracontroller.dependencyinjection.AppComponent;
import ionescu.dan.rccameracontroller.dependencyinjection.AppModule;
import ionescu.dan.rccameracontroller.dependencyinjection.DaggerAppComponent;
import ionescu.dan.rccameracontroller.dependencyinjection.RootModule;

public class RcCameraControllerApplication extends Application {
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = DaggerAppComponent.builder()
                .rootModule(new RootModule(this))
                .appModule(new AppModule())
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}