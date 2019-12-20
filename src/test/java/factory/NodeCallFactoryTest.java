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

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;

import common.SymbolSolverSetup;
import factory.NodeCallFactory;
import model.DataFlowMethod;
import model.DataFlowNode;
import model.NodeCall;

/**
 * Unit test for {@link NodeCallFactory}.
 *
 * @author Daan
 */
public class NodeCallFactoryTest {

  private NodeCallFactory sut = new NodeCallFactory();

  @Before
  public void setup() {
    SymbolSolverSetup.setup();
  }

  @Test
  public void testCreate() {
    CompilationUnit cu = StaticJavaParser.parse( //
        "public class Claz {\n" + //
            "  StringBuilder sb = new StringBuilder(); \n" + //
            "  public void met(int a) {\n" + //
            "    sb.charAt(a);\n" + // returns a char
            "  }\n" + //
            "}");

    List<MethodCallExpr> methodCalls = cu.findAll(MethodCallExpr.class);
    DataFlowMethod method = DataFlowMethod.builder().name("met").build();
    MethodCallExpr node = methodCalls.get(0);
    DataFlowNode instance = DataFlowNode.builder().name("inst").build();

    Optional<NodeCall> resultMethod = sut.create(method, node, instance);

    MethodCallExpr expectedRepresentedNode = cu.findAll(MethodCallExpr.class).get(0);
    DataFlowNode expectedDfn = DataFlowNode.builder().name("nodeCall_charAt_return").type("char").representedNode(expectedRepresentedNode).build();
    NodeCall expectedDfm = NodeCall.builder().name("charAt").representedNode(expectedRepresentedNode).claz("AbstractStringBuilder").peckage("java.lang")
        .returnNode(expectedDfn).build();

    Assert.assertTrue(resultMethod.isPresent());
    Assert.assertEquals("charAt", resultMethod.get().getName());
    Assert.assertEquals(expectedDfn, resultMethod.get().getReturnNode().get());
    Assert.assertEquals("Unexpected instanceName", instance, resultMethod.get().getInstance().get());
    Assert.assertEquals(expectedDfm, resultMethod.get());
  }

}
