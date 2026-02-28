import { useMemo, useCallback } from 'react';
import EnrichedMarkdownTextNativeComponent, {
  type NativeProps,
  type LinkPressEvent,
  type LinkLongPressEvent,
  type TaskListItemPressEvent,
} from './EnrichedMarkdownTextNativeComponent';
import EnrichedMarkdownNativeComponent from './EnrichedMarkdownNativeComponent';
import { normalizeMarkdownStyle } from './normalizeMarkdownStyle';
import type { ViewStyle, TextStyle, NativeSyntheticEvent } from 'react-native';

type TextAlign = 'auto' | 'left' | 'right' | 'center' | 'justify';

interface BaseBlockStyle {
  fontSize?: number;
  fontFamily?: string;
  fontWeight?: string;
  color?: string;
  marginTop?: number;
  marginBottom?: number;
  lineHeight?: number;
}

interface ParagraphStyle extends BaseBlockStyle {
  textAlign?: TextAlign;
}

interface HeadingStyle extends BaseBlockStyle {
  textAlign?: TextAlign;
}

interface BlockquoteStyle extends BaseBlockStyle {
  borderColor?: string;
  borderWidth?: number;
  gapWidth?: number;
  backgroundColor?: string;
}

interface ListStyle extends BaseBlockStyle {
  bulletColor?: string;
  bulletSize?: number;
  markerColor?: string;
  markerFontWeight?: string;
  gapWidth?: number;
  marginLeft?: number;
}

interface CodeBlockStyle extends BaseBlockStyle {
  backgroundColor?: string;
  borderColor?: string;
  borderRadius?: number;
  borderWidth?: number;
  padding?: number;
}

interface LinkStyle {
  fontFamily?: string;
  color?: string;
  underline?: boolean;
}

interface StrongStyle {
  fontFamily?: string;
  /**
   * Controls whether bold is applied on top of the custom fontFamily.
   * Only relevant when fontFamily is set. Defaults to 'bold'.
   * Set to 'normal' to use the font face as-is without adding bold.
   */
  fontWeight?: 'bold' | 'normal';
  color?: string;
}

interface EmphasisStyle {
  fontFamily?: string;
  /**
   * Controls whether italic is applied on top of the custom fontFamily.
   * Only relevant when fontFamily is set. Defaults to 'italic'.
   * Set to 'normal' to use the font face as-is without adding italic.
   */
  fontStyle?: 'italic' | 'normal';
  color?: string;
}

interface StrikethroughStyle {
  /**
   * Color of the strikethrough line.
   * @platform iOS
   */
  color?: string;
}

interface UnderlineStyle {
  /**
   * Color of the underline.
   * @platform iOS
   */
  color?: string;
}

interface CodeStyle {
  fontFamily?: string;
  fontSize?: number;
  color?: string;
  backgroundColor?: string;
  borderColor?: string;
}

interface ImageStyle {
  height?: number;
  borderRadius?: number;
  marginTop?: number;
  marginBottom?: number;
}

interface InlineImageStyle {
  size?: number;
}

interface ThematicBreakStyle {
  color?: string;
  height?: number;
  marginTop?: number;
  marginBottom?: number;
}

interface TableStyle extends BaseBlockStyle {
  headerFontFamily?: string;
  headerBackgroundColor?: string;
  headerTextColor?: string;
  rowEvenBackgroundColor?: string;
  rowOddBackgroundColor?: string;
  borderColor?: string;
  borderWidth?: number;
  borderRadius?: number;
  cellPaddingHorizontal?: number;
  cellPaddingVertical?: number;
}

interface TaskListStyle {
  checkedColor?: string;
  borderColor?: string;
  checkboxSize?: number;
  checkboxBorderRadius?: number;
  checkmarkColor?: string;
  checkedTextColor?: string;
  checkedStrikethrough?: boolean;
}
export interface MarkdownStyle {
  paragraph?: ParagraphStyle;
  h1?: HeadingStyle;
  h2?: HeadingStyle;
  h3?: HeadingStyle;
  h4?: HeadingStyle;
  h5?: HeadingStyle;
  h6?: HeadingStyle;
  blockquote?: BlockquoteStyle;
  list?: ListStyle;
  codeBlock?: CodeBlockStyle;
  link?: LinkStyle;
  strong?: StrongStyle;
  em?: EmphasisStyle;
  strikethrough?: StrikethroughStyle;
  underline?: UnderlineStyle;
  code?: CodeStyle;
  image?: ImageStyle;
  inlineImage?: InlineImageStyle;
  thematicBreak?: ThematicBreakStyle;
  table?: TableStyle;
  taskList?: TaskListStyle;
}

