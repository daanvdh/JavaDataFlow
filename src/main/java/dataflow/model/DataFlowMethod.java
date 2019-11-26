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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;

import dataflow.util.HashCodeWrapper;

/**
 * DataFlow class representing a method inside a {@link DataFlowGraph}.
 *
 * @author Daan
 */
public class DataFlowMethod extends OwnerNode<CallableDeclaration<?>> {

  // TODO the idea is to not have one list of nodes in DFM containing everything, but to let other owners like NodeCall, ParameterList and later "FlowBlock"
  // (representing BlockStatement) have a list of nodes of their own. Then recursively get all owned nodes of a specific OwnedNode via this method.

  /** All nodes defined within this method. This method should be an (indirect) owner for each of these nodes. */
  private Map<HashCodeWrapper<Node>, DataFlowNode> nodes = new HashMap<>();
  /** The graph which this method is part of. This is the owner of this method. */
  private DataFlowGraph graph;
  /**
   * The return value of this method, null if this is a void method. Note that a method can have multiple return statements, we model as if a method only has a
   * single return type with as Node the Node of the whole method. Then all real return statements have an edge to the single return statement.
   */
  private DataFlowNode returnNode;
  /** The input parameters of the method */
  private ParameterList inputParameters;
  /** The calls to other methods or constructors done from within this method. These can be either to methods in the same class or different classes. */
  private List<NodeCall> nodeCalls = new ArrayList<>();

  /** The fields of the class that are read inside this method */
  // TODO Should probably be removed since it's a derivative
  private List<DataFlowNode> inputFields = new ArrayList<>();
  /** The fields of the class that are written inside this method */
  // TODO Should probably be removed since it's a derivative
  private List<DataFlowNode> changedFields = new ArrayList<>();

  public DataFlowMethod(String name, CallableDeclaration<?> representedNode) {
    super(name, representedNode);
  }

  public DataFlowMethod(DataFlowGraph graph, CallableDeclaration<?> node, String name) {
    this(name, node);
    this.graph = graph;
    graph.addMethod(this);
  }

