package ai.medialab.rndfpana;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableNativeArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.google.android.gms.ads.AdSize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import sh.whisper.ads.Ana;
import sh.whisper.ads.UserGender;
import sh.whisper.eventtracker.EventTracker;

import static ai.medialab.rndfpana.RNDfpAnaBannerViewManager.Command.DESTROY_BANNER;
import static ai.medialab.rndfpana.RNDfpAnaBannerViewManager.Command.LOAD_BANNER;
import static ai.medialab.rndfpana.RNDfpAnaBannerViewManager.Command.PAUSE_BANNER;
import static ai.medialab.rndfpana.RNDfpAnaBannerViewManager.Command.RESUME_BANNER;

public class RNDfpAnaBannerViewManager extends ViewGroupManager<ReactPublisherAdView> {
    private static final String TAG = "RNDfpAnaBannerView";
    public static final String REACT_CLASS = "RNDfpAnaBannerView";

    public static final String PROP_AD_SIZE = "adSize";
    public static final String PROP_AD_UNIT_ID = "adUnitID";
    public static final String PROP_TEST_DEVICES = "testDevices";

    public enum Event {
        ON_SIZE_CHANGED("onSizeChanged"),
        ON_AD_LOADED("onAdLoaded"),
        ON_AD_FAILED_TO_LOAD("onAdFailedToLoad"),
        ON_AD_OPENED("onAdOpened"),
        ON_AD_CLOSED("onAdClosed"),
        ON_AD_LEFT_APPLICATION("onAdLeftApplication"),
        ON_APP_EVENT("onAppEvent");

        String value;
        Event(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum Command {
        LOAD_BANNER,
        RESUME_BANNER,
        PAUSE_BANNER,
        DESTROY_BANNER
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected ReactPublisherAdView createViewInstance(ThemedReactContext themedReactContext) {
        // SDKs should have been initialized in RNDfpAnaModule but it sporadically fails because the
        // current activity was null. Make sure SDks are initialized here.  Redundant calls to init
        // methods are ignored.
        Activity activity = themedReactContext.getCurrentActivity();
        EventTracker.getInstance().init(activity, SessionInfo.getUserId());
        Ana.getInstance().init(activity, SessionInfo.getUserId(), SessionInfo.getBaseUrl(),
                SessionInfo.getAge(), UserGender.fromString(SessionInfo.getGender()));
        return new ReactPublisherAdView(themedReactContext);
    }

    @Override
    @Nullable
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
        for (int i = 0; i < Event.values().length; i++) {
            String event = Event.values()[i].toString();
            builder.put(event, MapBuilder.of("registrationName", event));
        }
        return builder.build();
    }

    @ReactProp(name = PROP_AD_SIZE)
    public void setPropAdSize(final ReactPublisherAdView view, final String sizeString) {
        AdSize adSize = getAdSizeFromString(sizeString);
        view.setAdSize(adSize);
    }

    @ReactProp(name = PROP_AD_UNIT_ID)
    public void setPropAdUnitID(final ReactPublisherAdView view, final String adUnitID) {
        view.setAdUnitID(adUnitID);
    }

    @ReactProp(name = PROP_TEST_DEVICES)
    public void setPropTestDevices(final ReactPublisherAdView view, final ReadableArray testDevices) {
        ReadableNativeArray nativeArray = (ReadableNativeArray)testDevices;
        ArrayList<Object> list = nativeArray.toArrayList();
        view.setTestDevices(list.toArray(new String[list.size()]));
    }

    private AdSize getAdSizeFromString(String value) {
        if (value != null && value.toLowerCase(Locale.US).contains("medium")) {
            return AdSize.MEDIUM_RECTANGLE;
        } else {
            return AdSize.BANNER;
        }
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("loadBanner", LOAD_BANNER.ordinal());
        map.put("resumeBanner", RESUME_BANNER.ordinal());
        map.put("pauseBanner", PAUSE_BANNER.ordinal());
        map.put("destroyBanner", DESTROY_BANNER.ordinal());
        return map;
    }

    @Override
    public void receiveCommand(ReactPublisherAdView view, int commandId, @javax.annotation.Nullable ReadableArray args) {
        Command command = Command.values()[commandId];
        Log.v(TAG, "receiveCommand: " + command);
        switch (command) {
            case LOAD_BANNER:
                view.loadBanner();
                break;
            case RESUME_BANNER:
                view.resumeBanner();
                break;
            case PAUSE_BANNER:
                view.pauseBanner();
                break;
            case DESTROY_BANNER:
                view.destroyBanner();
                break;
        }
    }
}
