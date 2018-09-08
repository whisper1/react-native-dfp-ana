package ai.medialab.rndfpana;

import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableNativeArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static ai.medialab.rndfpana.RNDfpAnaBannerViewManager.Command.COMMAND_DESTROY_BANNER;
import static ai.medialab.rndfpana.RNDfpAnaBannerViewManager.Command.COMMAND_LOAD_BANNER;
import static ai.medialab.rndfpana.RNDfpAnaBannerViewManager.Command.COMMAND_PAUSE_BANNER;
import static ai.medialab.rndfpana.RNDfpAnaBannerViewManager.Command.COMMAND_RESUME_BANNER;

class ReactPublisherAdView extends ReactViewGroup implements AppEventListener, LifecycleEventListener {
    private static final String TAG = "ReactPublisherAdView";
    private static final int REFRESH_INTERVAL_SECONDS = 10;
    protected PublisherAdView adView;
    protected DfpBannerAdLoader adLoader;
    String[] testDevices;
    AdSize[] validAdSizes;
    String adUnitID;
    AdSize adSize;
    ReactContext reactContext;

    public ReactPublisherAdView(final ReactContext context) {
        super(context);
        reactContext = context;
        reactContext.addLifecycleEventListener(this);
        this.createAdView();
    }

    @Override
    public void onHostResume() {
        Log.v(TAG, "onHostResume");
        resumeBanner();
    }

    @Override
    public void onHostPause() {
        Log.v(TAG, "onHostPause");
        pauseBanner();
    }

    @Override
    public void onHostDestroy() {
        Log.v(TAG, "onHostDestroy");
        destroyBanner();
    }

    private void createAdView() {
        if (this.adView != null) this.adView.destroy();
        this.adView = new PublisherAdView(reactContext.getCurrentActivity());
        this.adView.setAppEventListener(this);
        this.addView(this.adView);
    }

    private void sendOnSizeChangeEvent() {
        int width;
        int height;
        WritableMap event = Arguments.createMap();
        AdSize adSize = this.adView.getAdSize();
        if (adSize == AdSize.SMART_BANNER) {
            width = (int) PixelUtil.toDIPFromPixel(adSize.getWidthInPixels(reactContext));
            height = (int) PixelUtil.toDIPFromPixel(adSize.getHeightInPixels(reactContext));
        } else {
            width = adSize.getWidth();
            height = adSize.getHeight();
        }
        event.putDouble("width", width);
        event.putDouble("height", height);
        sendEvent(RNDfpAnaBannerViewManager.EVENT_SIZE_CHANGE, event);
    }

    private void sendEvent(String name, @Nullable WritableMap event) {
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                        getId(),
                        name,
                        event);
    }

    public void loadBanner() {
        ArrayList<AdSize> adSizes = new ArrayList<AdSize>();
        if (this.adSize != null) {
            adSizes.add(this.adSize);
        }
        if (this.validAdSizes != null) {
            for (int i = 0; i < this.validAdSizes.length; i++) {
                adSizes.add(this.validAdSizes[i]);
            }
        }

        if (adSizes.size() == 0) {
            adSizes.add(AdSize.BANNER);
        }

        AdSize[] adSizesArray = adSizes.toArray(new AdSize[adSizes.size()]);
        this.adView.setAdSizes(adSizesArray);

        if (adLoader != null) {
            adLoader.loadAd(true);
        }
    }

    public void resumeBanner() {
        Log.v(TAG, "resumeBanner");
        if (adLoader != null) {
            adLoader.resume(true);
        }
    }

    public void pauseBanner() {
        Log.v(TAG, "pauseBanner");
        if (adLoader != null) {
            adLoader.pause();
        }
    }

    public void destroyBanner() {
        Log.v(TAG, "destroyBanner");
        if (adLoader != null) {
            adLoader.pause();
            adLoader.destroy();
            adLoader = null;
        }
    }

    public void setAdUnitID(String adUnitID) {
        if (this.adUnitID != null) {
            if (this.adUnitID.equals(adUnitID)) {
                // Trying to set the same ad unit ID.  Just return.
                return;
            }
            // We can only set adUnitID once, so when it was previously set we have
            // to recreate the view
            this.createAdView();
        }
        this.adUnitID = adUnitID;
        this.adView.setAdUnitId(adUnitID);
        adLoader = new DfpBannerAdLoader(this.adView, adUnitID, REFRESH_INTERVAL_SECONDS, null, new DfpBannerAdLoader.BannerLoadListener() {
            @Override
            public void onLoadFinished(boolean success, int errorCode) {
                if (success) {
                    int width = adView.getAdSize().getWidthInPixels(adView.getContext());
                    int height = adView.getAdSize().getHeightInPixels(adView.getContext());
                    int left = adView.getLeft();
                    int top = adView.getTop();
                    adView.measure(width, height);
                    adView.layout(left, top, left + width, top + height);
                    sendOnSizeChangeEvent();
                    sendEvent(RNDfpAnaBannerViewManager.EVENT_AD_LOADED, null);
                } else {
                    String errorMessage = "Unknown error";
                    switch (errorCode) {
                        case PublisherAdRequest.ERROR_CODE_INTERNAL_ERROR:
                            errorMessage = "Internal error, an invalid response was received from the ad server.";
                            break;
                        case PublisherAdRequest.ERROR_CODE_INVALID_REQUEST:
                            errorMessage = "Invalid ad request, possibly an incorrect ad unit ID was given.";
                            break;
                        case PublisherAdRequest.ERROR_CODE_NETWORK_ERROR:
                            errorMessage = "The ad request was unsuccessful due to network connectivity.";
                            break;
                        case PublisherAdRequest.ERROR_CODE_NO_FILL:
                            errorMessage = "The ad request was successful, but no ad was returned due to lack of ad inventory.";
                            break;
                    }
                    WritableMap event = Arguments.createMap();
                    WritableMap error = Arguments.createMap();
                    error.putString("message", errorMessage);
                    event.putMap("error", error);
                    sendEvent(RNDfpAnaBannerViewManager.EVENT_AD_FAILED_TO_LOAD, event);
                }
            }
        });

        adLoader.loadAd(true);
    }

    public void setTestDevices(String[] testDevices) {
        this.testDevices = testDevices;
    }

    public void setAdSize(AdSize adSize) {
        this.adSize = adSize;
    }

    public void setValidAdSizes(AdSize[] adSizes) {
        this.validAdSizes = adSizes;
    }

    @Override
    public void onAppEvent(String name, String info) {
        WritableMap event = Arguments.createMap();
        event.putString("name", name);
        event.putString("info", info);
        sendEvent(RNDfpAnaBannerViewManager.EVENT_APP_EVENT, event);
    }
}

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