/**
 * MD4C parser flags configuration.
 * Controls how the markdown parser interprets certain syntax.
 */
export interface Md4cFlags {
  /**
   * Enable underline syntax support (__text__).
   * When enabled, underscores are treated as underline markers.
   * When disabled, underscores are treated as emphasis markers (same as asterisks).
   * @default false
   */
  underline?: boolean;
}

export interface EnrichedMarkdownTextProps
  extends Omit<
    NativeProps,
    | 'markdownStyle'
    | 'style'
    | 'onLinkPress'
    | 'onLinkLongPress'
    | 'onTaskListItemPress'
    | 'md4cFlags'
    | 'enableLinkPreview'
  > {
  /**
   * Style configuration for markdown elements
   */
  markdownStyle?: MarkdownStyle;
  /**
   * Style for the container view.
   */
  containerStyle?: ViewStyle | TextStyle;
  /**
   * Callback fired when a link is pressed.
   * Receives the link URL directly.
   */
  onLinkPress?: (event: LinkPressEvent) => void;
  /**
   * Callback fired when a link is long pressed.
   * Receives the link URL directly.
   * - iOS: When provided, automatically disables the system link preview (unless `enableLinkPreview` is explicitly set to `true`).
   * - Android: Handles long press gestures on links.
   */
  onLinkLongPress?: (event: LinkLongPressEvent) => void;
  /**
   * Callback fired when a task list checkbox is tapped.
   *
   * The checkbox is toggled on the native side automatically.
   * Receives the 0-based task index, the new checked state (after toggling),
   * and the item's plain text.
   *
   * Only fires when `flavor="github"` (GFM task lists require GitHub flavor).
   */
  onTaskListItemPress?: (event: TaskListItemPressEvent) => void;
  /**
   * Controls whether the system link preview is shown on long press (iOS only).
   *
   * When `true`, long-pressing a link shows the native iOS link preview.
   * When `false`, the system preview is suppressed.
   *
   * Defaults to `true`, but automatically becomes `false` when `onLinkLongPress` is provided.
   * Set explicitly to override the automatic behavior.
   *
   * Android: No-op.
   *
   * @default true
   * @platform ios
   */
  enableLinkPreview?: boolean;
  /**
   * MD4C parser flags configuration.
   * Controls how the markdown parser interprets certain syntax.
   */
  md4cFlags?: Md4cFlags;
  /**
   * Specifies whether fonts should scale to respect Text Size accessibility settings.
   * When false, text will not scale with the user's accessibility settings.
   * @default true
   */
  allowFontScaling?: boolean;
  /**
   * Specifies the largest possible scale a font can reach when allowFontScaling is enabled.
   * Possible values:
   * - undefined/null (default): no limit
   * - 0: no limit
   * - >= 1: sets the maxFontSizeMultiplier of this node to this value
   * @default undefined
   */
  maxFontSizeMultiplier?: number;
  /**
   * When false (default), removes trailing margin from the last element to eliminate bottom spacing.
   * When true, keeps the trailing margin from the last element's marginBottom style.
   * @default false
   */
  allowTrailingMargin?: boolean;
  /**
   * Specifies which Markdown flavor to use for rendering.
   * - `'commonmark'` (default): standard CommonMark renderer (single TextView).
   * - `'github'`: GitHub Flavored Markdown — container-based renderer with support for tables and other GFM extensions.
   * @default 'commonmark'
   */
  flavor?: 'commonmark' | 'github';
}

const defaultMd4cFlags: Md4cFlags = {
  underline: false,
};

const BRACKET_DISPLAY_MATH_BLOCK_REGEX =
  /(^|\n)[ \t]*\\\[[ \t]*\n?([\s\S]*?)\n?[ \t]*\\\][ \t]*(?=\n|$)/g;
const INLINE_PAREN_MATH_REGEX = /(^|[^\\])\\\(([\s\S]*?)\\\)/g;

type MarkdownSegment = {
  type: 'text' | 'code';
  content: string;
};

