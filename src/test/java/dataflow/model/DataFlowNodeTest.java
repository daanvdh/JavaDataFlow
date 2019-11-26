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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.google.common.base.Objects;

import dataflow.util.HashCodeWrapper;
import dataflow.util.HashMapWrapper;

/**
 * Unit test for {@link DataFlowNode}.
 *
 * @author Daan
 */
public class DataFlowNodeTest {
  private static final Node JAVA_PARSER_NODE = new FieldDeclaration();
  private static final List<DataFlowEdge> IN = Collections.singletonList(DataFlowEdge.builder().build());
  private static final List<DataFlowEdge> OUT = Collections.singletonList(DataFlowEdge.builder().build());
  private static final String NAME = "a";

  @Test
  public void testDataFlowNode_minimum() {
    DataFlowNode dataFlowNode = DataFlowNode.builder().build();

    Assert.assertNull("Unexpected javaParserNode", dataFlowNode.getRepresentedNode());
    Assert.assertTrue("Unexpected in", dataFlowNode.getIn().isEmpty());
    Assert.assertTrue("Unexpected out", dataFlowNode.getOut().isEmpty());
    Assert.assertNull("Unexpected name", dataFlowNode.getName());
  }

  @Test
  public void testDataFlowNode_maximum() {
    DataFlowNode dataFlowNode = createAndFillBuilder().build();

    Assert.assertEquals("Unexpected javaParserNode", JAVA_PARSER_NODE, dataFlowNode.getRepresentedNode());
    Assert.assertEquals("Unexpected in", IN, dataFlowNode.getIn());
    Assert.assertEquals("Unexpected out", OUT, dataFlowNode.getOut());
    Assert.assertEquals("Unexpected name", NAME, dataFlowNode.getName());
  }

  @Test
  public void testEquals_Same() {
    DataFlowNode.Builder builder = createAndFillBuilder();
    DataFlowNode a = builder.build();
    DataFlowNode b = builder.build();
    Assert.assertTrue("Expected a and b to be equal", a.equals(b));
  }

  @Test
  public void testEquals_Different() {
    verifyEqualsDifferent(DataFlowNode.Builder::name, "b");
    verifyEqualsDifferent(DataFlowNode.Builder::representedNode, new MethodDeclaration());
    verifyEqualsDifferent(DataFlowNode.Builder::in, Collections.singletonList(DataFlowEdge.builder().build()));
    verifyEqualsDifferent(DataFlowNode.Builder::out, Collections.singletonList(DataFlowEdge.builder().build()));
  }

  @Test
  public void testHashCode_Same() {
    DataFlowNode.Builder builder = createAndFillBuilder();
    DataFlowNode a = builder.build();
    DataFlowNode b = builder.build();
    Assert.assertEquals("Expected hash code to be the same", a.hashCode(), b.hashCode());
  }

  @Test
  public void testHashCode_Different() {
    verifyHashCode_Different(DataFlowNode.Builder::name, "b");
    verifyHashCode_Different(DataFlowNode.Builder::representedNode, new MethodDeclaration());
    verifyHashCode_Different(DataFlowNode.Builder::in, Collections.singletonList(DataFlowEdge.builder().build()));
    verifyHashCode_Different(DataFlowNode.Builder::out, Collections.singletonList(DataFlowEdge.builder().build()));
  }

  /**
   * Assert that the names and all incoming and outgoing edges are equal, regardless of the order.
   *
   * @param expected
   * @param fields
   * @return Empty optional if assertion passed, optional containing an error message otherwise.
   */
  public Optional<String> assertNodesEqual(Collection<DataFlowNode> expected, Collection<DataFlowNode> fields) {
    Map<HashCodeWrapper<Node>, DataFlowNode> exp1 =
        expected.stream().collect(Collectors.toMap(t -> new HashCodeWrapper<>(t.getRepresentedNode()), Function.identity()));
    HashMapWrapper<Node, DataFlowNode> exp = new HashMapWrapper<>(exp1);

    Map<HashCodeWrapper<Node>, DataFlowNode> res1 =
        fields.stream().collect(Collectors.toMap(t -> new HashCodeWrapper<>(t.getRepresentedNode()), Function.identity()));
    HashMapWrapper<Node, DataFlowNode> res = new HashMapWrapper<>(res1);

    Optional<String> equal = exp.keySet().equals(res.keySet()) ? Optional.empty()
        : Optional.of("Nodes not equal, expected: " + exp.values().stream().map(DataFlowNode::getName).sorted().collect(Collectors.toList()) + " but was: "
            + res.values().stream().map(DataFlowNode::getName).sorted().collect(Collectors.toList()) + " with types [" + res.values().stream()
                .sorted(Comparator.comparing(DataFlowNode::getName)).map(DataFlowNode::getRepresentedNode).map(Node::getClass).collect(Collectors.toList()));
    if (!equal.isPresent()) {
      equal = exp.keySet().stream().map(key -> assertNodeEqual(exp.get(key), res.get(key))).filter(Optional::isPresent).map(Optional::get).findFirst();
    }
    return equal;
  }

