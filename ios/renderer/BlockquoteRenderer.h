#import "MarkdownASTNode.h"
#import "NodeRenderer.h"
#import "RenderContext.h"

@interface BlockquoteRenderer : NSObject <NodeRenderer>
@property (nonatomic, strong) id config;

- (instancetype)initWithRendererFactory:(id)rendererFactory config:(id)config;
@end
