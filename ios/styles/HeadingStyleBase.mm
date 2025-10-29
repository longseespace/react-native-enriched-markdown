#import "StyleHeaders.h"

@implementation HeadingStyleBase

- (CGFloat)getHeadingFontSize {
    // This is a base implementation - subclasses should override
    return 16.0;
}

- (NSString *)getHeadingFontFamily {
    // Base implementation returns nil; subclasses may override
    return nil;
}

@end
