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

+ (BOOL)isLastElementCodeBlock:(NSAttributedString *)text
{
  if (text.length == 0)
    return NO;

  // Find the last non-newline character (actual content)
  NSRange lastContent = [text.string rangeOfCharacterFromSet:[[NSCharacterSet newlineCharacterSet] invertedSet]
                                                     options:NSBackwardsSearch];
  if (lastContent.location == NSNotFound)
    return NO;

  // Check if it's inside a code block
  NSNumber *isCodeBlock = [text attribute:CodeBlockAttributeName atIndex:lastContent.location effectiveRange:nil];
  if (!isCodeBlock.boolValue)
    return NO;

  // Verify the code block extends to the end of text
  NSRange codeBlockRange;
  [text attribute:CodeBlockAttributeName atIndex:lastContent.location effectiveRange:&codeBlockRange];
  return NSMaxRange(codeBlockRange) == text.length;
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
  CGRect blockRect = [layoutManager boundingRectForGlyphRange:glyphRange inTextContainer:textContainer];

  if (CGRectIsEmpty(blockRect))
    return;

  blockRect.origin.x = origin.x;
  blockRect.origin.y += origin.y;
  blockRect.size.width = textContainer.size.width;

  // Compensate for iOS not measuring trailing newlines (bottom padding)
  // Only for the LAST code block (the one that ends at text.length)
  BOOL isLastCodeBlock = (NSMaxRange(range) == layoutManager.textStorage.length);
  if (isLastCodeBlock) {
    blockRect.size.height += [_config codeBlockPadding];
  }

  CGFloat borderWidth = [_config codeBlockBorderWidth];
  CGFloat borderRadius = [_config codeBlockBorderRadius];
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