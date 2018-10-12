#if __has_include(<React/RCTView.h>)
#import <React/RCTView.h>
#else
#import "RCTView.h"
#endif

@import GoogleMobileAds;

@class RCTEventDispatcher;

@interface RNDfpAnaBannerView : RCTView <GADBannerViewDelegate, GADAdSizeDelegate>

@property (nonatomic, copy) NSArray *testDevices;

@property (nonatomic, copy) RCTBubblingEventBlock onSizeChanged;
@property (nonatomic, copy) RCTBubblingEventBlock onAdLoaded;
@property (nonatomic, copy) RCTBubblingEventBlock onAdFailedToLoad;
@property (nonatomic, copy) RCTBubblingEventBlock onAdOpened;
@property (nonatomic, copy) RCTBubblingEventBlock onAdClosed;
@property (nonatomic, copy) RCTBubblingEventBlock onAdLeftApplication;
@property (nonatomic, copy) RCTBubblingEventBlock onAppEvent;

- (void)loadBanner;
- (void)resumeBanner;
- (void)pauseBanner;
- (void)destroyBanner;
    
@end
