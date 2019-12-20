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
package util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;

/**
 * A wrapper class to override the equals and hashCode methods to only be equal if 2 objects are the exact same instance. This class is typically used so that
 * {@link JavaParser} {@link Node} classes that have equals and hashCode methods that are not strict enough, can be used as key of a hashMap.
 *
 * @author Daan
 */
public class HashCodeWrapper<T> {

  private final T value;

  public HashCodeWrapper(T value) {
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "HashCodeWrapper[" + value.toString() + "]";
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(value);
  }

  @Override
  public boolean equals(Object obj) {
    boolean equal = false;
    if (this.getClass().isInstance(obj)) {
      equal = value == ((HashCodeWrapper<?>) obj).getValue();
    }
    return equal;
  }

}