  /**
   * Assert that the incoming and outgoing edges of both nodes are equal
   *
   * @param exp expected
   * @param res result
   * @return Empty optional if assertion passed, optional containing an error message otherwise.
   */
  public Optional<String> assertNodeEqual(DataFlowNode exp, DataFlowNode res) {
    List<DataFlowEdge> expIn = exp.getIn();
    List<DataFlowEdge> resIn = res.getIn();
    String message =
        !(exp.getName().equals(res.getName())) ? "Names are not equal of expected node " + exp.getName() + " and result node " + res.getName() : null;
    message = (message == null && expIn.size() != resIn.size())
        ? "number of incoming edges not equal expected " + expIn.size() + " but was " + resIn.size() + " for expected node " + exp + " and resultNode " + res
        : message;
    for (int i = 0; i < expIn.size() && message == null; i++) {
      String edgeMessage = assertEdgeEqual(expIn.get(0), resIn.get(0));
      if (edgeMessage != null) {
        message = "Incoming edges not equal of expected node " + exp + " and result node " + res + ": " + edgeMessage;
      }
    }

    List<DataFlowEdge> expOut = exp.getOut();
    List<DataFlowEdge> resOut = res.getOut();
    message = (message == null && expOut.size() != resOut.size()) ? "number of outgoing edges not equal for expected node " + exp + " and resultNode " + res
        : message;
    for (int i = 0; i < expOut.size() && message == null; i++) {
      String edgeMessage = assertEdgeEqual(expOut.get(0), resOut.get(0));
      if (edgeMessage != null) {
        message = "Outgoing edges not equal of expected node " + exp + " and result node " + res + ": " + edgeMessage;
      }
    }

    if (message == null) {
      String s = "Owner not equal for node " + exp.getName() + " expected " + exp.getOwner() + " but was " + res.getOwner();
      if (exp.getOwner().isPresent() && res.getOwner().isPresent()) {
        if (!Objects.equal(exp.getOwner().get().getName(), res.getOwner().get().getName()) || //
            !(exp.getOwner().get().getClass().equals(res.getOwner().get().getClass()))) {
          message = s;
        }
      } else if (exp.getOwner().isPresent() != res.getOwner().isPresent()) {
        message = s;
      }
    }

    if (message == null && !exp.getRepresentedNode().equals(res.getRepresentedNode())) {
      message = "RepresentedNode not equal for node " + exp.getName() + " expected " + exp.getRepresentedNode() + " (" + exp.getRepresentedNode().getClass()
          + ")" + " but was " + res.getRepresentedNode() + " (" + res.getRepresentedNode().getClass() + ")";
    }

    return Optional.ofNullable(message);
  }

  public DataFlowNode createField(CompilationUnit cu, String name) {
    VariableDeclarator represented = cu.findAll(VariableDeclarator.class).stream().filter(v -> v.getNameAsString().equals(name)).findFirst().get();
    return createNodeBuilder(name).representedNode(represented).build();
  }

  public DataFlowNode createMethodReturn(CompilationUnit cu, String methodName) {
    MethodDeclaration method = cu.findAll(MethodDeclaration.class).stream().filter(v -> v.getNameAsString().equals(methodName)).findFirst().get();
    return createNodeBuilder(methodName + "_return").representedNode(method).build();
  }

  public DataFlowNode createSpecificReturn(CompilationUnit cu, String methodName) {
    ReturnStmt ret =
        cu.findAll(MethodDeclaration.class).stream().filter(v -> v.getNameAsString().equals(methodName)).findFirst().get().findAll(ReturnStmt.class).get(0);
    return createNodeBuilder(methodName + "_return_line" + ret.getBegin().get().line + "_col" + ret.getBegin().get().column).representedNode(ret).build();
  }

  public DataFlowNode createParameter(CompilationUnit cu, String name) {
    Parameter represented =
        cu.findAll(MethodDeclaration.class).stream().map(md -> md.getParameterByName(name)).filter(Optional::isPresent).findFirst().get().get();
    return createNodeBuilder(name).representedNode(represented).build();
  }

  public DataFlowNode createNode(CompilationUnit cu, String name, Class<? extends Node> claz) {
    return createNode(cu, name, claz, 0);
  }

  public DataFlowNode createNode(CompilationUnit cu, String name, Class<? extends Node> claz, int index) {
    Node represented = cu.findAll(claz).get(index);
    return createNodeBuilder(name).representedNode(represented).build();
  }

  public DataFlowNode.Builder createNodeBuilder(String name) {
    return DataFlowNode.builder().name(name).representedNode(new SimpleName(name));
  }

  private String assertEdgeEqual(DataFlowEdge exp, DataFlowEdge res) {
    String message = null;
    if (!exp.getFrom().getName().equals(res.getFrom().getName()) && !exp.getTo().getName().equals(res.getTo().getName())) {
      message = exp.toString() + " not equal to " + res.toString();
    }
    return message;
  }

  private DataFlowNode.Builder createAndFillBuilder() {
    return DataFlowNode.builder().representedNode(JAVA_PARSER_NODE).in(IN).out(OUT).name(NAME);
  }

  private <T> void verifyEqualsDifferent(BiFunction<DataFlowNode.Builder, T, DataFlowNode.Builder> withMapper, T argument) {
    DataFlowNode.Builder builder = createAndFillBuilder();
    DataFlowNode a = builder.build();
    DataFlowNode b = withMapper.apply(builder, argument).build();
    Assert.assertFalse("Expected a and b not to be equal", a.equals(b));
  }

  private <T> void verifyHashCode_Different(BiFunction<DataFlowNode.Builder, T, DataFlowNode.Builder> withMapper, T argument) {
    DataFlowNode.Builder builder = createAndFillBuilder();
    DataFlowNode a = builder.build();
    DataFlowNode b = withMapper.apply(builder, argument).build();
    Assert.assertNotEquals("Expected hash code to be different", a.hashCode(), b.hashCode());
  }

}
