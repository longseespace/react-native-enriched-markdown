import Foundation
import SwiftMath
import UIKit

@objc(ENRMMathBridge)
public final class ENRMMathBridge: NSObject {
  private static let lock = NSLock()
  private static let cache = NSCache<NSString, UIImage>()

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

    let image: UIImage
    if let cached = cache.object(forKey: key) {
      image = cached
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
      image = rendered
      cache.setObject(image, forKey: key)
    }

    let attachment = NSTextAttachment()
    attachment.image = image
    if isDisplayMode {
      attachment.bounds = CGRect(origin: .zero, size: image.size)
    } else {
      let baselineOffset = -max(1, floor(image.size.height * 0.18))
      attachment.bounds = CGRect(x: 0, y: baselineOffset, width: image.size.width, height: image.size.height)
    }

    return NSAttributedString(attachment: attachment)
  }
}

private extension UIColor {
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
