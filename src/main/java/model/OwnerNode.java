/*
 * Copyright 2018 by Daan van den Heuvel.
 *
 * This file is part of JavaDataFlow.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package model;

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
