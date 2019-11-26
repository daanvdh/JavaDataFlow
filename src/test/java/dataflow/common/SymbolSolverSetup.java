/*
 * Copyright 2019 by Daan van den Heuvel.
 *
 * This file is part of JavaForger.
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
package dataflow.common;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

/**
 * Class with common test methods for seting up the symbol solver.
 *
 * @author Daan
 */
public class SymbolSolverSetup {

  public static void setup() {
    StaticJavaParser.getConfiguration().setSymbolResolver(getSymbolSolver());
  }

  private static JavaSymbolSolver getSymbolSolver() {
    JavaParserTypeSolver typeSolver_directory = new JavaParserTypeSolver("src/test/java/");
    ReflectionTypeSolver reflTypeSolver = new ReflectionTypeSolver();
    TypeSolver typeSolver = new CombinedTypeSolver(typeSolver_directory, reflTypeSolver);
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
    return symbolSolver;
  }

}
