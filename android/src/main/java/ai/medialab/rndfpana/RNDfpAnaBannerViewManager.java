package ai.medialab.rndfpana;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableNativeArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.google.android.gms.ads.AdSize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sh.whisper.ads.Ana;
import sh.whisper.ads.UserGender;
import sh.whisper.eventtracker.EventTracker;

import static ai.medialab.rndfpana.RNDfpAnaBannerViewManager.Command.COMMAND_DESTROY_BANNER;
import static ai.medialab.rndfpana.RNDfpAnaBannerViewManager.Command.COMMAND_LOAD_BANNER;
import static ai.medialab.rndfpana.RNDfpAnaBannerViewManager.Command.COMMAND_PAUSE_BANNER;
import static ai.medialab.rndfpana.RNDfpAnaBannerViewManager.Command.COMMAND_RESUME_BANNER;

public class RNDfpAnaBannerViewManager extends ViewGroupManager<ReactPublisherAdView> {
    private static final String TAG = "RNDfpAnaBannerView";
    public static final String REACT_CLASS = "RNDfpAnaBannerView";

    public static final String PROP_AD_SIZE = "adSize";
    public static final String PROP_VALID_AD_SIZES = "validAdSizes";
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
        COMMAND_LOAD_BANNER,
        COMMAND_RESUME_BANNER,
        COMMAND_PAUSE_BANNER,
        COMMAND_DESTROY_BANNER
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
        ReactPublisherAdView adView = new ReactPublisherAdView(themedReactContext);
        return adView;
    }

    @Override
    public void addView(ReactPublisherAdView parent, View child, int index) {
        throw new RuntimeException("ReactPublisherAdView cannot have subviews");
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

    @ReactProp(name = PROP_VALID_AD_SIZES)
    public void setPropValidAdSizes(final ReactPublisherAdView view, final ReadableArray adSizeStrings) {
        ReadableNativeArray nativeArray = (ReadableNativeArray)adSizeStrings;
        ArrayList<Object> list = nativeArray.toArrayList();
        String[] adSizeStringsArray = list.toArray(new String[list.size()]);
        AdSize[] adSizes = new AdSize[list.size()];

        for (int i = 0; i < adSizeStringsArray.length; i++) {
                String adSizeString = adSizeStringsArray[i];
                adSizes[i] = getAdSizeFromString(adSizeString);
        }
        view.setValidAdSizes(adSizes);
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

    private AdSize getAdSizeFromString(String adSize) {
        switch (adSize) {
            case "banner":
                return AdSize.BANNER;
            case "largeBanner":
                return AdSize.LARGE_BANNER;
            case "mediumRectangle":
                return AdSize.MEDIUM_RECTANGLE;
            case "fullBanner":
                return AdSize.FULL_BANNER;
            case "leaderBoard":
                return AdSize.LEADERBOARD;
            case "smartBannerPortrait":
                return AdSize.SMART_BANNER;
            case "smartBannerLandscape":
                return AdSize.SMART_BANNER;
            case "smartBanner":
                return AdSize.SMART_BANNER;
            default:
                return AdSize.BANNER;
        }
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("loadBanner", COMMAND_LOAD_BANNER.ordinal());
        map.put("resumeBanner", COMMAND_RESUME_BANNER.ordinal());
        map.put("pauseBanner", COMMAND_PAUSE_BANNER.ordinal());
        map.put("destroyBanner", COMMAND_DESTROY_BANNER.ordinal());
        return map;
    }

    @Override
    public void receiveCommand(ReactPublisherAdView root, int commandId, @javax.annotation.Nullable ReadableArray args) {
        Command command = Command.values()[commandId];
        Log.v(TAG, "receiveCommand: " + command);
        switch (command) {
            case COMMAND_LOAD_BANNER:
                root.loadBanner();
                break;
            case COMMAND_RESUME_BANNER:
                root.resumeBanner();
                break;
            case COMMAND_PAUSE_BANNER:
                root.pauseBanner();
                break;
            case COMMAND_DESTROY_BANNER:
                root.destroyBanner();
                break;
        }
    }
}
