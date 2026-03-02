import Foundation
import SwiftMath
import UIKit

@objc(ENRMMathAttachment)
public final class ENRMMathAttachment: NSTextAttachment {
  private let displayImage: UIImage
  private let exportImage: UIImage
  private let isDisplayMode: Bool

  @objc public let latexSource: String
  @objc public let isDisplayMath: Bool

  init(
    displayImage: UIImage,
    exportImage: UIImage,
    isDisplayMode: Bool,
    latexSource: String
  ) {
    self.displayImage = displayImage
    self.exportImage = exportImage
    self.isDisplayMode = isDisplayMode
    self.latexSource = latexSource
    self.isDisplayMath = isDisplayMode
    super.init(data: nil, ofType: nil)
    // Keep export image on the attachment so iOS "Save to Photos" has visible pixels.
    self.image = exportImage
  }

  required init?(coder: NSCoder) {
    return nil
  }

  override public func attachmentBounds(
    for textContainer: NSTextContainer?,
    proposedLineFragment lineFrag: CGRect,
    glyphPosition position: CGPoint,
    characterIndex charIndex: Int
  ) -> CGRect {
    if isDisplayMode {
      return CGRect(origin: .zero, size: displayImage.size)
    }
    return bounds
  }

  override public func image(
    forBounds imageBounds: CGRect,
    textContainer: NSTextContainer?,
    characterIndex charIndex: Int
  ) -> UIImage? {
    // UIKit export actions (copy/share/save) may request attachment image without
    // a text container. Use a non-transparent export asset to avoid white/blank saves.
    if textContainer == nil {
      return exportImage
    }
    return displayImage
  }
}

private final class ENRMMathRenderPayload: NSObject {
  let displayImage: UIImage
  let exportImage: UIImage

  init(displayImage: UIImage, exportImage: UIImage) {
    self.displayImage = displayImage
    self.exportImage = exportImage
  }
}

@objc(ENRMMathBridge)
public final class ENRMMathBridge: NSObject {
  private static let lock = NSLock()
  private static let cache = NSCache<NSString, ENRMMathRenderPayload>()

  @objc public static func attributedMath(
    _ latex: String,
    fontSize: CGFloat,
    textColor: UIColor,
    isDisplayMode: Bool
  ) -> NSAttributedString? {
    let trimmed = latex.trimmingCharacters(in: .whitespacesAndNewlines)
    guard !trimmed.isEmpty else {
      return nil
    }

    let effectiveSize = max(fontSize, 12)
    let key = "\(trimmed)|\(effectiveSize)|\(textColor.hexKey)|\(isDisplayMode ? "display" : "inline")" as NSString

    lock.lock()
    defer { lock.unlock() }

    let payload: ENRMMathRenderPayload
    if let cached = cache.object(forKey: key) {
      payload = cached
    } else {
      var mathImage = MathImage(
        latex: trimmed,
        fontSize: effectiveSize,
        textColor: textColor,
        labelMode: isDisplayMode ? .display : .text,
        textAlignment: isDisplayMode ? .center : .left
      )
      if isDisplayMode {
        let verticalInset = max(4, floor(effectiveSize * 0.22))
        mathImage.contentInsets = MTEdgeInsets(
          top: verticalInset,
          left: 2,
          bottom: verticalInset,
          right: 2
        )
      }
      let (_, rendered, _) = mathImage.asImage()
      guard let rendered else {
        return nil
      }

      let exportImage = makeExportImage(from: rendered, textColor: textColor)
      payload = ENRMMathRenderPayload(displayImage: rendered, exportImage: exportImage)
      cache.setObject(payload, forKey: key)
    }

    let attachment = ENRMMathAttachment(
      displayImage: payload.displayImage,
      exportImage: payload.exportImage,
      isDisplayMode: isDisplayMode,
      latexSource: trimmed
    )

    if isDisplayMode {
      attachment.bounds = CGRect(origin: .zero, size: payload.displayImage.size)
    } else {
      let baselineOffset = -max(1, floor(payload.displayImage.size.height * 0.18))
      attachment.bounds = CGRect(
        x: 0,
        y: baselineOffset,
        width: payload.displayImage.size.width,
        height: payload.displayImage.size.height
      )
    }

    return NSAttributedString(attachment: attachment)
  }

  private static func makeExportImage(from rendered: UIImage, textColor: UIColor) -> UIImage {
    let shouldUseDarkBackground = textColor.relativeLuminance > 0.65
    let backgroundColor = shouldUseDarkBackground ? UIColor.black : UIColor.white
    let format = UIGraphicsImageRendererFormat.default()
    format.opaque = true
    format.scale = rendered.scale
    let renderer = UIGraphicsImageRenderer(size: rendered.size, format: format)

    return renderer.image { context in
      backgroundColor.setFill()
      context.cgContext.fill(CGRect(origin: .zero, size: rendered.size))
      rendered.draw(in: CGRect(origin: .zero, size: rendered.size))
    }
  }
}

private extension UIColor {
  var relativeLuminance: CGFloat {
    var red: CGFloat = 0
    var green: CGFloat = 0
    var blue: CGFloat = 0
    var alpha: CGFloat = 0

    if getRed(&red, green: &green, blue: &blue, alpha: &alpha) {
      return 0.2126 * red + 0.7152 * green + 0.0722 * blue
    }

    guard let components = cgColor.components, components.count >= 3 else {
      return 0
    }
    return 0.2126 * components[0] + 0.7152 * components[1] + 0.0722 * components[2]
  }

  var hexKey: String {
    var red: CGFloat = 0
    var green: CGFloat = 0
    var blue: CGFloat = 0
    var alpha: CGFloat = 0
    guard getRed(&red, green: &green, blue: &blue, alpha: &alpha) else {
      return "000000ff"
    }

    return String(
      format: "%02x%02x%02x%02x",
      Int(red * 255.0),
      Int(green * 255.0),
      Int(blue * 255.0),
      Int(alpha * 255.0)
    )
  }
}
