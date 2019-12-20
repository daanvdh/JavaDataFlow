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
package facade;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

/**
 * Contains all static setting for {@link JavaDataFlow}.
 *
 * @author Daan
 */
public class StaticJavaDataFlow {
  private static final Logger LOG = LoggerFactory.getLogger(StaticJavaDataFlow.class);

  /** Used to gather more data about a parsed class, such as resolving imports or super classes. */
  private JavaSymbolSolver symbolSolver;

  private static StaticJavaDataFlow config;

  private StaticJavaDataFlow() {
    // don't create it via any constructor
    setupSymbolSolver();
  }

  public static synchronized StaticJavaDataFlow getConfig() {
    if (config == null) {
      config = new StaticJavaDataFlow();
    }
    return config;
  }

  public final void setSymbolSolver(JavaSymbolSolver symbolSolver) {
    this.symbolSolver = symbolSolver;
    StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
  }

  public JavaSymbolSolver getSymbolSolver() {
    return symbolSolver;
  }

  /**
   * Sets the project paths to be used to find classes. Note that these paths should be the full path to the source folder, typically ending with
   * ".../src/main/java" for maven projects. This method will override anything set by the method {@link StaticJavaDataFlow#setSymbolSolver(JavaSymbolSolver)}.
   *
   * @param paths The full paths to source folders where {@link JavaDataFlow} needs to look for classes that any input class depends on.
   */
  public void setProjectPaths(String... paths) {
    Stream.of(paths).filter(p -> !Files.exists(new File(p).toPath())).forEach(p -> LOG.error("Could not find the folder located at: " + p));
    JavaParserTypeSolver[] solvers =
        Stream.of(paths).filter(p -> Files.exists(new File(p).toPath())).map(JavaParserTypeSolver::new).toArray(JavaParserTypeSolver[]::new);
    TypeSolver[] reflTypeSolver = {new ReflectionTypeSolver()};
    TypeSolver typeSolver = new CombinedTypeSolver(ArrayUtils.addAll(reflTypeSolver, solvers));
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
    setSymbolSolver(symbolSolver);
  }

  private final void setupSymbolSolver() {
    TypeSolver reflTypeSolver = new ReflectionTypeSolver();
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(reflTypeSolver);
    this.setSymbolSolver(symbolSolver);
  }

}
