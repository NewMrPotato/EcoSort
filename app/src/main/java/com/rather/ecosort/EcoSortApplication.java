package com.rather.ecosort;

import android.app.Application;

import com.yandex.mapkit.MapKitFactory;

public class EcoSortApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.MAPKIT_API_KEY.isEmpty()) {
            MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY);
        }
    }
}
