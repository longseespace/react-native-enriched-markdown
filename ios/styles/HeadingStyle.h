#pragma once
#import "RichTextConfig.h"

@interface HeadingStyle : NSObject

- (instancetype)initWithLevel:(NSInteger)level config:(RichTextConfig *)config;

- (CGFloat)fontSize;
- (NSString *)fontFamily;

@end

