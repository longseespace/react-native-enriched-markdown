#import "ParagraphStyleUtils.h"

NSMutableParagraphStyle *getOrCreateParagraphStyle(NSMutableAttributedString *output, NSUInteger index)
{
  NSParagraphStyle *existing = [output attribute:NSParagraphStyleAttributeName atIndex:index effectiveRange:NULL];
  return existing ? [existing mutableCopy] : [[NSMutableParagraphStyle alloc] init];
}

// For content blocks (paragraphs, headings) - applies paragraphSpacing to the content range
void applyParagraphSpacing(NSMutableAttributedString *output, NSUInteger start, CGFloat marginBottom)
{
  [output appendAttributedString:[[NSAttributedString alloc] initWithString:@"\n"]];

  NSMutableParagraphStyle *style = getOrCreateParagraphStyle(output, start);
  style.paragraphSpacing = marginBottom;

  NSRange range = NSMakeRange(start, output.length - start);
  [output addAttribute:NSParagraphStyleAttributeName value:style range:range];
}

// For container blocks (blockquotes, lists, code blocks) - isolated spacer that doesn't affect content styles
void applyBlockSpacing(NSMutableAttributedString *output, CGFloat marginBottom)
{
  NSUInteger spacerLocation = output.length;
  [output appendAttributedString:[[NSAttributedString alloc] initWithString:@"\n"]];

  NSMutableParagraphStyle *spacerStyle = [[NSMutableParagraphStyle alloc] init];
  // Collapse the line height to minimal (the \n itself should be nearly invisible)
  spacerStyle.minimumLineHeight = 1;
  spacerStyle.maximumLineHeight = 1;
  // Add the actual margin as space after the line
  spacerStyle.paragraphSpacing = marginBottom;

  [output addAttribute:NSParagraphStyleAttributeName value:spacerStyle range:NSMakeRange(spacerLocation, 1)];
}

void applyLineHeight(NSMutableAttributedString *output, NSRange range, CGFloat lineHeight)
{
  if (lineHeight <= 0) {
    return;
  }

  NSMutableParagraphStyle *style = getOrCreateParagraphStyle(output, range.location);
  UIFont *font = [output attribute:NSFontAttributeName atIndex:range.location effectiveRange:NULL];
  if (!font) {
    return;
  }

  style.lineHeightMultiple = lineHeight / font.pointSize;
  style.minimumLineHeight = 0;
  style.maximumLineHeight = 0;
  style.lineSpacing = 0;

  [output addAttribute:NSParagraphStyleAttributeName value:style range:range];
}
