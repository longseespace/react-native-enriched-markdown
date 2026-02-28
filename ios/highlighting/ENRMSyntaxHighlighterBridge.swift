import Foundation
import UIKit

@objc(ENRMSyntaxHighlighterBridge)
public final class ENRMSyntaxHighlighterBridge: NSObject {
  private static let lock = NSLock()
  private static var highlighter: Highlighter?
  private static var currentThemeName: String?
  private static var currentFontSignature: String?

  @objc public static func highlightedCode(
    _ code: String,
    language: String?,
    font: UIFont,
    fallbackColor: UIColor,
    usesDarkBackground: Bool
  ) -> NSAttributedString? {
    guard !code.isEmpty else {
      return nil
    }

    lock.lock()
    defer { lock.unlock() }

    if highlighter == nil {
      highlighter = Highlighter()
    }

    guard let highlighter else {
      return nil
    }

    let themeName = usesDarkBackground ? "github-dark" : "github"
    let fontSignature = "\(font.fontName)-\(font.pointSize)-\(themeName)"
    if currentThemeName != themeName || currentFontSignature != fontSignature {
      let didSetTheme = highlighter.setTheme(themeName, withFont: font.fontName, ofSize: font.pointSize)
      if !didSetTheme {
        _ = highlighter.setTheme("default", withFont: font.fontName, ofSize: font.pointSize)
      }
      highlighter.theme.lineSpacing = 0
      highlighter.theme.paraSpacing = 0
      currentThemeName = themeName
      currentFontSignature = fontSignature
    }

    let normalizedLanguage = normalizeLanguage(language)
    guard let highlighted = highlighter.highlight(code, as: normalizedLanguage, doFastRender: true) else {
      return nil
    }

    let mutable = NSMutableAttributedString(attributedString: highlighted)
    let fullRange = NSRange(location: 0, length: mutable.length)
    mutable.enumerateAttribute(.foregroundColor, in: fullRange, options: []) { value, range, _ in
      if value == nil {
        mutable.addAttribute(.foregroundColor, value: fallbackColor, range: range)
      }
    }
    return mutable
  }

  private static func normalizeLanguage(_ language: String?) -> String? {
    guard let raw = language?.trimmingCharacters(in: .whitespacesAndNewlines), !raw.isEmpty else {
      return nil
    }

    let lower = raw.lowercased()
    switch lower {
      case "js":
        return "javascript"
      case "ts":
        return "typescript"
      case "tsx":
        return "tsx"
      case "jsx":
        return "jsx"
      case "sh":
        return "bash"
      case "shell":
        return "bash"
      case "yml":
        return "yaml"
      case "objc":
        return "objectivec"
      case "obj-c":
        return "objectivec"
      case "c++":
        return "cpp"
      case "c#":
        return "csharp"
      default:
        return lower
    }
  }
}
