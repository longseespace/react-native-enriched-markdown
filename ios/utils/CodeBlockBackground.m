#import "CodeBlockBackground.h"
#import "StyleConfig.h"

NSString *const CodeBlockAttributeName = @"CodeBlock";

@implementation CodeBlockBackground {
  StyleConfig *_config;
}

- (instancetype)initWithConfig:(StyleConfig *)config
{
  if (self = [super init]) {
    _config = config;
  }
  return self;
}

- (void)drawBackgroundsForGlyphRange:(NSRange)glyphsToShow
                       layoutManager:(NSLayoutManager *)layoutManager
                       textContainer:(NSTextContainer *)textContainer
                             atPoint:(CGPoint)origin
{
  NSTextStorage *textStorage = layoutManager.textStorage;
  NSRange charRange = [layoutManager characterRangeForGlyphRange:glyphsToShow actualGlyphRange:NULL];

  [textStorage enumerateAttribute:CodeBlockAttributeName
                          inRange:charRange
                          options:0
                       usingBlock:^(id value, NSRange range, BOOL *stop) {
                         if (!value)
                           return;
                         [self drawCodeBlockBackgroundForRange:range
                                                 layoutManager:layoutManager
                                                 textContainer:textContainer
                                                       atPoint:origin];
                       }];
}

- (void)drawCodeBlockBackgroundForRange:(NSRange)range
                          layoutManager:(NSLayoutManager *)layoutManager
                          textContainer:(NSTextContainer *)textContainer
                                atPoint:(CGPoint)origin
{
  NSRange glyphRange = [layoutManager glyphRangeForCharacterRange:range actualCharacterRange:NULL];

  __block CGRect blockRect = CGRectNull;
  [layoutManager enumerateLineFragmentsForGlyphRange:glyphRange
                                          usingBlock:^(CGRect rect, CGRect usedRect, NSTextContainer *tc,
                                                       NSRange lineRange, BOOL *stop) {
                                            CGRect lineRect = rect;
                                            lineRect.origin.x += origin.x;
                                            lineRect.origin.y += origin.y;
                                            // Union the rects to create a single block
                                            blockRect =
                                                CGRectIsNull(blockRect) ? lineRect : CGRectUnion(blockRect, lineRect);
                                          }];

  if (CGRectIsNull(blockRect))
    return;

  blockRect.origin.x = origin.x;
  blockRect.size.width = textContainer.size.width;

  CGFloat borderWidth = [_config codeBlockBorderWidth];
  CGFloat borderRadius = [_config codeBlockBorderRadius];

  // Inset the drawing by half the border width
  CGFloat inset = borderWidth / 2.0;
  CGRect insetRect = CGRectInset(blockRect, inset, inset);
  UIBezierPath *path = [UIBezierPath bezierPathWithRoundedRect:insetRect cornerRadius:MAX(0, borderRadius - inset)];

  CGContextRef ctx = UIGraphicsGetCurrentContext();
  CGContextSaveGState(ctx);
  {
    [[_config codeBlockBackgroundColor] setFill];
    [path fill];

    if (borderWidth > 0) {
      [[_config codeBlockBorderColor] setStroke];
      path.lineWidth = borderWidth;
      path.lineJoinStyle = kCGLineJoinRound;
      [path stroke];
    }
  }
  CGContextRestoreGState(ctx);
}
@end