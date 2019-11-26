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

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link DataFlowEdge}.
 *
 * @author Daan
 */
public class DataFlowEdgeTest {
  private static final DataFlowNode FROM = DataFlowNode.builder().build();
  private static final DataFlowNode TO = DataFlowNode.builder().build();

  @Test
  public void testDataFlowEdge_minimum() {
    DataFlowEdge dataFlowEdge = DataFlowEdge.builder().build();

    Assert.assertNull("Unexpected from", dataFlowEdge.getFrom());
    Assert.assertNull("Unexpected to", dataFlowEdge.getTo());
  }

  @Test
  public void testDataFlowEdge_maximum() {
    DataFlowEdge dataFlowEdge = createAndFillBuilder().build();

    Assert.assertEquals("Unexpected from", FROM, dataFlowEdge.getFrom());
    Assert.assertEquals("Unexpected to", TO, dataFlowEdge.getTo());
  }

  private DataFlowEdge.Builder createAndFillBuilder() {
    return DataFlowEdge.builder().from(FROM).to(TO);
  }

}
