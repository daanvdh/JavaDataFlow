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
package util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import common.GraphBuilder;
import common.NodeBuilder;
import model.DataFlowGraph;
import model.DataFlowMethod;
import model.DataFlowNode;

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
