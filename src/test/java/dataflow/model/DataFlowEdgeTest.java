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
