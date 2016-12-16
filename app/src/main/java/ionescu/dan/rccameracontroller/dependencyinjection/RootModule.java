package ionescu.dan.rccameracontroller.dependencyinjection;

import android.app.Application;
import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class RootModule {
    Application application;

    public RootModule(Application application) {
        this.application = application;
    }

    @Provides
    public Context providesContext() {
        return application.getApplicationContext();
    }

    @Provides
    public Application providesApplication() {
        return application;
    }
}
