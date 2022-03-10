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
package model;

/**
 * The condition that needs to be true before a {@link DataFlowCodeBlock} is executed.
 *
 * @author daan.vandenheuvel
 */
public class CodeBlockCondition {

  // TODO maybe this class should also be a DataFlowNode because I assume that all if statements are also nodes themselves. However, I'm not sure about else-if
  // statements in regards to the internal preCondition. The general rule that there should be a one-on-one mapping between JavaParser and JavaDataFlow. We
  // could also wrap them in an "inverted condition", but keep a reference to the original CodeBlockCondition.

  /** The DataFlowNode should evaluate to this same value before the CodeBlock will be executed. */
  private final boolean nodeEvaluatesTo;
  /**
   * If this node evaluates to the same value as {@link CodeBlockCondition#nodeEvaluatesTo} the codeBlock should be executed. This DataFlowNode should have an
   * outgoing edge to this CodeBlockCondition.
   */
  private final DataFlowNode node;
  /**
   * The codeBlock should only be executed if this precondition also evaluates to true or if this is null. This is typically used in case of an else-if
   * statement where the first if needs to be false and the second if needs to be true.
   */
  private final CodeBlockCondition preCondition;

  public CodeBlockCondition(DataFlowNode node, boolean nodeEvaluatesTo, CodeBlockCondition preCondition) {
    this.node = node;
    this.nodeEvaluatesTo = nodeEvaluatesTo;
    this.preCondition = preCondition;
  }

  public CodeBlockCondition(DataFlowNode node, boolean nodeEvaluatesTo) {
    this(node, nodeEvaluatesTo, null);
  }

  public CodeBlockCondition(DataFlowNode node) {
    this(node, true, null);
  }

  public CodeBlockCondition getPreCondition() {
    return preCondition;
  }

  public DataFlowNode getNode() {
    return node;
  }

  public boolean isNodeEvaluatesTo() {
    return nodeEvaluatesTo;
  }

}
