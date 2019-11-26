/*
 * Copyright (c) 2019 by Eyefreight BV (www.eyefreight.com). All rights reserved.
 *
 * This software is provided by the copyright holder and contributors "as is" and any express or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall
 * Eyefreight BV or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services; * loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including
 * negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
 */
package dataflow.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.github.javaparser.ast.Node;

/**
 * A {@link OwnedNode} that owns other {@link OwnedNode}s. For instance a method owning a return node.
 *
 * @author Daan
 */
public abstract class OwnerNode<T extends Node> extends OwnedNode<T> {

  public OwnerNode() {
    // empty constructor which would otherwise be invisible due to the constructor receiving the builder.
  }

  public OwnerNode(OwnedNode.Builder<T, ?> builder) {
    super(builder);
  }

  public OwnerNode(String name, T representedNode) {
    super(name, representedNode);
  }

  /**
   * True when this owner is either a direct owner or is an indirect owner of the input node.
   *
   * @param node The {@link OwnedNode} to check if it's owned by this.
   * @return true if this owns it, false otherwise.
   */
  public boolean owns(DataFlowNode node) {
    return getOwnedNodes().contains(node);
  }

  /**
   * Gets all direct or indirectly owned nodes.
   *
   * @return {@link Set} of {@link DataFlowNode}.
   */
  public final Set<DataFlowNode> getOwnedNodes() {
    Set<DataFlowNode> nodes = new HashSet<>(getDirectOwnedNodes());
    getOwnedOwners().stream().map(OwnerNode::getOwnedNodes).forEach(nodes::addAll);
    return nodes;
  }

  /**
   * @return all nodes directly owned by this {@link OwnerNode} for which it holds that
   */
  abstract Collection<OwnerNode<?>> getOwnedOwners();

  /**
   * @return all {@link DataFlowNode}s directly owned by this {@link OwnerNode}.
   */
  abstract Collection<DataFlowNode> getDirectOwnedNodes();

}
