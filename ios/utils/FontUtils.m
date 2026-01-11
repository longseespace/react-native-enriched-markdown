#import "FontUtils.h"
#import "RenderContext.h"
#import <React/RCTFont.h>

UIFont *cachedFontFromBlockStyle(BlockStyle *blockStyle, RenderContext *context)
{
  if (!blockStyle) {
    return nil;
  }
  return [context cachedFontForSize:blockStyle.fontSize family:blockStyle.fontFamily weight:blockStyle.fontWeight];
}

UIFont *fontFromProperties(CGFloat fontSize, NSString *fontFamily, NSString *fontWeight)
{
  return [RCTFont updateFont:nil
                  withFamily:fontFamily.length > 0 ? fontFamily : nil
                        size:@(fontSize)
                      weight:fontWeight ?: @"normal"
                       style:nil
                     variant:nil
             scaleMultiplier:1];
}
