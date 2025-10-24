#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface RichTextTheme : NSObject

@property (nonatomic, strong) UIFont *baseFont;
@property (nonatomic, strong) UIColor *textColor;

+ (instancetype)defaultTheme;

@end
