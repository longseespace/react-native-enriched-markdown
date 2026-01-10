#pragma once
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@class StyleConfig;

NS_ASSUME_NONNULL_BEGIN

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Builds a customized edit menu that replaces the native Copy with an enhanced version
 * supporting multiple formats (plain text, RTF, RTFD, HTML, Markdown).
 *
 * Also adds "Copy as Markdown" and "Copy Image URL" options when applicable.
 */
UIMenu *buildEditMenuForSelection(NSAttributedString *attributedText, NSRange range, NSString *_Nullable cachedMarkdown,
                                  StyleConfig *styleConfig, NSArray<UIMenuElement *> *suggestedActions)
    API_AVAILABLE(ios(16.0));

#ifdef __cplusplus
}
#endif

NS_ASSUME_NONNULL_END
