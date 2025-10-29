#import "NodeRenderer.h"
#import "MarkdownASTNode.h"

@interface RendererFactory : NSObject
+ (instancetype)sharedFactory;
- (instancetype)initWithConfig:(id)config;
- (id<NodeRenderer>)rendererForNodeType:(MarkdownNodeType)type;
@end
