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
package facade;

import com.github.javaparser.ast.CompilationUnit;

import factory.DataFlowGraphFactory;
import model.DataFlowGraph;
import util.ParserUtil;

/**
 * Facade class to create {@link DataFlowGraph}s.
 *
 * @author Daan
 */
public class JavaDataFlow {

  /**
   * Creates a {@link DataFlowGraph} from the class located at the given classPath.
   *
   * @param classPath The path to the input class.
   * @return A {@link DataFlowGraph} representing the input class.
   */
  public static DataFlowGraph create(String classPath) {
    return create(new ParserUtil().createCompilationUnit(classPath));
  }

  /**
   * Creates a {@link DataFlowGraph} from the given {@link CompilationUnit}.
   *
   * @param cu The input {@link CompilationUnit}.
   * @return A {@link DataFlowGraph} representing the input class.
   */
  public static DataFlowGraph create(CompilationUnit cu) {
    return new DataFlowGraphFactory().create(cu);
  }

}
