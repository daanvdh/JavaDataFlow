/*
 * Copyright 2018 by Daan van den Heuvel.
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
 */package dataflow.model;

import java.util.Optional;
import java.util.function.BiFunction;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;

/**
 * Unit test for {@link NodeCall}.
 *
 * @author Daan
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeCallTest {
  private static final ParameterList IN = ParameterList.builder().name("n").build();
  private static final ParameterList OUT = ParameterList.builder().name("a").build();
  private static final OwnedNode<?> OWNER = DataFlowNode.builder().build();
  private static final DataFlowMethod CALLED_METHOD = DataFlowMethod.builder().build();
  private static final String CLAZ = "a";
  private static final String PECKAGE = "c";
  private static final CallableDeclaration<?> REPRESENTED_NODE = new ConstructorDeclaration();

  @Test
  public void testNodeCall_minimum() {
    NodeCall nodeCall = NodeCall.builder().owner(OWNER).build();

    Assert.assertFalse("Unexpected in", nodeCall.getIn().isPresent());
    Assert.assertNull("Unexpected out", nodeCall.getOut());
    Assert.assertTrue("Unexpected owner", nodeCall.getOwner().isPresent());
    Assert.assertFalse("Unexpected calledMethod", nodeCall.getCalledMethod().isPresent());
    Assert.assertNull("Unexpected claz", nodeCall.getClaz());
    Assert.assertNull("Unexpected peckage", nodeCall.getPeckage());
  }

  @Test
  public void testNodeCall_maximum() {
    NodeCall nodeCall = createAndFillBuilder().build();

    Assert.assertEquals("Unexpected in", IN, nodeCall.getIn().get());
    Assert.assertEquals("Unexpected out", OUT, nodeCall.getOut());
    Assert.assertEquals("Unexpected owner", OWNER, nodeCall.getOwner().get());
    Assert.assertEquals("Unexpected calledMethod", Optional.of(CALLED_METHOD), nodeCall.getCalledMethod());
    Assert.assertEquals("Unexpected claz", CLAZ, nodeCall.getClaz());
    Assert.assertEquals("Unexpected peckage", PECKAGE, nodeCall.getPeckage());
  }

  @Test
  public void testEquals_Same() {
    NodeCall.Builder builder = createAndFillBuilder();
    NodeCall a = builder.build();
    NodeCall b = builder.build();
    Assert.assertTrue("Expected a and b to be equal", a.equals(b));
  }

  @Test
  public void testEquals_Different() {
    verifyEqualsDifferent(NodeCall.Builder::in, ParameterList.builder().representedNode(REPRESENTED_NODE).build());
    verifyEqualsDifferent(NodeCall.Builder::out, ParameterList.builder().representedNode(REPRESENTED_NODE).build());
    verifyEqualsDifferent(NodeCall.Builder::calledMethod, DataFlowMethod.builder().name("x").build());
    verifyEqualsDifferent(NodeCall.Builder::claz, "b");
    verifyEqualsDifferent(NodeCall.Builder::peckage, "d");
  }

  @Test
  public void testHashCode_Same() {
    NodeCall.Builder builder = createAndFillBuilder();
    NodeCall a = builder.build();
    NodeCall b = builder.build();
    Assert.assertEquals("Expected hash code to be the same", a.hashCode(), b.hashCode());
  }

  @Test
  public void testHashCode_Different() {
    verifyHashCode_Different(NodeCall.Builder::in, ParameterList.builder().representedNode(REPRESENTED_NODE).build());
    verifyHashCode_Different(NodeCall.Builder::out, ParameterList.builder().representedNode(REPRESENTED_NODE).build());
    verifyHashCode_Different(NodeCall.Builder::calledMethod, DataFlowMethod.builder().name("x").build());
    verifyHashCode_Different(NodeCall.Builder::claz, "b");
    verifyHashCode_Different(NodeCall.Builder::peckage, "d");
  }

  private NodeCall.Builder createAndFillBuilder() {
    return NodeCall.builder().in(IN).out(OUT).owner(OWNER).calledMethod(CALLED_METHOD).claz(CLAZ).peckage(PECKAGE);
  }

  private <T> void verifyEqualsDifferent(BiFunction<NodeCall.Builder, T, NodeCall.Builder> withMapper, T argument) {
    NodeCall.Builder builder = createAndFillBuilder();
    NodeCall a = builder.build();
    NodeCall b = withMapper.apply(builder, argument).build();
    Assert.assertFalse("Expected a and b not to be equal", a.equals(b));
  }

  private <T> void verifyHashCode_Different(BiFunction<NodeCall.Builder, T, NodeCall.Builder> withMapper, T argument) {
    NodeCall.Builder builder = createAndFillBuilder();
    NodeCall a = builder.build();
    NodeCall b = withMapper.apply(builder, argument).build();
    Assert.assertNotEquals("Expected hash code to be different", a.hashCode(), b.hashCode());
  }

}
