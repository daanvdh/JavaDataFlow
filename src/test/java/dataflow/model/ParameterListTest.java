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

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link ParameterList}.
 *
 * @author Daan
 */
@RunWith(MockitoJUnitRunner.class)
public class ParameterListTest {
  private static final List<DataFlowNode> NODES = Collections.singletonList(DataFlowNode.builder().build());
  private static final OwnedNode<?> OWNER = DataFlowNode.builder().build();

  @Test
  public void testParameterList_minimum() {
    ParameterList parameterList = ParameterList.builder().build();

    Assert.assertTrue("Unexpected nodes", parameterList.getNodes().isEmpty());
    Assert.assertFalse("Unexpected owner", parameterList.getOwner().isPresent());
  }

  @Test
  public void testParameterList_maximum() {
    ParameterList parameterList = createAndFillBuilder().build();

    Assert.assertEquals("Unexpected nodes", NODES, parameterList.getNodes());
    Assert.assertEquals("Unexpected owner", OWNER, parameterList.getOwner().get());
  }

  @Test
  public void testEquals_Same() {
    ParameterList.Builder builder = createAndFillBuilder();
    ParameterList a = builder.build();
    ParameterList b = builder.build();
    Assert.assertTrue("Expected a and b to be equal", a.equals(b));
  }

  @Test
  public void testEquals_Different() {
    verifyEqualsDifferent(ParameterList.Builder::nodes, Collections.singletonList(DataFlowNode.builder().name("a").build()));
  }

  @Test
  public void testHashCode_Same() {
    ParameterList.Builder builder = createAndFillBuilder();
    ParameterList a = builder.build();
    ParameterList b = builder.build();
    Assert.assertEquals("Expected hash code to be the same", a.hashCode(), b.hashCode());
  }

  @Test
  public void testHashCode_Different() {
    verifyHashCode_Different(ParameterList.Builder::nodes, Collections.singletonList(DataFlowNode.builder().name("a").build()));
  }

  private ParameterList.Builder createAndFillBuilder() {
    return ParameterList.builder().nodes(NODES).owner(OWNER);
  }

  private <T> void verifyEqualsDifferent(BiFunction<ParameterList.Builder, T, ParameterList.Builder> withMapper, T argument) {
    ParameterList.Builder builder = createAndFillBuilder();
    ParameterList a = builder.build();
    ParameterList b = withMapper.apply(builder, argument).build();
    Assert.assertFalse("Expected a and b not to be equal", a.equals(b));
  }

  private <T> void verifyHashCode_Different(BiFunction<ParameterList.Builder, T, ParameterList.Builder> withMapper, T argument) {
    ParameterList.Builder builder = createAndFillBuilder();
    ParameterList a = builder.build();
    ParameterList b = withMapper.apply(builder, argument).build();
    Assert.assertNotEquals("Expected hash code to be different", a.hashCode(), b.hashCode());
  }

}
