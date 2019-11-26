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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;

import dataflow.model.DataFlowEdge;
import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowNode;
import dataflow.model.NodeCall;
import dataflow.model.OwnedNode;
import dataflow.model.ParameterList;

/**
 * Class for handling {@link JavaParser} {@link Node}s while filling a {@link DataFlowMethod}.
 *
 * @author Daan
 */
public class MethodNodeHandler {
  private static final Logger LOG = LoggerFactory.getLogger(MethodNodeHandler.class);

  private ParserUtil parserUtil = new ParserUtil();
  private NodeCallFactory nodeCallFactory = new NodeCallFactory();
  private DataFlowNodeFactory dfnFactory = new DataFlowNodeFactory();

  /**
   * Recursively creates new {@link DataFlowNode} or finds existing ones and creates {@link DataFlowEdge} between those nodes if needed. This is done within the
   * scope of a single method. This method assumes all methods to already exist in the {@link DataFlowGraph}, including the {@link DataFlowNode}s for the input
   * parameters and return value. If external method calls are done, {@link NodeCall}s representing them will also be created.
   *
   * @param graph {@link DataFlowGraph}
   * @param method {@link DataFlowMethod} to add {@link DataFlowNode} to
   * @param overriddenValues The values that have been overridden in previous iterations.
   * @param n The {@link Node} to handle. ChildNodes will recursively be handled if needed.
   * @return An optional of the {@link DataFlowNode} of the input node. If multiple head nodes are created, (In case of a {@link BlockStmt}) the optional will
   *         be empty.
   */
  public Optional<DataFlowNode> handleNode(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, Node n, OwnedNode<?> owner) {
    LOG.trace("handling node {}", n);
    Optional<DataFlowNode> created = Optional.empty();
    if (n instanceof BlockStmt) {
      created = handleBlockStmt(graph, method, overriddenValues, (BlockStmt) n, owner);
    } else if (n instanceof ExpressionStmt) {
      created = handleExpressionStmt(graph, method, overriddenValues, (ExpressionStmt) n, owner);
    } else if (n instanceof AssignExpr) {
      created = handleAssignExpr(graph, method, overriddenValues, (AssignExpr) n, owner);
    } else if (n instanceof ReturnStmt) {
      created = handleReturnStmt(graph, method, overriddenValues, (ReturnStmt) n, owner);
    } else if (n instanceof NameExpr) {
      created = handleNameExpr(graph, method, overriddenValues, (NameExpr) n, owner);
    } else if (n instanceof MethodCallExpr) {
      created = handleMethodCallExpr(graph, method, overriddenValues, (MethodCallExpr) n, owner);
    } else if (n instanceof VariableDeclarationExpr) {
      created = handleVariableDeclarationExpr(graph, method, overriddenValues, (VariableDeclarationExpr) n, owner);
    } else if (n instanceof VariableDeclarator) {
      created = handleVariableDeclarator(graph, method, overriddenValues, (VariableDeclarator) n, owner);
    } else if (n instanceof FieldAccessExpr) {
      created = handleFieldAccessExpr(graph, method, overriddenValues, (FieldAccessExpr) n, owner);
    } else if (n instanceof LineComment) {
      // do nothing for comments
    } else {
      LOG.warn("In method {} could not handle node [{}] of type {}", method.getName(), n, n.getClass());
    }
    LOG.trace("created: {}", created);
    return created;
  }

  private Optional<DataFlowNode> handleVariableDeclarator(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues,
      VariableDeclarator n, OwnedNode<?> owner) {
    DataFlowNode created = dfnFactory.create(n, owner);
    Optional<Expression> initializer = n.getInitializer();
    if (initializer.isPresent()) {
      Optional<DataFlowNode> assigner = handleNode(graph, method, overriddenValues, initializer.get(), owner);
      if (assigner.isPresent()) {
        assigner.get().addEdgeTo(created);
      } else {
        LOG.warn("In method {} was not able to resolve {} of type {}", method.getName(), initializer.get(), initializer.get().getClass());
      }
    }
    method.addNode(created);
    return Optional.ofNullable(created);
  }

