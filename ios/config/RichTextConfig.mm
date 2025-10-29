#import "RichTextConfig.h"
#import <React/RCTFont.h>

@implementation RichTextConfig {
    UIColor *_primaryColor;
    NSNumber *_primaryFontSize;
    NSString *_primaryFontWeight;
    NSString *_primaryFontFamily;
    UIFont *_primaryFont;
    BOOL _primaryFontNeedsRecreation;
    
    CGFloat _h1FontSize;
    NSString *_h1FontFamily;
}

- (instancetype)init {
    self = [super init];
    _primaryFontNeedsRecreation = YES;
    return self;
}

- (id)copyWithZone:(NSZone *)zone {
    RichTextConfig *copy = [[[self class] allocWithZone:zone] init];
    copy->_primaryColor = [_primaryColor copy];
    copy->_primaryFontSize = [_primaryFontSize copy];
    copy->_primaryFontWeight = [_primaryFontWeight copy];
    copy->_primaryFontFamily = [_primaryFontFamily copy];
    copy->_primaryFontNeedsRecreation = YES;
    
    copy->_h1FontSize = _h1FontSize;
    copy->_h1FontFamily = [_h1FontFamily copy];
    
    return copy;
}

#pragma mark - Primary Font Properties

- (UIColor *)primaryColor {
    return _primaryColor != nullptr ? _primaryColor : [UIColor blackColor];
}

- (void)setPrimaryColor:(UIColor *)newValue {
    _primaryColor = newValue;
}

- (NSNumber *)primaryFontSize {
    return _primaryFontSize != nullptr ? _primaryFontSize : @16;
}

- (void)setPrimaryFontSize:(NSNumber *)newValue {
    _primaryFontSize = newValue;
    _primaryFontNeedsRecreation = YES;
}

- (NSString *)primaryFontWeight {
    return _primaryFontWeight != nullptr ? _primaryFontWeight : @"normal";
}

- (void)setPrimaryFontWeight:(NSString *)newValue {
    _primaryFontWeight = newValue;
    _primaryFontNeedsRecreation = YES;
}

- (NSString *)primaryFontFamily {
    return _primaryFontFamily;
}

- (void)setPrimaryFontFamily:(NSString *)newValue {
    _primaryFontFamily = newValue;
    _primaryFontNeedsRecreation = YES;
}

- (UIFont *)primaryFont {
    if (_primaryFontNeedsRecreation || !_primaryFont) {
        _primaryFont = [RCTFont updateFont:nil
                                withFamily:_primaryFontFamily
                                       size:_primaryFontSize
                                     weight:_primaryFontWeight
                                      style:nil
                                   variant:nil
                             scaleMultiplier:1];
        _primaryFontNeedsRecreation = NO;
    }
    return _primaryFont;
}

#pragma mark - H1 Properties

- (CGFloat)h1FontSize {
    return _h1FontSize > 0 ? _h1FontSize : 32.0;
}

- (void)setH1FontSize:(CGFloat)newValue {
    _h1FontSize = newValue;
}

- (NSString *)h1FontFamily {
    return _h1FontFamily;
}

- (void)setH1FontFamily:(NSString *)newValue {
    _h1FontFamily = newValue;
}

@end
