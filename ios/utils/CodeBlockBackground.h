#pragma once
#import "StyleConfig.h"
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

extern NSString *const CodeBlockAttributeName;

@interface CodeBlockBackground : NSObject

- (instancetype)initWithConfig:(StyleConfig *)config;
- (void)drawBackgroundsForGlyphRange:(NSRange)glyphsToShow
                       layoutManager:(NSLayoutManager *)layoutManager
                       textContainer:(NSTextContainer *)textContainer
                             atPoint:(CGPoint)origin;

/**
 * Checks if the last element in the attributed string is a code block.
 * Used to compensate for iOS text APIs not measuring/drawing trailing newlines with custom line heights.
 */
+ (BOOL)isLastElementCodeBlock:(NSAttributedString *)text;

@end

NS_ASSUME_NONNULL_END
