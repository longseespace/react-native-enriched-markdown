#import "MathRenderer.h"
#import "FontUtils.h"
#import "MarkdownASTNode.h"
#import "RenderContext.h"
#import "RendererFactory.h"
#import "StyleConfig.h"
#if __has_include("ReactNativeEnrichedMarkdown-Swift.h")
#import "ReactNativeEnrichedMarkdown-Swift.h"
#endif

@implementation MathRenderer {
  RendererFactory *_rendererFactory;
  StyleConfig *_config;
  BOOL _isDisplay;
}

- (instancetype)initWithRendererFactory:(RendererFactory *)rendererFactory
                                 config:(StyleConfig *)config
                              isDisplay:(BOOL)isDisplay
{
  self = [super init];
  if (self) {
    _rendererFactory = rendererFactory;
    _config = config;
    _isDisplay = isDisplay;
  }
  return self;
}

- (void)renderNode:(MarkdownASTNode *)node into:(NSMutableAttributedString *)output context:(RenderContext *)context
{
  NSString *latex = [self extractTextFromNode:node];
  if (latex.length == 0) {
    return;
  }

  BlockStyle *blockStyle = [context getBlockStyle];
  UIFont *blockFont = cachedFontFromBlockStyle(blockStyle, context);
  CGFloat fontSize = blockFont ? blockFont.pointSize : _config.paragraphFontSize;
  UIColor *textColor = blockStyle.color ?: _config.paragraphColor;

  NSAttributedString *rendered = nil;
#if __has_include("ReactNativeEnrichedMarkdown-Swift.h")
  rendered = [ENRMMathBridge attributedMath:latex
                                   fontSize:fontSize
                                  textColor:(textColor ?: [UIColor blackColor])isDisplayMode:_isDisplay];
#endif

  if (!rendered || rendered.length == 0) {
    NSString *fallback =
        _isDisplay ? [NSString stringWithFormat:@"$$%@$$", latex] : [NSString stringWithFormat:@"$%@$", latex];
    [output appendAttributedString:[[NSAttributedString alloc] initWithString:fallback
                                                                   attributes:[context getTextAttributes]]];
    return;
  }

  if (_isDisplay) {
    if (output.length > 0 && ![output.string hasSuffix:@"\n"]) {
      [output appendAttributedString:[[NSAttributedString alloc] initWithString:@"\n"]];
    }
    NSUInteger blockStart = output.length;
    [output appendAttributedString:rendered];
    [output appendAttributedString:[[NSAttributedString alloc] initWithString:@"\n"]];

    NSUInteger blockEnd = output.length;
    if (blockEnd > blockStart) {
      NSMutableParagraphStyle *style = [[NSMutableParagraphStyle alloc] init];
      style.alignment = NSTextAlignmentCenter;
      style.baseWritingDirection = NSWritingDirectionLeftToRight;
      style.lineSpacing = MAX(2.0, fontSize * 0.12);
      style.paragraphSpacingBefore = MAX(4.0, fontSize * 0.22);
      style.paragraphSpacing = MAX(6.0, fontSize * 0.22);
      [output addAttribute:NSParagraphStyleAttributeName
                     value:style
                     range:NSMakeRange(blockStart, blockEnd - blockStart)];
    }
    return;
  }

  [output appendAttributedString:rendered];
}

- (NSString *)extractTextFromNode:(MarkdownASTNode *)node
{
  if (!node) {
    return @"";
  }

  NSMutableString *buffer = [NSMutableString string];
  [self appendTextFromNode:node toBuffer:buffer];
  return [buffer stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
}

- (void)appendTextFromNode:(MarkdownASTNode *)node toBuffer:(NSMutableString *)buffer
{
  if (node.content.length > 0) {
    [buffer appendString:node.content];
  }
  for (MarkdownASTNode *child in node.children) {
    [self appendTextFromNode:child toBuffer:buffer];
  }
}

@end
