import {
  codegenNativeComponent,
  type ViewProps,
  type CodegenTypes,
  type ColorValue,
} from 'react-native';

export interface HeaderConfig {
  /**
   * Header scaling factor relative to base fontSize.
   * @default 2.0
   * @example
   * fontSize=18, scale=2.0 → H1=30pt, H2=28pt, H6=20pt
   */
  scale?: CodegenTypes.Double;
  /**
   * Make headers bold.
   * @default true
   * @note fontFamily takes precedence over this setting
   */
  isBold?: boolean;
}

interface NativeProps extends ViewProps {
  /**
   * Markdown content to render.
   * Supports standard markdown syntax including headers, links, lists, etc.
   */
  markdown?: string;
  /**
   * Base font size for all text elements (in points).
   * - Regular text, links, lists: Use fontSize directly
   * - Headers: Scaled relative to fontSize using headerConfig.scale
   * @example
   * fontSize=18 → text=18pt, H1=30pt, H2=28pt, H6=20pt
   */
  fontSize?: CodegenTypes.Int32;
  /**
   * Font family name for all text elements.
   * @note Takes precedence over headerConfig.isBold for boldness
   */
  fontFamily?: string;
  /**
   * Font weight for all text elements.
   * @example "normal", "bold", "100", "200", "300", "400", "500", "600", "700", "800", "900"
   */
  fontWeight?: string;
  /**
   * Font style for all text elements.
   * @example "normal", "italic"
   */
  fontStyle?: string;
  /**
   * Text color in hex format.
   */
  color?: ColorValue;
  /**
   * Header configuration for scaling and boldness.
   */
  headerConfig?: HeaderConfig;
  /**
   * Callback fired when a link is pressed.
   * Receives the URL that was tapped.
   */
  onLinkPress?: CodegenTypes.BubblingEventHandler<{ url: string }>;
}

export default codegenNativeComponent<NativeProps>('RichTextView');
