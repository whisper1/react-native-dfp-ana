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

    public static final String EVENT_SIZE_CHANGE = "onSizeChange";
    public static final String EVENT_AD_LOADED = "onAdLoaded";
    public static final String EVENT_AD_FAILED_TO_LOAD = "onAdFailedToLoad";
    public static final String EVENT_AD_OPENED = "onAdOpened";
    public static final String EVENT_AD_CLOSED = "onAdClosed";
    public static final String EVENT_AD_LEFT_APPLICATION = "onAdLeftApplication";
    public static final String EVENT_APP_EVENT = "onAppEvent";

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
        String[] events = {
            EVENT_SIZE_CHANGE,
            EVENT_AD_LOADED,
            EVENT_AD_FAILED_TO_LOAD,
            EVENT_AD_OPENED,
            EVENT_AD_CLOSED,
            EVENT_AD_LEFT_APPLICATION,
            EVENT_APP_EVENT
        };
        for (int i = 0; i < events.length; i++) {
            builder.put(events[i], MapBuilder.of("registrationName", events[i]));
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
    public void receiveCommand(ReactPublisherAdView root, int commandId, @javax.annotation.Nullable ReadableArray args) {
        Command command = Command.values()[commandId];
        Log.v(TAG, "receiveCommand: " + command);
        switch (command) {
            case LOAD_BANNER:
                root.loadBanner();
                break;
            case RESUME_BANNER:
                root.resumeBanner();
                break;
            case PAUSE_BANNER:
                root.pauseBanner();
                break;
            case DESTROY_BANNER:
                root.destroyBanner();
                break;
        }
    }
}
