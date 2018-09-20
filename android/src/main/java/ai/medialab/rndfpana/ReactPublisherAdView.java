package ai.medialab.rndfpana;

import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import java.util.ArrayList;

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
