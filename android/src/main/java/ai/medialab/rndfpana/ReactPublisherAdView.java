package ai.medialab.rndfpana;

import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

class ReactPublisherAdView extends ReactViewGroup implements AppEventListener, LifecycleEventListener {
    private static final String TAG = "ReactPublisherAdView";
    private static final int REFRESH_INTERVAL_SECONDS = 10;
    private PublisherAdView mAdView;
    private DfpBannerAdLoader mAdLoader;
    private String mAdUnitId;
    private AdSize mAdSize;
    private ReactContext mReactContext;

    public ReactPublisherAdView(final ReactContext context) {
        super(context);
        mReactContext = context;
        mReactContext.addLifecycleEventListener(this);
        createAdView();
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
        if (mAdView != null) {
            ViewParent parent = mAdView.getParent();
            if (parent != null && parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(mAdView);
            }
            mAdView.destroy();
        }
        mAdView = new PublisherAdView(mReactContext.getCurrentActivity());
        mAdView.setAppEventListener(this);
        addView(mAdView);
    }

    private void sendOnSizeChangedEvent() {
        WritableMap event = Arguments.createMap();
        AdSize adSize = mAdView.getAdSize();
        event.putDouble("width", adSize.getWidth());
        event.putDouble("height", adSize.getHeight());
        sendEvent(RNDfpAnaBannerViewManager.Event.ON_SIZE_CHANGED.toString(), event);
    }

    private void sendEvent(String name, @Nullable WritableMap event) {
        mReactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), name, event);
    }

    public void loadBanner() {
        if (mAdSize != null) {
            mAdView.setAdSizes(mAdSize);
        } else {
            mAdView.setAdSizes(AdSize.BANNER);
        }

        if (mAdLoader != null) {
            mAdLoader.loadAd(true);
        }
    }

    public void resumeBanner() {
        Log.v(TAG, "resumeBanner");
        if (mAdLoader != null) {
            mAdLoader.resume(true);
        }
    }

    public void pauseBanner() {
        Log.v(TAG, "pauseBanner");
        if (mAdLoader != null) {
            mAdLoader.pause();
        }
    }

    public void destroyBanner() {
        Log.v(TAG, "destroyBanner");
        if (mAdLoader != null) {
            mAdLoader.pause();
            mAdLoader.destroy();
            mAdLoader = null;
        }
    }

    public void setAdUnitID(String adUnitID) {
        if (mAdUnitId != null) {
            if (mAdUnitId.equals(adUnitID)) {
                // Trying to set the same ad unit ID.  Just return.
                return;
            }
            createAdView();
        }
        mAdUnitId = adUnitID;
        mAdView.setAdUnitId(adUnitID);
        mAdLoader = new DfpBannerAdLoader(mAdView, adUnitID, REFRESH_INTERVAL_SECONDS, null, new DfpBannerAdLoader.BannerLoadListener() {
            @Override
            public void onLoadFinished(boolean success, int errorCode) {
                if (success) {
                    int width = mAdView.getAdSize().getWidthInPixels(mAdView.getContext());
                    int height = mAdView.getAdSize().getHeightInPixels(mAdView.getContext());
                    int left = mAdView.getLeft();
                    int top = mAdView.getTop();
                    mAdView.measure(width, height);
                    mAdView.layout(left, top, left + width, top + height);
                    sendOnSizeChangedEvent();
                    sendEvent(RNDfpAnaBannerViewManager.Event.ON_AD_LOADED.toString(), null);
                } else {
                    WritableMap event = Arguments.createMap();
                    WritableMap error = Arguments.createMap();
                    error.putString("message", "Request failed");
                    event.putMap("error", error);
                    sendEvent(RNDfpAnaBannerViewManager.Event.ON_AD_FAILED_TO_LOAD.toString(), event);
                }
            }
        });

        mAdLoader.loadAd(true);
    }

    public void setTestDevices(String[] testDevices) {

    }

    public void setAdSize(AdSize adSize) {
        mAdSize = adSize;
    }

    @Override
    public void onAppEvent(String name, String info) {
        WritableMap event = Arguments.createMap();
        event.putString("name", name);
        event.putString("info", info);
        sendEvent(RNDfpAnaBannerViewManager.Event.ON_APP_EVENT.toString(), event);
    }
}
