#pragma once
#include "RichTextViewState.h"
#include <react/renderer/components/RichTextViewSpec/EventEmitters.h>
#include <react/renderer/components/RichTextViewSpec/Props.h>
#include <react/renderer/components/view/ConcreteViewShadowNode.h>
#include <react/renderer/core/LayoutConstraints.h>

namespace facebook::react {

JSI_EXPORT extern const char RichTextViewComponentName[];

/// ShadowNode implementing measureContent for automatic height calculation.
class RichTextViewShadowNode : public ConcreteViewShadowNode<RichTextViewComponentName, RichTextViewProps,
                                                             RichTextViewEventEmitter, RichTextViewState> {
public:
  using ConcreteViewShadowNode::ConcreteViewShadowNode;

  RichTextViewShadowNode(const ShadowNodeFragment &fragment, const ShadowNodeFamily::Shared &family,
                         ShadowNodeTraits traits);

  RichTextViewShadowNode(const ShadowNode &sourceShadowNode, const ShadowNodeFragment &fragment);

  void dirtyLayoutIfNeeded();

  Size measureContent(const LayoutContext &layoutContext, const LayoutConstraints &layoutConstraints) const override;

  static ShadowNodeTraits BaseTraits() {
    auto traits = ConcreteViewShadowNode::BaseTraits();
    traits.set(ShadowNodeTraits::Trait::LeafYogaNode);
    traits.set(ShadowNodeTraits::Trait::MeasurableYogaNode);
    return traits;
  }

private:
  int localHeightRecalculationCounter_{0};
};

} // namespace facebook::react
