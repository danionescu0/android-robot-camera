package ionescu.dan.rccameracontroller.dependencyinjection;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ionescu.dan.rccameracontroller.communication.Communicator;
import ionescu.dan.rccameracontroller.communication.MotorCommandFormatter;
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
    MotorCommandFormatter providesMottorCommandFormatter() {
        return new MotorCommandFormatter();
    }

    @Provides
    @Singleton
    Communicator providesCommunicator(Mqttt mqttt, MotorCommandFormatter motorCommandFormatter) {
        return new Communicator(mqttt, motorCommandFormatter);
    }
}