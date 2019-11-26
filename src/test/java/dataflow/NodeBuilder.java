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
import java.util.Collections;
import java.util.List;

import com.github.javaparser.ast.expr.SimpleName;

import dataflow.model.DataFlowNode;

/**
 * Builder for {@link DataFlowNode}, only to be used for test purposes.
 *
 * @author User
 */
public class NodeBuilder {

  protected enum NodeType {
    IN_BETWEEN,
    METHOD_PARAMETER,
    CLASS_FIELD,
    RETURN
  }

  private String method;
  private String name;
  private List<NodeBuilder> out = new ArrayList<>();
  private final NodeType type;
  private List<NodeBuilder> roots = new ArrayList<>();
  private DataFlowNode build;

  public NodeBuilder(String name, NodeType type) {
    this.name = name;
    this.type = type;
  }

  /**
   * Create a {@link NodeBuilder} for given method and parameter.
   *
   * @param method Name of the method
   * @param name Name of the parameter
   * @return {@link NodeBuilder}
   */
  public static NodeBuilder ofParameter(String method, String name) {
    NodeBuilder builder = new NodeBuilder(name, NodeType.METHOD_PARAMETER);
    builder.method = method;
    return builder;
  }

  public static NodeBuilder ofField(String name) {
    NodeBuilder builder = new NodeBuilder(name, NodeType.CLASS_FIELD);
    return builder;
  }

  public static NodeBuilder ofInBetween(String name) {
    NodeBuilder builder = new NodeBuilder(name, NodeType.IN_BETWEEN);
    return builder;
  }

  public static NodeBuilder ofReturn(String methodName, String line, String column) {
    NodeBuilder methodReturn = new NodeBuilder(methodName + "_return_" + line + "_" + column, NodeType.RETURN);
    methodReturn.method = methodName;
    return methodReturn;
  }

  public NodeBuilder to(String name) {
    NodeBuilder next = new NodeBuilder(name, NodeType.IN_BETWEEN);
    next.addRoots(this);
    out.add(next);
    return next;
  }

  public void to(String... names) {
    Arrays.stream(names).forEach(this::to);
  }

  public NodeBuilder to(NodeBuilder next) {
    out.add(next);
    // TODO don't think this is correct, it should be next.addRoots(this.roots)
    next.addRoots(this);
    return next;
  }

  public void to(NodeBuilder... names) {
    Arrays.stream(names).forEach(this::to);
  }

  private void addRoots(NodeBuilder root) {
    this.roots.addAll(root.getRoots());
  }

  public List<NodeBuilder> getRoots() {
    return this.roots.isEmpty() ? Collections.singletonList(this) : roots;
  }

  public NodeBuilder getRoot() {
    return this.roots.isEmpty() ? this : roots.get(0);
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<NodeBuilder> getOut() {
    return out;
  }

  public void setOut(List<NodeBuilder> out) {
    this.out = out;
  }

  public NodeType getType() {
    return type;
  }

  public DataFlowNode build() {
    return DataFlowNode.builder().name(name).representedNode(new SimpleName(name)).build();
  }

  public DataFlowNode getOrBuild() {
    if (build == null) {
      build = build();
    }
    return build;
  }

  @Override
  public String toString() {
    return getName();
  }

}
