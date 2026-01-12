#pragma once

#include "RichTextMeasurementManager.h"
#include "RichTextState.h"

#include <react/renderer/components/RichTextViewSpec/EventEmitters.h>
#include <react/renderer/components/RichTextViewSpec/Props.h>
#include <react/renderer/components/view/ConcreteViewShadowNode.h>

namespace facebook::react {

JSI_EXPORT extern const char RichTextComponentName[];

/*
 * `ShadowNode` for <RichTextView> component.
 */
class RichTextShadowNode final
    : public ConcreteViewShadowNode<RichTextComponentName, RichTextViewProps, RichTextViewEventEmitter, RichTextState> {
public:
  using ConcreteViewShadowNode::ConcreteViewShadowNode;

  // This constructor is called when we "update" shadow node, e.g. after
  // updating shadow node's state
  RichTextShadowNode(ShadowNode const &sourceShadowNode, ShadowNodeFragment const &fragment)
      : ConcreteViewShadowNode(sourceShadowNode, fragment) {
    dirtyLayoutIfNeeded();
  }

  static ShadowNodeTraits BaseTraits() {
    auto traits = ConcreteViewShadowNode::BaseTraits();
    traits.set(ShadowNodeTraits::Trait::LeafYogaNode);
    traits.set(ShadowNodeTraits::Trait::MeasurableYogaNode);
    return traits;
  }

  // Associates a shared `RichTextMeasurementManager` with the node.
  void setMeasurementsManager(const std::shared_ptr<RichTextMeasurementManager> &measurementsManager);

  void dirtyLayoutIfNeeded();

  Size measureContent(const LayoutContext &layoutContext, const LayoutConstraints &layoutConstraints) const override;

private:
  int forceHeightRecalculationCounter_;
  std::shared_ptr<RichTextMeasurementManager> measurementsManager_;
};

} // namespace facebook::react
