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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodLikeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;

import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowNode;
import dataflow.model.NodeCall;
import dataflow.model.OwnedNode;

/**
 * Class for resolving {@link DataFlowMethod}s and {@link DataFlowNode}s from {@link Node}s and {@link DataFlowGraph}s.
 *
 * @author Daan
 */
public class NodeCallFactory {
  private static final Logger LOG = LoggerFactory.getLogger(NodeCallFactory.class);

  private ParserUtil parserUtil = new ParserUtil();

  public Optional<NodeCall> create(OwnedNode<?> owner, MethodCallExpr node) {
    Object resolved = parserUtil.resolve(owner, node);

    NodeCall resolvedMethod = null;
    if (resolved instanceof ResolvedMethodLikeDeclaration) {
      resolvedMethod = createMethodCall(owner, (ResolvedMethodLikeDeclaration) resolved, node);
    } else {
      LOG.warn("In method {}, resolving is not supported for node {} of type {}", owner.getName(), node, resolved == null ? null : resolved.getClass());
    }
    return Optional.ofNullable(resolvedMethod);
  }

  private NodeCall createMethodCall(OwnedNode<?> owner, ResolvedMethodLikeDeclaration resolved, MethodCallExpr node) {
    NodeCall methodCall =
        NodeCall.builder().name(resolved.getName()).claz(resolved.getClassName()).peckage(resolved.getPackageName()).owner(owner).representedNode(node).build();
    setInstanceName(methodCall, node);
    setReturn(methodCall, owner, node, resolved);
    return methodCall;
  }

  private void setReturn(NodeCall methodCall, OwnedNode<?> method, MethodCallExpr node, ResolvedMethodLikeDeclaration rmd) {
    if (rmd instanceof ResolvedMethodDeclaration) {
      ResolvedType returnType = ((ResolvedMethodDeclaration) rmd).getReturnType();
      if (!returnType.isVoid()) {
        DataFlowNode returnNode = DataFlowNode.builder().name("nodeCall_" + methodCall.getName() + "_return").representedNode(node).build();
        methodCall.setReturnNode(returnNode);
      }
    } else {
      LOG.warn("Not supported to create return node in NodeCall from resolved node of type {} in method {}", rmd.getClass(), method.getName());
    }
  }

  private void setInstanceName(NodeCall methodCall, MethodCallExpr node) {
    Optional<Expression> scope = node.getScope();
    if (scope.isPresent()) {
      if (scope.get() instanceof NameExpr) {
        methodCall.setInstanceName(((NameExpr) scope.get()).getNameAsString());
      }
    }
  }

}
