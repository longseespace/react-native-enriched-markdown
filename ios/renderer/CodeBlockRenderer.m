#import "CodeBlockRenderer.h"
#import "CodeBlockBackground.h"
#import "LastElementUtils.h"
#import "MarkdownASTNode.h"
#import "ParagraphStyleUtils.h"
#import "RenderContext.h"
#import "RendererFactory.h"
#import "StyleConfig.h"
#if __has_include("ReactNativeEnrichedMarkdown-Swift.h")
#import "ReactNativeEnrichedMarkdown-Swift.h"
#endif

static BOOL ENRMColorIsDark(UIColor *color)
{
  if (!color) {
    return NO;
  }

  UIColor *resolved = color;
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 130000
  if ([resolved respondsToSelector:@selector(resolvedColorWithTraitCollection:)]) {
    resolved = [resolved resolvedColorWithTraitCollection:[UITraitCollection currentTraitCollection]];
  }
#endif

  CGFloat red = 0;
  CGFloat green = 0;
  CGFloat blue = 0;
  CGFloat alpha = 0;
  if (![resolved getRed:&red green:&green blue:&blue alpha:&alpha]) {
    return NO;
  }

  CGFloat luminance = (0.299 * red) + (0.587 * green) + (0.114 * blue);
  return luminance < 0.5;
}

@implementation CodeBlockRenderer {
  RendererFactory *_rendererFactory;
  StyleConfig *_config;
}

- (instancetype)initWithRendererFactory:(id)rendererFactory config:(id)config
{
  if (self = [super init]) {
    _rendererFactory = rendererFactory;
    _config = (StyleConfig *)config;
  }
  return self;
}

- (void)applyFallbackFont:(UIFont *)font
                    color:(UIColor *)color
                  toRange:(NSRange)range
                 inOutput:(NSMutableAttributedString *)output
{
  if (range.length == 0) {
    return;
  }

  UIColor *resolvedColor = color ?: [UIColor blackColor];
  NSMutableArray<NSValue *> *fontRanges = [NSMutableArray new];
  NSMutableArray<NSValue *> *colorRanges = [NSMutableArray new];
  [output enumerateAttributesInRange:range
                             options:0
                          usingBlock:^(NSDictionary<NSAttributedStringKey, id> *attrs, NSRange subrange, BOOL *stop) {
                            if (!attrs[NSFontAttributeName]) {
                              [fontRanges addObject:[NSValue valueWithRange:subrange]];
                            }
                            if (!attrs[NSForegroundColorAttributeName]) {
                              [colorRanges addObject:[NSValue valueWithRange:subrange]];
                            }
                          }];

  for (NSValue *value in fontRanges) {
    [output addAttribute:NSFontAttributeName value:font range:value.rangeValue];
  }
  for (NSValue *value in colorRanges) {
    [output addAttribute:NSForegroundColorAttributeName value:resolvedColor range:value.rangeValue];
  }
}

- (void)renderNode:(MarkdownASTNode *)node into:(NSMutableAttributedString *)output context:(RenderContext *)context
{
  [context setBlockStyle:BlockTypeCodeBlock font:[_config codeBlockFont] color:[_config codeBlockColor] headingLevel:0];

  CGFloat padding = [_config codeBlockPadding];
  CGFloat lineHeight = [_config codeBlockLineHeight];
  CGFloat marginTop = [_config codeBlockMarginTop];
  CGFloat marginBottom = [_config codeBlockMarginBottom];

  NSUInteger blockStart = output.length;
  blockStart += applyBlockSpacingBefore(output, blockStart, marginTop);

  // Top Padding: Inserted as a spacer character inside the background area
  [output appendAttributedString:kNewlineAttributedString];
  NSMutableParagraphStyle *topSpacerStyle = [context spacerStyleWithHeight:padding spacing:0];
  topSpacerStyle.baseWritingDirection = NSWritingDirectionLeftToRight;
  [output addAttribute:NSParagraphStyleAttributeName value:topSpacerStyle range:NSMakeRange(blockStart, 1)];

  NSUInteger contentStart = output.length;
  @try {
    [_rendererFactory renderChildrenOfNode:node into:output context:context];
  } @finally {
    [context clearBlockStyle];
  }

  NSUInteger contentEnd = output.length;
  if (contentEnd <= contentStart)
    return;

  NSRange contentRange = NSMakeRange(contentStart, contentEnd - contentStart);

  UIFont *codeFont = [_config codeBlockFont];
  UIColor *codeColor = [_config codeBlockColor];
#if __has_include("ReactNativeEnrichedMarkdown-Swift.h")
  NSString *codeContent = [output attributedSubstringFromRange:contentRange].string ?: @"";
  NSString *language = node.attributes[@"language"];
  UIColor *codeBackgroundColor = [_config codeBlockBackgroundColor];
  BOOL usesDarkBackground = ENRMColorIsDark(codeBackgroundColor);

  NSAttributedString *highlighted = [ENRMSyntaxHighlighterBridge
      highlightedCode:codeContent
             language:language
                 font:codeFont
        fallbackColor:(codeColor ?: [UIColor blackColor])usesDarkBackground:usesDarkBackground];
  if (highlighted.length > 0) {
    [output replaceCharactersInRange:contentRange withAttributedString:highlighted];
    contentEnd = contentStart + highlighted.length;
    contentRange = NSMakeRange(contentStart, contentEnd - contentStart);
  }
#endif

  [self applyFallbackFont:codeFont color:codeColor toRange:contentRange inOutput:output];

  if (lineHeight > 0) {
    applyLineHeight(output, contentRange, lineHeight);
  }

  // Code is always LTR regardless of app writing direction
  NSMutableParagraphStyle *baseStyle = [getOrCreateParagraphStyle(output, contentStart) mutableCopy];
  baseStyle.baseWritingDirection = NSWritingDirectionLeftToRight;
  baseStyle.alignment = NSTextAlignmentLeft;
  baseStyle.firstLineHeadIndent = padding;
  baseStyle.headIndent = padding;
  baseStyle.tailIndent = -padding;
  [output addAttribute:NSParagraphStyleAttributeName value:baseStyle range:contentRange];

  // Bottom Padding: Inserted as a spacer character inside the background area
  NSUInteger bottomPaddingStart = output.length;
  [output appendAttributedString:kNewlineAttributedString];
  NSMutableParagraphStyle *bottomPaddingStyle = [context spacerStyleWithHeight:padding spacing:0];
  bottomPaddingStyle.baseWritingDirection = NSWritingDirectionLeftToRight;
  [output addAttribute:NSParagraphStyleAttributeName value:bottomPaddingStyle range:NSMakeRange(bottomPaddingStart, 1)];

  // Define the range for background rendering (includes padding, excludes margins)
  NSRange backgroundRange = NSMakeRange(blockStart, output.length - blockStart);
  [output addAttribute:CodeBlockAttributeName value:@YES range:backgroundRange];

  // External Margin: Applied outside the background range
  if (marginBottom > 0) {
    applyBlockSpacingAfter(output, marginBottom);
  }
}

@end
