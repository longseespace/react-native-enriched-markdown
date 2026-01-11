#import "FontUtils.h"
#import "RenderContext.h"

UIFont *cachedFontFromBlockStyle(BlockStyle *blockStyle, RenderContext *context)
{
  if (!blockStyle) {
    return nil;
  }
  if (blockStyle.cachedFont) {
    return blockStyle.cachedFont;
  }
  return [context cachedFontForSize:blockStyle.fontSize family:blockStyle.fontFamily weight:blockStyle.fontWeight];
}
