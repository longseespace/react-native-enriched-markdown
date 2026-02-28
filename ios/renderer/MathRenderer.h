#import "NodeRenderer.h"

@class RendererFactory;
@class StyleConfig;

@interface MathRenderer : NSObject <NodeRenderer>
- (instancetype)initWithRendererFactory:(RendererFactory *)rendererFactory
                                 config:(StyleConfig *)config
                              isDisplay:(BOOL)isDisplay;
@end
