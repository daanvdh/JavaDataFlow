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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.printer.Printable;

import dataflow.model.DataFlowNode;
import dataflow.model.OwnedNode;

/**
 * Factory for {@link DataFlowNode}s.
 *
 * @author Daan
 */
public class DataFlowNodeFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DataFlowNodeFactory.class);

  public DataFlowNode create(Node n, OwnedNode<?> owner) {
    DataFlowNode.Builder builder = DataFlowNode.builder().representedNode(n).owner(owner);
    Node nodeWithName = n;
    if (n instanceof ReturnStmt) {
      nodeWithName = ((ReturnStmt) n).getExpression().orElse(null);
    }

    if (nodeWithName instanceof NodeWithSimpleName) {
      builder.name(((NodeWithSimpleName<?>) nodeWithName).getNameAsString());
    } else if (nodeWithName instanceof Printable) {
      builder.name(((Printable) nodeWithName).asString());
    } else {
      LOG.warn("Not supported to add a name to a created DataFlowNode for node of type {}, input node is {}", n.getClass(), n);
    }

    // TODO set the type
    // builder.type(null);

    // for (int i = 0; i < resolved.getNumberOfParams(); i++) {
    // ResolvedParameterDeclaration p = resolved.getParam(i);
    // DataFlowNode newNode =
    // DataFlowNode.builder().name(p.getName()).type(p.getType().describe().toString()).representedNode(node.getArgument(i)).owner(params).build();
    // params.add(newNode);
    // }

    return builder.build();
  }

}
