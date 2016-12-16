package ionescu.dan.rccameracontroller.services;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class MetaDataContainer {
    public static String get(Context context, String key) {
        try{
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;

            return bundle.getString(key);
        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
            throw new RuntimeException(String.format("No value found for param: ", (key)));
        }
    }
}