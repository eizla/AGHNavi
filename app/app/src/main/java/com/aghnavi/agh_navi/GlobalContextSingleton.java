package com.aghnavi.agh_navi;


import android.content.Context;
import android.support.annotation.NonNull;

import com.aghnavi.agh_navi.dmsl.nav.AnyUserData;
import com.aghnavi.agh_navi.dmsl.wifi.SimpleWifiManager;

public class GlobalContextSingleton {

    private static volatile GlobalContextSingleton instance;
    private final Context mContext;
    private AnyUserData userData;

    private GlobalContextSingleton(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    public static GlobalContextSingleton getInstance(@NonNull Context context) {
        GlobalContextSingleton localInstance = instance;
        if (localInstance == null) {
            synchronized (GlobalContextSingleton.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new GlobalContextSingleton(context);
                }
            }
        }
        return localInstance;
    }

    public SimpleWifiManager getSimpleWifiManager() {
        return SimpleWifiManager.getInstance(mContext);
    }

    public void setUserData(AnyUserData userData) {
        this.userData = userData;
    }

    public AnyUserData getUserData() {
        return userData;
    }
}
