#import "BoldRenderer.h"
#import "MarkdownASTNode.h"
#import "RenderContext.h"
#import "RichTextConfig.h"
#import "RendererFactory.h"

@implementation BoldRenderer {
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

- (UIFont *)ensureFontIsBold:(UIFont *)font {
    if (!font) {
        return nil;
    }
    UIFontDescriptorSymbolicTraits traits = font.fontDescriptor.symbolicTraits;
    if (traits & UIFontDescriptorTraitBold) {
        return font;
    }

    // Combine bold with existing traits (preserve italic if present)
    UIFontDescriptorSymbolicTraits combinedTraits = traits | UIFontDescriptorTraitBold;
    UIFontDescriptor *boldDescriptor = [font.fontDescriptor fontDescriptorWithSymbolicTraits:combinedTraits];
    return [UIFont fontWithDescriptor:boldDescriptor size:font.pointSize] ?: font;
}

- (UIColor *)boldColorFromColor:(UIColor *)color {
    if (!_config) {
        return color;
    }
    
    RichTextConfig *config = (RichTextConfig *)_config;
    UIColor *configBoldColor = [config boldColor];
    return configBoldColor ?: color;
}

- (void)renderNode:(MarkdownASTNode *)node
             into:(NSMutableAttributedString *)output
          withFont:(UIFont *)font
            color:(UIColor *)color
           context:(RenderContext *)context {
    NSUInteger start = output.length;
    
    UIColor *boldColor = [self boldColorFromColor:color];
    UIFont *boldFont = [self ensureFontIsBold:font];
    
    [_rendererFactory renderChildrenOfNode:node
                                      into:output
                                  withFont:boldFont
                                     color:boldColor
                                    context:context];
    
    NSUInteger len = output.length - start;
    if (len > 0) {
        NSRange range = NSMakeRange(start, len);
        NSDictionary *existingAttributes = [output attributesAtIndex:start effectiveRange:NULL];
        UIFont *currentFont = existingAttributes[NSFontAttributeName];
        UIFont *verifiedBoldFont = [self ensureFontIsBold:currentFont ?: boldFont];
        
        if (![verifiedBoldFont isEqual:currentFont]) {
            NSMutableDictionary *boldAttributes = [existingAttributes ?: @{} mutableCopy];
            boldAttributes[NSFontAttributeName] = verifiedBoldFont;
            [output setAttributes:boldAttributes range:range];
        }
    }
}

@end
