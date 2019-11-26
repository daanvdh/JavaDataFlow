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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

import dataflow.GraphUtil;

/**
 * A node inside the {@link DataFlowGraph} containing a {@link JavaParser} {@link Node}. The incoming {@link DataFlowEdge}s are {@link DataFlowNode}s that
 * influence the state of this node. The outgoing {@link DataFlowEdge}s point to {@link DataFlowNode}s which state is influenced by this {@link DataFlowNode}.
 *
 * @author Daan
 */
public class DataFlowNode extends OwnedNode<Node> {

  /** The {@link DataFlowEdge}s from {@link DataFlowNode}s that influence the state of this node */
  private List<DataFlowEdge> in = new ArrayList<>();
  /** The {@link DataFlowEdge}s to {@link DataFlowNode}s who's state is influenced by this node */
  private List<DataFlowEdge> out = new ArrayList<>();
  /**
   * The type of the represented node. This is needed in the case that we need to create a {@link DataFlowNode} without a representedNode, for instance when the
   * {@link CompilationUnit} of a dependend graph is not available while constructing a {@link DataFlowGraph}.
   */
  private String type;
  /**
   * The {@link DataFlowMethod} that contains this {@link DataFlowNode}, this will be null in case this node was not defined within a method, for instance in
   * case of a class field.
   */
  private OwnedNode<?> owner;

  public DataFlowNode(Node representedNode) {
    super(representedNode);
  }

  public DataFlowNode(String name, Node representedNode) {
    super(name, representedNode);
  }

  private DataFlowNode(Builder builder) {
    super(builder);
    this.in.clear();
    this.in.addAll(builder.in);
    this.out.clear();
    this.out.addAll(builder.out);
    this.setType(builder.type);
    this.owner = builder.owner;
  }

  public List<DataFlowEdge> getIn() {
    return in;
  }

  public void setIn(List<DataFlowEdge> in) {
    this.in = in;
  }

  public List<DataFlowEdge> getOut() {
    return out;
  }

  public void setOut(List<DataFlowEdge> out) {
    this.out = out;
  }

  public void addEdgeTo(DataFlowNode to) {
    DataFlowEdge edge = new DataFlowEdge(this, to);
    this.addOutgoing(edge);
    to.addIncoming(edge);
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isField() {
    return this.getOwner().map(DataFlowGraph.class::isInstance).orElse(false);
  }

  public boolean isInputParameter() {
    return this.getOwner().filter(DataFlowMethod.class::isInstance).map(DataFlowMethod.class::cast).map(dfm -> dfm.getInputParameters().contains(this))
        .orElse(false);
  }

  /**
   * Walks back over incoming edges until predicate is met or no incoming edges are present.
   *
   * @see GraphUtil#walkBackUntil(DataFlowNode, Predicate)
   * @param predicate The {@link Predicate} to meet
   * @param scope The scope for the variable, the search is stopped as soon as the scope does not hold and an empty list is returned.
   * @return {@link List} of {@link DataFlowNode}
   */
  public List<DataFlowNode> walkBackUntil(Predicate<DataFlowNode> predicate, Predicate<DataFlowNode> scope) {
    return GraphUtil.walkBackUntil(this, predicate, scope);
  }

  /**
   * @param node The {@link DataFlowNode} to check.
   * @return True if this node is equal to the given node or has a direct incoming edge from the input node, false otherwise.
   */
  public boolean hasAsDirectInput(DataFlowNode node) {
    return this.equals(node) || this.in.stream().map(DataFlowEdge::getFrom).filter(node::equals).findAny().isPresent();
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), in, out);
  }

  @Override
  public boolean equals(Object obj) {
    boolean equals = false;
    if (this == obj) {
      equals = true;
    } else if (obj != null && getClass() == obj.getClass()) {
      DataFlowNode other = (DataFlowNode) obj;
      equals = new EqualsBuilder().appendSuper(super.equals(obj)).append(in, other.in).append(out, other.out).append(type, other.type).isEquals();
    }
    return equals;
  }

  private void addIncoming(DataFlowEdge edge) {
    this.in.add(edge);
  }

  private void addOutgoing(DataFlowEdge edge) {
    this.out.add(edge);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("in", in).append("out", out).append("type", type)
        .build();
  }

  public String toStringForward(int tabs) {
    if (tabs > 10) {
      return "TestDataFlowNode::toStringForward tabs>10";
    }
    return toStringForward(tabs, 0);
  }

  public String toStringForward(int tabs, int firstTabs) {
    StringBuilder sb = new StringBuilder();
    sb.append(tabs(firstTabs) + this.getName());
    boolean first = true;
    for (DataFlowEdge e : out) {
      if (first) {
        first = false;
        sb.append("\t-> " + e.getTo().toStringForward(tabs + 1));
      } else {
        sb.append("\n" + tabs(firstTabs + tabs + 3) + "-> " + e.getTo().toStringForward(tabs + 1, firstTabs));
      }
    }
    return sb.toString();
  }

  public String toStringBackward(int tabs) {
    if (tabs > 10) {
      return "TestDataFlowNode::toStringForward tabs>10";
    }
    return toStringBackward(tabs, 0);
  }

  private String toStringBackward(int tabs, int firstTabs) {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getName());
    boolean first = true;
    for (DataFlowEdge e : in) {
      if (first) {
        first = false;
        sb.append("\t<- " + e.getFrom().toStringBackward(tabs + 1));
      } else {
        sb.append("\n" + tabs(tabs + 1) + "<- " + e.getFrom().toStringBackward(tabs + 1, tabs + 1));
      }
    }
    return sb.toString();
  }

  /**
   * Creates builder to build {@link DataFlowNode}.
   *
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  private String tabs(int tabs) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < tabs; i++) {
      sb.append("\t");
    }
    return sb.toString();
  }

  @Override
  public Optional<OwnedNode<?>> getOwner() {
    return Optional.ofNullable(this.owner);
  }

  public void setOwner(OwnedNode<?> owner) {
    this.owner = owner;
  }

  /**
   * Builder to build {@link DataFlowNode}.
   */
  public static final class Builder extends NodeRepresenter.Builder<Node, DataFlowNode.Builder> {
    private OwnedNode<?> owner;
    private List<DataFlowEdge> in = new ArrayList<>();
    private List<DataFlowEdge> out = new ArrayList<>();
    private String type;

    private Builder() {
      // Builder should only be constructed via the parent class
    }

    public Builder in(List<DataFlowEdge> in) {
      this.in.clear();
      this.in.addAll(in);
      return this;
    }

    public Builder out(List<DataFlowEdge> out) {
      this.out.clear();
      this.out.addAll(out);
      return this;
    }

    public Builder type(String type) {
      this.type = type;
      return this;
    }

    public Builder owner(OwnedNode<?> owner) {
      this.owner = owner;
      return this;
    }

    public DataFlowNode build() {
      return new DataFlowNode(this);
    }

  }

}
