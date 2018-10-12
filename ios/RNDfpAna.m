
#import "RNDfpAna.h"
#import "RNDfpAna-Swift.h"

@import whisper;
//@import Datametrical;

@interface RNDfpAna()

//@property (nonatomic, strong) AnaAdapter * anaAdapter;

@end

@implementation RNDfpAna

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(init:(NSString *)uuid :(NSString *)baseURL :(NSInteger)age :(NSString *)gender)
{
    [[NSUserDefaults standardUserDefaults] setObject:uuid forKey:@"sh.whisper.UserID"];
    AnaAdapter.shared.baseURL = baseURL;
    AnaAdapter.shared.age = @(age);
    AnaAdapter.shared.gender = gender;
}



@end
