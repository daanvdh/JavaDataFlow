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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;

import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowNode;
import dataflow.model.NodeCall;
import dataflow.model.OwnedNode;
import dataflow.model.ParameterList;

/**
 * Factory for creating a {@link DataFlowGraph} from a {@link JavaParser} {@link CompilationUnit}.
 *
 * @author Daan
 */
public class DataFlowGraphFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DataFlowGraphFactory.class);

  private MethodNodeHandler nodeHandler = new MethodNodeHandler();
  private DataFlowNodeFactory dfnFactory = new DataFlowNodeFactory();

  /**
   * Creates a {@link DataFlowGraph} for the given {@link CompilationUnit}.
   *
   * @param cu The {@link CompilationUnit} containing the parsed class.
   * @return A {@link DataFlowGraph}
   */
  public DataFlowGraph create(CompilationUnit cu) {
    DataFlowGraph graph = DataFlowGraph.builder().build();
    Optional<ClassOrInterfaceDeclaration> representedNode = cu.findFirst(ClassOrInterfaceDeclaration.class);
    if (representedNode.isPresent()) {
      graph.setRepresentedNode(representedNode.get());
      representedNode.map(ClassOrInterfaceDeclaration::getNameAsString).ifPresent(graph::setName);
    }
    executeForEachChildNode(cu, (node) -> this.addField(graph, node));
    executeForEachChildNode(cu, (node) -> this.createMethod(graph, node));
    executeForEachChildNode(cu, (node) -> this.fillMethod(graph, node));
    connectMethods(graph);
    return graph;
  }

  private void executeForEachChildNode(CompilationUnit cu, Consumer<Node> consumer) {
    for (TypeDeclaration<?> type : cu.getTypes()) {
      List<Node> childNodes = type.getChildNodes();
      for (Node node : childNodes) {
        consumer.accept(node);
      }
    }
  }

  private void addField(DataFlowGraph graph, Node node) {
    if (node instanceof FieldDeclaration) {
      parseField((FieldDeclaration) node, graph).forEach(graph::addField);
    }
  }

  private DataFlowMethod createMethod(DataFlowGraph graph, Node node) {
    DataFlowMethod method = null;
    if (node instanceof CallableDeclaration) {
      CallableDeclaration<?> cd = (CallableDeclaration<?>) node;
      method = new DataFlowMethod(graph, cd, cd.getNameAsString());
      List<DataFlowNode> dfnParameters = parseParameters(cd, method);
      ParameterList paramList = ParameterList.builder().nodes(dfnParameters).owner(method).build();
      method.setInputParameters(paramList);
      if (node instanceof MethodDeclaration) {
        MethodDeclaration md = (MethodDeclaration) node;
        if (!(md.getType() instanceof VoidType)) {
          method.setReturnNode(new DataFlowNode(cd.getNameAsString() + "_return", node));
        }
      } else {
        // Always add a return statement for a constructor.
        method.setReturnNode(new DataFlowNode(node));
      }
    }
    return method;
  }

  private void fillMethod(DataFlowGraph graph, Node node) {
    if (node instanceof MethodDeclaration) {
      MethodDeclaration md = (MethodDeclaration) node;
      parseCallable(graph, md);
    }
  }

  /**
   * Connects all method calls to methods inside this graph to each other.
   *
   * @param graph The graph to connect the methods from.
   */
  private void connectMethods(DataFlowGraph graph) {
    // TODO probably best to extract this to another class.
    for (DataFlowMethod method : graph.getMethods()) {
      for (NodeCall call : method.getNodeCalls()) {
        Node node = call.getRepresentedNode();
        Object resolved = resolve(method, node);
        if (resolved instanceof JavaParserMethodDeclaration) {
          MethodDeclaration resolvedNode = ((JavaParserMethodDeclaration) resolved).getWrappedNode();
          DataFlowMethod resolvedMethod = graph.getMethod(resolvedNode);
          if (resolvedMethod != null) {
            call.setCalledMethod(resolvedMethod);
          } else {
            // TODO handle connecting to other graphs
          }
        } else {
          LOG.warn("In method {}, Connecting methods of type {} is not supported, the node that was not connected is: {}", method.getName(),
              resolved == null ? null : resolved.getClass(), node);
        }
      }
    }
  }

  private Object resolve(DataFlowMethod method, Node node) {
    if (!Resolvable.class.isAssignableFrom(node.getClass())) {
      // LOG.warn("In method {}, node is not Resolvable for expression {} of type {}", method.getName(), node, node.getClass());
      return null;
    }

    Resolvable<?> resolvable = (Resolvable<?>) node;
    Object resolved = null;
    try {
      resolved = resolvable.resolve();
    } catch (Exception e) {
      LOG.warn(e.getMessage());
    }
    return resolved;
  }

  /**
   * Returns a list of DataFlowNodes that represent all fields defined with this fieldDeclaration. (With the syntax <code>int i,j; </code> one FieldDeclaration
   * can define multiple fields.
   *
   * @param node
   * @return
   */
  private List<DataFlowNode> parseField(FieldDeclaration node, OwnedNode<?> owner) {
    return node.getVariables().stream().map(var -> dfnFactory.create(var, owner)).collect(Collectors.toList());
  }

  private void parseCallable(DataFlowGraph graph, CallableDeclaration<?> cd) {
    // TODO we need this method later to add outgoing and incoming nodes too.
    DataFlowMethod method = graph.getMethod(cd);
    // The values that are overwridden inside this method, for example assigning a field.
    Map<Node, DataFlowNode> overwriddenValues = new HashMap<>();

    Optional<BlockStmt> callableBody =
        cd.getChildNodes().stream().filter(n -> BlockStmt.class.isAssignableFrom(n.getClass())).findFirst().map(BlockStmt.class::cast);

    if (callableBody.isPresent()) {
      nodeHandler.handleNode(graph, method, overwriddenValues, callableBody.get(), method);
    }

    // Each overwridden value has to receive the value that it was overwridden with
    overwriddenValues.forEach((javaParserNode, dataFlowNode) -> dataFlowNode.addEdgeTo(graph.getNode(javaParserNode)));

    // Add changed fields
    overwriddenValues.keySet().stream().filter(VariableDeclarator.class::isInstance).map(graph::getNode).filter(n -> n != null).distinct()
        .forEach(method::addChangedField);
  }

  private List<DataFlowNode> parseParameters(CallableDeclaration<?> cd, OwnedNode<?> owner) {
    return cd.getParameters().stream().map(n -> dfnFactory.create(n, owner)).collect(Collectors.toList());
  }

}
