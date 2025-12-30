#import "MarkdownParser.h"
#import "MarkdownASTNode.h"

extern MarkdownASTNode *parseMarkdownWithCppParser(NSString *markdown);

@implementation MarkdownParser

- (MarkdownASTNode *)parseMarkdown:(NSString *)markdown
{
  return parseMarkdownWithCppParser(markdown);
}

@end