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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowNode;

/**
 * Unit test for {@link GraphUtil}.
 *
 * @author Daan
 */
public class GraphUtilTest {

  @Test
  public void testWalkBackUntil_simple() {
    DataFlowGraph graph = GraphBuilder.withStartingNodes(NodeBuilder.ofParameter("setS", "a").to("setS.a").to("setS.b").to(NodeBuilder.ofField("s"))).build();
    DataFlowMethod m = graph.getMethods().iterator().next();
    DataFlowNode node = m.getChangedFields().get(0);
    DataFlowNode expected = m.getInputParameters().getNodes().get(0);

    List<DataFlowNode> result = GraphUtil.walkBackUntil(node, m::isInputBoundary, graph::owns);
    Assert.assertEquals(Collections.singletonList(expected), result);
  }

  @Test
  public void testWalkBackUntil_inputIsBoundaryNode() {
    DataFlowGraph graph = GraphBuilder.withStartingNodes(NodeBuilder.ofParameter("setS", "a").to("setS.a").to("setS.b").to(NodeBuilder.ofField("s"))).build();
    DataFlowMethod m = graph.getMethods().iterator().next();
    DataFlowNode expected = m.getInputParameters().getNodes().get(0);

    List<DataFlowNode> result = GraphUtil.walkBackUntil(expected, m::isInputBoundary, graph::owns);
    Assert.assertEquals(Collections.singletonList(expected), result);
  }

  @Test
  public void testWalkBackUntil_multipleOutput() {
    NodeBuilder field = NodeBuilder.ofField("x");
    GraphBuilder withStartingNodes = GraphBuilder.withStartingNodes( //
        NodeBuilder.ofParameter("setS", "a").to(field), //
        NodeBuilder.ofParameter("setS", "b").to(field), //
        NodeBuilder.ofParameter("setS", "c").to(NodeBuilder.ofField("y")) //
    );
    DataFlowGraph graph = withStartingNodes.build();
    DataFlowMethod m = graph.getMethods().iterator().next();
    DataFlowNode node = m.getChangedFields().get(0);
    List<DataFlowNode> parameters = m.getInputParameters().getNodes();

    List<DataFlowNode> result = GraphUtil.walkBackUntil(node, m::isInputBoundary, graph::owns);
    Assert.assertEquals(Arrays.asList(parameters.get(0), parameters.get(1)), result);
  }

}
