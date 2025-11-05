#import "EmphasisRenderer.h"
#import "MarkdownASTNode.h"
#import "RenderContext.h"
#import "RichTextConfig.h"
#import "RendererFactory.h"

@implementation EmphasisRenderer {
    RendererFactory *_rendererFactory;
    id _config;
}

- (instancetype)initWithRendererFactory:(id)rendererFactory
                                 config:(id)config {
    self = [super init];
    if (self) {
        _rendererFactory = rendererFactory;
        _config = config;
    }
    return self;
}

- (UIFont *)ensureFontIsItalic:(UIFont *)font {
    if (!font) {
        return nil;
    }
    UIFontDescriptorSymbolicTraits traits = font.fontDescriptor.symbolicTraits;
    if (traits & UIFontDescriptorTraitItalic) {
        return font;
    }

    UIFontDescriptor *italicDescriptor = [font.fontDescriptor fontDescriptorWithSymbolicTraits:UIFontDescriptorTraitItalic];
    return [UIFont fontWithDescriptor:italicDescriptor size:font.pointSize] ?: font;
}

- (void)renderNode:(MarkdownASTNode *)node
             into:(NSMutableAttributedString *)output
          withFont:(UIFont *)font
            color:(UIColor *)color
           context:(RenderContext *)context {
    NSUInteger start = output.length;
    
    UIColor *emphasisColor = color;
    if (_config) {
        RichTextConfig *config = (RichTextConfig *)_config;
        UIColor *configEmphasisColor = [config emphasisColor];
        if (configEmphasisColor) {
            emphasisColor = configEmphasisColor;
        }
    }
    
    UIFontDescriptor *fontDescriptor = font.fontDescriptor;
    UIFontDescriptor *italicDescriptor = [fontDescriptor fontDescriptorWithSymbolicTraits:UIFontDescriptorTraitItalic];
    UIFont *italicFont = [UIFont fontWithDescriptor:italicDescriptor size:font.pointSize];
    
    [_rendererFactory renderChildrenOfNode:node
                                      into:output
                                  withFont:italicFont
                                     color:emphasisColor
                                    context:context];
    
    NSUInteger len = output.length - start;
    if (len > 0) {
        NSRange range = NSMakeRange(start, len);
        NSDictionary *existingAttributes = [output attributesAtIndex:start effectiveRange:NULL];
        UIFont *currentFont = existingAttributes[NSFontAttributeName];
        
        if (currentFont) {
            UIFont *verifiedItalicFont = [self ensureFontIsItalic:currentFont];
            if (![verifiedItalicFont isEqual:currentFont]) {
                NSMutableDictionary *emphasisAttributes = [existingAttributes mutableCopy];
                emphasisAttributes[NSFontAttributeName] = verifiedItalicFont;
                [output setAttributes:emphasisAttributes range:range];
            }
        } else {
            NSMutableDictionary *emphasisAttributes = [existingAttributes ?: @{} mutableCopy];
            emphasisAttributes[NSFontAttributeName] = italicFont;
            [output setAttributes:emphasisAttributes range:range];
        }
    }
}

@end

