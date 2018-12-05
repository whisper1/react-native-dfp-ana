//
//  RNDfpAnaBannerView.m
//  RNDfpAna
//
//  Created by David Jackman on 10/5/18.
//  Copyright © 2018 Facebook. All rights reserved.
//

#import "RNDfpAnaBannerView.h"
#import "RNDfpAna-Swift.h"
@import Foundation;
@import whisper;
@import GoogleMobileAds;
@import AdSupport;
#import <React/RCTLog.h>
#import <React/RCTConvert.h>
#import "RCTConvert+GADAdSize.h"



@interface RNDfpAnaBannerView() <GADAppEventDelegate>

    @property (nonatomic, strong) DFPBannerView * bannerView;
    @property (nonatomic, assign) BOOL paused;
    @property (nonatomic, strong) NSTimer * refreshTimer;
    
@end

@implementation RNDfpAnaBannerView
{
    DFPBannerView  *_bannerView;
}

- (void)dealloc
{
    _bannerView.delegate = nil;
    _bannerView.adSizeDelegate = nil;
    _bannerView.appEventDelegate = nil;
}

- (instancetype)initWithFrame:(CGRect)frame
{
    if ((self = [super initWithFrame:frame])) {
        super.backgroundColor = [UIColor clearColor];
        
        UIWindow *keyWindow = [[UIApplication sharedApplication] keyWindow];
        UIViewController *rootViewController = [keyWindow rootViewController];
        
        _bannerView = [[DFPBannerView alloc] initWithAdSize:kGADAdSizeBanner];
        _bannerView.delegate = self;
        _bannerView.adSizeDelegate = self;
        _bannerView.appEventDelegate = self;
        _bannerView.rootViewController = rootViewController;
        [self addSubview:_bannerView];
    }
    
    return self;
}

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wobjc-missing-super-calls"
- (void)insertReactSubview:(UIView *)subview atIndex:(NSInteger)atIndex
{
    RCTLogError(@"RNDFPBannerView cannot have subviews");
}
#pragma clang diagnostic pop

- (void)loadBanner
{
    [self.refreshTimer invalidate];
    self.refreshTimer = [NSTimer scheduledTimerWithTimeInterval:10 repeats:NO block:^(NSTimer * _Nonnull timer) {
        [self loadBanner];
    }];
    
    if (self.onSizeChanged) {
        CGSize size = CGSizeFromGADAdSize(_bannerView.adSize);
        if (!CGSizeEqualToSize(size, self.bounds.size)) {
            self.onSizeChanged(@{
                                 @"width": @(size.width),
                                 @"height": @(size.height)
                                 });
        }
    }
    
    DFPRequest *request = [DFPRequest request];
//    self.bannerAbout 1 hourView.adUnitID = @"/114106652/singleton_banner_ios";
    request.testDevices = _testDevices;
    [AnaAdapter augmentWithRequest:request adUnit:self.bannerView.adUnitID completion:^(DFPRequest * alteredRequest, NSError * e) {
        [NSOperationQueue.mainQueue addOperationWithBlock:^{
            [_bannerView loadRequest:alteredRequest];
        }];
    }];
}
    
- (void)resumeBanner {
    self.paused = NO;
    [self loadBanner];
}
- (void)pauseBanner {
    self.paused = YES;
    [self.refreshTimer invalidate];
    self.refreshTimer = nil;
}
- (void)destroyBanner {
    [self pauseBanner];
}

- (void)setAppID:(NSString *)appID
{
    AnaDelegateConfiguration.shared.appID = appID;
}
    
//- (void)setValidAdSizes:(NSArray *)adSizes
//{
//    __block NSMutableArray *validAdSizes = [[NSMutableArray alloc] initWithCapacity:adSizes.count];
//    [adSizes enumerateObjectsUsingBlock:^(id jsonValue, NSUInteger idx, __unused BOOL *stop) {
//        GADAdSize adSize = [RCTConvert GADAdSize:jsonValue];
//        if (GADAdSizeEqualToSize(adSize, kGADAdSizeInvalid)) {
//            RCTLogWarn(@"Invalid adSize %@", jsonValue);
//        } else {
//            [validAdSizes addObject:NSValueFromGADAdSize(adSize)];
//        }
//    }];
//    _bannerView.validAdSizes = validAdSizes;
//}

- (void)setValidAdSizes:(NSArray *)adSizes
{
    NSMutableArray *validAdSizes = [[NSMutableArray alloc] initWithCapacity:1];
    [validAdSizes addObject:NSValueFromGADAdSize(kGADAdSizeBanner)];
    _bannerView.validAdSizes = validAdSizes;
}

- (void)setTestDevices:(NSArray *)testDevices
{
//    _testDevices = RNAdMobProcessTestDevices(testDevices, kDFPSimulatorID);
}

-(void)layoutSubviews
{
    [super layoutSubviews];
    _bannerView.frame = self.bounds;
}

# pragma mark GADBannerViewDelegate

/// Tells the delegate an ad request loaded an ad.
- (void)adViewDidReceiveAd:(DFPBannerView *)adView
{
    if (self.onSizeChanged) {
        self.onSizeChanged(@{
                            @"width": @(adView.frame.size.width),
                            @"height": @(adView.frame.size.height) });
    }
    if (self.onAdLoaded) {
        self.onAdLoaded(@{});
    }
}

/// Tells the delegate an ad request failed.
- (void)adView:(DFPBannerView *)adView
didFailToReceiveAdWithError:(GADRequestError *)error
{
    if (self.onAdFailedToLoad) {
        self.onAdFailedToLoad(@{ @"error": @{ @"message": [error localizedDescription] } });
    }
}

/// Tells the delegate that a full screen view will be presented in response
/// to the user clicking on an ad.
- (void)adViewWillPresentScreen:(DFPBannerView *)adView
{
    if (self.onAdOpened) {
        self.onAdOpened(@{});
    }
}

/// Tells the delegate that the full screen view will be dismissed.
- (void)adViewWillDismissScreen:(__unused DFPBannerView *)adView
{
    if (self.onAdClosed) {
        self.onAdClosed(@{});
    }
}

/// Tells the delegate that a user click will open another app (such as
/// the App Store), backgrounding the current app.
- (void)adViewWillLeaveApplication:(DFPBannerView *)adView
{
    if (self.onAdLeftApplication) {
        self.onAdLeftApplication(@{});
    }
}

# pragma mark GADAdSizeDelegate

- (void)adView:(GADBannerView *)bannerView willChangeAdSizeTo:(GADAdSize)size
{
    CGSize adSize = CGSizeFromGADAdSize(size);
    self.onSizeChanged(@{
                        @"width": @(adSize.width),
                        @"height": @(adSize.height) });
}

# pragma mark GADAppEventDelegate

- (void)adView:(GADBannerView *)banner didReceiveAppEvent:(NSString *)name withInfo:(NSString *)info
{
    if (self.onAppEvent) {
        self.onAppEvent(@{ @"name": name, @"info": info });
    }
}

@end
