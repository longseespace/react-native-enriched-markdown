#pragma once
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@class StyleConfig;

NS_ASSUME_NONNULL_BEGIN

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Generates semantic HTML from NSAttributedString with inline styles.
 * Produces clean HTML that works in email clients without external stylesheets.
 */
NSString *_Nullable generateHTML(NSAttributedString *attributedString, StyleConfig *styleConfig);

#ifdef __cplusplus
}
#endif

NS_ASSUME_NONNULL_END
