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
package dataflow.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SimpleName;

/**
 * Unit test for {@link DataFlowGraph}.
 *
 * @author Daan
 */
@RunWith(MockitoJUnitRunner.class)
public class DataFlowGraphTest {
  private static final String NAME = "a";
  private static final String CLASS_PACKAGE = "c";
  private static final List<DataFlowNode> FIELDS = Collections.singletonList(DataFlowNode.builder().representedNode(new SimpleName("f")).build());
  private static final List<DataFlowMethod> CONSTRUCTORS = Collections.singletonList(DataFlowMethod.builder().build());
  private static final Map<Node, DataFlowMethod> METHODS = Collections.singletonMap(new MethodDeclaration(), DataFlowMethod.builder().build());
  private static final Map<Node, DataFlowNode> NODES = Collections.singletonMap(new FieldDeclaration(), DataFlowNode.builder().build());
  private static final Map<String, DataFlowGraph> DEPENDED_GRAPHS = Collections.singletonMap("e", DataFlowGraph.builder().build());

  @Test
  public void testDataFlowGraph_minimum() {
    DataFlowGraph dataFlowGraph = DataFlowGraph.builder().build();

    Assert.assertNull("Unexpected name", dataFlowGraph.getName());
    Assert.assertNull("Unexpected classPackage", dataFlowGraph.getClassPackage());
    Assert.assertTrue("Unexpected fields", dataFlowGraph.getFields().isEmpty());
    Assert.assertTrue("Unexpected constructors", dataFlowGraph.getConstructors().isEmpty());
    Assert.assertTrue("Unexpected methods", dataFlowGraph.getMethods().isEmpty());
    Assert.assertEquals("Unexpected nodes", Collections.emptyMap(), dataFlowGraph.getNodes());
    Assert.assertEquals("Unexpected dependedGraphs", Collections.emptyMap(), dataFlowGraph.getDependedGraphs());
  }

  @Test
  public void testDataFlowGraph_maximum() {
    DataFlowGraph dataFlowGraph = createAndFillBuilder().build();

    Assert.assertEquals("Unexpected name", NAME, dataFlowGraph.getName());
    Assert.assertEquals("Unexpected classPackage", CLASS_PACKAGE, dataFlowGraph.getClassPackage());
    Assert.assertEquals("Unexpected fields", FIELDS, dataFlowGraph.getFields());
    Assert.assertEquals("Unexpected constructors", CONSTRUCTORS, dataFlowGraph.getConstructors());
    Assert.assertEquals("Unexpected methods", METHODS.values(), dataFlowGraph.getMethods());
    Assert.assertEquals("Unexpected nodes", NODES, dataFlowGraph.getNodes());
    Assert.assertEquals("Unexpected dependedGraphs", DEPENDED_GRAPHS, dataFlowGraph.getDependedGraphs());
  }

  private DataFlowGraph.Builder createAndFillBuilder() {
    return DataFlowGraph.builder().name(NAME).classPackage(CLASS_PACKAGE).fields(FIELDS).constructors(CONSTRUCTORS).methods(METHODS).nodes(NODES)
        .dependedGraphs(DEPENDED_GRAPHS);
  }

}
