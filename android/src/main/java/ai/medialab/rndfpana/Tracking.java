package ai.medialab.rndfpana;

class Tracking {
    static class Event {
        static final String AD_ATTEMPT_MADE = "Ad Attempt Made";
        static final String AD_ATTEMPT_SUCCEEDED = "Ad Attempt Succeeded";
        static final String AD_ATTEMPT_FAILED = "Ad Attempt Failed";
        static final String AD_CLICKED = "Ad Clicked";
        static final String NEW_ACTIVITY_STARTED = "New Activity Started";
        static final String AD_REDIRECT_BLOCKED = "Ad Redirect Blocked";
        static final String AD_PLAY_STORE_REDIRECT_BLOCKED = "Ad App Store Redirect Blocked";
        static final String AD_MRAID_BLOCKED = "Ad MRAID Blocked";
        static final String AD_MRAID_DISPLAYED = "Ad MRAID Displayed";
    }

    static class Property {
        static final String DURATION = "duration";
        static final String EXTRA = "extra";
        static final String EXTRA_JSON = "extra_json";
        static final String OBJECT_TYPE = "object_type";
        static final String COHORT = "cohort";
    }
}