  private Optional<DataFlowNode> handleVariableDeclarationExpr(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues,
      VariableDeclarationExpr n, OwnedNode<?> owner) {
    NodeList<VariableDeclarator> variables = n.getVariables();
    for (VariableDeclarator vd : variables) {
      handleNode(graph, method, overriddenValues, vd, owner);
    }
    return Optional.empty();
  }

  private Optional<DataFlowNode> handleMethodCallExpr(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, MethodCallExpr n,
      OwnedNode<?> owner) {
    // Create the nodeCall
    Optional<NodeCall> optionalCalledMethod = nodeCallFactory.create(owner, n);
    if (!optionalCalledMethod.isPresent()) {
      // logged in called method
      return Optional.empty();
    }
    NodeCall calledMethod = optionalCalledMethod.get();

    // Handle input to call.
    NodeList<Expression> arguments = n.getArguments();
    List<Optional<DataFlowNode>> optionalInputArguments =
        arguments.stream().map(arg -> handleNode(graph, method, overriddenValues, arg, calledMethod)).collect(Collectors.toList());
    if (optionalInputArguments.stream().filter(o -> !o.isPresent()).findAny().isPresent()) {
      LOG.warn("Could not resolve all input arguments for methodCall {} in method {} with input parameters {}", n.getNameAsString(), method.getName(),
          optionalInputArguments);
      return Optional.empty();
    }
    List<DataFlowNode> inputArguments = optionalInputArguments.stream().map(Optional::get).collect(Collectors.toList());

    // Add input to method
    ParameterList.Builder params = ParameterList.builder().name(calledMethod.getName() + "CallParameters");
    if (inputArguments != null && !inputArguments.isEmpty()) {
      params.nodes(inputArguments).build();
    }
    calledMethod.setIn(params.build());

    method.addMethodCall(calledMethod);
    calledMethod.getReturnNode().ifPresent(method::addNode);
    // Return the return node of the called method so that the return value can be assigned to the caller.
    return calledMethod.getReturnNode();
  }

  private Optional<DataFlowNode> handleBlockStmt(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, BlockStmt node,
      OwnedNode<?> owner) {
    for (Node n : node.getChildNodes()) {
      handleNode(graph, method, overriddenValues, n, owner);
    }
    return Optional.empty();
  }

  private Optional<DataFlowNode> handleReturnStmt(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, ReturnStmt n,
      OwnedNode<?> owner) {
    DataFlowNode createdReturn = null;
    if (n.getExpression().isPresent()) {
      Expression expression = n.getExpression().get();
      Optional<DataFlowNode> assignToReturn = handleNode(graph, method, overriddenValues, expression, owner);

      if (assignToReturn.isPresent()) {
        // TODO remove setting weird custom names later.
        String name = method.getName() + "_return_" + n.getBegin().map(t -> "line" + t.line + "_col" + t.column).orElse("?");
        createdReturn = dfnFactory.create(n, owner);
        createdReturn.setName(name);
        assignToReturn.get().addEdgeTo(createdReturn);
        method.addNode(createdReturn);
        if (method.getReturnNode().isPresent()) {
          createdReturn.addEdgeTo(method.getReturnNode().get());
        } else {
          throw new DataFlowException("Expected the method %s for which the return statement %s is handled to already have a return node", method, n);
        }
      } else {
        LOG.warn("In method {} could not find node for assigning to the return value for node {} of type {}", method.getName(), expression,
            expression.getClass());
      }
    }
    return Optional.ofNullable(createdReturn);
  }

  private Optional<DataFlowNode> handleNameExpr(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, NameExpr n,
      OwnedNode<?> owner) {
    DataFlowNode newDfn = dfnFactory.create(n, owner);
    Optional<DataFlowNode> origin = getDataFlowNode(graph, method, overriddenValues, n);
    origin.ifPresent(ori -> ori.addEdgeTo(newDfn));
    method.addNode(newDfn);
    return Optional.of(newDfn);
  }

