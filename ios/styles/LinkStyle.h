#pragma once
#import "RichTextConfig.h"

@interface LinkStyle : NSObject

- (instancetype)initWithConfig:(RichTextConfig *)config;

- (UIColor *)color;
- (BOOL)underline;

@end

