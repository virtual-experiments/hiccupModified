package interactivehicupp;

import hicupp.classify.ClassNode;

interface NodeViewFactory {
  NodeView createNodeView(SplitView parent, ClassNode classNode);
}
