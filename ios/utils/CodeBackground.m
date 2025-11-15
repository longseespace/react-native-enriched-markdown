#import "CodeBackground.h"

NSString *const RichTextCodeAttributeName = @"RichTextCode";

static const CGFloat kCodeBackgroundCornerRadius = 2.0;
static const CGFloat kCodeBackgroundBorderWidth = 1.0;

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
    NSMutableArray<NSValue *> *boundingRects = [NSMutableArray array];
    NSMutableArray<NSValue *> *fragmentRects = [NSMutableArray array];
    
    [layoutManager enumerateLineFragmentsForGlyphRange:glyphRange usingBlock:^(CGRect rect, CGRect usedRect, NSTextContainer *container, NSRange lineGlyphRange, BOOL *stop) {
        NSRange intersection = NSIntersectionRange(lineGlyphRange, glyphRange);
        if (intersection.length > 0) {
            CGRect boundingRect = [self boundingRectForGlyphRange:intersection layoutManager:layoutManager textContainer:textContainer];
            [boundingRects addObject:[NSValue valueWithCGRect:boundingRect]];
            [fragmentRects addObject:[NSValue valueWithCGRect:rect]];
        }
    }];
    
    if (boundingRects.count == 0) return;
    
    // Draw start line (rounded left, no right border)
    CGRect firstBoundingRect = [boundingRects[0] CGRectValue];
    CGRect firstFragmentRect = [fragmentRects[0] CGRectValue];
    CGRect startRect = [self adjustedRect:firstBoundingRect atPoint:origin];
    startRect.size.width = CGRectGetMaxX(firstFragmentRect) + origin.x - startRect.origin.x;
    [self drawRoundedEdge:startRect backgroundColor:backgroundColor borderColor:borderColor isLeft:YES];
    
    // Draw middle lines (no left border, no rounded corners)
    for (NSUInteger i = 1; i < boundingRects.count - 1; i++) {
        CGRect fragmentRect = [fragmentRects[i] CGRectValue];
        CGRect middleRect = [self adjustedRect:fragmentRect atPoint:origin];
        middleRect.origin.x = fragmentRect.origin.x + origin.x;
        middleRect.size.width = fragmentRect.size.width;
        
        [backgroundColor setFill];
        UIRectFill(middleRect);
        
        if (borderColor) {
            [self drawMiddleBorders:middleRect borderColor:borderColor];
        }
    }
    
    // Draw end line (rounded right, no left border)
    if (boundingRects.count > 1) {
        NSUInteger lastIndex = boundingRects.count - 1;
        CGRect lastBoundingRect = [boundingRects[lastIndex] CGRectValue];
        CGRect lastFragmentRect = [fragmentRects[lastIndex] CGRectValue];
        CGRect endRect = [self adjustedRect:lastBoundingRect atPoint:origin];
        endRect.origin.x = lastFragmentRect.origin.x + origin.x;
        endRect.size.width = CGRectGetMaxX(lastBoundingRect) + origin.x - endRect.origin.x;
        [self drawRoundedEdge:endRect backgroundColor:backgroundColor borderColor:borderColor isLeft:NO];
    }
}

#pragma mark - Drawing Methods

- (void)drawRoundedEdge:(CGRect)rect
        backgroundColor:(UIColor *)backgroundColor
            borderColor:(UIColor *)borderColor
                isLeft:(BOOL)isLeft {
    // Draw fill
    UIBezierPath *fillPath = [self createRoundedFillPath:rect isLeft:isLeft];
    [backgroundColor setFill];
    [fillPath fill];
    
    // Draw border
    if (borderColor) {
        CGFloat topY = rect.origin.y + HalfStroke();
        CGFloat bottomY = CGRectGetMaxY(rect) - HalfStroke();
        UIBezierPath *borderPath = [self createRoundedBorderPath:rect topY:topY bottomY:bottomY isLeft:isLeft];
        [self strokePath:borderPath withColor:borderColor];
    }
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
        [path moveToPoint:CGPointMake(borderX, rect.origin.y + kCodeBackgroundCornerRadius)];
        [path addQuadCurveToPoint:CGPointMake(cornerX, topY) controlPoint:CGPointMake(borderX, rect.origin.y)];
        [path addLineToPoint:CGPointMake(CGRectGetMaxX(rect), topY)];
        [path moveToPoint:CGPointMake(borderX, rect.origin.y + kCodeBackgroundCornerRadius)];
        [path addLineToPoint:CGPointMake(borderX, CGRectGetMaxY(rect) - kCodeBackgroundCornerRadius)];
        [path addQuadCurveToPoint:CGPointMake(cornerX, bottomY) controlPoint:CGPointMake(borderX, CGRectGetMaxY(rect))];
        [path addLineToPoint:CGPointMake(CGRectGetMaxX(rect), bottomY)];
    } else {
        [path moveToPoint:CGPointMake(rect.origin.x, topY)];
        [path addLineToPoint:CGPointMake(cornerX, topY)];
        [path addQuadCurveToPoint:CGPointMake(borderX, rect.origin.y + kCodeBackgroundCornerRadius) controlPoint:CGPointMake(edgeX, rect.origin.y)];
        [path moveToPoint:CGPointMake(borderX, rect.origin.y + kCodeBackgroundCornerRadius)];
        [path addLineToPoint:CGPointMake(borderX, CGRectGetMaxY(rect) - kCodeBackgroundCornerRadius)];
        [path addQuadCurveToPoint:CGPointMake(cornerX, bottomY) controlPoint:CGPointMake(edgeX, CGRectGetMaxY(rect))];
        [path addLineToPoint:CGPointMake(rect.origin.x, bottomY)];
    }
    
    return path;
}

#pragma mark - Helper Methods

- (CGRect)adjustedRect:(CGRect)rect atPoint:(CGPoint)origin {
    return CGRectMake(rect.origin.x + origin.x, rect.origin.y + origin.y, rect.size.width, rect.size.height);
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
