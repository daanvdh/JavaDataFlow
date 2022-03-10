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

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.Node;

/**
 * Represents a call to a node (method, constructor or other code block). This node will be owned by the calling method. This class groups all in/output data
 * from one method to another.
 *
 * @author Daan
 */
public class NodeCall extends OwnedNode<Node> {

  private static final Logger LOG = LoggerFactory.getLogger(NodeCall.class);

  /**
   * The {@link ParameterList}s that contain the {@link DataFlowNode}s that where used for a specific {@link DataFlowMethod} call to the owner
   * {@link DataFlowMethod}. Can be null if method does not have any input.
   */
  private ParameterList in;
  /** The method/constructor/codeBlock from which the method is called */
  private OwnedNode<?> owner;
  /** The called method, this can be null in case that the given method is not parsed. */
  private DataFlowMethod calledMethod;
  /**
   * The return Node of a node call, this will be null if method is void. If the return value is not read, the outgoing edges of this node will be empty. There
   * should only be a single incoming edge from the return node of the called method. This NodeCall is the owner of the returnNode.
   */
  private DataFlowNode returnNode;

  /**
   * The {@link DataFlowNode} on which this {@link NodeCall} was called. Can be null if this {@link NodeCall} was a static call. Will be "this" if the method
   * was called on the same instance as the method to which the call belongs to.
   */
  private DataFlowNode instance;

  private String claz;
  private String peckage;

  public NodeCall(OwnedNode<?> owner) {
    this.owner = owner;
  }

  private NodeCall(Builder builder) {
    super(builder);
    if (builder.in != null) {
      this.setIn(builder.in);
    }
    this.owner = builder.owner == null ? this.owner : builder.owner;
    this.calledMethod = builder.calledMethod == null ? this.calledMethod : builder.calledMethod;
    this.claz = builder.claz == null ? this.claz : builder.claz;
    this.peckage = builder.peckage == null ? this.peckage : builder.peckage;
    this.returnNode = builder.returnNode == null ? this.returnNode : builder.returnNode;
    this.instance = builder.instance == null ? this.instance : builder.instance;
  }

  @Override
  public Optional<OwnedNode<?>> getOwner() {
    return Optional.of(owner);
  }

  public Optional<ParameterList> getIn() {
    return Optional.ofNullable(in);
  }

  public final void setIn(ParameterList in) {
    this.in = in;
    in.setOwnerAndName(this);
  }

  public Optional<DataFlowMethod> getCalledMethod() {
    return Optional.ofNullable(calledMethod);
  }

  public void setCalledMethod(DataFlowMethod calledMethod) {
    this.calledMethod = calledMethod;
    this.in.connectTo(calledMethod.getParameters());
    if (this.returnNode != null) {
      if (calledMethod.getReturnNode().isPresent()) {
        calledMethod.getReturnNode().get().addEdgeTo(returnNode);
      } else {
        LOG.warn("Could not connect method return node to NodeCall return Node because return node was not present in method {}", calledMethod);
      }

    }
  }

  public String getClaz() {
    return claz;
  }

  public void setClaz(String claz) {
    this.claz = claz;
  }

  public String getPeckage() {
    return peckage;
  }

  public void setPeckage(String peckage) {
    this.peckage = peckage;
  }

  public void setOwner(OwnedNode<?> owner) {
    this.owner = owner;
  }

  public Optional<DataFlowNode> getReturnNode() {
    return Optional.ofNullable(returnNode);
  }

  public void setReturnNode(DataFlowNode returnNode) {
    this.returnNode = returnNode;
  }

  public Optional<DataFlowNode> getInstance() {
    return Optional.ofNullable(instance);
  }

  public void setInstance(DataFlowNode instance) {
    this.instance = instance;
  }

  /**
   * @return True if the return value is used, false otherwise.
   */
  public boolean isReturnRead() {
    return returnNode != null && (
    // If it has outgoing edges, it's value is read
    !returnNode.getOut().isEmpty() ||
    // If the owner is a methodCall, that means that this nodeCall's return is the direct input to another method, example a(b());
        (owner != null && owner instanceof NodeCall));
  }

  /**
   * Creates builder to build {@link NodeCall}.
   *
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(Object obj) {
    boolean equals = false;
    if (this == obj) {
      equals = true;
    } else if (obj != null && getClass() == obj.getClass()) {
      NodeCall other = (NodeCall) obj;
      equals = new EqualsBuilder().appendSuper(super.equals(obj)).append(in, other.in).append(calledMethod, other.calledMethod).append(claz, other.claz)
          .append(peckage, other.peckage).isEquals();
    }
    return equals;
  }

  @Override
  public int hashCode() {
    return Objects.hash(in, calledMethod, claz, peckage);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("in", in).append("calledMethod", calledMethod)
        .append("returnNode", returnNode).append("class", claz).append("package", peckage).build();
  }

  /**
   * Builder to build {@link NodeCall}.
   */
  public static final class Builder extends NodeRepresenter.Builder<Node, NodeCall.Builder> {
    private ParameterList in;
    private OwnedNode<?> owner;
    private DataFlowMethod calledMethod;
    private String claz;
    private String peckage;
    private DataFlowNode returnNode;
    private DataFlowNode instance;

    private Builder() {
      // Builder should only be constructed via the parent class
    }

    public Builder in(ParameterList in) {
      this.in = in;
      return this;
    }

    public Builder in(DataFlowNode... inputNodes) {
      this.in = ParameterList.builder().nodes(inputNodes).build();
      return this;
    }

    public Builder owner(OwnedNode<?> owner) {
      this.owner = owner;
      return this;
    }

    public Builder calledMethod(DataFlowMethod calledMethod) {
      this.calledMethod = calledMethod;
      return this;
    }

    public Builder claz(String claz) {
      this.claz = claz;
      return this;
    }

    public Builder peckage(String peckage) {
      this.peckage = peckage;
      return this;
    }

    public Builder instance(DataFlowNode instance) {
      this.instance = instance;
      return this;
    }

    public Builder returnNode(DataFlowNode node) {
      this.returnNode = node;
      return this;
    }

    public NodeCall build() {
      return new NodeCall(this);
    }

  }

}
