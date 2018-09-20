package ai.medialab.rndfpana;

import android.app.Activity;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import sh.whisper.ads.Ana;
import sh.whisper.ads.UserGender;
import sh.whisper.eventtracker.EventTracker;

public class RNDfpAnaModule extends ReactContextBaseJavaModule {
    private static final String TAG = "RNDfpAnaModule";
    private final ReactApplicationContext reactContext;

    public RNDfpAnaModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNDfpAna";
    }

    @ReactMethod
    public void init(String userId, String baseUrl, int age, String gender) {
        Log.v(TAG, "Initializing libraries with uid: " + userId);
        SessionInfo.set(userId, baseUrl, age, gender);
        Activity activity = getCurrentActivity();
        if (activity != null) {
            EventTracker.getInstance().init(activity, userId);
            Ana.getInstance().init(activity, userId, baseUrl, age, UserGender.fromString(gender));
        } else {
            // Will be retried in RNDfpAnaBannerViewManager
            Log.e(TAG, "init - current activity was null");
        }
    }
}