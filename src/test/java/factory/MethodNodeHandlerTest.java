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
package factory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import org.hamcrest.Matcher;
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
import com.github.javaparser.ast.expr.NameExpr;

import factory.MethodNodeHandler;
import factory.NodeCallFactory;
import model.DataFlowGraph;
import model.DataFlowMethod;
import model.DataFlowNode;
import model.NodeCall;
import model.ParameterList;
import util.MethodMatcher;

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
    mockNodeCallFactory(method, node, cu.findAll(NameExpr.class).get(0), methodCall);

    Optional<DataFlowNode> resultNode = execute(node, method);

    Assert.assertTrue(resultNode.isPresent());
    Assert.assertEquals(Arrays.asList(methodCall), method.getNodeCalls());
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
    mockNodeCallFactory(method, node, cu.findAll(NameExpr.class).get(0), methodCall);

    Optional<DataFlowNode> resultNode = execute(node, method);

    Assert.assertTrue(resultNode.isPresent());
    Assert.assertEquals(Arrays.asList(methodCall), method.getNodeCalls());
  }

  @Test
  public void testHandleMethodCallExpr_methodConcatenation() {
    CompilationUnit cu = createCompilationUnit(//
        "  StringBuilder sb = new StringBuilder(); \n" + //
            "  public StringBuilder met(String a, int b) {\n" + //
            "    return sb.append(a).charAt(b);\n" + //
            "  }\n"); //
    DataFlowMethod method = DataFlowMethod.builder().build();

    MethodCallExpr append = cu.findAll(MethodCallExpr.class).get(1);
    DataFlowNode dfnSbAppend = DataFlowNode.builder().name("app").representedNode(append).build();
    NodeCall appendCall = NodeCall.builder().name("nc1").returnNode(dfnSbAppend).build();
    NameExpr instance = cu.findAll(NameExpr.class).get(0);
    mockNodeCallFactory(method, append, instance, appendCall);

    MethodCallExpr charrAt = cu.findAll(MethodCallExpr.class).get(0);
    DataFlowNode dfnCharrAt = DataFlowNode.builder().name("charrAt").representedNode(charrAt).build();
    NodeCall charrAtCall = NodeCall.builder().name("nc2").returnNode(dfnCharrAt).build();
    mockNodeCallFactory(method, charrAt, append, charrAtCall);

    Optional<DataFlowNode> resultNode = execute(charrAt, method);

    Assert.assertTrue(resultNode.isPresent());
    Assert.assertEquals(Arrays.asList(appendCall, charrAtCall), method.getNodeCalls());
  }

  private void mockNodeCallFactory(DataFlowMethod method, MethodCallExpr node, Node instance, NodeCall methodCall) {
    Matcher<DataFlowNode> matchesInstance = MethodMatcher.of(DataFlowNode::getRepresentedNode, instance);
    Mockito.when(nodeCallFactory.create(Mockito.eq(method), Mockito.eq(node), Mockito.argThat(matchesInstance))).thenReturn(Optional.of(methodCall));
  }

  private CompilationUnit createCompilationUnit(String code) {
    CompilationUnit cu = StaticJavaParser.parse(//
        "public class Claz {\n" + //
            code + //
            "}");
    return cu;
  }

  private Optional<DataFlowNode> execute(MethodCallExpr node, DataFlowMethod method) {
    DataFlowGraph graph = DataFlowGraph.builder().build();
    HashMap<Node, DataFlowNode> overriddenValues = new HashMap<>();
    Optional<DataFlowNode> resultNode = sut.handleNode(graph, method, overriddenValues, node, method);
    return resultNode;
  }

}
