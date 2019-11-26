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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;

/**
 * A class representing a {@link JavaParser} {@link Node}.
 *
 * @author Daan
 * @param <T> The type of the {@link JavaParser} {@link Node} to represent.
 */
public abstract class NodeRepresenter<T extends Node> {

  /** The {@link JavaParser} {@link Node} */
  private T representedNode;
  /** The name of this node */
  protected String name;

  protected NodeRepresenter() {
    // empty constructor which would otherwise be invisible
  }

  public NodeRepresenter(T representedNode) {
    this.representedNode = representedNode;
  }

  public NodeRepresenter(String name, T representedNode) {
    this(representedNode);
    this.name = name;
  }

  protected NodeRepresenter(NodeRepresenter.Builder<T, ?> builder) {
    this.representedNode = builder.representedNode == null ? this.representedNode : builder.representedNode;
    this.name = builder.name == null ? this.name : builder.name;
  }

  public NodeRepresenter(String name) {
    this.name = name;
  }

  public T getRepresentedNode() {
    return representedNode;
  }

  public void setRepresentedNode(T representedNode) {
    this.representedNode = representedNode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object obj) {
    boolean equals = false;
    if (this == obj) {
      equals = true;
    } else if (obj != null && getClass() == obj.getClass()) {
      NodeRepresenter<?> other = (NodeRepresenter<?>) obj;
      equals = new EqualsBuilder().append(representedNode, other.representedNode).append(name, other.name).isEquals();
    }
    return equals;
  }

  @Override
  public int hashCode() {
    return Objects.hash(representedNode, name);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("name", name).append("representedNode", representedNode).build();
  }

  /**
   * Creates builder to build {@link NodeRepresenter}.
   *
   * @return created builder
   */
  public static <T extends Node> Builder<T, ? extends NodeRepresenter.Builder<T, ?>> builder() {
    return new Builder<>();
  }

  /**
   * Builder to build {@link NodeRepresenter}.
   */
  @SuppressWarnings("unchecked")
  public static class Builder<T extends Node, S extends NodeRepresenter.Builder<T, ?>> {
    private T representedNode;
    private String name;

    protected Builder() {
      // Builder should only be used via the parent class or extending builder
    }

    public S representedNode(T representedNode) {
      this.representedNode = representedNode;
      return (S) this;
    }

    public S name(String name) {
      this.name = name;
      return (S) this;
    }

  }

}
