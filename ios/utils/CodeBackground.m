#import "CodeBackground.h"

NSString *const RichTextCodeAttributeName = @"RichTextCode";

static const CGFloat kCodeBackgroundCornerRadius = 2.0;
static const CGFloat kCodeBackgroundBorderWidth = 1.0;
// Through this variable we could set height for the inline code.
// Potentially this should be removed in the future - when we establish approach for the consistent height
static const CGFloat kCodeBackgroundHeightReductionFactor = 0.0;

static inline CGFloat HalfStroke(void) {
    return kCodeBackgroundBorderWidth / 2.0;
}

@implementation CodeBackground {
    RichTextConfig *_config;
}

- (instancetype)initWithConfig:(RichTextConfig *)config {
    self = [super init];
    if (self) {
        _config = config;
    }
    return self;
}

- (void)drawBackgroundsForGlyphRange:(NSRange)glyphsToShow
                        layoutManager:(NSLayoutManager *)layoutManager
                        textContainer:(NSTextContainer *)textContainer
                               atPoint:(CGPoint)origin {
    UIColor *backgroundColor = _config.codeBackgroundColor;
    if (!backgroundColor) return;
    
    NSTextStorage *textStorage = layoutManager.textStorage;
    if (!textStorage || textStorage.length == 0) return;
    
    NSRange charRange = [layoutManager characterRangeForGlyphRange:glyphsToShow actualGlyphRange:NULL];
    if (charRange.location == NSNotFound || charRange.length == 0) return;
    
    UIColor *borderColor = _config.codeBorderColor;
    
    [textStorage enumerateAttribute:RichTextCodeAttributeName
                             inRange:NSMakeRange(0, textStorage.length)
                             options:0
                          usingBlock:^(id value, NSRange range, BOOL *stop) {
        if (value != nil && range.length > 0 && NSIntersectionRange(range, charRange).length > 0) {
            [self drawCodeBackgroundForRange:range
                                   layoutManager:layoutManager
                                   textContainer:textContainer
                                          atPoint:origin
                                 backgroundColor:backgroundColor
                                      borderColor:borderColor];
        }
    }];
}

- (void)drawCodeBackgroundForRange:(NSRange)range
                     layoutManager:(NSLayoutManager *)layoutManager
                     textContainer:(NSTextContainer *)textContainer
                            atPoint:(CGPoint)origin
                   backgroundColor:(UIColor *)backgroundColor
                        borderColor:(UIColor *)borderColor {
    NSRange glyphRange = [layoutManager glyphRangeForCharacterRange:range actualCharacterRange:NULL];
    if (glyphRange.location == NSNotFound || glyphRange.length == 0) return;
    
    NSRange lineRange, lastLineRange;
    [layoutManager lineFragmentRectForGlyphAtIndex:glyphRange.location effectiveRange:&lineRange];
    [layoutManager lineFragmentRectForGlyphAtIndex:NSMaxRange(glyphRange) - 1 effectiveRange:&lastLineRange];
    
    if (NSEqualRanges(lineRange, lastLineRange)) {
        [self drawSingleLineBackground:glyphRange layoutManager:layoutManager textContainer:textContainer
                                atPoint:origin backgroundColor:backgroundColor borderColor:borderColor];
    } else {
        [self drawMultiLineBackground:glyphRange layoutManager:layoutManager textContainer:textContainer
                               atPoint:origin backgroundColor:backgroundColor borderColor:borderColor];
    }
}

- (void)drawSingleLineBackground:(NSRange)glyphRange
                   layoutManager:(NSLayoutManager *)layoutManager
                   textContainer:(NSTextContainer *)textContainer
                          atPoint:(CGPoint)origin
                 backgroundColor:(UIColor *)backgroundColor
                      borderColor:(UIColor *)borderColor {
    CGRect boundingRect = [self boundingRectForGlyphRange:glyphRange layoutManager:layoutManager textContainer:textContainer];
    if (CGRectIsEmpty(boundingRect)) return;
    
    CGRect rect = [self adjustedRect:boundingRect atPoint:origin];
    if (CGRectIsEmpty(rect) || CGRectIsInfinite(rect)) return;
    
    UIBezierPath *path = [UIBezierPath bezierPathWithRoundedRect:rect cornerRadius:kCodeBackgroundCornerRadius];
    
    [backgroundColor setFill];
    [path fill];
    
    if (borderColor) {
        [self strokePath:path withColor:borderColor];
    }
}

