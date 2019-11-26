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
package dataflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.printer.Printable;

import dataflow.model.DataFlowNode;
import dataflow.model.OwnedNode;

/**
 * Factory for {@link DataFlowNode}s.
 *
 * @author Daan
 */
public class DataFlowNodeFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DataFlowNodeFactory.class);

  public DataFlowNode create(Node n, OwnedNode<?> owner) {
    DataFlowNode.Builder builder = DataFlowNode.builder().representedNode(n).owner(owner);
    Node nodeWithName = n;
    if (n instanceof ReturnStmt) {
      nodeWithName = ((ReturnStmt) n).getExpression().orElse(null);
    }

    if (nodeWithName instanceof NodeWithSimpleName) {
      builder.name(((NodeWithSimpleName<?>) nodeWithName).getNameAsString());
    } else if (nodeWithName instanceof Printable) {
      builder.name(((Printable) nodeWithName).asString());
    } else {
      LOG.warn("Not supported to add a name to a created DataFlowNode for node of type {}, input node is {}", n.getClass(), n);
    }

    // TODO set the type
    // builder.type(null);

    // for (int i = 0; i < resolved.getNumberOfParams(); i++) {
    // ResolvedParameterDeclaration p = resolved.getParam(i);
    // DataFlowNode newNode =
    // DataFlowNode.builder().name(p.getName()).type(p.getType().describe().toString()).representedNode(node.getArgument(i)).owner(params).build();
    // params.add(newNode);
    // }

    return builder.build();
  }

}
