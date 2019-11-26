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
package dataflow.model;

/**
 * An edge inside a {@link DataFlowGraph} representing a {@link DataFlowNode} influencing the state of another {@link DataFlowNode}.
 *
 * @author Daan
 */
public class DataFlowEdge {

  private DataFlowNode from;
  private DataFlowNode to;

  public DataFlowEdge() {
    // empty constructor which would otherwise be invisible due to the constructor receiving the builder.
  }

  private DataFlowEdge(Builder builder) {
    this.from = builder.from == null ? this.from : builder.from;
    this.to = builder.to == null ? this.to : builder.to;
  }

  public DataFlowEdge(DataFlowNode from, DataFlowNode to) {
    this.from = from;
    this.to = to;
  }

  public DataFlowNode getFrom() {
    return from;
  }

  public void setFrom(DataFlowNode from) {
    this.from = from;
  }

  public DataFlowNode getTo() {
    return to;
  }

  public void setTo(DataFlowNode to) {
    this.to = to;
  }
  
  @Override
  public String toString() {
    return from.getName() + "->" + to.getName();
  }

  /**
   * Creates builder to build {@link DataFlowEdge}.
   *
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder to build {@link DataFlowEdge}.
   */
  public static final class Builder {
    private DataFlowNode from;
    private DataFlowNode to;

    private Builder() {
      // Builder should only be constructed via the parent class
    }

    public Builder from(DataFlowNode from) {
      this.from = from;
      return this;
    }

    public Builder to(DataFlowNode to) {
      this.to = to;
      return this;
    }

    public DataFlowEdge build() {
      return new DataFlowEdge(this);
    }
  }

}
