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

import java.util.Optional;

import com.github.javaparser.ast.Node;

/**
 * Interface for {@link DataFlowGraph} classes that own one or more {@link DataFlowNode}s. This interface contains a self reference since a node can be owned by
 * a {@link DataFlowMethod}, and that method can then be owned by a graph.
 *
 * @author Daan
 */
public abstract class OwnedNode<T extends Node> extends NodeRepresenter<T> {

  protected OwnedNode() {
    super();
  }

  public OwnedNode(T representedNode) {
    super(representedNode);
  }

  public OwnedNode(String name, T representedNode) {
    super(name, representedNode);
  }

  public OwnedNode(NodeRepresenter.Builder<T, ?> builder) {
    super(builder);
  }

  public OwnedNode(String name) {
    super(name);
  }

  /**
   * @return An optional of the {@link OwnedNode} of this node. The optional will be empty in case of a {@link DataFlowGraph} representing a non inner class or
   *         a method for which the rest of the graph was not parsed.
   */
  public abstract Optional<OwnedNode<?>> getOwner();

}