  protected DataFlowMethod(Builder builder) {
    super(builder);
    if (builder.returnNode != null) {
      this.setReturnNode(builder.returnNode);
    }
    if (builder.inputParameters != null) {
      this.setInputParameters(builder.inputParameters);
    }
    this.addNodes(builder.nodes);
    // If it's added via the builder this method will be the owner, otherwise this node should have been added via a nodeCall
    builder.nodes.forEach(n -> n.setOwner(this));

    this.nodeCalls.clear();
    this.nodeCalls.addAll(builder.nodeCalls);
    this.addNodes(builder.nodeCalls.stream().map(NodeCall::getReturnNode).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()));
    this.addNodes(builder.nodeCalls.stream().map(NodeCall::getIn).filter(Optional::isPresent).map(Optional::get).map(ParameterList::getNodes)
        .flatMap(List::stream).collect(Collectors.toList()));
    this.inputFields.clear();
    this.inputFields.addAll(builder.inputFields);
    this.changedFields.clear();
    this.changedFields.addAll(builder.changedFields);
    this.graph = builder.graph;
  }

  public Optional<DataFlowNode> getReturnNode() {
    return Optional.ofNullable(returnNode);
  }

  public final void setReturnNode(DataFlowNode returnNode) {
    this.returnNode = returnNode;
    this.addNode(returnNode);
  }

  public ParameterList getInputParameters() {
    return inputParameters;
  }

  public final void setInputParameters(ParameterList inputParameters) {
    this.inputParameters = inputParameters;
    this.addNodes(inputParameters.getNodes());
    inputParameters.setOwnerAndName(this);
  }

  public List<DataFlowNode> getInputFields() {
    return inputFields;
  }

  public void setInputFields(List<DataFlowNode> inputFields) {
    this.inputFields = inputFields;
  }

  public List<DataFlowNode> getChangedFields() {
    return changedFields;
  }

  public void setChangedFields(List<DataFlowNode> changedFields) {
    this.changedFields = changedFields;
  }

  @Override
  public Optional<OwnedNode<?>> getOwner() {
    return Optional.ofNullable((OwnedNode<?>) this.graph);
  }

  public DataFlowGraph getGraph() {
    return graph;
  }

  public void setGraph(DataFlowGraph graph) {
    this.graph = graph;
  }

  public Collection<DataFlowNode> getNodes() {
    return nodes.values();
  }

  public final void addNodes(List<DataFlowNode> nodes) {
    nodes.forEach(this::addNode);
  }

  public final void addNode(DataFlowNode created) {
    this.nodes.put(new HashCodeWrapper<>(created.getRepresentedNode()), created);
  }

  public DataFlowNode getNode(Node node) {
    return nodes.get(new HashCodeWrapper<>(node));
  }

  public void addParameter(DataFlowNode node) {
    this.inputParameters.add(node);
    this.addNode(node);
  }

  public void addChangedField(DataFlowNode node) {
    this.changedFields.add(node);
  }

  public void addChangedFields(DataFlowNode... fields) {
    Stream.of(fields).forEach(this::addChangedField);
  }

  /**
   * @return List of {@link DataFlowMethod}s containing both the input and output methods.
   */
  public List<NodeCall> getNodeCalls() {
    return this.nodeCalls;
  }

  public void setCalledMethods(List<NodeCall> calledMethods) {
    this.nodeCalls = calledMethods;
  }

  public void addMethodCall(NodeCall calledMethod) {
    this.nodeCalls.add(calledMethod);
    calledMethod.getIn().map(ParameterList::getNodes).ifPresent(this::addNodes);
  }

  @Override
  public Set<OwnerNode<?>> getOwnedOwners() {
    // TODO later add NodeCall and InputParameter
    return Collections.emptySet();
  }

  @Override
  public Collection<DataFlowNode> getDirectOwnedNodes() {
    return this.nodes.values();
  }

  public boolean isInputBoundary(DataFlowNode n) {
    // TODO not tested yet
    boolean isInputBoundary = false;
    if (this.inputParameters.getNodes().contains(n) //
        || this.nodeCalls.stream().map(NodeCall::getReturnNode).filter(Optional::isPresent).map(Optional::get).filter(n::equals).findAny().isPresent() //
    // TODO || class fields
    ) {
      isInputBoundary = true;
    }
    return isInputBoundary;
  }

  /**
   * Get's all nodes that are directly connected with an edge to (not from) the input node, which are in scope of this method.
   *
   * @param node The node to get the direct input nodes for.
   * @return The list of {@link DataFlowNode}
   */
  public List<DataFlowNode> getDirectInputNodesFor(DataFlowNode node) {
    return node.getIn().stream().map(DataFlowEdge::getFrom).filter(this::owns).collect(Collectors.toList());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("method " + super.getName() + "{\n");

    if (inputParameters != null) {
      sb.append("\tparameters{\n");
      for (DataFlowNode p : inputParameters.getParameters()) {
        sb.append(p.toStringForward(1, 2) + "\n");
      }
      sb.append("\t}\n");
    }

    sb.append("\tchangedFields{\n");
    for (DataFlowNode p : changedFields) {
      sb.append(p.toStringForward(1, 2) + "\n");
    }
    sb.append("\t}\n");

    sb.append("\tnodes{\n");
    for (DataFlowNode p : nodes.values()) {
      sb.append("\t\t" + p.toString() + "\n");
    }
    sb.append("\t}\n");

    sb.append("\treturn " + (this.returnNode == null ? "null" : this.returnNode.getName()) + "\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Creates builder to build {@link DataFlowMethod}.
   *
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), returnNode, inputParameters, inputFields, changedFields);
  }

  @Override
  public boolean equals(Object obj) {
    boolean equals = false;
    if (this == obj) {
      equals = true;
    } else if (obj != null && getClass() == obj.getClass()) {
      DataFlowMethod other = (DataFlowMethod) obj;
      equals = new EqualsBuilder().appendSuper(super.equals(obj)).append(returnNode, other.returnNode).append(inputParameters, other.inputParameters)
          .append(graph, other.graph).isEquals();
    }
    return equals;
  }

  /**
   * Builder to build {@link DataFlowMethod}.
   */
  public static class Builder extends NodeRepresenter.Builder<CallableDeclaration<?>, DataFlowMethod.Builder> {
    protected ParameterList inputParameters;
    protected List<DataFlowNode> inputFields = new ArrayList<>();
    protected List<DataFlowNode> changedFields = new ArrayList<>();
    private DataFlowNode returnNode;
    private DataFlowGraph graph;
    private List<DataFlowNode> nodes = new ArrayList<>();
    private List<NodeCall> nodeCalls = new ArrayList<>();

    protected Builder() {
      // Builder should only be constructed via the parent class
    }

    public Builder returnNode(DataFlowNode returnNode) {
      this.returnNode = returnNode;
      return this;
    }

    public Builder inputParameters(ParameterList inputParameters) {
      this.inputParameters = inputParameters;
      return this;
    }

    public Builder inputParameters(DataFlowNode... inputParameters) {
      this.inputParameters = ParameterList.builder().nodes(inputParameters).build();
      return this;
    }

    public Builder inputFields(List<DataFlowNode> inputFields) {
      this.inputFields.clear();
      this.inputFields.addAll(inputFields);
      return this;
    }

    public Builder changedFields(List<DataFlowNode> changedFields) {
      this.changedFields.clear();
      this.changedFields.addAll(changedFields);
      return this;
    }

    public Builder changedFields(DataFlowNode... changedFields) {
      this.changedFields.clear();
      this.changedFields.addAll(Arrays.asList(changedFields));
      return this;
    }

    public Builder graph(DataFlowGraph graph) {
      this.graph = graph;
      return this;
    }

    public Builder nodes(DataFlowNode... nodes) {
      this.nodes.clear();
      this.nodes.addAll(Arrays.asList(nodes));
      return this;
    }

    public Builder nodeCalls(NodeCall... calls) {
      this.nodeCalls.clear();
      Stream.of(calls).forEach(this.nodeCalls::add);
      return this;
    }

    public DataFlowMethod build() {
      return new DataFlowMethod(this);
    }

  }

}
