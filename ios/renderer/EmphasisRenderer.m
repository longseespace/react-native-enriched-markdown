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

    // Combine italic with existing traits (preserve bold if present)
    UIFontDescriptorSymbolicTraits combinedTraits = traits | UIFontDescriptorTraitItalic;
    UIFontDescriptor *italicDescriptor = [font.fontDescriptor fontDescriptorWithSymbolicTraits:combinedTraits];
    return [UIFont fontWithDescriptor:italicDescriptor size:font.pointSize] ?: font;
}

- (UIColor *)emphasisColorFromColor:(UIColor *)color {
    if (!_config) {
        return color;
    }
    
    RichTextConfig *config = (RichTextConfig *)_config;
    UIColor *configBoldColor = [config boldColor];
    UIColor *configEmphasisColor = [config emphasisColor];
    
    // If nested inside bold (color matches boldColor), preserve bold color
    if (configBoldColor && [color isEqual:configBoldColor]) {
        return configBoldColor;
    }
    
    return configEmphasisColor ?: color;
}

- (void)renderNode:(MarkdownASTNode *)node
             into:(NSMutableAttributedString *)output
          withFont:(UIFont *)font
            color:(UIColor *)color
           context:(RenderContext *)context {
    NSUInteger start = output.length;
    
    UIColor *emphasisColor = [self emphasisColorFromColor:color];
    UIFont *italicFont = [self ensureFontIsItalic:font];
    
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
        UIFont *verifiedItalicFont = [self ensureFontIsItalic:currentFont ?: italicFont];
        
        if (![verifiedItalicFont isEqual:currentFont]) {
            NSMutableDictionary *emphasisAttributes = [existingAttributes ?: @{} mutableCopy];
            emphasisAttributes[NSFontAttributeName] = verifiedItalicFont;
            [output setAttributes:emphasisAttributes range:range];
        }
    }
}

@end

