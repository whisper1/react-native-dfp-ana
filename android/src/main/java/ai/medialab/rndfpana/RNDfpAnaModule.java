package ai.medialab.rndfpana;

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
    public void init(String userId, String baseUrl, int userAge, String gender) {
        Log.v(TAG, "Initializing libraries with uid: " + userId);
        EventTracker.getInstance().init(getCurrentActivity(), userId);
        Ana.getInstance().init(getCurrentActivity(), userId, baseUrl, userAge, UserGender.fromString(gender));
    }
}