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

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import org.junit.Assert;
import org.junit.Test;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * Unit test for {@link DataFlowMethod}.
 *
 * @author Daan
 */
public class DataFlowMethodTest {
  private static final ParameterList INPUT_PARAMETERS = new ParameterList(DataFlowNode.builder().build());
  private static final List<DataFlowNode> INPUT_FIELDS = Collections.singletonList(DataFlowNode.builder().name("b").build());
  private static final List<DataFlowNode> CHANGED_FIELDS = Collections.singletonList(DataFlowNode.builder().name("a").build());
  private static final String NAME = "a";
  private static final CallableDeclaration<?> REPRESENTED_NODE = new ConstructorDeclaration();
  private static final DataFlowNode RETURN_NODE = DataFlowNode.builder().name("d").build();

  @Test
  public void testDataFlowMethod_minimum() {
    DataFlowMethod dataFlowMethod = DataFlowMethod.builder().build();

    Assert.assertNull("Unexpected inputParameters", dataFlowMethod.getInputParameters());
    Assert.assertTrue("Unexpected inputFields", dataFlowMethod.getInputFields().isEmpty());
    Assert.assertTrue("Unexpected changedFields", dataFlowMethod.getChangedFields().isEmpty());
  }

  @Test
  public void testDataFlowMethod_maximum() {
    DataFlowMethod dataFlowMethod = createAndFillBuilder().build();

    Assert.assertEquals("Unexpected inputParameters", INPUT_PARAMETERS, dataFlowMethod.getInputParameters());
    Assert.assertEquals("Unexpected inputFields", INPUT_FIELDS, dataFlowMethod.getInputFields());
    Assert.assertEquals("Unexpected changedFields", CHANGED_FIELDS, dataFlowMethod.getChangedFields());
  }

  @Test
  public void testHashCode_Same() {
    DataFlowMethod.Builder builder = createAndFillBuilder();
    DataFlowMethod a = builder.build();
    DataFlowMethod b = builder.build();
    Assert.assertEquals("Expected hash code to be the same", a.hashCode(), b.hashCode());
  }

  @Test
  public void testHashCode_Different() {
    verifyHashCode_Different(DataFlowMethod.Builder::name, "b");
    verifyHashCode_Different(DataFlowMethod.Builder::representedNode, new MethodDeclaration());
    verifyHashCode_Different(DataFlowMethod.Builder::returnNode, DataFlowNode.builder().build());
    verifyHashCode_Different(DataFlowMethod.Builder::inputParameters, ParameterList.builder().representedNode(REPRESENTED_NODE).build());
    verifyHashCode_Different(DataFlowMethod.Builder::inputFields, Collections.singletonList(DataFlowNode.builder().build()));
    verifyHashCode_Different(DataFlowMethod.Builder::changedFields, Collections.singletonList(DataFlowNode.builder().build()));
  }

  @Test
  public void testEquals_Same() {
    DataFlowMethod.Builder builder = createAndFillBuilder();
    DataFlowMethod a = builder.build();
    DataFlowMethod b = builder.build();
    Assert.assertTrue("Expected a and b to be equal", a.equals(b));
  }

  @Test
  public void testEquals_Different() {
    verifyEqualsDifferent(DataFlowMethod.Builder::name, "b");
    verifyEqualsDifferent(DataFlowMethod.Builder::representedNode, new MethodDeclaration());
    verifyEqualsDifferent(DataFlowMethod.Builder::returnNode, DataFlowNode.builder().build());
    verifyEqualsDifferent(DataFlowMethod.Builder::inputParameters, ParameterList.builder().representedNode(REPRESENTED_NODE).build());
  }

  private DataFlowMethod.Builder createAndFillBuilder() {
    return DataFlowMethod.builder().name(NAME).representedNode(REPRESENTED_NODE).returnNode(RETURN_NODE).inputParameters(INPUT_PARAMETERS)
        .inputFields(INPUT_FIELDS).changedFields(CHANGED_FIELDS);
  }

  private <T> void verifyHashCode_Different(BiFunction<DataFlowMethod.Builder, T, DataFlowMethod.Builder> withMapper, T argument) {
    DataFlowMethod.Builder builder = createAndFillBuilder();
    DataFlowMethod a = builder.build();
    DataFlowMethod b = withMapper.apply(builder, argument).build();
    Assert.assertNotEquals("Expected hash code to be different", a.hashCode(), b.hashCode());
  }

  private <T> void verifyEqualsDifferent(BiFunction<DataFlowMethod.Builder, T, DataFlowMethod.Builder> withMapper, T argument) {
    DataFlowMethod.Builder builder = createAndFillBuilder();
    DataFlowMethod a = builder.build();
    DataFlowMethod b = withMapper.apply(builder, argument).build();
    Assert.assertFalse("Expected a and b not to be equal", a.equals(b));
  }

}
