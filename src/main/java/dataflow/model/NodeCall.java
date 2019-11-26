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
  /** The {@link ParameterList}s that contain the {@link DataFlowNode}s that where used as input for a call to another {@link DataFlowMethod}. */
  // TODO should probably be removed since this data is already contained in calledMethod
  private ParameterList out;
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
   * The name of the instance on which this {@link NodeCall} was called. Can be null if this {@link NodeCall} was a static call. Will be "this" if the method
   * was called on the same instance as the method to which the call belongs to.
   */
  private String instanceName;

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
    this.out = builder.out == null ? this.out : builder.out;
    this.owner = builder.owner == null ? this.owner : builder.owner;
    this.calledMethod = builder.calledMethod == null ? this.calledMethod : builder.calledMethod;
    this.claz = builder.claz == null ? this.claz : builder.claz;
    this.peckage = builder.peckage == null ? this.peckage : builder.peckage;
    this.returnNode = builder.returnNode == null ? this.returnNode : builder.returnNode;
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

  public ParameterList getOut() {
    return this.out;
  }

  public void setOut(ParameterList out) {
    this.out = out;
  }

  public Optional<DataFlowMethod> getCalledMethod() {
    return Optional.ofNullable(calledMethod);
  }

  public void setCalledMethod(DataFlowMethod calledMethod) {
    this.calledMethod = calledMethod;
    this.in.connectTo(calledMethod.getInputParameters());
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

  public Optional<String> getInstanceName() {
    return Optional.ofNullable(instanceName);
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
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
      equals = new EqualsBuilder().appendSuper(super.equals(obj)).append(in, other.in).append(out, other.out).append(calledMethod, other.calledMethod)
          .append(claz, other.claz).append(peckage, other.peckage).isEquals();
    }
    return equals;
  }

  @Override
  public int hashCode() {
    return Objects.hash(in, out, calledMethod, claz, peckage);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("in", in).append("out", out)
        .append("calledMethod", calledMethod).append("returnNode", returnNode).append("class", claz).append("package", peckage).build();
  }

  /**
   * Builder to build {@link NodeCall}.
   */
  public static final class Builder extends NodeRepresenter.Builder<Node, NodeCall.Builder> {
    private ParameterList in;
    private ParameterList out;
    private OwnedNode<?> owner;
    private DataFlowMethod calledMethod;
    private String claz;
    private String peckage;
    private DataFlowNode returnNode;

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

    public Builder out(ParameterList out) {
      this.out = out;
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

    public Builder returnNode(DataFlowNode node) {
      this.returnNode = node;
      return this;
    }

    public NodeCall build() {
      return new NodeCall(this);
    }

  }

}