  private Optional<DataFlowNode> handleFieldAccessExpr(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, FieldAccessExpr n,
      OwnedNode<?> owner) {
    DataFlowNode newDfn = dfnFactory.create(n, owner);
    Optional<DataFlowNode> origin = getDataFlowNode(graph, method, overriddenValues, n);
    origin.ifPresent(ori -> ori.addEdgeTo(newDfn));
    method.addNode(newDfn);
    return Optional.of(newDfn);
  }

  private Optional<DataFlowNode> handleExpressionStmt(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, ExpressionStmt n,
      OwnedNode<?> owner) {
    for (Node c : n.getChildNodes()) {
      handleNode(graph, method, overriddenValues, c, owner);
    }
    return Optional.empty();
  }

  private Optional<DataFlowNode> handleAssignExpr(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, AssignExpr expr,
      OwnedNode<?> owner) {
    Expression assignedJP = expr.getTarget();
    Expression assignerJP = expr.getValue();
    Optional<Node> optionalRealAssignedJP = parserUtil.getJavaParserNode(method, assignedJP);
    Optional<DataFlowNode> assignerDF = handleNode(graph, method, overriddenValues, assignerJP, owner);

    if (!optionalRealAssignedJP.isPresent() || !assignerDF.isPresent()) {
      // Logging is already done in the method call.
      return Optional.empty();
    }
    if (!(assignedJP instanceof NodeWithSimpleName)) {
      LOG.warn("Not able to create a new DFN if the assigned node does not implement NodeWithSimpleName, for node {}", assignedJP);
      return Optional.empty();
    }

    Node realAssignedJP = optionalRealAssignedJP.get();
    DataFlowNode flowNode = dfnFactory.create(expr, method);
    String name = nameForInBetweenNode(method, overriddenValues, realAssignedJP, (NodeWithSimpleName<?>) assignedJP);
    flowNode.setName(name);
    method.addNode(flowNode);
    if (isField(realAssignedJP)) {
      // This is the version of the field that will receive the assigner edge.
      // If this is the last assignment to the field, an edge to the original field will be created.
      overriddenValues.put(realAssignedJP, flowNode);
    }

    assignerDF.get().addEdgeTo(flowNode);
    return Optional.of(flowNode);
  }

  /**
   * TODO javadoc
   *
   * @param graph
   * @param method
   * @param overwriddenValues
   * @param node
   * @return
   */
  private Optional<DataFlowNode> getDataFlowNode(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overwriddenValues, Node node) {
    Optional<Node> optionalResolvedNode = parserUtil.getJavaParserNode(method, node);
    DataFlowNode flowNode = null;
    if (optionalResolvedNode.isPresent()) {
      Node resolvedNode = optionalResolvedNode.get();
      flowNode = overwriddenValues.get(resolvedNode);
      flowNode = flowNode != null ? flowNode : graph.getNode(resolvedNode);
      flowNode = flowNode != null ? flowNode : method.getNode(resolvedNode);
    }
    if (flowNode == null) {
      LOG.warn("In method {} did not resolve the type of node {} of type {}", method.getName(), node, node.getClass());
    }
    return Optional.ofNullable(flowNode);
  }

  private String nameForInBetweenNode(DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, Node realAssignedJP,
      NodeWithSimpleName<?> nodeWithName) {
    String namePostFix = "";
    if (overriddenValues.containsKey(realAssignedJP)) {
      DataFlowNode overridden = overriddenValues.get(realAssignedJP);
      String stringNumber = overridden.getName().substring(overridden.getName().lastIndexOf("."));
      namePostFix = StringUtils.isNumeric(stringNumber) ? "." + (new Integer(stringNumber) + 1) : ".2";
    }

    // Make the name unique for multiple assignments to the same variable
    return method.getName() + "." + nodeWithName.getNameAsString() + namePostFix;
  }

  private boolean isField(Node representedNode) {
    boolean isField = false;
    if (representedNode instanceof VariableDeclarator) {
      VariableDeclarator vd = (VariableDeclarator) representedNode;
      if (vd.getParentNode().isPresent() && vd.getParentNode().get() instanceof FieldDeclaration) {
        isField = true;
      }
    }
    return isField;
  }

}
