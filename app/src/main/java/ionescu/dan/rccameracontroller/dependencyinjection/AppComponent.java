package ionescu.dan.rccameracontroller.dependencyinjection;

import javax.inject.Singleton;

import dagger.Component;
import ionescu.dan.rccameracontroller.MainActivity;

@Singleton
@Component(modules = {RootModule.class, AppModule.class})
public interface AppComponent {
    void inject(MainActivity mainActivity);
}