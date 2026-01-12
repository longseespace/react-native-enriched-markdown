#pragma once

#include "RichTextMeasurementManager.h"
#include "RichTextShadowNode.h"

#include <react/renderer/core/ConcreteComponentDescriptor.h>

namespace facebook::react {

class RichTextComponentDescriptor final : public ConcreteComponentDescriptor<RichTextShadowNode> {
public:
  RichTextComponentDescriptor(const ComponentDescriptorParameters &parameters)
      : ConcreteComponentDescriptor(parameters),
        measurementsManager_(std::make_shared<RichTextMeasurementManager>(contextContainer_)) {}

  void adopt(ShadowNode &shadowNode) const override {
    ConcreteComponentDescriptor::adopt(shadowNode);
    auto &richTextShadowNode = static_cast<RichTextShadowNode &>(shadowNode);

    // RichTextShadowNode uses RichTextMeasurementManager
    // to provide measurements to Yoga.
    richTextShadowNode.setMeasurementsManager(measurementsManager_);
  }

private:
  const std::shared_ptr<RichTextMeasurementManager> measurementsManager_;
};

} // namespace facebook::react