- (void)drawMultiLineBackground:(NSRange)glyphRange
                 layoutManager:(NSLayoutManager *)layoutManager
                 textContainer:(NSTextContainer *)textContainer
                        atPoint:(CGPoint)origin
               backgroundColor:(UIColor *)backgroundColor
                    borderColor:(UIColor *)borderColor {
    NSArray<NSValue *> *lineFragments = [self collectLineFragmentsForGlyphRange:glyphRange
                                                                  layoutManager:layoutManager
                                                                  textContainer:textContainer];
    if (lineFragments.count < 2) return; // Need at least boundingRect and fragmentRect for one line
    
    NSUInteger lineCount = lineFragments.count / 2;
    
    // Draw start line (rounded left, no right border)
    [self drawStartLine:lineFragments[0] fragmentRect:lineFragments[1]
        backgroundColor:backgroundColor borderColor:borderColor origin:origin];
    
    // Draw middle lines (no left border, no rounded corners)
    if (lineCount > 2) {
        NSRange middleRange = NSMakeRange(2, (lineCount - 2) * 2);
        [self drawMiddleLines:[lineFragments subarrayWithRange:middleRange]
            backgroundColor:backgroundColor borderColor:borderColor origin:origin];
    }
    
    // Draw end line (rounded right, no left border)
    if (lineCount > 1) {
        NSUInteger lastIndex = lineFragments.count - 2;
        [self drawEndLine:lineFragments[lastIndex] fragmentRect:lineFragments[lastIndex + 1]
            backgroundColor:backgroundColor borderColor:borderColor origin:origin];
    }
}

#pragma mark - Drawing Methods

- (void)drawRoundedEdge:(CGRect)rect
        backgroundColor:(UIColor *)backgroundColor
            borderColor:(UIColor *)borderColor
                isLeft:(BOOL)isLeft {
    [self fillRoundedEdge:rect backgroundColor:backgroundColor isLeft:isLeft];
    if (borderColor) {
        [self strokeRoundedEdge:rect borderColor:borderColor isLeft:isLeft];
    }
}

- (void)fillRoundedEdge:(CGRect)rect backgroundColor:(UIColor *)backgroundColor isLeft:(BOOL)isLeft {
    UIBezierPath *fillPath = [self createRoundedFillPath:rect isLeft:isLeft];
    [backgroundColor setFill];
    [fillPath fill];
}

- (void)strokeRoundedEdge:(CGRect)rect borderColor:(UIColor *)borderColor isLeft:(BOOL)isLeft {
    CGFloat topY = rect.origin.y + HalfStroke();
    CGFloat bottomY = CGRectGetMaxY(rect) - HalfStroke();
    UIBezierPath *borderPath = [self createRoundedBorderPath:rect topY:topY bottomY:bottomY isLeft:isLeft];
    [self strokePath:borderPath withColor:borderColor];
}

- (UIBezierPath *)createRoundedFillPath:(CGRect)rect isLeft:(BOOL)isLeft {
    UIBezierPath *path = [UIBezierPath bezierPath];
    
    if (isLeft) {
        [path moveToPoint:CGPointMake(rect.origin.x + kCodeBackgroundCornerRadius, rect.origin.y)];
        [path addLineToPoint:CGPointMake(CGRectGetMaxX(rect), rect.origin.y)];
        [path addLineToPoint:CGPointMake(CGRectGetMaxX(rect), CGRectGetMaxY(rect))];
        [path addLineToPoint:CGPointMake(rect.origin.x + kCodeBackgroundCornerRadius, CGRectGetMaxY(rect))];
        [path addQuadCurveToPoint:CGPointMake(rect.origin.x, CGRectGetMaxY(rect) - kCodeBackgroundCornerRadius)
                      controlPoint:CGPointMake(rect.origin.x, CGRectGetMaxY(rect))];
        [path addLineToPoint:CGPointMake(rect.origin.x, rect.origin.y + kCodeBackgroundCornerRadius)];
        [path addQuadCurveToPoint:CGPointMake(rect.origin.x + kCodeBackgroundCornerRadius, rect.origin.y)
                      controlPoint:CGPointMake(rect.origin.x, rect.origin.y)];
    } else {
        [path moveToPoint:CGPointMake(rect.origin.x, rect.origin.y)];
        [path addLineToPoint:CGPointMake(CGRectGetMaxX(rect) - kCodeBackgroundCornerRadius, rect.origin.y)];
        [path addQuadCurveToPoint:CGPointMake(CGRectGetMaxX(rect), rect.origin.y + kCodeBackgroundCornerRadius)
                      controlPoint:CGPointMake(CGRectGetMaxX(rect), rect.origin.y)];
        [path addLineToPoint:CGPointMake(CGRectGetMaxX(rect), CGRectGetMaxY(rect) - kCodeBackgroundCornerRadius)];
        [path addQuadCurveToPoint:CGPointMake(CGRectGetMaxX(rect) - kCodeBackgroundCornerRadius, CGRectGetMaxY(rect))
                      controlPoint:CGPointMake(CGRectGetMaxX(rect), CGRectGetMaxY(rect))];
        [path addLineToPoint:CGPointMake(rect.origin.x, CGRectGetMaxY(rect))];
        [path closePath];
    }
    
    return path;
}