const parseFenceOpening = (
  line: string
): { markerChar: '`' | '~'; markerLength: number } | null => {
  const trimmed = line.trimStart();
  const leadingSpaces = line.length - trimmed.length;
  if (leadingSpaces > 3) return null;

  const match = trimmed.match(/^(`{3,}|~{3,})(.*)$/);
  if (!match) return null;
  const marker = match[1] ?? '';
  if (marker.length === 0) return null;
  const markerChar = marker[0] as '`' | '~';
  return {
    markerChar,
    markerLength: marker.length,
  };
};

const isFenceClosing = (
  line: string,
  markerChar: '`' | '~',
  markerLength: number
): boolean => {
  const trimmed = line.trimStart();
  const leadingSpaces = line.length - trimmed.length;
  if (leadingSpaces > 3) return false;
  const marker = markerChar === '`' ? '`' : '~';
  const regex = new RegExp(`^${marker}{${markerLength},}\\s*$`);
  return regex.test(trimmed);
};

const splitByFencedCodeBlocks = (markdown: string): MarkdownSegment[] => {
  if (!markdown.includes('```') && !markdown.includes('~~~')) {
    return [{ type: 'text', content: markdown }];
  }

  const lines = markdown.split('\n');
  const segments: MarkdownSegment[] = [];
  let buffer: string[] = [];
  let inFence = false;
  let fenceMarkerChar: '`' | '~' = '`';
  let fenceMarkerLength = 3;

  const pushBuffer = (type: 'text' | 'code') => {
    if (buffer.length === 0) return;
    segments.push({ type, content: buffer.join('\n') });
    buffer = [];
  };

  for (const line of lines) {
    if (!inFence) {
      const opening = parseFenceOpening(line);
      if (opening) {
        pushBuffer('text');
        inFence = true;
        fenceMarkerChar = opening.markerChar;
        fenceMarkerLength = opening.markerLength;
      }
      buffer.push(line);
      continue;
    }

    buffer.push(line);
    if (isFenceClosing(line, fenceMarkerChar, fenceMarkerLength)) {
      pushBuffer('code');
      inFence = false;
    }
  }

  pushBuffer(inFence ? 'code' : 'text');
  return segments;
};

const normalizeMathDelimitersInText = (markdown: string): string => {
  const withDisplayMath = markdown.replace(
    BRACKET_DISPLAY_MATH_BLOCK_REGEX,
    (_match, leadingNewline: string, rawBody: string) =>
      `${leadingNewline}\n\n$$\n${rawBody.trim()}\n$$\n`
  );
  return withDisplayMath.replace(
    INLINE_PAREN_MATH_REGEX,
    (_match, prefix: string, rawBody: string) => `${prefix}$${rawBody}$`
  );
};

const normalizeMathDelimiters = (markdown: string): string => {
  const segments = splitByFencedCodeBlocks(markdown);
  return segments
    .map((segment) =>
      segment.type === 'text'
        ? normalizeMathDelimitersInText(segment.content)
        : segment.content
    )
    .join('\n');
};

export const EnrichedMarkdownText = ({
  markdown,
  markdownStyle = {},
  containerStyle,
  onLinkPress,
  onLinkLongPress,
  onTaskListItemPress,
  enableLinkPreview,
  selectable = true,
  md4cFlags = defaultMd4cFlags,
  allowFontScaling = true,
  maxFontSizeMultiplier,
  allowTrailingMargin = false,
  flavor = 'commonmark',
  ...rest
}: EnrichedMarkdownTextProps) => {
  const normalizedMarkdown = useMemo(
    () => normalizeMathDelimiters(markdown),
    [markdown]
  );
  const normalizedStyle = useMemo(
    () => normalizeMarkdownStyle(markdownStyle),
    [markdownStyle]
  );

  const normalizedMd4cFlags = useMemo(
    () => ({
      underline: md4cFlags.underline ?? false,
    }),
    [md4cFlags]
  );

  const handleLinkPress = useCallback(
    (e: NativeSyntheticEvent<LinkPressEvent>) => {
      const { url } = e.nativeEvent;
      onLinkPress?.({ url });
    },
    [onLinkPress]
  );

  const handleLinkLongPress = useCallback(
    (e: NativeSyntheticEvent<LinkLongPressEvent>) => {
      const { url } = e.nativeEvent;
      onLinkLongPress?.({ url });
    },
    [onLinkLongPress]
  );

  const handleTaskListItemPress = useCallback(
    (e: NativeSyntheticEvent<TaskListItemPressEvent>) => {
      const { index, checked, text } = e.nativeEvent;
      onTaskListItemPress?.({ index, checked, text });
    },
    [onTaskListItemPress]
  );

  const sharedProps = {
    markdown: normalizedMarkdown,
    markdownStyle: normalizedStyle,
    onLinkPress: handleLinkPress,
    onLinkLongPress: handleLinkLongPress,
    onTaskListItemPress: handleTaskListItemPress,
    enableLinkPreview: onLinkLongPress == null && (enableLinkPreview ?? true),
    selectable,
    md4cFlags: normalizedMd4cFlags,
    allowFontScaling,
    maxFontSizeMultiplier,
    allowTrailingMargin,
    style: containerStyle,
    ...rest,
  };

  if (flavor === 'github') {
    return <EnrichedMarkdownNativeComponent {...sharedProps} />;
  }

  return <EnrichedMarkdownTextNativeComponent {...sharedProps} />;
};

export default EnrichedMarkdownText;
