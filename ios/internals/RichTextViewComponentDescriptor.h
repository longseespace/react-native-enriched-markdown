#pragma once
#include "RichTextViewShadowNode.h"
#include <react/debug/react_native_assert.h>
#include <react/renderer/core/ConcreteComponentDescriptor.h>

namespace facebook::react {

class RichTextViewComponentDescriptor final : public ConcreteComponentDescriptor<RichTextViewShadowNode> {
public:
  using ConcreteComponentDescriptor::ConcreteComponentDescriptor;

  void adopt(ShadowNode &shadowNode) const override {
    react_native_assert(dynamic_cast<RichTextViewShadowNode *>(&shadowNode));
    ConcreteComponentDescriptor::adopt(shadowNode);
  }
};

} // namespace facebook::react
