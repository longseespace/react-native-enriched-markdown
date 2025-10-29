import type { RichTextStyle } from './RichTextView';
import type { RichTextStyleInternal } from './RichTextViewNativeComponent';

const defaultH1Style: RichTextStyleInternal['h1'] = {
  fontSize: 36,
  fontFamily: 'Helvetica-Bold',
};

export const normalizeRichTextStyle = (
  style: RichTextStyle
): RichTextStyleInternal => {
  return {
    h1: {
      ...defaultH1Style,
      ...style.h1,
    },
  };
};
