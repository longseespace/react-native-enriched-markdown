#pragma once

#include "MarkdownASTNode.hpp"
#include <string>
#include <memory>

namespace Markdown {

class MD4CParser {
public:
    MD4CParser();
    ~MD4CParser();

    // Parse markdown string and return AST root node
    std::shared_ptr<MarkdownASTNode> parse(const std::string& markdown);

private:
    class Impl;
    std::unique_ptr<Impl> impl_;
};

} // namespace Markdown

