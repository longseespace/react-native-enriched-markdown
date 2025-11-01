#import "HeadingRenderer.h"
#import "SpacingUtils.h"
#import "HeadingStyle.h"

@implementation HeadingRenderer

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

    UIFont *headingFont = font;
    
    NSInteger level = 1; // Default to H1
    NSString *levelString = node.attributes[@"level"];
    if (levelString) {
        level = [levelString integerValue];
    }
    
    HeadingStyle *headingStyle = [[HeadingStyle alloc] initWithLevel:level config:self.config];
    CGFloat fontSize = [headingStyle fontSize];
    NSString *fontFamily = [headingStyle fontFamily];
    
    // Try custom font family first, fallback to base font with size
    if (fontFamily.length > 0) {
        UIFont *customFont = [UIFont fontWithName:fontFamily size:fontSize];
        headingFont = customFont ?: [UIFont fontWithDescriptor:font.fontDescriptor size:fontSize];
    } else {
        headingFont = [UIFont fontWithDescriptor:font.fontDescriptor size:fontSize];
    }
    
    for (MarkdownASTNode *child in node.children) {
        if (child.type == MarkdownNodeTypeText && child.content) {
            [self.textRenderer renderNode:child 
                                    into:output 
                               withFont:headingFont
                                  color:color
                                 context:context];
        }
    }
    
    NSAttributedString *spacing = createSpacing();
    [output appendAttributedString:spacing];
}


@end
