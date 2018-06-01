package ionescu.dan.rccameracontroller.dependencyinjection;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ionescu.dan.rccameracontroller.VideoStreamWebviewSetup;
import ionescu.dan.rccameracontroller.WheelRotate;
import ionescu.dan.rccameracontroller.communication.Communicator;
import ionescu.dan.rccameracontroller.communication.DirectionsInterpretter;
import ionescu.dan.rccameracontroller.communication.SerialCommandFormatter;
import ionescu.dan.rccameracontroller.communication.Mqttt;
import ionescu.dan.rccameracontroller.services.MetaDataContainer;

@Module
public class AppModule {

    @Provides
    @Singleton
    Mqttt providesMqttt(Context context) {
        String mqttEndpoint = MetaDataContainer.get(context, "dan.ionescu.rccameracontroller.mqtt_endpoint");
        String mqtttUsername = MetaDataContainer.get(context, "dan.ionescu.rccameracontroller.mqtt_username");
        String mqtttPassword = MetaDataContainer.get(context, "dan.ionescu.rccameracontroller.mqtt_password");

        return new Mqttt(mqttEndpoint, mqtttUsername, mqtttPassword);
    }

    @Provides
    @Singleton
    WheelRotate providesWheelRotate(DirectionsInterpretter directionsInterpretter) {
        return new WheelRotate(directionsInterpretter);
    }

    @Provides
    @Singleton
    SerialCommandFormatter providesMottorCommandFormatter() {
        return new SerialCommandFormatter();
    }

    @Provides
    @Singleton
    DirectionsInterpretter providesDirectionsInterpretter() {
        return new DirectionsInterpretter();
    }

    @Provides
    @Singleton
    Communicator providesCommunicator(Mqttt mqttt, SerialCommandFormatter serialCommandFormatter,
                                      DirectionsInterpretter directionsInterpretter) {
        return new Communicator(mqttt, serialCommandFormatter, directionsInterpretter);
    }

    @Provides
    @Singleton
    VideoStreamWebviewSetup providesWeview() {
        return new VideoStreamWebviewSetup();
    }
}