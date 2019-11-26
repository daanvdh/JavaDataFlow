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
package dataflow;

import java.util.HashMap;
import java.util.Map;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.VarType;

import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowNode;

/**
 * Builder for {@link DataFlowMethod} with some build method only ment for testing.
 *
 * @author Daan
 */
public class DataFlowMethodBuilder extends DataFlowMethod.Builder {

  private Map<String, DataFlowNode> currentNodes = new HashMap<>();

  private DataFlowMethodBuilder() {
    super.representedNode(new MethodDeclaration());
  }

  public static DataFlowMethodBuilder builder() {
    return new DataFlowMethodBuilder();
  }

  public DataFlowMethodBuilder withParameter(String name) {
    inputParameters.add(getNode(name));
    return this;
  }

  public DataFlowMethodBuilder withInputField(String name) {
    this.inputFields.add(new DataFlowNode(name, new VariableDeclarator(new VarType(), name)));
    return this;
  }

  public DataFlowMethodBuilder withChangedFieldEdge(String input, String changedField) {
    DataFlowNode a = getNode(input);
    DataFlowNode b = getNode("this." + changedField);
    a.addEdgeTo(b);
    this.changedFields.add(b);
    return this;
  }

  private DataFlowNode getNode(String name) {
    if (!this.currentNodes.containsKey(name)) {
      DataFlowNode node = new DataFlowNode(name, new VariableDeclarator(new VarType(), name));
      this.currentNodes.put(name, node);
    }
    return currentNodes.get(name);
  }

}
