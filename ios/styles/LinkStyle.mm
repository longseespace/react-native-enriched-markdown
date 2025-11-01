#import "LinkStyle.h"

@implementation LinkStyle {
    RichTextConfig *_config;
}

- (instancetype)initWithConfig:(RichTextConfig *)config {
    self = [super init];
    if (self) {
        _config = config;
    }
    return self;
}

- (UIColor *)color {
    return [_config linkColor];
}

- (BOOL)underline {
    return [_config linkUnderline];
}

@end

