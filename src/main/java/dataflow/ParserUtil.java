package dataflow;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserSymbolDeclaration;

import dataflow.model.DataFlowMethod;
import dataflow.model.OwnedNode;

public class ParserUtil {
  private static final Logger LOG = LoggerFactory.getLogger(ParserUtil.class);

  /**
   * Gets the node to which the input points to. For example for the input "this.s" which points to a field usage in a class, the {@link JavaParser}
   * {@link Node} that will be returned is the field "s".
   *
   * @param method Only needed for logging purposes
   * @param node The node to resolve.
   * @return An empty {@link Optional} if the node could not be resolved, an {@link Optional} with the pointed to node otherwise.
   */
  public Optional<Node> getJavaParserNode(DataFlowMethod method, Node node) {
    Object resolved = resolve(method, node);
    Node resolvedNode = null;
    if (resolved instanceof JavaParserFieldDeclaration) {
      resolvedNode = ((JavaParserFieldDeclaration) resolved).getVariableDeclarator();
    } else if (resolved instanceof JavaParserParameterDeclaration) {
      resolvedNode = ((JavaParserParameterDeclaration) resolved).getWrappedNode();
    } else if (resolved instanceof JavaParserSymbolDeclaration) {
      resolvedNode = ((JavaParserSymbolDeclaration) resolved).getWrappedNode();
    } else {
      LOG.warn("In method {}, resolving is not supported for node {} of type {}", method.getName(), node, resolved == null ? null : resolved.getClass());
    }
    return Optional.ofNullable(resolvedNode);
  }

  public Object resolve(OwnedNode<?> method, Node node) {
    if (!Resolvable.class.isAssignableFrom(node.getClass())) {
      LOG.warn("In method {}, node is not Resolvable for expression {} of type {}", method.getName(), node, node.getClass());
      return null;
    }

    Resolvable<?> resolvable = (Resolvable<?>) node;
    Object resolved = null;
    try {
      resolved = resolvable.resolve();
    } catch (Exception e) {
      LOG.warn(e.getMessage());
      LOG.trace(
          Stream.of(e.getStackTrace()).map(StackTraceElement::toString).reduce((str1, str2) -> StringUtils.join(new String[] {str1, str2}, '\n')).orElse(""));
    }
    return resolved;
  }

}
