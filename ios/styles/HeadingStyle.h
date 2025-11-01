#pragma once
#import "RichTextConfig.h"

/**
 * Encapsulates heading style logic for all heading levels (H1-H6).
 * Provides a clean interface for retrieving heading-specific font properties.
 */
@interface HeadingStyle : NSObject

- (instancetype)initWithLevel:(NSInteger)level config:(RichTextConfig *)config;

- (CGFloat)fontSize;
- (NSString *)fontFamily;

@end

