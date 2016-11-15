package neobyte.scode.webflip;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import neobyte.scode.webflip.tools.NetworkChangeReceiver;

/**
 * Created by neobyte on 11/3/2016.
 */

public class MyPDFApp extends Application {

    private static MyPDFApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    public static synchronized MyPDFApp getInstance(){
        return mInstance;
    }

    public void setConnectivityListener(NetworkChangeReceiver.ConnectivityReceiverListener listener){
        NetworkChangeReceiver.connectivityReceiverListener = listener;
    }

}