- (void)drawMiddleBorders:(CGRect)rect borderColor:(UIColor *)borderColor {
    CGFloat topY = rect.origin.y + HalfStroke();
    CGFloat bottomY = CGRectGetMaxY(rect) - HalfStroke();
    
    UIBezierPath *path = [UIBezierPath bezierPath];
    [path moveToPoint:CGPointMake(rect.origin.x, topY)];
    [path addLineToPoint:CGPointMake(CGRectGetMaxX(rect), topY)];
    [path moveToPoint:CGPointMake(rect.origin.x, bottomY)];
    [path addLineToPoint:CGPointMake(CGRectGetMaxX(rect), bottomY)];
    
    [self strokePath:path withColor:borderColor];
}

#pragma mark - Path Creation

- (UIBezierPath *)createRoundedBorderPath:(CGRect)rect topY:(CGFloat)topY bottomY:(CGFloat)bottomY isLeft:(BOOL)isLeft {
    CGFloat borderX = isLeft ? (rect.origin.x + HalfStroke()) : (CGRectGetMaxX(rect) - HalfStroke());
    CGFloat cornerX = isLeft ? (rect.origin.x + kCodeBackgroundCornerRadius) : (CGRectGetMaxX(rect) - kCodeBackgroundCornerRadius);
    CGFloat edgeX = isLeft ? rect.origin.x : CGRectGetMaxX(rect);
    
    UIBezierPath *path = [UIBezierPath bezierPath];
    
    if (isLeft) {
        [self addLeftRoundedBorderToPath:path rect:rect borderX:borderX cornerX:cornerX topY:topY bottomY:bottomY];
    } else {
        [self addRightRoundedBorderToPath:path rect:rect borderX:borderX cornerX:cornerX edgeX:edgeX topY:topY bottomY:bottomY];
    }
    
    return path;
}

- (void)addLeftRoundedBorderToPath:(UIBezierPath *)path rect:(CGRect)rect borderX:(CGFloat)borderX cornerX:(CGFloat)cornerX topY:(CGFloat)topY bottomY:(CGFloat)bottomY {
    [path moveToPoint:CGPointMake(borderX, rect.origin.y + kCodeBackgroundCornerRadius)];
    [path addQuadCurveToPoint:CGPointMake(cornerX, topY) controlPoint:CGPointMake(borderX, rect.origin.y)];
    [path addLineToPoint:CGPointMake(CGRectGetMaxX(rect), topY)];
    [path moveToPoint:CGPointMake(borderX, rect.origin.y + kCodeBackgroundCornerRadius)];
    [path addLineToPoint:CGPointMake(borderX, CGRectGetMaxY(rect) - kCodeBackgroundCornerRadius)];
    [path addQuadCurveToPoint:CGPointMake(cornerX, bottomY) controlPoint:CGPointMake(borderX, CGRectGetMaxY(rect))];
    [path addLineToPoint:CGPointMake(CGRectGetMaxX(rect), bottomY)];
}

- (void)addRightRoundedBorderToPath:(UIBezierPath *)path rect:(CGRect)rect borderX:(CGFloat)borderX cornerX:(CGFloat)cornerX edgeX:(CGFloat)edgeX topY:(CGFloat)topY bottomY:(CGFloat)bottomY {
    [path moveToPoint:CGPointMake(rect.origin.x, topY)];
    [path addLineToPoint:CGPointMake(cornerX, topY)];
    [path addQuadCurveToPoint:CGPointMake(borderX, rect.origin.y + kCodeBackgroundCornerRadius) controlPoint:CGPointMake(edgeX, rect.origin.y)];
    [path moveToPoint:CGPointMake(borderX, rect.origin.y + kCodeBackgroundCornerRadius)];
    [path addLineToPoint:CGPointMake(borderX, CGRectGetMaxY(rect) - kCodeBackgroundCornerRadius)];
    [path addQuadCurveToPoint:CGPointMake(cornerX, bottomY) controlPoint:CGPointMake(edgeX, CGRectGetMaxY(rect))];
    [path addLineToPoint:CGPointMake(rect.origin.x, bottomY)];
}

