package com.reactlibrarylocalhotspot;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class LocalOnlyHotspotModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private class HandlerThread extends Thread {
        private WifiManager.LocalOnlyHotspotReservation mReservation;

        public HandlerThread(WifiManager.LocalOnlyHotspotReservation reservation) {
            mReservation = reservation;
        }

        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final ReactApplicationContext reactContext;

    private final WifiManager wifiManager;
    private WifiManager.LocalOnlyHotspotReservation mReservation;

    public LocalOnlyHotspotModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        reactContext.addLifecycleEventListener(this);
        wifiManager = (WifiManager) reactContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public String getName() {
        return "LocalOnlyHotspot";
    }

    @ReactMethod
    public void start() {
        this.stop();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {
                    @Override
                    public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                        super.onStarted(reservation);
                        mReservation = reservation;
                        sendEvent(reactContext, "LocalOnlyHotspotStarted", config());
                        HandlerThread testHandlerThread = new HandlerThread(reservation);
                        testHandlerThread.setDaemon(true);
                        testHandlerThread.start();
                    }

                    @Override
                    public void onFailed(int reason) {
                        super.onFailed(reason);
                        WritableMap params = Arguments.createMap();
                        params.putInt("reason", reason);
                        sendEvent(reactContext, "LocalOnlyHotspotFailed", params);
                    }

                    @Override
                    public void onStopped() {
                        super.onStopped();
                        WritableMap params = Arguments.createMap();
                        params.putBoolean("unexpected", true);
                        sendEvent(reactContext, "LocalOnlyHotspotStopped", params);
                    }
                }, new Handler());
            } catch (RuntimeException e) {
                WritableMap params = Arguments.createMap();
                params.putString("exception", e.getClass().getName());
                params.putString("msg", e.getLocalizedMessage());
                sendEvent(reactContext, "LocalOnlyHotspotException", params);
            }
        }
    }

    @ReactMethod
    public void stop() {
        if (mReservation != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mReservation.close();
                WritableMap params = Arguments.createMap();
                params.putBoolean("unexpected", false);
                sendEvent(reactContext, "LocalOnlyHotspotStopped", params);
            }
        }
    }

    @ReactMethod
    public void getConfig(Callback callback) {
        callback.invoke(config());
    }

    @ReactMethod
    public void addListener(String eventName) {
        // Set up any upstream listeners or background tasks as necessary
    }

    @ReactMethod
    public void removeListeners(Integer count) {
        // Remove upstream listeners, stop unnecessary background tasks
    }

    private WritableMap config() {
        WritableMap resultData = new WritableNativeMap();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mReservation != null) {
            resultData.putString("ssid", mReservation.getWifiConfiguration().SSID);
            resultData.putString("secret", mReservation.getWifiConfiguration().preSharedKey);
        }
        return resultData;
    }

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    @Override
    public void onHostResume() {
        // Activity `onResume`
    }

    @Override
    public void onHostPause() {
        // Activity `onPause`
    }

    @Override
    public void onHostDestroy() {
        // Activity `onDestroy`
        stop();
    }

    @Override
    public void onCatalystInstanceDestroy() {
        // Activity `onCatalystInstanceDestroy`
        stop();
    }
}
