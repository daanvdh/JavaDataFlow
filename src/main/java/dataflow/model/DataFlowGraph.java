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
package dataflow.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

/**
 * Graph representing the data flow within a single class. The {@link DataFlowNode}s represent variables. An {@link DataFlowEdge} goes from node a to b iff a
 * influences the state of b. Conditional statements are not supported in the current implementation.
 *
 * @author Daan
 */
public class DataFlowGraph extends OwnerNode<ClassOrInterfaceDeclaration> {

  /** The package of the class that this {@link DataFlowGraph} represents. */
  private String classPackage;
  /** This fields within the represented class */
  private List<DataFlowNode> fields = new ArrayList<>();
  /** This Constructors within the represented class */
  private List<DataFlowMethod> constructors = new ArrayList<>();
  /** This Methods within the represented class */
  private Map<Node, DataFlowMethod> methods = new HashMap<>();
  /**
   * All nodes defined within the class: fields and method/constructor parameters and return values. Does not contain method/constructor in-between variables.
   */
  private Map<Node, DataFlowNode> nodes = new HashMap<>();
  /**
   * List containing all external DataFlowGraphs that this {@link DataFlowGraph} depends on. The contained dfg's are not complete graphs they only contain the
   * signatures of methods called from this class. The keys are the package and class name concatenated with a dot.
   */
  private Map<String, DataFlowGraph> dependedGraphs = new HashMap<>();
  /** In case that this {@link DataFlowGraph} represents an inner class, the owner graph represents the class outer class. */
  private DataFlowGraph ownerGraph;

  public DataFlowGraph() {
    // empty constructor which would otherwise be invisible due to the constructor receiving the builder.
  }

  private DataFlowGraph(Builder builder) {
    super(builder);
    this.classPackage = builder.classPackage == null ? this.classPackage : builder.classPackage;
    this.fields.clear();
    this.addFields(builder.fields);
    this.constructors.clear();
    this.constructors.addAll(builder.constructors);
    this.methods = builder.methods == null ? this.methods : builder.methods;
    this.nodes = builder.nodes == null ? this.nodes : builder.nodes;
    this.dependedGraphs = builder.dependedGraphs == null ? this.dependedGraphs : builder.dependedGraphs;
  }

  public List<DataFlowNode> getFields() {
    return fields;
  }

  public void setFields(List<DataFlowNode> fields) {
    this.fields = fields;
  }

  public List<DataFlowMethod> getConstructors() {
    return constructors;
  }

  public void setConstructors(List<DataFlowMethod> constructors) {
    this.constructors = constructors;
  }

  public Collection<DataFlowMethod> getMethods() {
    return methods.values();
  }

  public Map<Node, DataFlowMethod> getMethodMap() {
    return this.methods;
  }

  public void setMethods(List<DataFlowMethod> methods) {
    this.methods.clear();
    methods.forEach(this::addMethod);
  }

  public void addMethod(DataFlowMethod method) {
    if (method.getRepresentedNode() == null) {
      throw new NullPointerException("The representedNode may not be null, this risks overriding existing methods.");
    }
    this.methods.put(method.getRepresentedNode(), method);
    method.setGraph(this);
  }

  public DataFlowMethod getMethod(Node node) {
    return methods.get(node);
  }

  public final void addField(DataFlowNode node) {
    this.fields.add(node);
    if (node.getRepresentedNode() == null) {
      throw new NullPointerException("The representedNode may not be null, this risks overriding existing fields.");
    }
    this.nodes.put(node.getRepresentedNode(), node);
    node.setOwner(this);
  }

  public final void addFields(DataFlowNode... fields) {
    Stream.of(fields).forEach(this::addField);
  }

  private final void addFields(List<DataFlowNode> fields) {
    fields.forEach(this::addField);
  }

  public void addNodes(List<DataFlowNode> nodes) {
    nodes.forEach(this::addNode);
  }

  public void addNode(DataFlowNode node) {
    this.nodes.put(node.getRepresentedNode(), node);
  }

  public DataFlowNode getNode(Node node) {
    return nodes.get(node);
  }

  public Map<Node, DataFlowNode> getNodes() {
    return this.nodes;
  }

  public Map<String, DataFlowGraph> getDependedGraphs() {
    return dependedGraphs;
  }

  public DataFlowGraph getDependedGraph(String path) {
    return this.dependedGraphs.get(path);
  }

  public void setDependedGraphs(Map<String, DataFlowGraph> dependedGraphs) {
    this.dependedGraphs = dependedGraphs;
  }

  public void addDependedGraph(DataFlowGraph graph) {
    this.dependedGraphs.put(graph.getClassPackage() + "." + graph.getName(), graph);
  }

  public String getClassPackage() {
    return classPackage;
  }

  public void setClassPackage(String classPackage) {
    this.classPackage = classPackage;
  }

  @Override
  public Optional<OwnedNode<?>> getOwner() {
    return Optional.ofNullable(this.ownerGraph);
  }

  @Override
  Collection<OwnerNode<?>> getOwnedOwners() {
    // streaming and collecting needed for casting.
    return this.methods.values().stream().collect(Collectors.toList());
  }

  @Override
  Collection<DataFlowNode> getDirectOwnedNodes() {
    return this.fields;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append(super.toString());
    sb.append("fields{");
    fields.forEach(f -> sb.append("\n->").append(f.toStringForward(1)));
    sb.append("\n");
    fields.forEach(f -> sb.append("\n<-").append(f.toStringBackward(1)));
    sb.append("\n}\n");

    sb.append("methods{\n");
    for (DataFlowMethod m : methods.values()) {
      sb.append(m.toString());
    }
    sb.append("\n}");
    return sb.toString();
  }

  /**
   * Creates builder to build {@link DataFlowGraph}.
   *
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder to build {@link DataFlowGraph}.
   */
  public static final class Builder extends NodeRepresenter.Builder<ClassOrInterfaceDeclaration, DataFlowGraph.Builder> {
    private String classPackage;
    private List<DataFlowNode> fields = new ArrayList<>();
    private List<DataFlowMethod> constructors = new ArrayList<>();
    private Map<Node, DataFlowMethod> methods = new HashMap<>();
    private Map<Node, DataFlowNode> nodes;
    private Map<String, DataFlowGraph> dependedGraphs;

    private Builder() {
      // Builder should only be constructed via the parent class
    }

    public Builder classPackage(String classPackage) {
      this.classPackage = classPackage;
      return this;
    }

    public Builder fields(List<DataFlowNode> fields) {
      this.fields.clear();
      this.fields.addAll(fields);
      return this;
    }

    public Builder fields(DataFlowNode... fields) {
      this.fields.clear();
      this.fields.addAll(Arrays.asList(fields));
      return this;
    }

    public Builder constructors(List<DataFlowMethod> constructors) {
      this.constructors.clear();
      this.constructors.addAll(constructors);
      return this;
    }

    public Builder methods(Map<Node, DataFlowMethod> methods) {
      this.methods = methods;
      return this;
    }

    public Builder methods(DataFlowMethod... methods) {
      Stream.of(methods).forEach(m -> this.methods.put(m.getRepresentedNode(), m));
      return this;
    }

    public Builder nodes(Map<Node, DataFlowNode> nodes) {
      this.nodes = nodes;
      return this;
    }

    public Builder dependedGraphs(Map<String, DataFlowGraph> dependedGraphs) {
      this.dependedGraphs = dependedGraphs;
      return this;
    }

    public DataFlowGraph build() {
      return new DataFlowGraph(this);
    }

  }

}
