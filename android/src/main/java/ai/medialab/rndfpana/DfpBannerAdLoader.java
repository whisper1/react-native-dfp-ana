package ai.medialab.rndfpana;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.webkit.ValueCallback;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import sh.whisper.ads.Ana;
import sh.whisper.ads.AnaCustomEventBanner;
import sh.whisper.eventtracker.EventTracker;

/**
 * Created by jinohan on 8/2/18.
 */

public class DfpBannerAdLoader {
    private static final String TAG = "DfpBannerAdLoader";
    static final String ANA_BASE_URL = "http://ana-base.whisper.sh/ana/index.html";
    private static final int ANA_TIMEOUT_MILLIS = -1;
    private PublisherAdRequest mAdRequest;
    private PublisherAdView mAdView;
    private String mAdUnitId;
    private long mRefreshIntervalMillis;
    private BannerLoadListener mExternalAdListener;
    private Location mLocation;
    private long mAdRequestTimeMillis;
    private boolean mAdRefreshTimerEnabled;
    private Handler mHandler;
    private HashMap<String, String> mExtraTargeting = new HashMap<>();
    private Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadAd();
        }
    };
    private int mRefreshCount = 1;
    private boolean mAdRequestInProgress;

    public interface BannerLoadListener {
        void onLoadFinished(boolean success, int code);
    }

    public DfpBannerAdLoader(PublisherAdView adView, String adUnitId, int refreshIntervalSeconds, @Nullable Location location, @Nullable BannerLoadListener externalListener) {
        Log.v(TAG, "Creating DfpBannerAdLoader for adView: " + adView);
        mAdView = adView;
        mExternalAdListener = externalListener;
        mLocation = location;
        mAdUnitId = adUnitId;

        // Set the ad unit id only if it has never been set before for this ad view
        if (mAdView.getAdUnitId() == null) {
            mAdView.setAdUnitId(mAdUnitId);
        }
        mRefreshIntervalMillis = 1000 * refreshIntervalSeconds;

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int code) {
                super.onAdFailedToLoad(code);
                mAdRequestInProgress = false;
                long durationMillis = SystemClock.uptimeMillis() - mAdRequestTimeMillis;
                Log.e(TAG, "ad load failed code:" + code + " duration: " + durationMillis);
                Ana.getInstance().onAdRequestCompleted(mAdUnitId, false, code, mAdRequest);
                if (mExternalAdListener != null) {
                    mExternalAdListener.onLoadFinished(false, code);
                }
                scheduleAdRefresh(durationMillis);
                trackAdAttemptResult(false, durationMillis, code);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdRequestInProgress = false;
                long durationMillis = SystemClock.uptimeMillis() - mAdRequestTimeMillis;
                Log.v(TAG, "ad loaded - duration: " + durationMillis);
                Ana.getInstance().onAdRequestCompleted(mAdUnitId, true, 0, mAdRequest);
                if (mExternalAdListener != null) {
                    mExternalAdListener.onLoadFinished(true, 0);
                }
                scheduleAdRefresh(durationMillis);
                trackAdAttemptResult(true, durationMillis, 0);
            }
        });

        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Adds extra targeting to include in DFP ad request
     *
     * @param key
     * @param value
     */
    public void addCustomDFPTag(String key, String value) {
        mExtraTargeting.put(key, value);
    }

    /**
     * Calls loadAd on the PublisherAdView and starts a refresh timer based on boolean paramter
     *
     * @param startRefreshTimer
     */
    public void loadAd(boolean startRefreshTimer) {
        // For thread safety, this must only be called on main thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            loadAd();
            mAdRefreshTimerEnabled = startRefreshTimer;
        } else {
            throw new IllegalStateException("Must be invoked from the main thread.");
        }
    }

    /**
     * Stops the ad refresh timer
     */
    public void stopRefreshTimer() {
        mAdRefreshTimerEnabled = false;
        mHandler.removeCallbacks(mRefreshRunnable);
    }

    /**
     * Calls resume on the PublisherAdView and starts the refresh timer based on the boolean parameter
     *
     * @param startRefreshTimer
     */
    public void resume(boolean startRefreshTimer) {
        Log.v(TAG, "resume");
        if (mAdView != null) {
            mAdView.resume();
        }
        mAdRefreshTimerEnabled = startRefreshTimer;
        if (mAdRefreshTimerEnabled) {
            scheduleAdRefresh(mRefreshIntervalMillis);
        }
    }

    /**
     * Calls pause on the PublisherAdView and stops the refresh timer
     */
    public void pause() {
        Log.v(TAG, "pause");
        stopRefreshTimer();
        if (mAdView != null) {
            mAdView.pause();
        }
    }

    /**
     * Calls destroy on the PublisherAdView and stops the refresh timer
     */
    public void destroy() {
        Log.v(TAG, "destroy");
        stopRefreshTimer();
        if (mAdView != null) {
            mAdView.destroy();
            mAdView = null;
        }
        Ana.getInstance().onAdViewDestroyed(mAdUnitId);
        mExternalAdListener = null;
    }

    /**
     * Returns true if the PublisherAdView is loading
     *
     * @return
     */
    public boolean isLoading() {
        boolean loading = false;
        if (mAdView != null) {
            loading = mAdView.isLoading();
        }
        return loading;
    }

    private void loadAd() {
        if (!mAdRequestInProgress) {
            Log.v(TAG, "loading ad for " + mAdUnitId);
            if (mAdView != null) {
                //Crashlytics.log("loadAd - " + mAdUnitId);
                mAdRequestInProgress = true;
                PublisherAdRequest.Builder adRequestBuilder = new PublisherAdRequest.Builder();
                if (mLocation != null) {
                    adRequestBuilder.setLocation(mLocation);
                }
                mAdRequestTimeMillis = SystemClock.uptimeMillis();
                adRequestBuilder.addCustomTargeting("app_refresh", String.valueOf(mRefreshCount));
                mRefreshCount++;

                for (String key : mExtraTargeting.keySet()) {
                    adRequestBuilder.addCustomTargeting(key, mExtraTargeting.get(key));
                }
                mAdRequest = adRequestBuilder.build();

                addAnaBidsAndLoadAd();
            }
        } else {
            Log.e(TAG, "Skipping ad load - already in progress");
        }
    }

    /**
     * Calls ANA to add extra bids to the ad request.  Once complete, AppMonet bids will be
     * added as well, then the actual ad request will take place.
     */
    private void addAnaBidsAndLoadAd() {
        if (mAdView != null) {
            // Async fetch ANA bids. After an updated AdRequestBuilder is received, load ad
            Log.v(TAG, "Calling ANA");

            double lat = 0;
            double lon = 0;
            if (mLocation != null) {
                lat = mLocation.getLatitude();
                lon = mLocation.getLongitude();
            }
            Ana.getInstance().addBidsToRequest(mAdView.getContext(), mAdUnitId, mAdRequest,
                    ANA_TIMEOUT_MILLIS, lat, lon, new ValueCallback<PublisherAdRequest>() {
                        @Override
                        public void onReceiveValue(PublisherAdRequest value) {
                            Log.v(TAG, "ANA returned");
                            // ANA bids have been added, request the ad now
                            mAdRequest = value;
                            sendAdRequest();
                        }
                    });
        } else {
            // Skip ANA and load ad
            sendAdRequest();
        }
    }

    private void sendAdRequest() {
        if (mAdView != null) {
            mAdView.loadAd(mAdRequest);
        }

        JSONObject extraJson = getCustomTargetingExtraJson(mAdRequest);
        EventTracker.getInstance().trackEventWeaverOnly(Tracking.Event.AD_ATTEMPT_MADE,
                new Pair<>(Tracking.Property.COHORT, mAdUnitId),
                new Pair<>(Tracking.Property.OBJECT_TYPE, "ANA"),
                new Pair<>(Tracking.Property.EXTRA_JSON, extraJson != null ? extraJson.toString() : null));
    }

    private void scheduleAdRefresh(long lastRequestDurationMillis) {
        if (mAdRefreshTimerEnabled && mRefreshIntervalMillis > 0) {
            // Calculate how much of a delay is required until next ad refresh based on the last
            // time a refresh was triggered, the current time, and the ad unit's refresh interval.
            // If the last ad request took longer than the refresh interval, the next ad refresh
            // will be scheduled immediately
            long refreshDelayMillis = mRefreshIntervalMillis - lastRequestDurationMillis;
            Log.v(TAG, "scheduleAdRefresh with delay: " + refreshDelayMillis);
            if (refreshDelayMillis < 0) {
                refreshDelayMillis = 0;
            }
            mHandler.removeCallbacks(mRefreshRunnable);
            mHandler.postDelayed(mRefreshRunnable, refreshDelayMillis);
        }
    }

    private void trackAdAttemptResult(boolean success, long durationMillis, int errorCode) {
        ArrayList<Pair> nvPairs = new ArrayList<>();
        if (!success) {
            nvPairs.add(new Pair<>(Tracking.Property.EXTRA, String.valueOf(errorCode)));
        }
        nvPairs.add(new Pair<>(Tracking.Property.COHORT, mAdUnitId));
        nvPairs.add(new Pair<>(Tracking.Property.DURATION, String.valueOf(durationMillis)));
        nvPairs.add(new Pair<>(Tracking.Property.OBJECT_TYPE, "ANA"));
        JSONObject extraJson = getCustomTargetingExtraJson(mAdRequest);
        if (extraJson != null) {
            nvPairs.add(new Pair<>(Tracking.Property.EXTRA_JSON, extraJson.toString()));
        }

        String event = success ? Tracking.Event.AD_ATTEMPT_SUCCEEDED : Tracking.Event.AD_ATTEMPT_FAILED;
        EventTracker.getInstance().trackEventWeaverOnly(event, nvPairs.toArray(new Pair[nvPairs.size()]));
    }

    static JSONObject getCustomTargetingExtraJson(PublisherAdRequest adRequest) {
        JSONObject extraJson = new JSONObject();
        addCustomEventExtrasToJson(adRequest, extraJson, AnaCustomEventBanner.class);
        Bundle customTargetingBundle = adRequest.getCustomTargeting();
        if (customTargetingBundle != null) {
            if (extraJson == null) {
                extraJson = new JSONObject();
            }
            JSONObject customTargeting = new JSONObject();
            try {
                for (String key : customTargetingBundle.keySet()) {
                    customTargeting.put(key, customTargetingBundle.get(key));
                }
                extraJson.put("custom_targeting", customTargeting.toString());
            } catch (JSONException ex) {
            }
        }
        return extraJson;
    }

    private static void addCustomEventExtrasToJson(PublisherAdRequest adRequest, JSONObject extraJson, Class customEventClass) {
        JSONObject customEventExtrasJson = new JSONObject();
        Bundle customEventExtrasBundle = adRequest.getCustomEventExtrasBundle(customEventClass);
        try {
            if (customEventExtrasBundle != null) {
                for (String key : customEventExtrasBundle.keySet()) {
                    customEventExtrasJson.put(key, customEventExtrasBundle.get(key));
                }
            }
            extraJson.put(customEventClass.getCanonicalName(), customEventExtrasJson);
        } catch (JSONException ex) {
            Log.e(TAG, "JSON ex: " + ex);
        }
    }
}

