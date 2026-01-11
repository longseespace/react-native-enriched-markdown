#import "TextRenderer.h"
#import "FontUtils.h"
#import "RenderContext.h"

@implementation TextRenderer

- (void)renderNode:(MarkdownASTNode *)node into:(NSMutableAttributedString *)output context:(RenderContext *)context
{
  if (!node.content)
    return;

  BlockStyle *blockStyle = [context getBlockStyle];
  UIFont *textFont = cachedFontFromBlockStyle(blockStyle, context);
  UIColor *textColor = blockStyle.color;

  NSAttributedString *text = [[NSAttributedString alloc]
      initWithString:node.content
          attributes:@{NSFontAttributeName : textFont, NSForegroundColorAttributeName : textColor}];
  [output appendAttributedString:text];
}

@end
