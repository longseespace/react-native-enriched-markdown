#import "BlockquoteBorder.h"
#import "RichTextConfig.h"

NSString *const RichTextBlockquoteDepthAttributeName = @"RichTextBlockquoteDepth";
NSString *const RichTextBlockquoteBackgroundColorAttributeName = @"RichTextBlockquoteBackgroundColor";

static NSString *const kFragmentRectKey = @"rect";
static NSString *const kFragmentDepthKey = @"depth";
static NSString *const kFragmentDepthLocationKey = @"depthLocation";

@implementation BlockquoteBorder {
  RichTextConfig *_config;
}

- (instancetype)initWithConfig:(RichTextConfig *)config
{
  if (self = [super init]) {
    _config = config;
  }
  return self;
}

- (void)drawBordersForGlyphRange:(NSRange)glyphsToShow
                   layoutManager:(NSLayoutManager *)layoutManager
                   textContainer:(NSTextContainer *)textContainer
                         atPoint:(CGPoint)origin
{
  NSTextStorage *textStorage = layoutManager.textStorage;
  if (!textStorage || textStorage.length == 0)
    return;

  UIColor *borderColor = [_config blockquoteBorderColor];
  CGFloat borderWidth = [_config blockquoteBorderWidth];
  CGFloat levelSpacing = borderWidth + [_config blockquoteGapWidth];
  CGFloat containerWidth = textContainer.size.width;

  NSMutableArray<NSDictionary *> *fragments = [NSMutableArray array];

  [layoutManager enumerateLineFragmentsForGlyphRange:glyphsToShow
                                          usingBlock:^(CGRect rect, CGRect usedRect, NSTextContainer *container,
                                                       NSRange glyphRange, BOOL *stop) {
                                            NSRange charRange = [layoutManager characterRangeForGlyphRange:glyphRange
                                                                                          actualGlyphRange:NULL];
                                            if (charRange.location == NSNotFound || charRange.length == 0) {
                                              return;
                                            }

                                            NSNumber *depth =
                                                [textStorage attribute:RichTextBlockquoteDepthAttributeName
                                                               atIndex:charRange.location
                                                        effectiveRange:NULL];
                                            if (!depth) {
                                              return;
                                            }

                                            [fragments addObject:@{
                                              kFragmentRectKey : [NSValue valueWithCGRect:rect],
                                              kFragmentDepthKey : depth,
                                              kFragmentDepthLocationKey : @(charRange.location)
                                            }];
                                          }];

  for (NSDictionary *fragment in fragments) {
    [self drawFragment:fragment
           textStorage:textStorage
                origin:origin
          levelSpacing:levelSpacing
           borderColor:borderColor
           borderWidth:borderWidth
        containerWidth:containerWidth];
  }
}

#pragma mark - Drawing

- (void)drawFragment:(NSDictionary *)fragment
         textStorage:(NSTextStorage *)textStorage
              origin:(CGPoint)origin
        levelSpacing:(CGFloat)levelSpacing
         borderColor:(UIColor *)borderColor
         borderWidth:(CGFloat)borderWidth
      containerWidth:(CGFloat)containerWidth
{
  CGRect rect = [fragment[kFragmentRectKey] CGRectValue];
  NSInteger depth = [fragment[kFragmentDepthKey] integerValue];
  NSUInteger charLocation = [fragment[kFragmentDepthLocationKey] unsignedIntegerValue];
  CGFloat baseY = origin.y + rect.origin.y;

  // Draw background if configured
  UIColor *backgroundColor = [textStorage attribute:RichTextBlockquoteBackgroundColorAttributeName
                                            atIndex:charLocation
                                     effectiveRange:NULL]
                                 ?: [_config blockquoteBackgroundColor];
  if (backgroundColor && backgroundColor != [UIColor clearColor]) {
    CGRect bgRect = CGRectMake(origin.x, baseY, containerWidth, rect.size.height);
    [backgroundColor setFill];
    UIRectFill(bgRect);
  }

  // Draw borders for each nesting level (0 to depth)
  // Each level adds another vertical line at increasing indentation
  for (NSInteger level = 0; level <= depth; level++) {
    CGFloat borderX = origin.x + (levelSpacing * level);
    CGRect borderRect = CGRectMake(borderX, baseY, borderWidth, rect.size.height);
    [borderColor setFill];
    UIRectFill(borderRect);
  }
}

@end
