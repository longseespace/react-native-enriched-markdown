#pragma once
#import "RichTextConfig.h"

@interface HeadingStyleBase : NSObject {
    RichTextConfig *config;
}
@property (nonatomic, weak) RichTextConfig *config;
- (CGFloat)getHeadingFontSize;
- (NSString *)getHeadingFontFamily;
@end

@interface H1Style : HeadingStyleBase
@end

// Future: Add H2-H6 style declarations here
// @interface H2Style : HeadingStyleBase
// @end
// @interface H3Style : HeadingStyleBase
// @end
// @interface H4Style : HeadingStyleBase
// @end
// @interface H5Style : HeadingStyleBase
// @end
// @interface H6Style : HeadingStyleBase
// @end

// Future: LinkStyle, ParagraphStyle, etc.
