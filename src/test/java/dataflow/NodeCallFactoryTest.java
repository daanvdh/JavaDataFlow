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
package dataflow;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;

import dataflow.common.SymbolSolverSetup;
import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowNode;
import dataflow.model.DataFlowNodeTest;
import dataflow.model.NodeCall;

/**
 * Unit test for {@link NodeCallFactory}.
 *
 * @author Daan
 */
public class NodeCallFactoryTest {

  private DataFlowNodeTest dfnTest = new DataFlowNodeTest();

  private NodeCallFactory sut = new NodeCallFactory();

  @Before
  public void setup() {
    // TODO remove dependency to JavaForger
    SymbolSolverSetup.setup();
  }

  @Test
  public void testCreate() {
    String claz = //
        "public class Claz {\n" + //
            "  StringBuilder sb = new StringBuilder(); \n" + //
            "  public void met(String a) {\n" + //
            "    sb.append(a);\n" + //
            "  }\n" + //
            "}"; //
    CompilationUnit cu = StaticJavaParser.parse(claz);
    List<MethodCallExpr> methodCalls = cu.findAll(MethodCallExpr.class);
    DataFlowMethod method = DataFlowMethod.builder().name("met").build();
    MethodCallExpr node = methodCalls.get(0);

    Optional<NodeCall> resultMethod = sut.create(method, node);

    MethodCallExpr expectedRepresentedNode = cu.findAll(MethodCallExpr.class).get(0);
    DataFlowNode expectedDfn = DataFlowNode.builder().name("nodeCall_append_return").representedNode(expectedRepresentedNode).build();
    NodeCall expectedDfm =
        NodeCall.builder().name("append").representedNode(expectedRepresentedNode).claz("StringBuilder").peckage("java.lang").returnNode(expectedDfn).build();

    Assert.assertTrue(resultMethod.isPresent());
    Assert.assertEquals("append", resultMethod.get().getName());
    Assert.assertEquals(expectedDfn, resultMethod.get().getReturnNode().get());
    Assert.assertEquals("Unexpected instanceName", "sb", resultMethod.get().getInstanceName().get());
    Assert.assertEquals(expectedDfm, resultMethod.get());
  }

}
