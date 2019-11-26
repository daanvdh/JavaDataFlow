/*
 * Copyright 2019 by Daan van den Heuvel.
 *
 * This file is part of JavaForger.
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
package dataflow;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import dataflow.model.DataFlowEdge;
import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowNode;

/**
 * Service for methods to be executed on a {@link DataFlowGraph}.
 *
 * @author Daan
 */
public class GraphUtil {

  public static List<DataFlowNode> walkBackUntil(List<DataFlowNode> nodes, Predicate<DataFlowNode> predicate, Predicate<DataFlowNode> scopePredicate) {
    return nodes.stream().map(n -> walkBackUntil(n, predicate, scopePredicate)).flatMap(List::stream).collect(Collectors.toList());
  }

  /**
   * Walks back via {@link DataFlowNode#getIn()} until for each node it holds that the predicate holds. The input {@link DataFlowNode} will be returned if the
   * {@link Predicate} holds for it. An empty list will be returned if the scopePredicate does not hold for the input node.
   *
   * @param dfn The input {@link DataFlowNode}
   * @param predicate The {@link Predicate} to check on the {@link DataFlowNode}
   * @param scopePredicate This predicate determines the scope for when to stop searching. If this predicate does not hold for the input node an empty list will
   *          be returned.
   * @return Returns a list of nodes that either have no incoming edges, or for which the predicate holds.
   */
  public static List<DataFlowNode> walkBackUntil(DataFlowNode dfn, Predicate<DataFlowNode> predicate, Predicate<DataFlowNode> scopePredicate) {
    if (!scopePredicate.test(dfn)) {
      return Collections.emptyList();
    }
    if (predicate.test(dfn)) {
      return Collections.singletonList(dfn);
    }
    return dfn.getIn().stream().map(DataFlowEdge::getFrom).map(node -> walkBackUntil(node, predicate, scopePredicate)).flatMap(List::stream)
        .collect(Collectors.toList());
  }

  /**
   * Walks forward via {@link DataFlowNode#getOut()} until for each node it holds that the predicate holds. The input {@link DataFlowNode} will be returned if
   * the {@link Predicate} holds for it. An empty list will be returned if the scopePredicate does not hold for the input node.
   *
   * @param dfn The input {@link DataFlowNode}
   * @param predicate The {@link Predicate} to check on the {@link DataFlowNode}
   * @param scopePredicate This predicate determines the scope for when to stop searching. If this predicate does not hold for the input node an empty list will
   *          be returned.
   * @return Returns a list of nodes that either have no incoming edges, or for which the predicate holds.
   */
  public static List<DataFlowNode> walkForwardUntil(DataFlowNode dfn, Predicate<DataFlowNode> predicate, Predicate<DataFlowNode> scopePredicate) {
    if (!scopePredicate.test(dfn)) {
      return Collections.emptyList();
    }
    if (predicate.test(dfn)) {
      return Collections.singletonList(dfn);
    }
    return dfn.getOut().stream().map(DataFlowEdge::getTo).map(node -> walkForwardUntil(node, predicate, scopePredicate)).flatMap(List::stream)
        .collect(Collectors.toList());

  }

}
