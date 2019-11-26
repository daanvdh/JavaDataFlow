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
package dataflow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;

import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowNode;
import dataflow.model.NodeCall;
import dataflow.model.ParameterList;

/**
 * Unit test for {@link MethodNodeHandler}.
 *
 * @author Daan
 */
@RunWith(MockitoJUnitRunner.class)
public class MethodNodeHandlerTest {

  @Mock
  private NodeCallFactory nodeCallFactory;

  @InjectMocks
  private MethodNodeHandler sut;

  @Test
  public void testHandleMethodCallExpr_inputMethod() {
    CompilationUnit cu = createCompilationUnit( //
        "  StringBuilder sb = new StringBuilder(); \n" + //
            "  public StringBuilder met(String a) {\n" + //
            "    return sb.append(a);\n" + //
            "  }\n");

    MethodCallExpr node = cu.findAll(MethodCallExpr.class).iterator().next();
    DataFlowNode returnNode = DataFlowNode.builder().name("niceName").build();
    NodeCall methodCall = NodeCall.builder().in(ParameterList.builder().nodes(Arrays.asList(DataFlowNode.builder().name("param1").build())).build())
        .returnNode(returnNode).build();
    DataFlowMethod method = DataFlowMethod.builder().build();
    Mockito.when(nodeCallFactory.create(method, node)).thenReturn(Optional.of(methodCall));

    Optional<DataFlowNode> resultNode = execute(node, method);

    Assert.assertTrue(resultNode.isPresent());
    Assert.assertEquals(returnNode, resultNode.get());
    Assert.assertEquals(Arrays.asList(methodCall), method.getNodeCalls());
  }

  private CompilationUnit createCompilationUnit(String code) {
    CompilationUnit cu = StaticJavaParser.parse(//
        "public class Claz {\n" + //
            code + //
            "}");
    return cu;
  }

  @Test
  public void testHandleMethodCallExpr_outputMethod() {
    CompilationUnit cu = createCompilationUnit( //
        "  StringBuilder sb = new StringBuilder(); \n" + //
            "  public void met(String a) {\n" + //
            "    sb.append(a);\n" + //
            "}");

    MethodCallExpr node = cu.findAll(MethodCallExpr.class).iterator().next();
    DataFlowNode returnNode = DataFlowNode.builder().name("niceName").build();
    NodeCall methodCall = NodeCall.builder().in(ParameterList.builder().nodes(Arrays.asList(DataFlowNode.builder().name("param1").build())).build())
        .returnNode(returnNode).build();
    DataFlowMethod method = DataFlowMethod.builder().build();
    Mockito.when(nodeCallFactory.create(method, node)).thenReturn(Optional.of(methodCall));

    Optional<DataFlowNode> resultNode = execute(node, method);

    Assert.assertTrue(resultNode.isPresent());
    Assert.assertEquals(returnNode, resultNode.get());
    Assert.assertEquals(Arrays.asList(methodCall), method.getNodeCalls());
  }

  private Optional<DataFlowNode> execute(MethodCallExpr node, DataFlowMethod method) {
    DataFlowGraph graph = DataFlowGraph.builder().build();
    HashMap<Node, DataFlowNode> overriddenValues = new HashMap<>();
    Optional<DataFlowNode> resultNode = sut.handleNode(graph, method, overriddenValues, node, method);
    return resultNode;
  }

}
