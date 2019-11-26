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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowNode;
import dataflow.model.ParameterList;

/**
 * Builder for {@link DataFlowGraph}, only to be used for test purposes.
 *
 * @author User
 */
public class GraphBuilder {

  private List<NodeBuilder> startNodes = new ArrayList<>();

  public static GraphBuilder withStartingNodes(NodeBuilder... nodes) {
    GraphBuilder graphBuilder = new GraphBuilder();
    Arrays.stream(nodes).map(NodeBuilder::getRoots).flatMap(List::stream).distinct().forEach(graphBuilder.startNodes::add);
    return graphBuilder;
  }

  public DataFlowGraph build() {
    DataFlowGraph graph = new DataFlowGraph();
    Map<String, DataFlowNode> nodes = new HashMap<>();
    Map<String, DataFlowMethod> methods = new HashMap<>();
    List<DataFlowNode> addedNodes = new ArrayList<>();
    startNodes.forEach(node -> this.addNode(graph, node, nodes, methods, addedNodes));
    return graph;
  }

  private void addNode(DataFlowGraph graph, NodeBuilder nodeBuilder, Map<String, DataFlowNode> nodes, Map<String, DataFlowMethod> methods,
      List<DataFlowNode> addedNodes) {
    addNode(graph, nodeBuilder, nodes, methods, null, null, addedNodes);
  }

  /**
   * Recursively adds a new nodes to the given {@link DataFlowGraph}. This method should only be called for nodes not already added.
   *
   * @param graph The {@link DataFlowGraph} to add Nodes to.
   * @param nodeBuilder The next Node to add.
   * @param nodes Previously added nodes.
   * @param methods previously added methods.
   * @param previousNode The node that was added right before the given input node, can be null.
   * @param currentMethod The current method that is being added.
   * @param addNodes The nodes added in previous iterations. If a node is present, it will not be added to the graph again. Note that this has to be a list,
   *          because the hash value might change in multiple iterations.
   */
  private void addNode(DataFlowGraph graph, NodeBuilder nodeBuilder, Map<String, DataFlowNode> nodes, Map<String, DataFlowMethod> methods,
      DataFlowNode previousNode, DataFlowMethod currentMethod, List<DataFlowNode> addedNodes) {

    DataFlowNode node = nodeBuilder.getOrBuild();
    if (previousNode != null) {
      // Always add the node to the previous node
      previousNode.addEdgeTo(node);
    }
    if (addedNodes.contains(node)) {
      // Don't add a node that was already added.
      return;
    }

    DataFlowMethod method = null;
    switch (nodeBuilder.getType()) {
    case CLASS_FIELD:
      graph.addField(node);
      if (currentMethod != null) {
        currentMethod.addChangedField(node);
      }
      break;
    case METHOD_PARAMETER:
      method = getOrCreateMethod(graph, methods, nodeBuilder.getMethod());
      if (method.getInputParameters() == null) {
        method.setInputParameters(ParameterList.builder().build());
      }
      // TODO if we want to influence the order of the parameters,
      // we need to create a NodeBuilder.ofParameter method with a parameter index as input and handle it here.
      method.addParameter(node);
      break;
    case RETURN:
      method = getOrCreateMethod(graph, methods, nodeBuilder.getMethod());

      method.addNode(node);

      DataFlowNode methodReturn = getOrCreateReturnNode(method);
      node.addEdgeTo(methodReturn);

      break;
    case IN_BETWEEN:
      currentMethod.addNode(node);
      break;
    default:
      // Do nothing
    }

    DataFlowMethod nextMethod = method == null ? currentMethod : method;
    addedNodes.add(node);
    nodeBuilder.getOut().forEach(nb -> addNode(graph, nb, nodes, methods, node, nextMethod, addedNodes));
  }

  private DataFlowNode getOrCreateReturnNode(DataFlowMethod method) {
    if (!method.getReturnNode().isPresent()) {
      method.setReturnNode(new DataFlowNode(method.getName() + "_return", method.getRepresentedNode()));
    }
    return method.getReturnNode().get();
  }

  private DataFlowMethod getOrCreateMethod(DataFlowGraph graph, Map<String, DataFlowMethod> methods, String methodName) {
    if (!methods.containsKey(methodName)) {
      CallableDeclaration<?> node = new MethodDeclaration(NodeList.nodeList(Modifier.publicModifier()), new ClassOrInterfaceType(), methodName);
      DataFlowMethod method = new DataFlowMethod(methodName, node);
      graph.addMethod(method);
      methods.put(methodName, method);
    }
    return methods.get(methodName);
  }

}
