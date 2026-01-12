#import "RichTextViewShadowNode.h"
#import "RichTextView.h"
#import <react/utils/ManagedObjectWrapper.h>
#import <yoga/Yoga.h>

namespace facebook::react {

extern const char RichTextViewComponentName[] = "RichTextView";

RichTextViewShadowNode::RichTextViewShadowNode(const ShadowNodeFragment &fragment,
                                               const ShadowNodeFamily::Shared &family, ShadowNodeTraits traits)
    : ConcreteViewShadowNode(fragment, family, traits)
{
}

RichTextViewShadowNode::RichTextViewShadowNode(const ShadowNode &sourceShadowNode, const ShadowNodeFragment &fragment)
    : ConcreteViewShadowNode(sourceShadowNode, fragment)
{
  dirtyLayoutIfNeeded();
}

void RichTextViewShadowNode::dirtyLayoutIfNeeded()
{
  const auto state = this->getStateData();
  const int receivedCounter = state.getHeightRecalculationCounter();

  if (receivedCounter > localHeightRecalculationCounter_) {
    localHeightRecalculationCounter_ = receivedCounter;
    YGNodeMarkDirty(&yogaNode_);
  }
}

Size RichTextViewShadowNode::measureContent(const LayoutContext &layoutContext,
                                            const LayoutConstraints &layoutConstraints) const
{
  CGFloat maxWidth = layoutConstraints.maximumSize.width;
  CGFloat maxHeight = layoutConstraints.maximumSize.height;

  // Get view reference from state
  RCTInternalGenericWeakWrapper *weakWrapper =
      (RCTInternalGenericWeakWrapper *)unwrapManagedObject(getStateData().getComponentViewRef());
  RichTextView *view = weakWrapper ? (RichTextView *)weakWrapper.object : nil;

  if (!view) {
    return {maxWidth, MIN(20.0, maxHeight)};
  }

  // Measure on main thread
  __block CGSize size;
  if ([NSThread isMainThread]) {
    size = [view measureSize:maxWidth];
  } else {
    dispatch_sync(dispatch_get_main_queue(), ^{ size = [view measureSize:maxWidth]; });
  }

  return {size.width, MIN(size.height, maxHeight)};
}

} // namespace facebook::react
