#import "ThematicBreakRenderer.h"
#import "MarkdownASTNode.h"
#import "StyleConfig.h"
#import "ThematicBreakAttachment.h"

#pragma mark - Renderer Implementation

@implementation ThematicBreakRenderer {
  __weak id _rendererFactory;
  StyleConfig *_config;
}

- (instancetype)initWithRendererFactory:(id)rendererFactory config:(id)config
{
  self = [super init];
  if (self) {
    _rendererFactory = rendererFactory;
    _config = (StyleConfig *)config;
  }
  return self;
}

- (void)renderNode:(MarkdownASTNode *)node into:(NSMutableAttributedString *)output context:(RenderContext *)context
{
  // 1. Ensure the line starts on a fresh line
  [self ensureStartingNewline:output];

  // 2. Setup the attachment with config values
  ThematicBreakAttachment *attachment = [[ThematicBreakAttachment alloc] init];
  attachment.lineColor = _config.thematicBreakColor ?: [UIColor separatorColor];
  attachment.lineHeight = _config.thematicBreakHeight > 0 ? _config.thematicBreakHeight : 1.0;
  attachment.marginTop = _config.thematicBreakMarginTop;
  attachment.marginBottom = _config.thematicBreakMarginBottom;

  // 3. Define attributes (using the standard Object Replacement Character \uFFFC)
  NSDictionary *attributes = @{
    NSAttachmentAttributeName : attachment,
    NSParagraphStyleAttributeName : [NSParagraphStyle defaultParagraphStyle]
  };

  NSAttributedString *breakString = [[NSAttributedString alloc] initWithString:@"\uFFFC" attributes:attributes];

  // 4. Assemble
  [output appendAttributedString:breakString];
  [output appendAttributedString:[[NSAttributedString alloc] initWithString:@"\n"]];
}

#pragma mark - Private Utilities

- (void)ensureStartingNewline:(NSMutableAttributedString *)output
{
  if (output.length > 0 && ![output.string hasSuffix:@"\n"]) {
    [output appendAttributedString:[[NSAttributedString alloc] initWithString:@"\n"]];
  }
}

@end