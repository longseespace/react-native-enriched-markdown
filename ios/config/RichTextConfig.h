#pragma once
#import <UIKit/UIKit.h>

@interface RichTextConfig: NSObject<NSCopying>
- (instancetype)init;

// Primary font properties
- (UIColor *)primaryColor;
- (void)setPrimaryColor:(UIColor *)newValue;
- (NSNumber *)primaryFontSize;
- (void)setPrimaryFontSize:(NSNumber *)newValue;
- (NSString *)primaryFontWeight;
- (void)setPrimaryFontWeight:(NSString *)newValue;
- (NSString *)primaryFontFamily;
- (void)setPrimaryFontFamily:(NSString *)newValue;
- (UIFont *)primaryFont;

// H1 properties
- (CGFloat)h1FontSize;
- (void)setH1FontSize:(CGFloat)newValue;
// Optional specific font family for H1 (e.g., "Helvetica-Bold")
- (NSString *)h1FontFamily;
- (void)setH1FontFamily:(NSString *)newValue;

// Future: H2, H3, H4, H5, H6, link, paragraph properties
@end
