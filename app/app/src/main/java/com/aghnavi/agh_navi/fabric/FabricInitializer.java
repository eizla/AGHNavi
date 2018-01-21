package com.aghnavi.agh_navi.fabric;


import android.content.Context;

import com.aghnavi.agh_navi.BuildConfig;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

public interface FabricInitializer {

    default void initializeFabric(Context ctx) {
        Fabric.with(ctx, new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build());
    }
}
