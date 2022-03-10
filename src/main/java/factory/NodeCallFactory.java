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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodLikeDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedType;

import model.DataFlowGraph;
import model.DataFlowMethod;
import model.DataFlowNode;
import model.NodeCall;
import model.OwnedNode;
import model.OwnerNode;
import util.ParserUtil;

/**
 * Class for resolving {@link DataFlowMethod}s and {@link DataFlowNode}s from {@link Node}s and {@link DataFlowGraph}s.
 *
 * @author Daan
 */
public class NodeCallFactory {
  private static final Logger LOG = LoggerFactory.getLogger(NodeCallFactory.class);

  private ParserUtil parserUtil = new ParserUtil();

  /**
   * Creates a {@link NodeCall}.
   *
   * @param owner The direct {@link OwnerNode} for the {@link NodeCall} to be created.
   * @param node The {@link MethodCallExpr} that will be represented by the created {@link NodeCall}.
   * @param instance The {@link DataFlowNode} on which the call was executed.
   * @return created {@link NodeCall}.
   */
  public Optional<NodeCall> create(OwnedNode<?> owner, MethodCallExpr node, DataFlowNode instance) {
    Object resolved = parserUtil.resolve(owner, node);

    NodeCall resolvedMethod = null;
    if (resolved instanceof ResolvedMethodLikeDeclaration) {
      resolvedMethod = createMethodCall(owner, (ResolvedMethodLikeDeclaration) resolved, node, instance);
    } else {
      LOG.warn("In method {}, resolving is not supported for node {} of type {}", owner.getName(), node, resolved == null ? null : resolved.getClass());
    }
    return Optional.ofNullable(resolvedMethod);
  }

  private NodeCall createMethodCall(OwnedNode<?> owner, ResolvedMethodLikeDeclaration resolved, MethodCallExpr node, DataFlowNode instance) {
    NodeCall methodCall = NodeCall.builder().name(resolved.getName()).claz(resolved.getClassName()).peckage(resolved.getPackageName()).owner(owner)
        .representedNode(node).instance(instance).build();
    setReturn(methodCall, owner, node, resolved);
    return methodCall;
  }

  private void setReturn(NodeCall methodCall, OwnedNode<?> method, MethodCallExpr node, ResolvedMethodLikeDeclaration rmd) {
    if (rmd instanceof ResolvedMethodDeclaration) {
      ResolvedType returnType = ((ResolvedMethodDeclaration) rmd).getReturnType();
      if (!returnType.isVoid()) {
        DataFlowNode returnNode =
            DataFlowNode.builder().name("nodeCall_" + methodCall.getName() + "_return").representedNode(node).type(getType(returnType)).build();
        methodCall.setReturnNode(returnNode);
      }
    } else {
      LOG.warn("Not supported to create return node in NodeCall from resolved node of type {} in method {}", rmd.getClass(), method.getName());
    }
  }

  private String getType(ResolvedType returnType) {
    String name;
    if (returnType instanceof ResolvedPrimitiveType) {
      name = ((ResolvedPrimitiveType) returnType).describe();
    } else {
      LOG.warn("Could not resolve the type of {}", returnType);
      name = "UNKNOWN";
    }
    return name;
  }

}
