
#import "RNDfpAnaBannerViewManager.h"
#import "RNDfpAnaBannerView.h"
#import <React/RCTUIManager.h>
#import <React/RCTEventDispatcher.h>

@import GoogleMobileAds;
@import whisper;

// @interface RNDfpAnaBannerViewManager()
// @property (nonatomic, strong) DFPBannerView * bannerView;
// @end

@implementation RNDfpAnaBannerViewManager

RCT_EXPORT_MODULE()

- (UIView *)view {
    return [RNDfpAnaBannerView new];
}

RCT_EXPORT_METHOD(loadBanner:(nonnull NSNumber *)reactTag)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, RNDfpAnaBannerView *> *viewRegistry) {
        RNDfpAnaBannerView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[RNDfpAnaBannerView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting RNDfpAnaBannerView, got: %@", view);
        } else {
            [view loadBanner];
        }
    }];
}

RCT_REMAP_VIEW_PROPERTY(adSize, _bannerView.adSize, GADAdSize)
RCT_REMAP_VIEW_PROPERTY(adUnitID, _bannerView.adUnitID, NSString)
RCT_REMAP_VIEW_PROPERTY(appID, _bannerView.appID, NSString)
RCT_EXPORT_VIEW_PROPERTY(validAdSizes, NSArray)
RCT_EXPORT_VIEW_PROPERTY(testDevices, NSArray)

RCT_EXPORT_VIEW_PROPERTY(onSizeChanged, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAppEvent, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdLoaded, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdFailedToLoad, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdOpened, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdClosed, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdLeftApplication, RCTBubblingEventBlock)

@end