#pragma mark - Helper Methods

- (NSArray<NSValue *> *)collectLineFragmentsForGlyphRange:(NSRange)glyphRange
                                            layoutManager:(NSLayoutManager *)layoutManager
                                            textContainer:(NSTextContainer *)textContainer {
    NSMutableArray<NSValue *> *fragments = [NSMutableArray array];
    
    [layoutManager enumerateLineFragmentsForGlyphRange:glyphRange usingBlock:^(CGRect rect, CGRect usedRect, NSTextContainer *container, NSRange lineGlyphRange, BOOL *stop) {
        NSRange intersection = NSIntersectionRange(lineGlyphRange, glyphRange);
        if (intersection.length > 0) {
            CGRect boundingRect = [self boundingRectForGlyphRange:intersection layoutManager:layoutManager textContainer:textContainer];
            [fragments addObject:[NSValue valueWithCGRect:boundingRect]];
            [fragments addObject:[NSValue valueWithCGRect:rect]];
        }
    }];
    
    return fragments;
}

- (void)drawStartLine:(NSValue *)boundingRectValue
          fragmentRect:(NSValue *)fragmentRectValue
        backgroundColor:(UIColor *)backgroundColor
            borderColor:(UIColor *)borderColor
                 origin:(CGPoint)origin {
    CGRect boundingRect = [boundingRectValue CGRectValue];
    CGRect fragmentRect = [fragmentRectValue CGRectValue];
    
    CGRect rect = [self adjustedRect:boundingRect atPoint:origin];
    rect.size.width = CGRectGetMaxX(fragmentRect) + origin.x - rect.origin.x;
    [self drawRoundedEdge:rect backgroundColor:backgroundColor borderColor:borderColor isLeft:YES];
}

- (void)drawMiddleLines:(NSArray<NSValue *> *)fragments
        backgroundColor:(UIColor *)backgroundColor
            borderColor:(UIColor *)borderColor
                 origin:(CGPoint)origin {
    for (NSUInteger i = 0; i < fragments.count; i += 2) {
        CGRect fragmentRect = [fragments[i + 1] CGRectValue];
        CGRect middleRect = [self adjustedRect:fragmentRect atPoint:origin];
        middleRect.origin.x = fragmentRect.origin.x + origin.x;
        middleRect.size.width = fragmentRect.size.width;
        
        [backgroundColor setFill];
        UIRectFill(middleRect);
        
        if (borderColor) {
            [self drawMiddleBorders:middleRect borderColor:borderColor];
        }
    }
}

- (void)drawEndLine:(NSValue *)boundingRectValue
        fragmentRect:(NSValue *)fragmentRectValue
        backgroundColor:(UIColor *)backgroundColor
            borderColor:(UIColor *)borderColor
                 origin:(CGPoint)origin {
    CGRect boundingRect = [boundingRectValue CGRectValue];
    CGRect fragmentRect = [fragmentRectValue CGRectValue];
    
    CGRect rect = [self adjustedRect:boundingRect atPoint:origin];
    rect.origin.x = fragmentRect.origin.x + origin.x;
    rect.size.width = CGRectGetMaxX(boundingRect) + origin.x - rect.origin.x;
    [self drawRoundedEdge:rect backgroundColor:backgroundColor borderColor:borderColor isLeft:NO];
}

- (CGRect)adjustedRect:(CGRect)rect atPoint:(CGPoint)origin {
    CGFloat reduction = rect.size.height * kCodeBackgroundHeightReductionFactor;
    CGFloat top = rect.origin.y + reduction + origin.y;
    CGFloat bottom = CGRectGetMaxY(rect) - reduction + origin.y;
    return CGRectMake(rect.origin.x + origin.x, top, rect.size.width, bottom - top);
}

- (CGRect)boundingRectForGlyphRange:(NSRange)glyphRange
                      layoutManager:(NSLayoutManager *)layoutManager
                      textContainer:(NSTextContainer *)textContainer {
    if (glyphRange.location == NSNotFound || glyphRange.length == 0) return CGRectZero;
    return [layoutManager boundingRectForGlyphRange:glyphRange inTextContainer:textContainer];
}

- (void)strokePath:(UIBezierPath *)path withColor:(UIColor *)color {
    [color setStroke];
    path.lineWidth = kCodeBackgroundBorderWidth;
    path.lineCapStyle = kCGLineCapRound;
    path.lineJoinStyle = kCGLineJoinRound;
    [path stroke];
}

@end
