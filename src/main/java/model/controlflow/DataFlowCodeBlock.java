/*
 * Copyright (c) 2022 by Eyefreight BV (www.eyefreight.com). All rights reserved.
 *
 * This software is provided by the copyright holder and contributors "as is" and any express or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall
 * Eyefreight BV or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services; * loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including
 * negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
 */
package model.controlflow;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.github.javaparser.ast.stmt.BlockStmt;

import model.DataFlowNode;
import model.OwnedNode;
import model.OwnerNode;

/**
 * A block of code in a java class. This contains a list of nodes that will be executed in sequence. A code block has a (possibly empty) list of codeBlock's
 * that succeed this codeBlock as well as a list preceeding it.
 *
 * @author daan.vandenheuvel
 */
public class DataFlowCodeBlock extends OwnerNode<BlockStmt> { // TODO find the JavaParser Node that represents a code block and add it as type argument.

  // TODO methods and classes should extend this.

  /** The list of all possible CodeBlocks that might lead to this CodeBlock. */
  private List<DataFlowCodeBlock> calledBy;

  /**
   * A map of all CodeBlocks that might be executed inside this CodeBlock in order of occurrence. The CodeBlock is only executed if the {@link DataFlowNode} key
   * evaluates to true. The CodeBlockCondition can be null, if the DataFlowCodeBlock is always executed.
   */
  // TODO What do we do with if-else statements, where one node should be false (the if-statement) and the next should be true (the else-if-statement).
  private List<Pair<CodeBlockCondition, DataFlowCodeBlock>> called;

  /** List of {@link DataFlowNode}s that are executed inside this CodeBlock. */
  private List<DataFlowNode> dataFlowNodes;

  @Override
  public Collection<OwnerNode<?>> getOwnedOwners() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<DataFlowNode> getDirectOwnedNodes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<OwnedNode<?>> getOwner() {
    // TODO Auto-generated method stub
    return null;
  }

  public List<DataFlowCodeBlock> getCalledBy() {
    return calledBy;
  }

  public void setCalledBy(List<DataFlowCodeBlock> calledBy) {
    this.calledBy = calledBy;
  }

  public List<Pair<CodeBlockCondition, DataFlowCodeBlock>> getCalled() {
    return called;
  }

  public void setCalled(List<Pair<CodeBlockCondition, DataFlowCodeBlock>> called) {
    this.called = called;
  }

  public List<DataFlowNode> getDataFlowNodes() {
    return dataFlowNodes;
  }

  public void setDataFlowNodes(List<DataFlowNode> dataFlowNodes) {
    this.dataFlowNodes = dataFlowNodes;
  }

}
