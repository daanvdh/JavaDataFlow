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

import java.util.Optional;

import com.github.javaparser.ast.Node;

/**
 * Interface for {@link DataFlowGraph} classes that own one or more {@link DataFlowNode}s. This interface contains a self reference since a node can be owned by
 * a {@link DataFlowMethod}, and that method can then be owned by a graph.
 *
 * @author Daan
 */
public abstract class OwnedNode<T extends Node> extends NodeRepresenter<T> {

  protected OwnedNode() {
    super();
  }

  public OwnedNode(T representedNode) {
    super(representedNode);
  }

  public OwnedNode(String name, T representedNode) {
    super(name, representedNode);
  }

  public OwnedNode(NodeRepresenter.Builder<T, ?> builder) {
    super(builder);
  }

  public OwnedNode(String name) {
    super(name);
  }

  /**
   * @return An optional of the {@link OwnedNode} of this node. The optional will be empty in case of a {@link DataFlowGraph} representing a non inner class or
   *         a method for which the rest of the graph was not parsed.
   */
  public abstract Optional<OwnedNode<?>> getOwner();

}
