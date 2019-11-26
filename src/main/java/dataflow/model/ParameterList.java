/*
 * Copyright (c) 2019 by Eyefreight BV (www.eyefreight.com). All rights reserved.
 *
 * This software is provided by the copyright holder and contributors "as is" and any express or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall
 * Eyefreight BV or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services; * loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including
 * negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
 */
package dataflow.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.github.javaparser.ast.Node;

import dataflow.DataFlowException;

/**
 * Represents the set of parameters from a {@link DataFlowMethod}. Every method has at most one {@link ParameterList}. A parameter list can also exist outside
 * of a method when it is constructed to represent a call to another method. It can have input or output edges to other {@link ParameterList}s representing a
 * call from/to another location. With this class you can distinguish which variables where used as input for a method at a specific call.
 *
 * @author Daan
 */
public class ParameterList extends OwnedNode<Node> {

  private List<DataFlowNode> nodes = new ArrayList<>();
  /** The method/constructor/codeBlock that has this parameterList as input or the {@link NodeCall} for which this is the input. */
  private OwnedNode<?> owner;

  public ParameterList(OwnedNode<?> method) {
    this.owner = method;
  }

  public ParameterList(List<DataFlowNode> inputParameters, DataFlowMethod method) {
    this(method);
    this.nodes.addAll(inputParameters);
  }

  private ParameterList(Builder builder) {
    super(builder);
    this.addAll(builder.nodes);
    if (builder.owner != null) {
      this.setOwnerAndName(builder.owner);
    }
  }

  @Override
  public Optional<OwnedNode<?>> getOwner() {
    return Optional.ofNullable(this.owner);
  }

  public final void setOwnerAndName(OwnedNode<?> owner) {
    this.owner = owner;
    String ownerName = owner.getName() == null ? "unkown" + owner.getClass() : owner.getName();
    this.name = ownerName + "Parameters";
  }

  public List<DataFlowNode> getParameters() {
    return nodes;
  }

  public void setParameters(List<DataFlowNode> parameters) {
    this.nodes.clear();
    this.addAll(parameters);
  }

  public final void add(DataFlowNode node) {
    this.nodes.add(node);
    // set the owner since this is the lowest possible owner
    node.setOwner(this);
  }

  public void clear() {
    this.nodes.clear();
  }

  public final void addAll(List<DataFlowNode> inputParameters) {
    inputParameters.forEach(this::add);
  }

  public boolean contains(DataFlowNode dfn) {
    return this.nodes.contains(dfn);
  }

  public List<DataFlowNode> getNodes() {
    return this.nodes;
  }

  public int nofNodes() {
    return this.nodes.size();
  }

  public void connectTo(ParameterList otherParams) {
    if (this.nodes.size() != otherParams.nodes.size()) {
      throw new DataFlowException("Number of parameters is not equal for ParameterList {} and {}, with owner nodes {} and {}", this, otherParams, owner,
          otherParams.owner);
    }
    for (int i = 0; i < this.nodes.size(); i++) {
      this.nodes.get(i).addEdgeTo(otherParams.nodes.get(i));
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), nodes);
  }

  @Override
  public boolean equals(Object obj) {
    boolean equals = false;
    if (this == obj) {
      equals = true;
    } else if (obj != null && getClass() == obj.getClass()) {
      ParameterList other = (ParameterList) obj;
      equals = new EqualsBuilder().appendSuper(super.equals(obj)).append(nodes, other.nodes).append(name, other.name).isEquals();
    }
    return equals;
  }

  /**
   * Creates builder to build {@link ParameterList}.
   *
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("nodes", nodes)
        .append("owner", owner != null ? owner.getName() : "null").build();
  }

  /**
   * Builder to build {@link ParameterList}.
   */
  public static final class Builder extends NodeRepresenter.Builder<Node, ParameterList.Builder> {
    private List<DataFlowNode> nodes = new ArrayList<>();
    private OwnedNode<?> owner;

    private Builder() {
      // Builder should only be constructed via the parent class
    }

    public Builder node(DataFlowNode node) {
      this.nodes.add(node);
      return this;
    }

    public Builder nodes(List<DataFlowNode> nodes) {
      this.nodes.clear();
      this.nodes.addAll(nodes);
      return this;
    }

    public Builder nodes(DataFlowNode... nodes) {
      this.nodes.clear();
      this.nodes.addAll(Arrays.asList(nodes));
      return this;
    }

    public Builder owner(OwnedNode<?> owner) {
      this.owner = owner;
      return this;
    }

    public ParameterList build() {
      return new ParameterList(this);
    }

  }

}
