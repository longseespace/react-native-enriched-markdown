#import "StyleHeaders.h"

@implementation H1Style

- (CGFloat)getHeadingFontSize { 
    return [self.config h1FontSize]; 
}

- (NSString *)getHeadingFontFamily {
    return [self.config h1FontFamily];
}

@end
