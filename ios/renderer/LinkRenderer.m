#import "LinkRenderer.h"
#import "LinkStyle.h"

@implementation LinkRenderer

- (instancetype)initWithTextRenderer:(id<NodeRenderer>)textRenderer config:(id)config {
    self = [super init];
    if (self) {
        _textRenderer = textRenderer;
        self.config = config;
    }
    return self;
}

- (void)renderNode:(MarkdownASTNode *)node
             into:(NSMutableAttributedString *)output
          withFont:(UIFont *)font
            color:(UIColor *)color
           context:(RenderContext *)context {
    NSUInteger start = output.length;
    
    for (MarkdownASTNode *child in node.children) {
        if (child.type == MarkdownNodeTypeText && child.content) {
            [self.textRenderer renderNode:child 
                                    into:output 
                               withFont:font
                                  color:color
                                 context:context];
        }
    }
    
    NSUInteger len = output.length - start;
    if (len > 0) {
        NSRange range = NSMakeRange(start, len);
        NSString *url = node.attributes[@"url"] ?: @"";
        
        NSDictionary *existingAttributes = [output attributesAtIndex:start effectiveRange:NULL];
        LinkStyle *linkStyle = [[LinkStyle alloc] initWithConfig:self.config];
        
        NSMutableDictionary *linkAttributes = [existingAttributes mutableCopy];
        linkAttributes[NSLinkAttributeName] = url;
        
        UIColor *linkColor = [linkStyle color];
        if (linkColor) {
            linkAttributes[NSForegroundColorAttributeName] = linkColor;
            linkAttributes[NSUnderlineColorAttributeName] = linkColor;
        }
        
        if ([linkStyle underline]) {
            linkAttributes[NSUnderlineStyleAttributeName] = @(NSUnderlineStyleSingle);
        }
        
        [output setAttributes:linkAttributes range:range];
        [context registerLinkRange:range url:url];
    }
}

@end
